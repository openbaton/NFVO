package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by mob on 15.09.15.
 */
public class OrVnfmGrantLifecycleOperationMessage implements OrVnfmMessage {
    private String vimId;
    private boolean grantAllowed;

    public OrVnfmGrantLifecycleOperationMessage(String vimId, boolean grantAllowed) {

        this.vimId = vimId;
        this.grantAllowed = grantAllowed;
    }

    public String getVimId() {
        return vimId;
    }

    public void setVimId(String vimId) {
        this.vimId = vimId;
    }

    public boolean isGrantAllowed() {
        return grantAllowed;
    }

    public void setGrantAllowed(boolean grantAllowed) {
        this.grantAllowed = grantAllowed;
    }

    @Override
    public String toString() {
        return "OrVnfmGrantLifecycleOperationMessage{" +
                "vimId='" + vimId + '\'' +
                ", grantAllowed=" + grantAllowed +
                '}';
    }

    @Override
    public Action getAction() {
        return Action.GRANT_OPERATION;
    }
}
