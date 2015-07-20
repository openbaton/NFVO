/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.common;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VNFDeploymentFlavour extends DeploymentFlavour {

    /**
     * Constraint that this deployment flavour can only meet the requirements on certain hardware.
     * */
	@ElementCollection(fetch=FetchType.EAGER)
    private Set<String> df_constraint;

    /**
     * Examples include Control-plane VDU & Data-plane VDU & Load Balancer VDU Each needs a VDU element to support the
     * deployment flavour of 10k calls-per-sec of vPGW, Control-plane VDU may specify 3 VMs each with 4 GB vRAM, 2 vCPU, 32 GB
     * virtual storage, etc. Data-plane VDU may specify 2 VMs each with 8 GB vRAM, 4 vCPU, 64 GB virtual storage, etc.
     * */
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    private Set<CostituentVDU> costituent_vdu;

    public Set<String> getDf_constraint() {
        return df_constraint;
    }

    public void setDf_constraint(Set<String> df_constraint) {
        this.df_constraint = df_constraint;
    }

    public Set<CostituentVDU> getCostituent_vdu() {
        return costituent_vdu;
    }

    public void setCostituent_vdu(Set<CostituentVDU> costituent_vdu) {
        this.costituent_vdu = costituent_vdu;
    }

    public VNFDeploymentFlavour() {

    }
}
