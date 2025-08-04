package org.therismos.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import org.therismos.entity.FileModel;
import org.therismos.entity.FolderModel;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
//import org.primefaces.util.Callbacks;

/**
 *
 * @author cpliu
 */
@Named
@SessionScoped
public class NasBean implements WebBean, Serializable {

    private File base;
    private File currentPath;
    private List<FileModel> files;
    private List<FolderModel> subdirs;
    private String path;
    private String nasSelector;
    
    @Inject
    transient UserBean userBean;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        path = ".";
        files = new ArrayList<>();
        subdirs = new ArrayList<>();
        if ("NAS".equals(nasSelector)) {
            base = userBean.getNasPath();
        }
        else {
            base = userBean.getNasPath2();
        }
        currentPath = base;
        try {
            refresh();
        } catch (Exception ex) {
            this.files = Collections.EMPTY_LIST;
            this.subdirs = Collections.EMPTY_LIST;
        }
        
    }
    
    public NasBean() {
        nasSelector = "NAS";
    }
    
    public List<FolderModel> getBreadCrumbs() {
        String strBase = base.getAbsolutePath();
        ArrayList<FolderModel> breadCrumbs = new ArrayList<>();
        for (File current=currentPath; !strBase.equals(current.getAbsolutePath());
        current = current.getParentFile()) {
            breadCrumbs.add(new FolderModel(base, current));
        }
        Collections.reverse(breadCrumbs);
        return breadCrumbs;
    }

    /**
     * @return the currentPath
     */
    public File getCurrentPath() {
        return currentPath;
    }

    /**
     * @param currentPath the currentPath to set
     */
    public void setCurrentPath(File currentPath) {
        this.currentPath = currentPath;
    }

    public StreamedContent downloadFile(String fileName) {
        getLog().log(Level.FINE, "here 0: {0}", new File(currentPath,fileName).toString());
        File file = new File(currentPath,fileName);
        if (!file.exists()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "File not found", fileName);
            return null;
        }
        return DefaultStreamedContent.builder()
            .name(fileName)
            .contentType("application/octet-stream")
            .stream(() -> {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    // not possible
                    getLog().log(Level.SEVERE, null, ex);
                    return null;
                }
            }).build();
    }

    /**
     * @return the files
     */
    public List<FileModel> getFiles() {
        return files;
    }

    public void refresh() throws Exception {
        getLog().log(Level.INFO, "refresh base:{0}, currentPath:{1}", 
                new Object[]{base.getAbsolutePath(), currentPath.getAbsolutePath()});
        File[] subfolders = currentPath.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getName().startsWith(".") && pathname.isDirectory();
            }
        });
        Arrays.sort(subfolders, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        subdirs.clear();
        for (File f : subfolders) {
            subdirs.add(new FolderModel(this.base,f));
        }
        File[] fs = currentPath.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getName().startsWith(".") &&
                        !pathname.isDirectory();
            }
        });
        Arrays.sort(fs, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        files.clear();
        for (File f : fs) {
            files.add(new FileModel(f));
        }
    }

    /**
     * @return the subdirs
     */
    public List<FolderModel> getSubdirs() {
        return subdirs;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
        currentPath = new File(base, path);
        try {
            refresh();
        } catch (Exception ex) {
            this.files = Collections.EMPTY_LIST;
            this.subdirs = Collections.EMPTY_LIST;
        }
    }

    /**
     * @return the nasSelector
     */
    public String getNasSelector() {
        return nasSelector;
    }

    /**
     * @param nasSelector the nasSelector to set
     */
    public void setNasSelector(String nasSelector) {
        if (!this.nasSelector.equals(nasSelector)) {
// Only init() when changing nas
            this.nasSelector = nasSelector;
            init();
        }
    }
}
