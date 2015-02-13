package org.therismos.web;

import com.mongodb.WriteResult;
import java.util.*;
import java.util.logging.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.ArrayDataModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
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
    private Double amount;
    private ArrayDataModel entries;
    private String selectedDate;

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }
    
    public void removeBudget() {
        WriteResult wr = budgetDao.remove(budget.getYear(), budget.getCode());
        if (wr.getError() == null) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Delete record", "done"));
        }
        else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot delete", wr.getError()));
        }
    }
    
    public void removeItem() {
        budget.removeFromEntries(selectedDate);
        entries = populateEntries(budget);
        status = 1;
        logger.log(Level.FINE, "Removing {0}, total {1} items", 
                new Object[]{selectedDate,budget.getEntries().size()});
    }

    public void addItem() {
        budget.addToEntries(selectedDate, amount);
        entries = populateEntries(budget);
        status = 1;
        logger.log(Level.FINE, "Adding {0} and {1}, total {2} items", 
                new Object[]{selectedDate,amount,budget.getEntries().size()});
    }

    public ArrayDataModel getEntries() {
        return entries;
    }

    public int getStatus() {
        return status;
    }
            
    public EditBudgetBean() {
        budget = new BudgetModel();
        DateTime today = DateTime.now();
        budget.setYear(today.year().get());
        budget.setCode("50");
        status = 0;
        String tod = DateTimeFormat.forPattern("yyyy-MM-dd").print(today);
        logger.log(Level.FINE, "today is {0}", new Object[]{tod});
        selectedDate=tod;
        entries = null;
        amount = 0.0;
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
    
    public List<Account> getSubcodes() {
        return budget.getSubitems();
    }
    
    public void save() {
        try {
            budgetDao.save(budget);
            status = 2;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Saved record", 
                        String.format("%s(%d)",budget.getCode(),budget.getYear()))
            );
        }
        catch (RuntimeException ex) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getClass().getName(), ex.getMessage()));
        }
    }
    
    public void refresh() {
        String code = budget.getCode();
        if (budget.getYear()<2014 || code.length()<2) {
            status = 0;
        }
        else if (budgetDao.findAllAccountsUnder(code).isEmpty()) {
            status = 0;
        }
        else {
            BudgetModel budgetModel = budgetDao.find(budget.getYear(), code);
                StringBuilder buf = new StringBuilder();
                for (String c : budgetDao.findAllCodesInYear(budget.getYear())) {
                    buf.append(c).append(',');
                }
                FacesContext.getCurrentInstance().addMessage("form:code", 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Available budgets", buf.toString()));
            if (budgetModel==null) {
                status = 1;
            List<Account> l = budgetDao.findAllAccountsUnder(code);
                budgetModel = new BudgetModel();
                budgetModel.setYear(budget.getYear());
                budgetModel.setCode(code);
                budgetModel.clearSubitems();
                for (Account a : l) {
                    budgetModel.addToSubitems(a);
                }
                budget = budgetModel;
                entries = populateEntries(budgetModel);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "New record", "need to be saved"));
            }
            else {
                budget = budgetModel;
                entries = populateEntries(budgetModel);
//                budgetModel.clearSubitems();
//                for (Account a : l) {
//                    budgetModel.addToSubitems(a);
//        logger.log(Level.FINE, "Added {0} : {1}", new Object[]{a.getNameChi(), a.getCode()});
//                }
                status = 2;
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Existing record", "found"));
            }
        }
    }

    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
}
