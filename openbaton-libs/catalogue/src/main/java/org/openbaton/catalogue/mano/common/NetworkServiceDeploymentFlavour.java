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

package org.openbaton.catalogue.mano.common;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 * <p/>
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkServiceDeploymentFlavour extends DeploymentFlavour {

    /*Represents the characteristics of a constituent flavour element.*/
    @OneToMany(cascade = CascadeType.ALL)
    private Set<CostituentVNF> constituent_vnf;

    public NetworkServiceDeploymentFlavour() {
    }

    public Set<CostituentVNF> getConstituent_vnf() {
        return constituent_vnf;
    }

    public void setConstituent_vnf(Set<CostituentVNF> constituent_vnf) {
        this.constituent_vnf = constituent_vnf;
    }

}
