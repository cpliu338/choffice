package org.therismos.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.therismos.model.FileModel;
//import model.FolderModel;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author cpliu
 */
@ManagedBean
@ViewScoped
public class NasBean {

    private File base;
    private File currentPath;
    private String selectedFile;
    private List<FileModel> files;
//    private List<FolderModel> subdirs;
    private StreamedContent content;
    private String path;

    @javax.inject.Inject
    UserBean userBean;
    
    @javax.annotation.PostConstruct
    public void init() {
        currentPath = userBean.getBasePath();
        files = new ArrayList<>();
//        subdirs = new ArrayList<FolderModel>();
        content = null;
        try {
            refresh();
        } catch (Exception ex) {
            this.files = Collections.EMPTY_LIST;
//            this.subdirs = Collections.EMPTY_LIST;
        }
    }

//    public List<FolderModel> getBreadCrumbs() {
//        String strBase = base.getAbsolutePath();
//        ArrayList<FolderModel> breadCrumbs = new ArrayList<FolderModel>();
//        for (File current=currentPath; !strBase.equals(current.getAbsolutePath());
//        current = current.getParentFile()) {
//            breadCrumbs.add(new FolderModel(base, current));
//        }
//        Collections.reverse(breadCrumbs);
//        return breadCrumbs;
//    }

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

    /**
     * @return the selectedFile
     */
    public String getSelectedFile() {
        Logger.getLogger(NasBean.class.getName()).info("Get selectedFile");
        return selectedFile;
    }

    /**
     * @param selectedFile the selectedFile to set
     */
    public void setSelectedFile(String selectedFile) {
        this.selectedFile = selectedFile;
        try {
            content = new DefaultStreamedContent(new
            java.io.FileInputStream(new File(currentPath,selectedFile)),
                    "application/octet-stream", selectedFile);
        }
        catch (FileNotFoundException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("IO Exception"+ex.getMessage()));
        }
    }

    /**
     * @return the files
     */
    public List<FileModel> getFiles() {
        return files;
    }

    public final void refresh() throws Exception {
        Logger.getLogger(NasBean.class.getName()).log(Level.INFO, "refresh called");
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
//        subdirs.clear();
//        for (File f : subfolders) {
//            subdirs.add(new FolderModel(this.base,f));
//        }
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
//    public List<FolderModel> getSubdirs() {
//        return subdirs;
//    }

    /**
     * @return the content
     */
    public StreamedContent getContent() {
        return content;
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
        Logger.getLogger(NasBean.class.getName()).log(Level.INFO, "setting path {0}",path);
        this.path = path;
        currentPath = new File(base, path);
        try {
            refresh();
        } catch (Exception ex) {
            this.files = Collections.EMPTY_LIST;
//            this.subdirs = Collections.EMPTY_LIST;
        }
    }
}
