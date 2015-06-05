package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.DeploymentFlavorManagement;
import org.project.neutrino.nfvo.vim_interfaces.ImageManagement;
import org.project.neutrino.nfvo.vim_interfaces.NetworkManagement;
import org.project.neutrino.nfvo.vim_interfaces.VimBroker;
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
    private VimBroker<ImageManagement> imageManagementVimBroker;
    @Autowired
    private VimBroker<DeploymentFlavorManagement> flavorManagementVimBroker;
    @Autowired
    private VimBroker<NetworkManagement> networkManagementVimBroker;


    @Override
    public VimInstance add(VimInstance vimInstance) {
        this.refresh(vimInstance);
        return vimInstanceGenericRepository.create(vimInstance);
    }

    @Override
    public void delete(String id) {
        vimInstanceGenericRepository.remove(vimInstanceGenericRepository.find(id));
    }

    @Override
    public VimInstance update(VimInstance new_vimInstance, String id) {
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
    public void refresh(VimInstance vimInstance) {
        vimInstance.setImages(imageManagementVimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));
        vimInstance.setNetworks(networkManagementVimBroker.getVim(vimInstance.getType()).queryNetwork(vimInstance));
        vimInstance.setFlavours(flavorManagementVimBroker.getVim(vimInstance.getType()).queryDeploymentFlavors(vimInstance));
    }
}
