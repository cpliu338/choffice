package org.therismos.web;

import jakarta.faces.application.FacesMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * all beans providing StreamedContent from file system should implement me
 * @since Ver 7.0
 * @author cp_liu
 */
public interface DownloadFile extends WebBean {
    abstract File getFile2Download(String fileName);
    default StreamedContent downloadFile(String fileName) {
        File file = getFile2Download(fileName);
        if (!file.canRead() ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Cannot read file", fileName);
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
    
    
}
