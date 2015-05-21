package org.project.neutrino.nfvo.vim_interfaces.client_interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;

import java.util.List;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces {

    public String launch_instance(String name, String image, String flavor, String keypair, List<String> network, List<String> secGroup, String userData);
    public void init(VimInstance vimInstance);

    List<NFVImage> listImages();

    List<Server> listServer();
    List<Network> listNetworks();
}
