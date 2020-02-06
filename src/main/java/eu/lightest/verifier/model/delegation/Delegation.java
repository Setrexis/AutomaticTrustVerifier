package eu.lightest.verifier.model.delegation;

import eu.lightest.delegation.api.model.xsd.DelegationType;
import eu.lightest.delegation.api.model.xsd.ValidityType;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Delegation {
    
    private static final Logger mLog = Logger.getLogger(Delegation.class);
    private DelegationType mDelegationType;
    public Delegation(DelegationType dt) {
        this.mDelegationType = dt;
    }
    
    public boolean isDelegationSignatureValid() {
        throw new NotImplementedException();
    }
    
    public boolean isNotBeforeDateValid() {
        ValidityType vt = this.mDelegationType.getValidity();
        int cmp = compareCal(vt.getNotBefore(), Calendar.getInstance().getTime());
    
        Delegation.mLog.debug("Checking NotBeforeDate");
        
        if(cmp == -1) {
            Delegation.mLog.debug("Delegation is not valid yet!");
            return false;
        }
        
        return true;
    }
    
    public boolean isNotAfterDateValid() {
        ValidityType vt = this.mDelegationType.getValidity();
        int cmp = compareCal(vt.getNotAfter(), Calendar.getInstance().getTime());
        
        if(cmp == 1) {
            Delegation.mLog.debug("Delegation has expired!");
            return false;
        }
        
        return true;
    }
    
    public DelegationTypeE getDelegationType() {
        if(this.mDelegationType.isDelegationAllowed() == true && this.mDelegationType.isSubstitutionAllowed() == false) {
            return DelegationTypeE.Delegation;
        } else if(this.mDelegationType.isDelegationAllowed() == false && this.mDelegationType.isSubstitutionAllowed() == true) {
            return DelegationTypeE.Substitution;
        }
        return DelegationTypeE.Bilateral;
    }
    
    public boolean isDelegationRevoked() {
        throw new NotImplementedException();
    }
    
    private int compareCal(XMLGregorianCalendar gc, Date date) {
        GregorianCalendar cal_now = new GregorianCalendar();
        cal_now.setTime(date);
        
        return gc.toGregorianCalendar().compareTo(cal_now);
    }
    
    public enum DelegationTypeE {
        Bilateral,
        Substitution,
        Delegation
    }
}
