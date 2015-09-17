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

package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.common.Event;

import java.io.Serializable;

/**
 * Created by lto on 12/06/15.
 */
public class VDUMessage implements Serializable {

    private Event lifecycleEvent;
    private Serializable payload;

    public VDUMessage() {
    }

    public Event getLifecycleEvent() {
        return lifecycleEvent;
    }

    public void setLifecycleEvent(Event lifecycleEvent) {
        this.lifecycleEvent = lifecycleEvent;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "VDUMessage{" +
                "lifecycleEvent=" + lifecycleEvent +
                ", payload='" + payload + '\'' +
                '}';
    }
}
