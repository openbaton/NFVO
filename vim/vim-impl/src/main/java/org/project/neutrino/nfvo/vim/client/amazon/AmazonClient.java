package org.project.neutrino.nfvo.vim.client.amazon;

import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope
public class AmazonClient implements ClientInterfaces{
    @Override
    public Server launchInstance(String name, String image, String flavor, String keypair, List<String> network, List<String> secGroup, String userData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init(VimInstance vimInstance) {

    }

    @Override
    public List<NFVImage> listImages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Server> listServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Network> listNetworks() {
        throw new UnsupportedOperationException();
    }
}
