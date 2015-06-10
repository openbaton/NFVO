package org.project.neutrino.nfvo.vim_interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;

import java.io.InputStream;
import java.util.List;

/**
 * Created by mpa on 30/04/15.
 */

public interface ImageManagement {
    
	/**
     * This operation allows adding new VNF software 
     * images to the image repository.
     * @param vimInstance
     * @param image
     */
	NFVImage add(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException;
	
	/**
	 * This operation allows deleting in the VNF software 
	 * images from the image repository.
     * @param vimInstance
     * @param image
     */
    void delete(VimInstance vimInstance, NFVImage image) throws VimException;
    
    /**
	 * This operation allows updating the VNF software 
	 * images in the image repository.
     * @param vimInstance
     * @param image
	 */
    NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException;
    
    /**
	 * This operation allows querying the information of 
	 * the VNF software images in the image repository.
     * @param vimInstance
	 */
    List<NFVImage> queryImages(VimInstance vimInstance) throws VimException;
    
    /**
	 * This operation allows copying images from 
	 * a VIM to another.
     * @param vimInstance
     * @param image
     * @param inputStream
	 */
    void copy(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException;
	
	
    
}
