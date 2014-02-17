package org.therismos.model;

import java.io.File;

/**
 *
 * @author cpliu
 */
public class FileModel {
    
    private String name;
    private String strSize;
    private long size;

    public FileModel(File file) {
        name = file.getName();
        if (file.isDirectory()) {
            return;
        }
        try {
            size = new java.io.FileInputStream(file).getChannel().size();
        } catch (Exception ex) {
            size = 0;
        }
        if (size > 1048576)
            strSize = String.format("%.2f MB", (float)size/1048576);
        else if(size > 1024)
            strSize = String.format("%.2f kB", (float)size/1024);
        else
            strSize = String.format("%d bytes", size);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the strSize
     */
    public String getStrSize() {
        return strSize;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

}
