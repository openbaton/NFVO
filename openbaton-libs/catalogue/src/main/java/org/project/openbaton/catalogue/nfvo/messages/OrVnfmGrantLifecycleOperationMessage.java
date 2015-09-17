/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by mob on 15.09.15.
 */
public class OrVnfmGrantLifecycleOperationMessage implements OrVnfmMessage{
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
