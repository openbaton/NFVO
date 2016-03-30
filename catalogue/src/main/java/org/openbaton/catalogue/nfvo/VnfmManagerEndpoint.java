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

package org.openbaton.catalogue.nfvo;

import javax.persistence.Entity;

/**
 * Created by lto on 27/05/15.
 */
@Entity
public class VnfmManagerEndpoint extends Endpoint {


    public VnfmManagerEndpoint() {
    }

    public VnfmManagerEndpoint(String type, String endpoint, EndpointType endpointType) {
        this.type = type;
        this.endpoint = endpoint;
        this.endpointType = endpointType;
    }

    @Override
    public String toString() {
        return "VnfmManagerEndpoint{} " + super.toString();
    }
}
