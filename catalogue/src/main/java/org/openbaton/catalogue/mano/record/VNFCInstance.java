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

package org.openbaton.catalogue.mano.record;

import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 08/09/15.
 */
@Entity
public class VNFCInstance extends VNFComponent implements Serializable {

    protected String vim_id;
    protected String vc_id;
    protected String hostname;

    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    protected VNFComponent vnfComponent;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ip> floatingIps;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ip> ips;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVim_id() {
        return vim_id;
    }

    public void setVim_id(String vim_id) {
        this.vim_id = vim_id;
    }

    public String getVc_id() {
        return vc_id;
    }

    public void setVc_id(String vc_id) {
        this.vc_id = vc_id;
    }

    public VNFComponent getVnfComponent() {
        return vnfComponent;
    }

    public void setVnfComponent(VNFComponent vnfComponent) {
        this.vnfComponent = vnfComponent;
    }

    @Override
    public String toString() {
        return "VNFCInstance{" +
                "vim_id='" + vim_id + '\'' +
                ", vc_id='" + vc_id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", vnfComponent=" + vnfComponent +
                ", floatingIps='" + floatingIps + '\'' +
                ", ips=" + ips +
                "} " + super.toString();
    }

    public Set<Ip> getFloatingIps() {
        return floatingIps;
    }

    public void setFloatingIps(Set<Ip> floatingIps) {
        this.floatingIps = floatingIps;
    }

    public Set<Ip> getIps() {
        return ips;
    }

    public void setIps(Set<Ip> ips) {
        this.ips = ips;
    }
}
