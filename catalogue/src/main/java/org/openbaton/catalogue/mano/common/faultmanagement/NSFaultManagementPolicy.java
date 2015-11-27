package org.openbaton.catalogue.mano.common.faultmanagement;

import javax.persistence.Entity;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class NSFaultManagementPolicy extends FaultManagementPolicy {
    private String vnfSelector;
    //private FaultManagementVNFAction;

    public NSFaultManagementPolicy(){}

    public String getVnfSelector() {
        return vnfSelector;
    }

    public void setVnfSelector(String vnfSelector) {
        this.vnfSelector = vnfSelector;
    }

}
