package org.project.neutrino.nfvo.catalogue.mano.common;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkServiceDeploymentFlavour extends DeploymentFlavour {

    /*Represents the characteristics of a constituent flavour element.*/
	@OneToMany(cascade=CascadeType.ALL)
    private List<CostituentVNF> constituent_vnf;

    public NetworkServiceDeploymentFlavour() {
    }

    public List<CostituentVNF> getConstituent_vnf() {
        return constituent_vnf;
    }

    public void setConstituent_vnf(List<CostituentVNF> constituent_vnf) {
        this.constituent_vnf = constituent_vnf;
    }

}
