package org.therismos.web;

import barcode.GenBarcode;
import jakarta.enterprise.context.RequestScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.servlet.ServletContext;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.util.Callbacks.SerializableSupplier;

/**
 *
 * @author cpliu
 */
@jakarta.inject.Named
@RequestScoped
public class StreamedBean implements java.io.Serializable {

    static final Logger LOG = Logger.getLogger(StreamedBean.class.getName());
    
    private DefaultStreamedContent barcode;
    private File basePath;
    private File photospath;
    @jakarta.inject.Inject
    UserBean userBean;
    
    //@jakarta.annotation.Resource
    private ServletContext servletContext;

    @jakarta.annotation.PostConstruct
    public void init() {
        basePath = userBean.getBasePath();
        photospath = new File(basePath, "photos");
        servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
    }
/*
    public DefaultStreamedContent getWheat() {
        File bkg = new File(this.basePath, "photos/wheat.jpg");
        try {
            return new DefaultStreamedContent(new FileInputStream(bkg), "image/jpeg");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StreamedBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
*/
    public DefaultStreamedContent getPhoto() {
        DefaultStreamedContent photo = null;
        int id = 1;
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            java.util.Map<String,String> map = fc.getExternalContext().getRequestParameterMap();
            try {
                if (map.containsKey("id")) {
                    id = Integer.parseInt(map.get("id"));
                    LOG.log(Level.INFO, "getphoto Id:{0}", id);
                }
            }
            catch (NumberFormatException ex) {
                LOG.log(Level.SEVERE,null, ex);
            }
            File photoFile = new File(photospath, String.format("%d.jpg", id));
            if (!photoFile.canRead())
                photoFile = new File(photospath,"default.jpg");
            final File photoFile2 = photoFile;
            photo = DefaultStreamedContent.builder().contentType("image/jpg").stream(new SerializableSupplier<InputStream>() {
                @Override
                public InputStream get() {
                    try {
                        return new FileInputStream(photoFile2);
                    } catch (FileNotFoundException ex) {
                        return servletContext.getResourceAsStream("/resources/images/unknown.jpg");
                    }
                }
            }).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE,null, e);
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
                LOG.log(Level.SEVERE,null, ex);
            }
            File barcodeFile = new File(barcodebean.getFileName());
            if (!barcodeFile.canRead()) {
                barcodebean.start();
            }
            barcode = //new DefaultStreamedContent(new FileInputStream(barcodeFile), "image/gif");
            DefaultStreamedContent.builder().contentType("image/gif").stream(new SerializableSupplier<InputStream>() {
                @Override
                public InputStream get() {
                    try {
                        return new FileInputStream(barcodeFile);
                    } catch (FileNotFoundException ex) {
                        return servletContext.getResourceAsStream("/resources/images/default.gif");
                    }
                }
            }).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE,null, e);
        }
        return barcode;
    }

    /**
     * @param barcode the barcode to set
     */
    public void setBarcode(DefaultStreamedContent barcode) {
        this.barcode = barcode;
    }
/*
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
        LOG.fine(event.getFile().getContentType());
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
*/
    public int getMemberId() {
        FacesContext fc = FacesContext.getCurrentInstance();
        MemberBean memberBean = (MemberBean)fc.getApplication().evaluateExpressionGet(fc, "#{memberBean}", MemberBean.class);
        int id = memberBean.getId();
        File photoFile = new File(photospath, String.format("%d.jpg", id));
        LOG.log(Level.INFO, "member Id:{0}", (photoFile.canRead()) ? id : 0);
        return (photoFile.canRead()) ? id : 0;
    }

}
