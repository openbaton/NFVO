package org.openbaton.catalogue.mano.common.faultmanagement;

import javax.persistence.Entity;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class VNFFaultManagementPolicy extends FaultManagementPolicy {
    private FaultManagementVNFCAction faultManagementVNFCAction;

    public VNFFaultManagementPolicy(){

    }

    public FaultManagementVNFCAction getFaultManagementVNFCAction() {
        return faultManagementVNFCAction;
    }

    public void setFaultManagementVNFCAction(FaultManagementVNFCAction faultManagementVNFCAction) {
        this.faultManagementVNFCAction = faultManagementVNFCAction;
    }
}
