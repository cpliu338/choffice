package org.therismos.web;
import jakarta.annotation.PostConstruct;
import java.util.regex.Pattern;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.nio.file.*;
import java.util.stream.Collectors;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Faces;
import org.therismos.bean.ApplicationBean;
import org.therismos.entity.FileModel;
import org.therismos.job.AbstractJob;

/**
 *
 * @author cp_liu
 */
@Named
@ViewScoped
public class DownloadBean implements DownloadFile, Serializable {

    /**
     * @return the ms2keep
     */
    public long getMs2keep() {
        return ms2keep;
    }

    /**
     * @param ms2keep the ms2keep to set
     */
    public void setMs2keep(long ms2keep) {
        this.ms2keep = ms2keep;
    }

    /**
     * @return the files
     */
    public List<FileModel> getFiles() {
        return files;
    }

    /**
     * @return the selectedClassName
     */
    public String getSelectedClassName() {
        return selectedClassName;
    }

    final List<Class<? extends AbstractJob>> jobClasses;
    final List<SelectItem> types;
    private List<FileModel> files;
    private File downloadDir;
    ResourceBundle bundle;
    // download files older than 2 days long will be deleted for development set to 1 hour
    private long ms2keep;
    
    @Param
    String type;
    String selectedClassName;
    private static final String JOBPATH = "org.therismos.job.";
    // can use choffice.properties
    private static final Set<String> BLACKLIST = Set.of(
        "Job", "AbstractJob", "AbstractXlsxJob"
    );
    
    @Inject
    ApplicationBean appBean;
    
    /**
     * @param selectedClassName the selectedClassName to set
     */
    public void setSelectedClassName(String selectedClassName) {
        this.selectedClassName = selectedClassName; 
    }
    
    public DownloadBean() {
        jobClasses = new ArrayList<>();
        types = new ArrayList<>();
        files = new ArrayList<>();
    }
    
    @PostConstruct
    public void init() throws IOException, ClassNotFoundException {
        downloadDir = new File(appBean.getDatapath(), "downloads");
        ms2keep = ("Development".equalsIgnoreCase(appBean.getProjectStage())) ? 3600000L : 2*86400000L;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        bundle = ResourceBundle.getBundle("messages", facesContext.getViewRoot().getLocale());
        String basePath = facesContext.getExternalContext()
                .getRealPath("/WEB-INF/classes/org/therismos/job/");
        if (basePath == null) {
            throw new IllegalStateException("Cannot resolve /WEB-INF/classes/org/therismos/job/");
        }

        Path packagePath = Paths.get(basePath);

        List<Path> classFiles = Files.walk(packagePath)
                .filter(p -> p.toString().endsWith(".class"))
                .collect(Collectors.toList());

        Class<?> jobBase = Class.forName(JOBPATH + "AbstractJob");

        for (Path classFile : classFiles) {
            String fileName = classFile.getFileName().toString();
            String simpleName = fileName.substring(0, fileName.length() - 6);

            if (BLACKLIST.contains(simpleName)) {
                continue;
            }

            Path relative = packagePath.relativize(classFile);
            String fqcn = JOBPATH + relative.toString()
                .replace(FileSystems.getDefault().getSeparator(), ".")
                .replaceAll("\\.class$", "");

            Class<?> clazz = Thread.currentThread()
                .getContextClassLoader()
                .loadClass(fqcn);

            if (jobBase.isAssignableFrom(clazz)
                    && !clazz.isInterface()
                    && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())
                    ) {
                jobClasses.add((Class<? extends AbstractJob>)clazz);
            }
            populateTypes();
        }
        selectedClassName = "";
        for (Class<? extends AbstractJob> clazz : jobClasses) {
            if (clazz.getSimpleName().equals(type))
                selectedClassName = clazz.getName();                
        }
        if (selectedClassName != null && selectedClassName.length()>1)
            refreshFiles();
    }
        
    /**
     * Delete files with older than now - ms2keep
     */
    private void housekeep() {
        final long oldestTimestamp = System.currentTimeMillis() - ms2keep;
        getLog().log(Level.INFO, "Oldest ts: {0} due to {1}", new Long[]{oldestTimestamp, ms2keep});
        File[] f = downloadDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String string = file.getName();
                int last_ = string.lastIndexOf('_');
                if (last_ < 1) return false;
                int lastDot = string.lastIndexOf('.');
                if (last_ > lastDot) return false;
                try {
                    return oldestTimestamp > Long.parseLong(string.substring(last_+1, lastDot));
                }
                catch (RuntimeException ex) {
                    return false;
                }
            }

        });
        for (File fn : f) {
            String path = fn.getAbsolutePath();
            boolean deleted = fn.delete();
            getLog().log(Level.FINE, "Delete {0} was {1}", new Object[]{path, deleted ? "successful" : "failed"});
        }
    }

    private void populateTypes() {
        types.clear();
        for (Class<? extends AbstractJob> clazz : jobClasses) {
            types.add(new SelectItem(clazz.getName(), bundle.getString(clazz.getName())));
        }
        getLog().log(Level.FINE, "there are {0} types", types.size());
    }
    
    public List<SelectItem> getTypes() {
        return types;
    }
    
    @Override
    public File getFile2Download(String fileName) {
        return new File(downloadDir, fileName);
    }
    
    public void typeChange() {
        populateTypes();
        refreshFiles();
        housekeep();
    }

    private void refreshFiles() {
        final StringBuilder pattern = new StringBuilder();
        try {
            Class<AbstractJob> clazz = (Class<AbstractJob>) Class.forName(selectedClassName);
            AbstractJob job = AbstractJob.createJob(clazz.getSimpleName(), appBean, new org.bson.Document(), clazz);
            getLog().log(Level.FINE, "{0} pattern {1}", new String[]{clazz.getSimpleName(), job.getFilePattern()});
            pattern.append(job.getFilePattern());
        } catch (Exception ex) {
            getLog().log(Level.SEVERE, (String) null, ex);
        }
        files = getModels(downloadDir, pattern.toString(), System.currentTimeMillis()-getMs2keep());
        getLog().log(Level.FINE, "files size {0}", files.size());
    }
    
    public String getDebug() {
        if (selectedClassName == null || selectedClassName.length()==0)
            return "No selected type";
        return selectedClassName;        
    }

    public List<FileModel> getModels(File base, String regex, long timestamp) {
        List<FileModel> models = new ArrayList<>();

        // Ensure base is a directory
        if (base == null || !base.isDirectory()) {
            throw new IllegalArgumentException("Base must be an existing directory: " + base);
        }

        Pattern pattern = Pattern.compile(regex);

        File[] files = base.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && pattern.matcher(file.getName()).matches()) {
                    models.add(new FileModel(file));
                }
            }
        }

        // Remove outdated models and delete their files
        Iterator<FileModel> iterator = models.iterator();
        while (iterator.hasNext()) {
            FileModel m = iterator.next();
            if (m.getTimestamp() < timestamp) {
                iterator.remove();
                m.unlink();
            }
        }

        return models;
    }
}
