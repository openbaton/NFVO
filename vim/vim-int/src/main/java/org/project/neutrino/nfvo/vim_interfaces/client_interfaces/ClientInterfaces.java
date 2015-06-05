package org.project.neutrino.nfvo.vim_interfaces.client_interfaces;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;

import java.util.List;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces {

    public Server launchInstance(String name, String image, String flavor, String keypair, List<String> network, List<String> secGroup, String userData);
    public void init(VimInstance vimInstance);

    List<NFVImage> listImages();

    List<Server> listServer();
    List<Network> listNetworks();
    List<DeploymentFlavour> listFlavors();

    Server launchInstanceAndWait(String hostname, String image, String extId, String keyPair, List<String> networks, List<String> securityGroups, String s) throws VimException;

    void deleteServerByIdAndWait(String id);
}
