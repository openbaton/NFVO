package org.project.neutrino.nfvo.vim.client.amazon;

import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.client_interfaces.ClientInterfaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope
public class AmazonClient implements ClientInterfaces{
    @Override
    public String launch_instance(String name, String image, String flavor, String keypair, String network, Iterable<String> secGroup, String userData) {
        return null;
    }

    @Override
    public void init(VimInstance vimInstance) {

    }
}
