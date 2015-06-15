package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.vim.VimBroker;
import org.project.neutrino.nfvo.common.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope
public class VimManagement implements org.project.neutrino.nfvo.core.interfaces.VimManagement {
    @Autowired
    @Qualifier("vimRepository")
    private GenericRepository<VimInstance> vimInstanceGenericRepository;

    @Autowired
    private VimBroker vimBroker;

    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public VimInstance add(VimInstance vimInstance) throws VimException {
        this.refresh(vimInstance);
        log.trace("Persisting VimInstance: " + vimInstance);
        return vimInstanceGenericRepository.create(vimInstance);
    }

    @Override
    public void delete(String id) {
        vimInstanceGenericRepository.remove(vimInstanceGenericRepository.find(id));
    }

    @Override
    public VimInstance update(VimInstance new_vimInstance, String id) throws VimException {
        VimInstance old = vimInstanceGenericRepository.find(id);
        old.setName(new_vimInstance.getName());
        old.setType(new_vimInstance.getType());
        old.setAuthUrl(new_vimInstance.getAuthUrl());
        old.setKeyPair(new_vimInstance.getKeyPair());
        old.setLocation(new_vimInstance.getLocation());
        old.setUsername(new_vimInstance.getUsername());
        old.setPassword(new_vimInstance.getPassword());
        old.setTenant(new_vimInstance.getTenant());
        refresh(old);
        return old;
    }

    @Override
    public List<VimInstance> query() {
        return vimInstanceGenericRepository.findAll();
    }

    @Override
    public VimInstance query(String id) {
        return vimInstanceGenericRepository.find(id);
    }

    @Override
    public void refresh(VimInstance vimInstance) throws VimException {
        vimInstance.setImages(vimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));
        vimInstance.setNetworks(vimBroker.getVim(vimInstance.getType()).queryNetwork(vimInstance));
        vimInstance.setFlavours(vimBroker.getVim(vimInstance.getType()).queryDeploymentFlavors(vimInstance));
    }
}
