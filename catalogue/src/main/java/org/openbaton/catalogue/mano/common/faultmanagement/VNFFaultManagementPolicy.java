package org.openbaton.catalogue.mano.common.faultmanagement;

import javax.persistence.Entity;
import java.util.Iterator;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class VNFFaultManagementPolicy extends FaultManagementPolicy {
    private FaultManagementVNFCAction action;

    public VNFFaultManagementPolicy(){

    }

    public FaultManagementVNFCAction getAction() {
        return action;
    }

    public void setAction(FaultManagementVNFCAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        String result= "VNFFaultManagementPolicy{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", cooldown=" + cooldown +
                ", period=" + period +
                ", severity=" + severity +
                ", criteria=";
        if(criteria !=null){
            Iterator<Criteria>criteriaIterator= criteria.iterator();
            while(criteriaIterator.hasNext())
                result+= criteriaIterator.next().toString();
        }
        else result+="null";
        result+=", action=" + action;
        return result;
    }
}
