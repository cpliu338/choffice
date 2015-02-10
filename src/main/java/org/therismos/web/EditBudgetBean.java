package org.therismos.web;

import java.util.*;
import java.util.logging.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.ArrayDataModel;
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
//    private final List<String> codes;
    private ArrayDataModel entries;
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

    public ArrayDataModel getEntries() {
        return entries;
    }

    public int getStatus() {
        return status;
    }
            
    public EditBudgetBean() {
        budget = new BudgetModel();
        budget.setYear(Calendar.getInstance().get(Calendar.YEAR)-1);
        budget.setCode("50");
        status = 0;
        selectedDate="";
//        codes = new ArrayList<>();
        entries = null;
    }

    public BudgetModel getBudget() {
        return budget;
    }

    private ArrayDataModel populateEntries(BudgetModel bm) {
        Set set = bm.getEntries();
        int size = set.size();
        Map[] m = new Map[size];
        int i=0;
        for (Object entry : set) {
            m[i++] = (Map)entry;
        }
//        logger.log(Level.INFO, "Entries:" + m.length);
        return new ArrayDataModel(m);
    }
    
    @PostConstruct
    public void init() {
        refresh();
    }
    
    public Iterator<Map<String, Object>> getSubcodes() {
        return budget.getSubitems().iterator();
    }
    public void refresh() {
        String code = budget.getCode();
        if (budget.getYear()<2014 || code.length()<2) {
            status = 0;
//            codes.clear();
//            entries.clear();
        }
        else if (budgetDao.findAllAccountsUnder(code).isEmpty()) {
            status = 0;
        }
        else {
            BudgetModel budgetModel = budgetDao.find(budget.getYear(), code);
            List<Account> l = budgetDao.findAllAccountsUnder(code);
//            logger.log(Level.INFO, "Accounts under {0} : {1}", new Object[]{code, l.size()});
//            codes.clear();
//            for (Account a : l) {
//                codes.add(String.format("%s (%s)", a.getNameChi(),a. getCode()));
//            }
            if (budgetModel==null) {
                status = 1;
                budgetModel = new BudgetModel();
                budgetModel.setYear(budget.getYear());
                budgetModel.setCode(code);
                for (Account a : l) {
//        logger.log(Level.INFO, "Added {0} : {1}", new Object[]{a.getNameChi(), a.getCode()});
                    budgetModel.addToSubitems(a.getNameChi(),a.getCode());
                }
                budget = budgetModel;
                entries = populateEntries(budgetModel);
    //            FacesContext.getCurrentInstance().addMessage(null, 
    //                new FacesMessage(FacesMessage.SEVERITY_WARN, "No such code", "No such budget"));
            }
            else {
                budget = budgetModel;
                entries = populateEntries(budgetModel);
                status = 2;
                for (Map o : budgetModel.getEntries()) {
                    logger.log(Level.INFO, "{0}:{1}", new Object[]{o.get("date").toString(), o.get("amount").toString()});
                }
            }
        }
    }
    
}
