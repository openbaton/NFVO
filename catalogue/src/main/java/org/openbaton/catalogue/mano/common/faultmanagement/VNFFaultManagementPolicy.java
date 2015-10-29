package org.openbaton.catalogue.mano.common.faultmanagement;

import javax.persistence.Entity;

/**
 * Created by mob on 29.10.15.
 */
@Entity

public class VNFFaultManagementPolicy extends FaultManagementPolicy {
    private String vduSelector;
    private FaultManagementVNFCAction faultManagementVNFCAction;

    public VNFFaultManagementPolicy(){

    }

    public String getVduSelector() {
        return vduSelector;
    }

    public void setVduSelector(String vduSelector) {
        this.vduSelector = vduSelector;
    }

    public FaultManagementVNFCAction getFaultManagementVNFCAction() {
        return faultManagementVNFCAction;
    }

    public void setFaultManagementVNFCAction(FaultManagementVNFCAction faultManagementVNFCAction) {
        this.faultManagementVNFCAction = faultManagementVNFCAction;
    }
}
