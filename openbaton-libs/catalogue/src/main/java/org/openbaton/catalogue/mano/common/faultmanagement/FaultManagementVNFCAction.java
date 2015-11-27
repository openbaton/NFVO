package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 29.10.15.
 */
public enum FaultManagementVNFCAction {
    RESTART,
    REINSTANTIATE_SERVICE,

    REINSTANTIATE,
    SWITCH_TO_STANDBY,
    SWITCH_TO_ACTIVE
}
