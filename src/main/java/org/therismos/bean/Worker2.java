package org.therismos.bean;

import com.itextpdf.text.DocumentException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Worker2 implements Runnable {

    private int year;
    private int batch;
    private File folder;
    private List<Record> items;
    
    @Override
    public void run() {
        PrintShop printShop = new PrintShop();
        //ResourceBundle bundle = ResourceBundle.getBundle("formatStrings");
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            printShop.init(new File(folder, String.format("%d-r%d.pdf",year,batch)));
//            Date d1 = fmt.parse(String.format("%d-04-01",year-1));
//            Date d2 = fmt.parse(String.format("%d-03-31",year));
            for (Record rec : items) {
                printShop.printRecord(rec);
            }
//        } catch (ParseException ex) {
//            Logger.getLogger(Worker2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Worker2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(Worker2.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            printShop.cleanup();
        }
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(File folder) {
        this.folder = folder;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<Record> items) {
        this.items = items;
    }

    /**
     * @param batch the batch to set
     */
    public void setBatch(int batch) {
        this.batch = batch;
    }
    
}
