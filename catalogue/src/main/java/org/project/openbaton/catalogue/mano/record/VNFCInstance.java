package org.project.openbaton.catalogue.mano.record;

import org.project.openbaton.catalogue.mano.descriptor.VNFComponent;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Created by lto on 08/09/15.
 */
@Entity
public class VNFCInstance extends VNFComponent implements Serializable {

    protected String vim_id;
    protected String vc_id;
    protected String hostname;
    protected String vnc_reference;

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

    public String getVnc_reference() {
        return vnc_reference;
    }

    public void setVnc_reference(String vnc_reference) {
        this.vnc_reference = vnc_reference;
    }

    @Override
    public String toString() {
        return "VNFCInstance{" +
                "vim_id='" + vim_id + '\'' +
                ", vc_id='" + vc_id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", vnc_reference='" + vnc_reference + '\'' +
                '}';
    }
}
