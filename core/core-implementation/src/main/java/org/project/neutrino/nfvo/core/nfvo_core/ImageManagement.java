package org.project.neutrino.nfvo.core.nfvo_core;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.nfvo.Image;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * Created by lto on 11/05/15.
 */
public class ImageManagement implements org.project.neutrino.nfvo.core.interfaces.ImageManagement{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("imageRepository")
    private GenericRepository<Image> imageRepository;

    @Override
    public Image add(Image image) {
        log.trace("Adding image " + image);
        log.debug("Adding image with name " + image.getName());
        return imageRepository.create(image);
    }

    @Override
    public void delete(String id) {
        log.debug("Removing image with id " + id);
        imageRepository.remove(imageRepository.find(id));
    }

    @Override
    public Image update(Image new_image, String id) {
        throw new NotImplementedException();
    }

    @Override
    public List<Image> query() {
        return imageRepository.findAll();
    }
    
    @Override
    public Image query(String id){
        return imageRepository.find(id);
    }
    
    @Override
    public void copy() {
        throw new NotImplementedException();
    }
}
