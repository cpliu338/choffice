package org.therismos.web;

import java.util.*;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.therismos.ejb.BudgetDao;
import org.therismos.entity.Account;
import org.therismos.model.BudgetModel;

/**
 *
 * @author USER
 */
@javax.faces.bean.ManagedBean
@javax.faces.bean.ViewScoped
public class EditBudgetBean implements java.io.Serializable {
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
    private String selectedDate;

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }
    
    public void removeItem() {
        logger.log(Level.INFO, "Removing {0}", selectedDate);
        budget.removeItem(selectedDate);
    }

    public /*List<String>*/javax.faces.model.ArrayDataModel getCodes() {
        //return codes;
        Set set = budget.getEntries();
        int size = set.size();
        Map[] m = new Map[size];
        int i=0;
        for (Object entry : set) {
            m[i++] = (Map)entry;
        }
        logger.log(Level.INFO, "Entries:" + m.length);
        return new javax.faces.model.ArrayDataModel(m);
    }

    public int getStatus() {
        return status;
    }
            
    public EditBudgetBean() {
        budget = new BudgetModel();
        budget.setYear(Calendar.getInstance().get(Calendar.YEAR));
        status = 0;
        selectedDate="";
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
        else if (budgetDao.findAllAccountsUnder(code).isEmpty()) {
            status = 0;
        }
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
                m = new BudgetModel();
                m.setYear(budget.getYear());
                m.setCode(code);
                budget = m;
    //            FacesContext.getCurrentInstance().addMessage(null, 
    //                new FacesMessage(FacesMessage.SEVERITY_WARN, "No such code", "No such budget"));
            }
            else {
                budget = m;
        logger.log(Level.INFO, budget.toString());
                status = 2;
                for (Map o : m.getEntries()) {
                    logger.log(Level.INFO, "{0}:{1}", new Object[]{o.get("date").toString(), o.get("amount").toString()});
                }
            }
        }
    }
    
}
