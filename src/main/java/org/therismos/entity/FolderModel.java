package org.therismos.entity;

import java.io.File;

/**
 *
 * @author cpliu
 */
public class FolderModel {

    private final String name;
    private String strStrippedBase;

    public FolderModel(File base, File file) {
        name = file.getName();
        if (!file.isDirectory()) return;
        String strBase = base.getAbsolutePath();
        if (file.getAbsolutePath().startsWith(strBase))
            strStrippedBase = file.getAbsolutePath().substring(strBase.length()+1);
        else
            strStrippedBase = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the strStrippedBase
     */
    public String getStrStrippedBase() {
        return strStrippedBase;
    }
    
}
