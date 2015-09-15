package org.project.openbaton.catalogue.mano.record;

import org.project.openbaton.catalogue.mano.descriptor.VNFComponent;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Created by lto on 08/09/15.
 */
@Entity
public class VNFCInstance extends VNFComponent implements Serializable {

    private String vim_id;
    private String vc_id;
    private String hostname;

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

    @Override
    public String toString() {
        return "VNFCInstance{" +
                "vc_id='" + vc_id + '\'' +
                ", vim_id='" + vim_id + '\'' +
                '}';
    }
}
