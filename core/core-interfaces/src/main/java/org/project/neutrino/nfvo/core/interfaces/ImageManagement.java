package org.project.neutrino.nfvo.core.interfaces;

import java.util.List;

import org.project.neutrino.nfvo.catalogue.nfvo.Image;

/**
 * Created by mpa on 30/04/15.
 */

public interface ImageManagement {
    
	/**
     * This operation allows adding new VNF software 
     * images to the image repository.
     */
    Image add(Image image);

    /**
	 * This operation allows deleting in the VNF software 
	 * images from the image repository.
     * @param id
     */
    void delete(String id);
    
    /**
	 * This operation allows updating the VNF software 
	 * images in the image repository.
     * @param new_image
     * @param id
     */
    Image update(Image new_image, String id);
    
    /**
	 * This operation allows querying the information of 
	 * the VNF software images in the image repository.
	 */
    List<Image> query();
    
    /**
     * This operation allows querying the information of 
     * the VNF software image in the image repository.
     */
    Image query(String id);
    
    /**
	 * This operation allows copying images from 
	 * a VIM to another.
	 */
    void copy();
	
	
    
}
