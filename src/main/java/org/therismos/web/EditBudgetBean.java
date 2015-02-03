package org.therismos.web;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import org.therismos.ejb.BudgetDao;
import org.therismos.entity.Account;
import org.therismos.model.BudgetModel;

/**
 *
 * @author USER
 */
@javax.faces.bean.ManagedBean
@javax.faces.bean.ViewScoped
public class EditBudgetBean {
    @javax.inject.Inject
    BudgetDao budgetDao;
    
    private BudgetModel budget;
    static final Logger logger = Logger.getLogger(EditBudgetBean.class.getName());
    /**
     * 0 = not defined, 1 = new entry, 2 = existing entry
     */
    private int status;
    private final List<String> codes;
    private final List<Map> entries;

    public List<String> getCodes() {
        return codes;
    }

    public int getStatus() {
        return status;
    }
            
    public EditBudgetBean() {
        budget = new BudgetModel();
        budget.setYear(Calendar.getInstance().get(Calendar.YEAR));
        status = 0;
        codes = new ArrayList<>();
        entries = new ArrayList<>();
    }

    public BudgetModel getBudget() {
        return budget;
    }

    public void refresh() {
        String code = budget.getCode();
        logger.log(Level.INFO, "refresh {0} : {1}", new Object[]{budget.getYear(), code});
        if (budget.getYear()<2014 || code.length()<2) {
            status = 0;
            codes.clear();
            entries.clear();
        }
//        else if (budgetDao.findAllAccountsUnder(code).isEmpty()) {
//            status = 0;
//        }
        else {
            BudgetModel m = budgetDao.find(budget.getYear(), code);
            List<Account> l = budgetDao.findAllAccountsUnder(code);
            logger.log(Level.INFO, "Accounts under {0} : {1}", new Object[]{code, l.size()});
            codes.clear();
            for (Account a : l) {
                codes.add(String.format("%s (%s)", a.getNameChi(),a. getCode()));
            }
            if (m==null) {
                status = 1;
    //            FacesContext.getCurrentInstance().addMessage(null, 
    //                new FacesMessage(FacesMessage.SEVERITY_WARN, "No such code", "No such budget"));
            }
            else {
                budget = m;
                status = 2;
                for (Object o : m.getEntries()) {
                    DBObject dbo = (DBObject)o;
                }
            }
        }
    }
    
}
