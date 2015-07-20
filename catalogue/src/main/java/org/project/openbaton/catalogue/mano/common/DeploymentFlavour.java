/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.common;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class DeploymentFlavour implements Serializable{
    /*ID of the deployment flavour.*/
	@Id
    protected String id = IdGenerator.createUUID();
	@Version
	protected int version = 0;
    /*
        * Assurance parameter against which this flavour is being described. The key could be a combination of multiple assurance
        * parameters with a logical relationship between them. The parameters should be present as a monitoring_parameter supported in clause 6.2.1.1.
        * For example, a flavour of a virtual EPC could be described in terms of the assurance parameter "calls per second" (cps).
        * */
    protected String flavour_key;

    protected String extId;

    private int ram;

    private int disk;

    private int vcpus;

    public String getFlavour_key() {
        return flavour_key;
    }

    public void setFlavour_key(String flavour_key) {
        this.flavour_key = flavour_key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExtId() { return extId; }

    public void setExtId(String extId) { this.extId = extId; }

    public int getRam() { return ram; }

    public void setRam(int ram) { this.ram = ram; }

    public int getDisk() { return disk; }

    public void setDisk(int disk) { this.disk = disk; }

    public int getVcpus() { return vcpus; }

    public void setVcpus(int vcpus) { this.vcpus = vcpus; }

    @Override
    public String toString() {
        return "DeploymentFlavour{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", flavour_key='" + flavour_key + '\'' +
                ", extId='" + extId + '\'' +
                '}';
    }
}
