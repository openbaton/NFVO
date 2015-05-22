package org.project.neutrino.nfvo.vim.client.openstack;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by mpa on 05.05.15.
 */
@Service
@Scope
public class PolicyManagement implements org.project.neutrino.nfvo.vim_interfaces.PolicyManagement{
    @Override
    public Policy create() {
        return null;
    }

    @Override
    public Policy update() {
        return null;
    }

    @Override
    public void delete() {

    }

    @Override
    public List<Policy> query() {
        return null;
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }
}
