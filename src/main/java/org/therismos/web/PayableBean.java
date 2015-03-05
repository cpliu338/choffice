package org.therismos.web;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import javax.faces.bean.ManagedBean;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.therismos.ejb.EntryEjb;
import org.therismos.entity.Account;
import org.therismos.entity.Entry;

/**
 *
 * @author cp_liu
 */
@ManagedBean
@javax.faces.bean.ViewScoped
public class PayableBean implements java.io.Serializable {
    
    public static final long serialVersionUID = 59400432L;
    private static final int CMADEBT = 21002;
    private static final int CMAMDEBT = 22002;
    private static final int GENINC = 41001;
    private static final int THANKINC = 41002;
    private static final int MISSIONINC = 49001;
    private static final int CMAEXP = 59001;
    private static final int CMAMEXP = 59101;

    /**
     * @return the CMADEBT
     */
    public static int getCMADEBT() {
        return CMADEBT;
    }

    /**
     * @return the CMAMDEBT
     */
    public static int getCMAMDEBT() {
        return CMAMDEBT;
    }

    /**
     * @return the GENINC
     */
    public static int getGENINC() {
        return GENINC;
    }

    /**
     * @return the THANKINC
     */
    public static int getTHANKINC() {
        return THANKINC;
    }

    /**
     * @return the MISSIONINC
     */
    public static int getMISSIONINC() {
        return MISSIONINC;
    }

    /**
     * @return the CMAEXP
     */
    public static int getCMAEXP() {
        return CMAEXP;
    }

    /**
     * @return the CMAMEXP
     */
    public static int getCMAMEXP() {
        return CMAMEXP;
    }
    private Date startdate;
    private Date cutoffdate;
    private Map<Integer, BigDecimal> aggregates;
    private final Map<Integer, Entry[]> entriesCMA, entriesCMAM;
    private SelectItem[] choicesCMA;
    private Entry entry;
    private Integer transref;

    public Entry getEntry() {
        return entry;
    }

    @Inject
    EntryEjb entryEjb;
    
    public static java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
    
