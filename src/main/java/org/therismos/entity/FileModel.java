package org.therismos.entity;

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
    
    public String getTimeAgoInWords() {
        long ts = getTimestamp();
        if (ts > System.currentTimeMillis()) {
            return "Future";
        }
        else if (ts > 1) {
            long s = (System.currentTimeMillis() - ts)/1000L;
            if (s < 60) {
                return String.format("%d s ago", s);
            }
            if (s < 3600) {
                return String.format("%.1f m ago", s/60.0);
            }
            if (s < 86400) {
                return String.format("%.1f h ago", s/3600.0);
            }
            return String.format("%.1f d ago", s/86400.0);
        }
        else return "???";
    }
    
    public long getTimestamp() {
        try {
            return Long.parseLong(name.substring(name.lastIndexOf('_')+1, name.lastIndexOf('.')));
        }
        catch (RuntimeException r) {
            return -1L;
        }
    }

}
