package org.project.neutrino.nfvo.core.interfaces;

/**
 * Created by mpa on 30/04/15.
 */

public interface ImageManagement {
    
	/**
     * This operation allows adding new VNF software 
     * images to the image repository.
     */
	void add();
	
	/**
	 * This operation allows deleting in the VNF software 
	 * images from the image repository.
	 */
    void delete();
    
    /**
	 * This operation allows updating the VNF software 
	 * images in the image repository.
	 */
    void update();
    
    /**
	 * This operation allows querying the information of 
	 * the VNF software images in the image repository.
	 */
    void query();
    
    /**
	 * This operation allows copying images from 
	 * a VIM to another.
	 */
    void copy();
	
	
    
}
