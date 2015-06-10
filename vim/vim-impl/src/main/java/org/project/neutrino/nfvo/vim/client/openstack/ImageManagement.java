package org.project.neutrino.nfvo.vim.client.openstack;


import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * Created by mpa on 05.05.15.
 */
@Service
@Scope
public class ImageManagement implements org.project.neutrino.nfvo.vim_interfaces.ImageManagement {

    @Override
    public NFVImage add(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {
        return null;
    }

    @Override
    public void delete(VimInstance vimInstance, NFVImage image) throws VimException {

    }

    @Override
    public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException {
        return null;
    }

    @Override
    public List<NFVImage> queryImages(VimInstance vimInstance) throws VimException {
        return null;
    }

    @Override
    public void copy(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {

    }
}
