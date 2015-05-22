package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;

import java.util.List;


/**
 * Created by mpa on 30/04/15.
 */

public interface NFVImageManagement {
    
	/**
     * This operation allows adding new VNF software 
     * images to the image repository.
     */
    NFVImage add(NFVImage NFVImage);

    /**
	 * This operation allows deleting in the VNF software 
	 * images from the image repository.
     * @param id
     */
    void delete(String id);
    
    /**
	 * This operation allows updating the VNF software 
	 * images in the image repository.
     * @param new_NFV_image
     * @param id
     */
    NFVImage update(NFVImage new_NFV_image, String id);
    
    /**
	 * This operation allows querying the information of 
	 * the VNF software images in the image repository.
	 */
    List<NFVImage> query();
    
    /**
     * This operation allows querying the information of 
     * the VNF software image in the image repository.
     */
    NFVImage query(String id);
    
    /**
	 * This operation allows copying images from 
	 * a VIM to another.
	 */
    void copy();
	
	
    
}
