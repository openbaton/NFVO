package org.project.neutrino.nfvo.vim_interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;

import java.util.List;

/**
 * Created by mpa on 30/04/15.
 */

public interface ImageManagement {
    
	/**
     * This operation allows adding new VNF software 
     * images to the image repository.
     * @param image
     */
	NFVImage add(NFVImage image);
	
	/**
	 * This operation allows deleting in the VNF software 
	 * images from the image repository.
     * @param id
     */
    void delete(String id);
    
    /**
	 * This operation allows updating the VNF software 
	 * images in the image repository.
	 */
    NFVImage update();
    
    /**
	 * This operation allows querying the information of 
	 * the VNF software images in the image repository.
	 */
    List<NFVImage> queryImages(VimInstance vimInstance);
    
    /**
	 * This operation allows copying images from 
	 * a VIM to another.
	 */
    void copy();
	
	
    
}
