package org.therismos.web;

import com.mongodb.DBObject;
import java.util.*;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.therismos.ejb.ChequeDao;

/**
 *
 * @author cpliu
 */
@javax.faces.bean.ManagedBean
@javax.faces.bean.ViewScoped
public class ChequeBean {
    private final List<SelectItem> dateChoices;
    private String enddate;
    private DBObject result;

    public DBObject getResult() {
        return result;
    }
    
    @Inject
    ChequeDao chequeDao;
    static final Logger logger = Logger.getLogger(ChequeBean.class.getName());
    
    public ChequeBean() {
        dateChoices = new ArrayList<>();
    }
    
    @PostConstruct
    public void init() {
        if (!dateChoices.isEmpty())
            getDateChoices().clear();
        Stack<String> dates = chequeDao.getChequeDates();
        enddate = dates.peek();
        while (!dates.isEmpty()) {
            String s = dates.pop();
            getDateChoices().add(new SelectItem(s));
        }
    }
    
    public void updateCheques() {
        result = chequeDao.findCheques(enddate);
//        logger.info(result.get("pending").getClass().getName());
    }

    /**
     * @return the dateChoices
     */
    public List<SelectItem> getDateChoices() {
        return dateChoices;
    }

    /**
     * @return the enddate
     */
    public String getEnddate() {
        return enddate;
    }

    /**
     * @param enddate the enddate to set
     */
    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }
}
