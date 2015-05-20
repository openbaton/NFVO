package org.project.neutrino.nfvo.vim.client.openstack;


import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by mpa on 05.05.15.
 */
@Service
@Scope
public class ImageManagement implements org.project.neutrino.nfvo.vim_interfaces.ImageManagement {
    @Override
    public NFVImage add(NFVImage image) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public NFVImage update() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NFVImage> queryImages(VimInstance vimInstance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy() {

    }
}
