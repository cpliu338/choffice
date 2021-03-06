package org.therismos.web;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import org.therismos.ejb.BudgetDao;
import org.therismos.model.BudgetModel;

/**
 *
 * @author USER
 */
@javax.faces.bean.ManagedBean
@javax.faces.bean.SessionScoped
public class BudgetBean {
    @javax.inject.Inject
    BudgetDao budgetDao;
    
    private int year;
    private String code;
    private final List<SelectItem> codeChoices;
    private final List<SelectItem> dateChoices;
    private final List<SelectItem> yearChoices;
    private List<Map> entrySummary;
    private final Map<String,Object> result;
    private String cutoffDate;
    private static final SelectItem blank;
    private BudgetModel budget;
            
    public List<SelectItem> getDateChoices() {
        return dateChoices;
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    static {
        blank = new SelectItem("", "Select one");
    }
    
    public BudgetBean() {
        year = 0;
        budget = null;
        entrySummary = Collections.EMPTY_LIST;
        result = new HashMap<>();
        code = "";
        yearChoices = new ArrayList<>();
        codeChoices = new ArrayList<>();
        codeChoices.add(blank);
        dateChoices = new ArrayList<>();
        dateChoices.add(blank);
        cutoffDate = "";
    }
    
    @PostConstruct
    public void init() {
        Iterator it = budgetDao.findAllYears().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            yearChoices.add(new SelectItem(o, o.toString()));
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    
    public BudgetModel getBudget() {
        return budget;
    }

    public List<Map> getEntrySummary() {
        return entrySummary;
    }
    
    public List<SelectItem> getCodeChoices() {
        return codeChoices;
    }
    
    public List<SelectItem> getYearChoices() {
        return yearChoices;
    }
    
    public void refreshYear(AjaxBehaviorEvent event) throws javax.faces.event.AbortProcessingException {
        Logger.getLogger(BudgetBean.class.getName()).log(Level.FINE, "Year is now: {0}", year);
        codeChoices.clear();
        codeChoices.add(blank);
        code = "";
        result.put("actual_total", "");
        result.put("budget_items", Collections.EMPTY_LIST);
        for (BudgetModel b : budgetDao.find(new BasicDBObject("year",year))) {
            codeChoices.add(new SelectItem(b.getCode(), b.getName_chi()));
            Logger.getLogger(BudgetBean.class.getName()).log(Level.FINE, "budgetmodel: {0}", b);
        }
    }
    
    public void refreshCode(AjaxBehaviorEvent event) throws javax.faces.event.AbortProcessingException {
        String oCode = code;
        refreshYear(event);
        code = oCode;
        entrySummary.clear();
        dateChoices.clear();
        dateChoices.add(blank);
        cutoffDate = "";
        result.put("actual_total", "");
        result.put("remarks", "");
        result.put("budget_items", Collections.EMPTY_LIST);
        if (year!=0 && code.length()>0) {
            budget = budgetDao.find(year, code);
            Iterator<Map<String,Object>> it = budget.getEntries().iterator();
            while (it.hasNext()) {
                String dateStr = it.next().get("date").toString();
                dateChoices.add(new SelectItem(dateStr,dateStr));
            }
        }
        else {
            budget = null;
        }
        entrySummary = Collections.EMPTY_LIST;
    }
    
    public void execute() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date cutoff = fmt.parse(cutoffDate);
            Date yearStart = fmt.parse(cutoffDate.substring(0, 4).concat("-01-01"));
            entrySummary = budgetDao.aggregateEntries(yearStart, cutoff, budget.getSubitems());
            BigDecimal tot = BigDecimal.ZERO;
            for (Map entry : entrySummary) {
                BigDecimal subtotal = (BigDecimal)entry.get("total");
                tot = tot.add(subtotal);
            }
            result.put("actual_total", tot.toString());
        } catch (ParseException ex) {
            entrySummary = Collections.EMPTY_LIST;
            result.put("actual_total", "");
            result.put("budget_items", Collections.EMPTY_LIST);
            return;
        }
        Logger.getLogger(BudgetBean.class.getName()).log(Level.FINE, "updateCutoffDate {0}:{1}:{2}", new Object[]{code, year,cutoffDate});
        List<Map> entries = new ArrayList<>();
        BigDecimal tot = BigDecimal.ZERO;
//  Change below
        for (Iterator<Map<String, Object>> it = budget.getEntries().iterator(); it.hasNext();) {
//            DBObject o = (DBObject)it.next();
            Map<String,Object> map = it.next();
            String key = map.get("date").toString();
            if (key.compareTo(cutoffDate)>0) continue;
            Object value = map.get("amount");
            if (Double.class.isAssignableFrom(value.getClass()))
                tot = tot.add(new BigDecimal((Double)value));
            else if (Integer.class.isAssignableFrom(value.getClass()))
                tot = tot.add(new BigDecimal((Integer)value));
            else
                Logger.getLogger(BudgetBean.class.getName()).log(Level.FINE, "Value class: {0}", value.getClass().getName());
            map.put("date", key);
            map.put("value", value.toString());
            entries.add(map);
        }
        result.put("budget_items", entries);
        result.put("budget_total", tot.setScale(2, RoundingMode.HALF_UP).toString());
    }

    /**
     * @return the result
     */
    public Map getResult() {
        return result;
    }

    /**
     * @return the cutoffDate
     */
    public String getCutoffDate() {
        return cutoffDate;
    }

    /**
     * @param cutoffDate the cutoffDate to set
     */
    public void setCutoffDate(String cutoffDate) {
        this.cutoffDate = cutoffDate;
    }

}
