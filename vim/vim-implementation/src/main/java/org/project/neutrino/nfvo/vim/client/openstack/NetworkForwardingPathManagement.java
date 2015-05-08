package org.project.neutrino.nfvo.vim.client.openstack;


import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkForwardingPath;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by mpa on 05.05.15.
 */
@Service
@Scope
public class NetworkForwardingPathManagement implements org.project.neutrino.nfvo.vim_interfaces.NetworkForwardingPathManagement{
    @Override
    public NetworkForwardingPath create() {
        return null;
    }

    @Override
    public NetworkForwardingPath update() {
        return null;
    }

    @Override
    public void delete() {

    }

    @Override
    public NetworkForwardingPath query() {
        return null;
    }

    @Override
    public void notifyInformation() {

    }
}
