package org.therismos.model;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cpliu
 */
public class FolderModel {

    private String name;
    private String strStrippedBase;

    public FolderModel(File base, File file) {
        name = file.getName();
        if (!file.isDirectory()) return;
        String strBase = base.getAbsolutePath();
        if (file.getAbsolutePath().startsWith(strBase))
            strStrippedBase = file.getAbsolutePath().substring(strBase.length()+1);
        else
            strStrippedBase = name;
//        Logger.getLogger(FolderModel.class.getName()).log(Level.INFO, "strStrippedBase:{0}", this.strStrippedBase);
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
