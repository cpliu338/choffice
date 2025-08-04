package org.therismos.web;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import org.primefaces.model.DefaultStreamedContent;
import java.nio.file.*;
import java.util.stream.Collectors;
import org.bson.Document;
import org.primefaces.model.StreamedContent;
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
    private final List<FileModel> files;
    private File downloadDir;
    ResourceBundle bundle;

    private String selectedClassName;
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
                    && hasPublicStaticStringGetFilePattern(clazz)
                    ) {
                jobClasses.add((Class<? extends AbstractJob>)clazz);
            }
            populateTypes();
        }
    }
    
    public boolean hasPublicStaticStringGetFilePattern(Class<?> clazz) {
        try {
            Method m = clazz.getDeclaredMethod("getFilePattern");
            return Modifier.isStatic(m.getModifiers())
                    && Modifier.isPublic(m.getModifiers())
                    && m.getReturnType() == String.class
                    && m.getParameterCount() == 0;
        } catch (NoSuchMethodException e) {
            return false;
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
        final StringBuilder pattern = new StringBuilder();
        try {
            Class<?> clazz = Class.forName(selectedClassName);

            // Get the method (no parameters)
            Method m = clazz.getDeclaredMethod("getFilePattern");

            // Invoke the static method (null for instance)
            Object result = m.invoke(null);

            // Cast to String
            pattern.append((String) result);
        } catch (Exception ex) {
            getLog().log(Level.SEVERE, (String) null, ex);
        }
        File[] fs = downloadDir.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean matched = pathname.getName().matches(pattern.toString());
                return matched;
            }
        });
        files.clear();
        for (File f : fs) {
            files.add(new FileModel(f));
        }
    }
    
    public String getDebug() {
        if (selectedClassName == null || selectedClassName.length()==0)
            return "No selected type";
        try {
            Class<?> clazz = Class.forName(selectedClassName);
            // Get the method (no parameters)
            Method m = clazz.getDeclaredMethod("getFilePattern");
            // Invoke the static method (null for instance)
            Object result = m.invoke(null);
            return (String) result;
        } catch (Exception ex) {
            getLog().log(Level.SEVERE, (String) null, ex);
        }
        return "Oh No";
        
    }
    
}
