package org.therismos.dataResolver;

import java.util.*;
import java.util.stream.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * Retrieves the contents of a specified folder and returns a list of maps,
 * where each map contains detailed attributes (name, size, date, type, permissions)
 * for a single file or directory entry.
 *
 * @author cp_liu
 */
public class FilestoreResolver implements Resolver {

    protected int total_count = 0;
    Map<String, Object> result;
    ApplicationBean appBean;
    Document param;
    
    public FilestoreResolver() {
        result = new HashMap<>();
        result.put(TOTALCOUNT, 0);
        result.put(ENTRIES, java.util.Collections.EMPTY_LIST);
        param = new Document();
    }

    @Override    
    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }
    
    /**
     * HTTP GET /webresources/get-data?type=Filestore&sort={default name}&direction={default desc}&page={default 1}&page_size={default 20}
     * &root=[naspath|naspath2]&path={optional}
     * @param param Document with mandatory keys: "baseFolder"
     * @return A List of Maps, each representing an entry's attributes:
     * name, size, date, type, readable, writable
     * @throws IOException If the folder does not exist or access is denied.
     */
    @Override
    public Map<String, Object> getData(Document param)  {
        // 1. Convert the input string to a Path object.
        String root = param.get("root", "naspath");
        if (!root.equals("naspath") && !root.equals("naspath2")) root = "naspath";
        final String baseFolder = root.equals("naspath2") ? appBean.getNaspath2() : appBean.getNaspath();
        Path folderPath = Paths.get(baseFolder, param.get("path", ""));
        System.getLogger(FilestoreResolver.class.getName()).log(System.Logger.Level.INFO, baseFolder);
        // Check if the path exists and is a directory.
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            result.put(TOTALCOUNT, this.total_count);
            // Throw a specific exception for better error handling than java.io.File
            result.put("exception_class", "IOException");
            result.put("exception_message", "Path is not a valid directory: " + folderPath);
            return result;
        }
        try {
            List<String> sorts = this.getSortFields(param);
            List<Integer> dirs = this.getSortDirections(param);
            getPagedAndSortedDirectoryStream(folderPath, param.getInteger(PAGESIZE), param.getInteger(PAGE), 
                    sorts.isEmpty() ? NAME : sorts.get(0), dirs.isEmpty() ? 1 : dirs.get(0));
            result.put(TOTALCOUNT, this.total_count);
        } catch (IOException ex) {
            System.getLogger(FilestoreResolver.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return result;
    }

    @Override
    public Document getDefaults() {
        return new Document(PAGE, 1).append(SORTKEY, NAME).append(PAGESIZE, 20).append(DIRECTION, "asc")
        .append("root", appBean.getNaspath());
    }
    
    /**
     * Creates a paged and sorted sub-list of Path objects from a directory.
     * Directories always come before regular files. Symbolic links are resolved.
     *
     * @param folderPath The directory to stream.
     * @param pageSize The maximum number of entries per page.
     * @param pageOffset The 1-based page number (e.g., 1, 2, 3...).
     * @param sortKey The criteria for secondary sorting (NAME, DATE, or SIZE).
     * @param dir 1 (asc) -1 (desc)
     * @ return A List of Path objects representing the requested page.
     * @throws IOException If an I/O error occurs during directory streaming.
     */
    public void getPagedAndSortedDirectoryStream(
            Path folderPath, int pageSize, int pageOffset, String sortKey, int dir)
            throws IOException {
        if (sortKey == null) {
            sortKey = NAME;
        }
        final int direction = (dir != -1) ? 1 : -1;
        // --- 1. Convert DirectoryStream to Stream and collect to a List ---
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            List<Path> allPaths = StreamSupport.stream(stream.spliterator(), false)
                .collect(Collectors.toList());

            this.total_count = allPaths.size();
            // --- 2. Define the Secondary Comparator (user-specified key) ---
            Comparator<Path> secondaryComparator;
            final LinkOption[] followLinks = new LinkOption[]{}; // Follow symbolic links
            secondaryComparator = switch (sortKey) {
                case DATE -> (path1, path2) -> {
                    try {
                        // Read attributes, following the link
                        BasicFileAttributes attr1 = Files.readAttributes(path1, BasicFileAttributes.class, followLinks);
                        BasicFileAttributes attr2 = Files.readAttributes(path2, BasicFileAttributes.class, followLinks);
                        // Newest first: path2 (newest) compared to path1 (oldest)
System.getLogger(FilestoreResolver.class.getName()).log(System.Logger.Level.INFO, "{0} {1} {2} {3}", 
        path1, path2, attr1.lastModifiedTime(), direction
        );
                        return attr2.lastModifiedTime().compareTo(attr1.lastModifiedTime()) * direction;
                    } catch (IOException e) {
                        return 0; // Treat as equal on error
                    }
                };
                case SIZE -> (path1, path2) -> {
                    try {
                        // Files.size() automatically follows links by default, but let's be explicit
                        long size1 = Files.size(path1);
                        long size2 = Files.size(path2);
                        return Long.compare(size1, size2) * direction;
                    } catch (IOException e) {
                        return 0; // Treat as equal on error
                    }
                };
                case NAME -> direction == 1 ? Comparator.comparing(Path::getFileName) : Comparator.comparing(Path::getFileName).reversed();
                default -> direction == 1 ? Comparator.comparing(Path::getFileName) : Comparator.comparing(Path::getFileName).reversed();
            }; 
// Compares file names alphabetically
            // Compares the last modified time (Newest first)
            // Compares the size in bytes (Smallest first)

            // --- 3. Define the Primary Comparator (Directories first) ---

            // The 'is_directory_first' comparator:
            // - Returns -1 if path1 is a directory and path2 is a file (path1 comes first)
            // - Returns 1 if path1 is a file and path2 is a directory (path2 comes first)
            // - Returns 0 if both are the same type (pass to secondary comparator)
            Comparator<Path> primaryComparator = (path1, path2) -> {
                // Files.isDirectory(path, LinkOption...) checks if the target of a link is a directory
                boolean isDir1 = Files.isDirectory(path1, followLinks);
                boolean isDir2 = Files.isDirectory(path2, followLinks);

                if (isDir1 && !isDir2) {
                    return -1; // path1 is a directory, path2 is a file -> path1 comes first
                } else if (!isDir1 && isDir2) {
                    return 1;  // path1 is a file, path2 is a directory -> path2 comes first
                } else {
                    return 0;  // Both are the same type (both directories or both files)
                }
            };

            // --- 4. Combine Comparators and Apply Paging ---

            // Combine the primary comparator with the secondary one for a composite sort.
            Comparator<Path> finalComparator = primaryComparator.thenComparing(secondaryComparator);
            
            // Paging logic for 1-based page offset
            long skipValue = Math.max(0, (long)(pageOffset - 1) * pageSize);

            List<Map<String, Object>> list = allPaths.stream()
                .sorted(finalComparator)
                .skip(skipValue)
                .limit(pageSize)
                .map((entry) -> {
                    Map<String, Object> attributes = new HashMap<>();
                // Get filename (name)
                attributes.put(NAME, entry.getFileName().toString());

                // Fetch BasicFileAttributes in one atomic operation (efficient).
                // This gets size, timestamps, and file type efficiently.
                BasicFileAttributes basicAttr;
                try {
                    basicAttr = Files.readAttributes(entry, BasicFileAttributes.class);
                // Size (Bytes)
                attributes.put(SIZE, basicAttr.size());
                
                // Date (Last Modified)
                attributes.put(DATE, basicAttr.lastModifiedTime().toString());
                        //.toInstant());
                
                // Date (Creation Time)
                //attributes.put("Creation Time", basicAttr.creationTime().toInstant());

                // Folder or File Type
                String type;
                if (basicAttr.isDirectory()) {
                    type = "directory";
                } else if (basicAttr.isRegularFile()) {
                    type = "file";
                } else if (basicAttr.isSymbolicLink()) {
                    type = "Symbolic Link"; // ignored
                } else {
                    type = "Other"; //ignored
                }
                attributes.put("type", type);
                
                // Read/Write Permissions (Platform-specific details)
                // Use isReadable/isWritable for guaranteed compatibility.
                attributes.put("readable", Files.isReadable(entry));
                attributes.put("writable", Files.isWritable(entry));
                //attributes.put("Can Execute", Files.isExecutable(entry));

                /* Attempt to get POSIX permissions (for Linux/macOS)
                if (System.getProperty("os.name").toLowerCase().contains("nix") || 
                    System.getProperty("os.name").toLowerCase().contains("mac")) {
                    try {
                        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(entry);
                        // Convert the Set of permissions to the standard string representation (rwxrwxrwx)
                        attributes.put("POSIX Permissions", PosixFilePermissions.toString(perms));
                    } catch (UnsupportedOperationException e) {
                        attributes.put("POSIX Permissions", "N/A (Not POSIX File System)");
                    }
                } else {
                     attributes.put("POSIX Permissions", "N/A (Windows/Other)");
                }
                fileAttributesList.add(attributes);
                */
                } catch (IOException ex) {
                    System.getLogger(FilestoreResolver.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    attributes.put(DATE, FileTime.fromMillis(0).toString());
                    attributes.put(SIZE, 0);
                    attributes.put("type", "IO Exception");
                }                
                    return attributes;
                            })
                .collect(Collectors.toList());
            result.put(ENTRIES, list);

        }
    }
}