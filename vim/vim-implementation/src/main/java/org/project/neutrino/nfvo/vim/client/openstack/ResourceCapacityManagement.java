package org.project.neutrino.nfvo.vim.client.openstack;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by mpa on 06.05.15.
 */
@Service
@Scope
public class ResourceCapacityManagement implements org.project.neutrino.nfvo.vim_interfaces.ResourceCapacityManagement {
    @Override
    public String query(String pop) {
        return null;
    }

    @Override
    public void notifyChanges(String notification) {

    }
}
