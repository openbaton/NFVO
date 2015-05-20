package org.project.neutrino.nfvo.client_interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces {

    public String launch_instance(String name, String image, String flavor, String keypair, String network, Iterable<String> secGroup, String userData);
    public void init(VimInstance vimInstance);

}
