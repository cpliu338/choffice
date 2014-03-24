package org.therismos.web;

import java.io.FileNotFoundException;
import javax.faces.bean.ManagedBean;
import barcode.GenBarcode;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;

/**
 *
 * @author cpliu
 */
@ManagedBean
public class StreamedBean implements java.io.Serializable {

    private DefaultStreamedContent barcode;
    private File basePath;
    private File photospath;
    @javax.inject.Inject
    UserBean userBean;

    @javax.annotation.PostConstruct
    public void init() {
        basePath = userBean.getBasePath();
        photospath = new File(basePath, "photos");
    }

    public DefaultStreamedContent getWheat() {
        File bkg = new File(this.basePath, "photos/wheat.jpg");
        try {
            return new DefaultStreamedContent(new FileInputStream(bkg), "image/jpeg");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StreamedBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public DefaultStreamedContent getPhoto() {
        DefaultStreamedContent photo = null;
        int id = 1;
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            java.util.Map<String,String> map = fc.getExternalContext().getRequestParameterMap();
            try {
                if (map.containsKey("id")) {
                    id = Integer.parseInt(map.get("id"));
        Logger.getLogger(getClass().getName()).log(Level.INFO, "getphoto Id:{0}", id);
                }
            }
            catch (NumberFormatException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE,null, ex);
            }
            File photoFile = new File(photospath, String.format("%d.jpg", id));
            if (!photoFile.canRead())
                photoFile = new File(photospath,"default.jpg");
            photo = new DefaultStreamedContent(new FileInputStream(photoFile), "image/jpeg");
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,null, e);
        }
        return photo;
    }
    
    /**
     * @return the barcode
     */
    public DefaultStreamedContent getBarcode() {
        barcode = null;
        File barcodepath = new File(this.basePath, "codes");
        GenBarcode barcodebean = new GenBarcode();
        barcodebean.setBasePath(barcodepath.getAbsolutePath());
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            java.util.Map<String,String> map = fc.getExternalContext().getRequestParameterMap();
            try {
                if (map.containsKey("id")) {
                    int id = Integer.parseInt(map.get("id"));
                    barcodebean.setCode(id);
                }
                else
                    barcodebean.setCode(1);
            }
            catch (NumberFormatException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,null, ex);
            }
            File barcodeFile = new File(barcodebean.getFileName());
            if (!barcodeFile.canRead()) {
                barcodebean.start();
            }
            barcode = new DefaultStreamedContent(new FileInputStream(barcodeFile), "image/gif");
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,null, e);
        }
        return barcode;
    }

    /**
     * @param barcode the barcode to set
     */
    public void setBarcode(DefaultStreamedContent barcode) {
        this.barcode = barcode;
    }

    public void handleFileUpload(FileUploadEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        MemberBean memberBean = (MemberBean)fc.getApplication().evaluateExpressionGet(fc, "#{memberBean}", MemberBean.class);
        int id = 1;
        if (memberBean != null)
            id = memberBean.getId();
        File file = new File(photospath, String.format("%d.jpg", id));
        try {
        java.io.InputStream is = event.getFile().getInputstream();
        java.io.OutputStream os = new java.io.FileOutputStream(file);
            int ch;
            while ((ch = is.read()) != -1) {
                os.write(ch);
            }
            is.close();
            os.close();
        }
        catch (java.io.IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,null, ex);
        }
        Logger.getLogger(getClass().getName()).fine(event.getFile().getContentType());
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public int getMemberId() {
        FacesContext fc = FacesContext.getCurrentInstance();
        MemberBean memberBean = (MemberBean)fc.getApplication().evaluateExpressionGet(fc, "#{memberBean}", MemberBean.class);
        int id = memberBean.getId();
        File photoFile = new File(photospath, String.format("%d.jpg", id));
        Logger.getLogger(getClass().getName()).log(Level.INFO, "member Id:{0}", (photoFile.canRead()) ? id : 0);
        return (photoFile.canRead()) ? id : 0;
    }

}
