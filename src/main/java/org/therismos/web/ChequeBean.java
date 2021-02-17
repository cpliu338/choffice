package org.therismos.web;

import java.util.*;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import org.bson.Document;
import org.therismos.ejb.MongoService;

/**
 *
 * @author cpliu
 */
@javax.inject.Named
@javax.faces.view.ViewScoped
public class ChequeBean implements java.io.Serializable {
    private final List<SelectItem> dateChoices;
    private String enddate;
    private Document result;

    public Document getResult() {
        return result;
    }
    
    static final Logger logger = Logger.getLogger(ChequeBean.class.getName());
    
    public ChequeBean() {
        dateChoices = new ArrayList<>();
    }
    
    @javax.inject.Inject
    MongoService mongoService;
    
    @PostConstruct
    public void init() {
        if (!dateChoices.isEmpty())
            getDateChoices().clear();
        Stack<String> dates = mongoService.getChequeDates();
        enddate = dates.peek();
        while (!dates.isEmpty()) {
            String s = dates.pop();
            getDateChoices().add(new SelectItem(s));
        }
    }
    
    public void updateCheques() {
        result = mongoService.getCheques(enddate);
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