    static Date findYearStart(Date end) {
        String s2 = formatter.format(end);
        Date d1 = new Date();
        try {
            d1=formatter.parse(s2.substring(0, 4).concat("-01-01"));
        } catch (ParseException ex) {
            Logger.getLogger(PayableBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return d1;
    }

    public PayableBean() {
        cutoffdate = new Date();
        startdate = findYearStart(cutoffdate);
        aggregates = Collections.EMPTY_MAP;
        entriesCMA= new HashMap<>();
        entriesCMAM=new HashMap<>();
        choicesCMA = new SelectItem[0];
        entry = new Entry();
        entry.setId(0);
        transref = 0;
        entry.setTransref(transref);
        entry.setDate1(new Date());
    }
    
    public void updateCMAEntry() {
        if (transref == 0) {
            entry = new Entry();
            entry.setId(0);
            entry.setTransref(transref);
            entry.setDate1(cutoffdate);
            entry.setDetail("CMA Debt up to "+formatter.format(cutoffdate));
        }
        else {
            List<Entry> entries = entryEjb.findByTransref(transref);
            if (entries.isEmpty()) {
            logger.log(Level.INFO, "Transref found none");
                entry.setDetail("ERROR!");
                entry.setDate1(new Date());
            }
            else {
            logger.log(Level.INFO, "Cutoff date is {0,date}", cutoffdate);
            logger.log(Level.INFO, "Transref is {0}", transref);
                Entry e = entryEjb.findByTransref(transref).get(0);
                String olddetail = e.getDetail();
                entry = e;
                entry.setDate1(cutoffdate);
                entry.setDetail(olddetail + formatter.format(cutoffdate));
                entry.setAmount(this.getDue1().add(this.getExpense1()));
            logger.log(Level.INFO, "Entry amount is {0,number}", entry.getAmount());
            }
        }
    }
    
    public void commitCMAEntry() {
        FacesMessage msg = new FacesMessage();
        try {
            logger.log(Level.INFO, "Entry amount is {0,number}", entry.getAmount());
            logger.log(Level.INFO, "Detail is {0}", entry.getDetail());
            logger.log(Level.INFO, "Entry date is {0,date}", entry.getDate1());
            entryEjb.chargePayableShortfall(entry, CMAEXP);
            msg.setSummary("Success");
            if (transref == 0)
                msg.setDetail("Created new entries");
            else
                msg.setDetail("Modified new entries");
        }
        catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
            msg.setSummary(ex.getClass().getName());
            msg.setDetail(ex.getMessage());
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void execute() {
        startdate = findYearStart(cutoffdate);
        List<Integer> accs = new ArrayList<>();
        accs.add(CMAEXP);
        accs.add(CMADEBT);
        entriesCMA.clear();
        List<Integer> blacklist = new ArrayList<>();
        for (Entry e : entryEjb.findByEachAccount(accs, startdate, cutoffdate)) {
            if (blacklist.contains(e.getTransref()))
                continue;
            Entry[] ar = entriesCMA.get(e.getTransref());
            if (ar != null) {
                switch (e.getAccount().getId()) {
                    case CMADEBT:
                        if (ar[0] == null)
                            ar[0] = e;
                        else
                            // blacklist when same account occurs twice
                            blacklist.add(e.getTransref());
                        break;
                    case CMAEXP:
                        if (ar[1] == null)
                            ar[1] = e;
                        else
                            // blacklist when same account occurs twice
                            blacklist.add(e.getTransref());
                        break;
                    default:
                        // blacklist when account is neither CMADEBT nor CMAEXP
                        blacklist.add(e.getTransref());
                }
            }
            else {
                ar = new Entry[2];
                ar[0] = null;
                ar[1] = null;
                switch (e.getAccount().getId()) {
                    case CMADEBT:
                        ar[0] = e; break;
                    case CMAEXP:
                        ar[1] = e; break;
                    default:
                        blacklist.add(e.getTransref());
                }
                try {
                    entriesCMA.put(e.getTransref(), ar);
                }
                catch (RuntimeException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        accs.add(CMAMDEBT);
        accs.add(GENINC);
        accs.add(THANKINC);
        accs.add(CMAMEXP);
        accs.add(MISSIONINC);
        aggregates = entryEjb.aggregateEach(accs, startdate, cutoffdate);
        Iterator<Integer> it = entriesCMA.keySet().iterator();
        while (it.hasNext()) {
            int id=it.next();
            Entry[] ar = entriesCMA.get(id);
            // blacklist when either CMADEBT or CMAEXP is absent
            if (ar[0]==null || ar[1] == null)
                blacklist.add(id);
        }
        for (int blacksheep : blacklist) {
            entriesCMA.remove(blacksheep);
        }
        choicesCMA = new SelectItem[entriesCMA.size()];
        it = entriesCMA.keySet().iterator();
        MessageFormat fmt = new MessageFormat("{0}(${1,number,#.##})");
        for (int i=0; i<entriesCMA.size() && it.hasNext(); i++) {
            int id=it.next();
            Entry e = entriesCMA.get(id)[0];
            choicesCMA[i] = new SelectItem(id, fmt.format(new Object[]{e.getDetail(),e.getAmount()}));
        }
//        logger.log(Level.FINE, "41001: {0,number,#.##}", aggregates.get(41001));
    }

    /**
     * @return the startdate
     */
    public Date getStartdate() {
        return startdate;
    }

    /**
     * @return the cutoffdate
     */
    public Date getCutoffdate() {
        return cutoffdate;
    }

    /**
     * @param cutoffdate the cutoffdate to set
     */
    public void setCutoffdate(Date cutoffdate) {
        this.cutoffdate = cutoffdate;
    }

    public static final Logger logger = Logger.getLogger(PayableBean.class.getName());
    
    /**
     * @return the aggregates
     */
    public Map<Integer,BigDecimal> getAggregates() {
        return aggregates; //new ArrayList<>(aggregates.entrySet());
    }
    
    public BigDecimal getDue1() {
        return getIncome1().multiply(new BigDecimal(0.04));
    }
    
    public BigDecimal getIncome1() {
        if (aggregates.containsKey(GENINC) && aggregates.containsKey(THANKINC))
            return aggregates.get(GENINC).add(aggregates.get(THANKINC));
        return BigDecimal.ZERO;
    }

    public BigDecimal getExpense1() {
        if (aggregates.containsKey(CMAEXP))
            return aggregates.get(PayableBean.CMAEXP);
        return BigDecimal.ZERO;
    }

    public BigDecimal getDebt1() {
        if (aggregates.containsKey(CMADEBT))
            return aggregates.get(PayableBean.CMADEBT);
        return BigDecimal.ZERO;
    }
    /**
     * @return the entriesCMA
     */
    public List<java.util.Map.Entry<Integer,Entry[]>> getEntriesCMA() {
        return new ArrayList<>(entriesCMA.entrySet());
    }
    
    public SelectItem[] getChoicesCMA() {
        return choicesCMA;
    }

    /**
     * @return the entriesCMAM
     */
    public Map<Integer, Entry[]> getEntriesCMAM() {
        return entriesCMAM;
    }

    /**
     * @return the transref
     */
    public Integer getTransref() {
        return transref;
    }

    /**
     * @param transref the transref to set
     */
    public void setTransref(Integer transref) {
        this.transref = transref;
    }
    
}
