/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.nfvo.vim_interfaces.image_management;

import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.exceptions.VimException;

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
     * @param imageFile
     */
	NFVImage add(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException;
	
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
     * @param imageFile
	 */
    void copy(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException;
	
	
    
}
