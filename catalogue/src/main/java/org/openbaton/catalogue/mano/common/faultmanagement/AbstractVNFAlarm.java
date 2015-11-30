package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 27.10.15.
 */
public abstract class AbstractVNFAlarm {
    private String resourceId, fmPolicyId;

    public AbstractVNFAlarm(String vnfrId, String faultManagementPolicyId) {
        this.resourceId =vnfrId;
        this.fmPolicyId =faultManagementPolicyId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getFmPolicyId() {
        return fmPolicyId;
    }

    public void setFmPolicyId(String fmPolicyId) {
        this.fmPolicyId = fmPolicyId;
    }
}
