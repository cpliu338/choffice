package org.therismos.web;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.therismos.ejb.EntryEjb;
import org.therismos.entity.Entry;

/**
 *
 * @author cp_liu
 */
@javax.inject.Named
@javax.faces.view.ViewScoped
public class PayableBean implements java.io.Serializable {
    
    //public static final long serialVersionUID = 59400432L;
    private static final int CMADEBT = 21002;
    private static final int CMAMDEBT = 22002;
    private static final int GENINC = 41001;
    private static final int THANKINC = 41002;
    private static final int MISSIONINC = 49001;
    private static final int CMAEXP = 59001;
    private static final int CMAMEXP = 59101;
    private final List<Integer> localmission;

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
    private BigDecimal localMission;
    private Map<Integer, BigDecimal> aggregates;
    private Map<Integer, Entry[]> entriesCMA, entriesCMAM;
    private SelectItem[] choicesCMA, choicesCMAM;
    private Entry entry, entry2;
    private final ResourceBundle bundle; 

    public Entry getEntry2() {
        return entry2;
    }
    private Integer transref, transref2;

    public Entry getEntry() {
        return entry;
    }

    @Inject
    EntryEjb entryEjb;
    
    private static final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
    
    static Date findYearStart(Date end) {
        String s2 = formatter.format(end);
        Date d1 = new Date();
        try {
            d1=formatter.parse(s2.substring(0, 4).concat("-01-01"));
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return d1;
    }

    public PayableBean() {
        bundle = ResourceBundle.getBundle("messages", Locale.TRADITIONAL_CHINESE);
        cutoffdate = new Date();
        startdate = findYearStart(cutoffdate);
        localMission = BigDecimal.ZERO;
        aggregates = Collections.EMPTY_MAP;
        entriesCMA= new HashMap<>();
        entriesCMAM=new HashMap<>();
        choicesCMA = new SelectItem[0];
        choicesCMAM = new SelectItem[0];
        entry = new Entry();
        entry.setId(0);
        transref = 0;
        entry2 = new Entry();
        entry2.setId(0);
        transref2 = 0;
        entry.setTransref(transref);
        entry.setDate1(new Date());
        entry.setAmount(BigDecimal.ZERO);
        entry2.setTransref(transref2);
        entry2.setDate1(new Date());
        entry2.setAmount(BigDecimal.ZERO);
        localMission = BigDecimal.ZERO;
// 59203(code 5X30)->53113(5X31) - 2017-08-20
        localmission = java.util.Arrays.asList(59201,59202,53113,59204);
    }
    
    public boolean isGoodToCommit() {
        boolean r = (entry!=null && entry.getAmount()!=null && entry.getAmount().abs().doubleValue()>1.0);
        if (!r) {
            if (entry==null) LOG.info("Entry is NULL");
            else LOG.log(Level.INFO, "Entry amount is {0,number}", entry.getAmount());
        }
        return r;
    }
    
    public boolean isGoodToCommit2() {
        boolean r= (entry2!=null && entry2.getAmount()!=null && entry2.getAmount().abs().doubleValue()>1.0);
        if (!r) {
            if (entry2==null) LOG.info("Entry2 is NULL");
            else LOG.log(Level.INFO, "Entry2 amount is {0,number}", entry2.getAmount());
        }
        return r;
    }
    
    public void updateCMAMEntry() {
        if (transref2 == 0) {
            entry2 = new Entry();
            entry2.setId(0);
            entry2.setTransref(transref2);
            entry2.setDate1(cutoffdate);
            entry2.setDetail(MessageFormat.format(bundle.getString("fmt.cmamdebt"), cutoffdate));
            entry2.setAmount(getDue2().add(this.getExpense2()));
        }
        else {
            List<Entry> entries = entryEjb.findByTransref(transref2);
            if (entries.isEmpty()) {
                LOG.log(Level.INFO, "Transref2 found none");
                entry2.setDetail("ERROR!");
                entry2.setDate1(new Date());
            }
            else {
                LOG.log(Level.INFO, "Cutoff date is {0,date}", cutoffdate);
                LOG.log(Level.INFO, "Transref2 is {0}", transref2);
                entry2 = entryEjb.findByTransref(transref2).get(0);
                entry2.setDate1(cutoffdate);
                entry2.setDetail(MessageFormat.format(bundle.getString("fmt.cmamdebt"), cutoffdate));
                entry2.setAmount(this.getDue2().add(this.getExpense2()));
            }
        }
        LOG.log(Level.INFO, "Entry2 amount is {0,number}", entry2.getAmount());
    }
    
    public void updateCMAEntry() {
        if (transref == 0) {
            entry = new Entry();
            entry.setId(0);
            entry.setTransref(transref);
            entry.setDate1(cutoffdate);
            entry.setDetail(MessageFormat.format(bundle.getString("fmt.cmadebt"), cutoffdate));
            entry.setAmount(this.getDue1().add(this.getExpense1()));
        }
        else {
            List<Entry> entries = entryEjb.findByTransref(transref);
            if (entries.isEmpty()) {
                entry.setDetail("ERROR!");
                entry.setDate1(new Date());
            }
            else {
                entry = entryEjb.findByTransref(transref).get(0);
                entry.setDate1(cutoffdate);
                entry.setDetail(MessageFormat.format(bundle.getString("fmt.cmadebt"), cutoffdate));
                entry.setAmount(this.getDue1().add(this.getExpense1()));
            }
        }
            LOG.log(Level.INFO, "Entry amount is {0,number}", entry.getAmount());
    }

    public void commitCMAEntry() {
        FacesMessage msg = new FacesMessage("Commit CMA");
        try {
            LOG.log(Level.INFO, "Entry amount is {0,number}", entry.getAmount());
            LOG.log(Level.INFO, "Detail is {0}", entry.getDetail());
            entryEjb.chargePayableShortfall(entry, CMAEXP);
            msg.setSummary("Success");
            if (transref == 0)
                msg.setDetail("Created new entries");
            else
                msg.setDetail("Modified new entries");
            execute(); // update other portions
        }
        catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, null, ex);
            msg.setSummary(ex.getClass().getName());
            msg.setDetail(ex.getMessage());
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        }
        FacesContext.getCurrentInstance().addMessage("entriesCMA", msg);
    }

    public void commitCMAMEntry() {
        FacesMessage msg = new FacesMessage("Commit CMAM");
        try {
            LOG.log(Level.FINE, "Entry2 amount is {0,number}", entry2.getAmount());
            entryEjb.chargePayableShortfall(entry2, CMAMEXP);
            msg.setSummary("Success");
            if (transref2 == 0)
                msg.setDetail("Created new entries");
            else
                msg.setDetail("Modified new entries");
            execute(); // update other portions
        }
        catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, null, ex);
            msg.setSummary(ex.getClass().getName());
            msg.setDetail(ex.getMessage());
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        }
        FacesContext.getCurrentInstance().addMessage("entriesCMAM", msg);
    }
    
    public void execute() {
        startdate = findYearStart(cutoffdate);
        entriesCMA = this.gleanDebtPairs(CMADEBT, CMAEXP);
        entriesCMAM = gleanDebtPairs(CMAMDEBT, CMAMEXP);
        List<Integer> accs = new ArrayList<>();
        accs.add(CMADEBT);
        accs.add(CMAMDEBT);
        accs.add(GENINC);
        accs.add(THANKINC);
        accs.add(MISSIONINC);
        accs.add(CMAEXP);
        accs.add(CMAMEXP);
        aggregates = entryEjb.aggregateEach(accs, startdate, cutoffdate);
        choicesCMA = buildChoices(entriesCMA);
        choicesCMAM = buildChoices(entriesCMAM);
        transref = 0;
//        LOG.info(MessageFormat.format(bundle.getString("fmt.cmadebt"), cutoffdate));
        localMission = BigDecimal.ZERO.subtract(entryEjb.aggregate(localmission, startdate, cutoffdate));
        entry.setDetail(MessageFormat.format(bundle.getString("fmt.cmadebt"), cutoffdate));
        entry.setDate1(cutoffdate);
        this.updateCMAEntry();
        transref2 = 0;
        entry2.setDetail(MessageFormat.format(bundle.getString("fmt.cmamdebt"), cutoffdate));
        entry2.setDate1(cutoffdate);
        this.updateCMAMEntry();
    }

    private SelectItem[] buildChoices(Map<Integer,Entry[]> entriesPair) {
        LOG.log(Level.FINE, "EntriesPair has {0,number} items", entriesPair.size());
        SelectItem[] results = new SelectItem[entriesPair.size()];
        Iterator<Integer> it = entriesPair.keySet().iterator();
        MessageFormat fmt = new MessageFormat("{0}(${1,number,#.##})");
        for (int i=0; i<entriesPair.size() && it.hasNext(); i++) {
            int id=it.next();
            Entry e = entriesPair.get(id)[0];
            results[i] = new SelectItem(id, fmt.format(new Object[]{e.getDetail(),e.getAmount()}));
        }
        return results;
    }
    
    private Map<Integer,Entry[]> gleanDebtPairs(int debtacc, int expacc) {
        Map<Integer,Entry[]> entriesPair = new HashMap<>();
        List<Integer> accs = new ArrayList<>();
        accs.add(expacc);
        accs.add(debtacc);
        List<Integer> blacklist = new ArrayList<>();
        for (Entry e : entryEjb.findByEachAccount(accs, startdate, cutoffdate)) {
            if (blacklist.contains(e.getTransref()))
                continue;
            Entry[] ar = entriesPair.get(e.getTransref());
            if (ar != null) {
                if (debtacc==e.getAccount().getId()) {
                        if (ar[0] == null)
                            ar[0] = e;
                        else
                            // blacklist when same account occurs twice
                            blacklist.add(e.getTransref());
                }
                else if (expacc==e.getAccount().getId()) {
                        if (ar[1] == null)
                            ar[1] = e;
                        else
                            // blacklist when same account occurs twice
                            blacklist.add(e.getTransref());
                }
                else {
                        // blacklist when account is neither DEBT nor EXP
                        blacklist.add(e.getTransref());
                }
            }
            else {
                ar = new Entry[2];
                ar[0] = null;
                ar[1] = null;
                if (debtacc==e.getAccount().getId()) {
                        ar[0] = e;
                }
                else if (expacc==e.getAccount().getId()) {
                        ar[1] = e; 
                }
                else {
                    blacklist.add(e.getTransref());
                }
                try {
                    entriesPair.put(e.getTransref(), ar);
                }
                catch (RuntimeException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
        Iterator<Integer> it = entriesPair.keySet().iterator();
        while (it.hasNext()) {
            int id=it.next();
            Entry[] ar = entriesPair.get(id);
            // blacklist when either DEBT or EXP is absent
            if (ar[0]==null || ar[1] == null)
                blacklist.add(id);
        }
        for (int blacksheep : blacklist) {
            entriesPair.remove(blacksheep);
        }
        LOG.log(Level.FINER, "EntriesPair has {0,number} items", entriesPair.size());
        return entriesPair;
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

    private static final Logger LOG = Logger.getLogger(PayableBean.class.getName());
    
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
        return (aggregates.containsKey(GENINC) && aggregates.containsKey(THANKINC)) ?
            aggregates.get(GENINC).add(aggregates.get(THANKINC)):
            BigDecimal.ZERO;
    }

    public BigDecimal getExpense1() {
        return (aggregates.containsKey(CMAEXP)) ?
            aggregates.get(PayableBean.CMAEXP):
           BigDecimal.ZERO;
    }

    public BigDecimal getDebt1() {
        return (aggregates.containsKey(CMADEBT)) ?
            aggregates.get(PayableBean.CMADEBT):
        BigDecimal.ZERO;
    }

    public BigDecimal getIncome2() {
        return (aggregates.containsKey(MISSIONINC)) ?
            aggregates.get(MISSIONINC) :
            BigDecimal.ZERO;
    }
    
    public BigDecimal getDue2() {
        BigDecimal t = getIncome2().multiply(new BigDecimal(0.7));
        BigDecimal l = localMission.multiply(new BigDecimal(7)).divide(new BigDecimal(3),BigDecimal.ROUND_HALF_UP);
        return l.compareTo(t)>=0 ? l : t;
    }
    
    public BigDecimal getLocalMission() {
        return localMission;
    }

    public BigDecimal getExpense2() {
        return (aggregates.containsKey(CMAMEXP)) ?
            aggregates.get(PayableBean.CMAMEXP) :
            BigDecimal.ZERO;
    }

    public BigDecimal getDebt2() {
        return (aggregates.containsKey(CMAMDEBT)) ?
            aggregates.get(PayableBean.CMAMDEBT):
            BigDecimal.ZERO;
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

    /**
     * @return the transref2
     */
    public Integer getTransref2() {
        return transref2;
    }

    /**
     * @param transref2 the transref2 to set
     */
    public void setTransref2(Integer transref2) {
        this.transref2 = transref2;
    }

    /**
     * @return the choicesCMAM
     */
    public SelectItem[] getChoicesCMAM() {
        return choicesCMAM;
    }
    
}
