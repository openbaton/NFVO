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

package org.project.openbaton.nfvo.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.project.openbaton.nfvo.api.RestVirtualLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * OpenBaton VirtualLinkDescriptor-related commands implementation using the spring-shell library.
 */
@Component
public class VirtualLink implements CommandMarker {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Gson mapper = new Gson();

	@Autowired
	private RestVirtualLink virtualLinkRequest;

	/**
	 * Adds a new VirtualLinkDescriptor to the repository
	 *
	 * @param virtualLinkDescriptor
	 *            : VirtualLinkDescriptor to add
	 * @return VirtualLinkDescriptor: The VirtualLinkDescriptor filled with values from the core
	 */
	@CliCommand(value = "virtualLinkDescriptor create", help = "Adds a new virtualLinkDescriptor to the virtualLinkDescriptor repository")
	public String create(@CliOption(key = { "virtualLinkDescriptorFile" }, mandatory = true, help = "The virtualLinkDescriptor json file") final File virtualLinkDescriptor) {
		try{
			return "VLD CREATED: " + virtualLinkRequest.create(mapper.<VirtualLinkDescriptor>fromJson(new InputStreamReader(new FileInputStream(virtualLinkDescriptor)), VirtualLinkDescriptor.class));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		}
	}

	/**
	 * Removes the VirtualLinkDescriptor from the repository
	 *
	 * @param id: The VirtualLinkDescriptor's id to be deleted
	 */
	@CliCommand(value = "virtualLinkDescriptor delete", help = "Removes the virtualLinkDescriptor from the virtualLinkDescriptor repository")
	public String delete(
			@CliOption(key = { "id" }, mandatory = true, help = "The virtualLinkDescriptor id") final String id) {
		virtualLinkRequest.delete(id);
		return  "VLD DELETED";
	}

	/**
	 * Returns the VirtualLinkDescriptor selected by id
	 * @param id: The VirtualLinkDescriptor's id selected
	 * @return Datacenter: The VirtualLinkDescriptor selected
	 */
	@CliCommand(value = "virtualLinkDescriptor find", help = "Returns the virtualLinkDescriptor selected by id, or all if no id is given")
	public String findById(
			@CliOption(key = { "id" }, mandatory = true, help = "The virtualLinkDescriptor id") final String id) {
		if (id != null) {
			return "FOUND VLD: " + virtualLinkRequest.findDescriptorById(id);
		} else {
			return "FOUND VLDs: " + virtualLinkRequest.findAllDescriptors();
		}
	}

	/**
	 * This operation updates the VirtualLinkDescriptor
	 *
	 * @param virtualLinkDescriptor
	 *            : the new VirtualLinkDescriptor to be updated to
	 * @param id
	 *            : the id of the old VirtualLinkDescriptor
	 * @return VirtualLinkDescriptor: the VirtualLinkDescriptor updated
	 */
	@CliCommand(value = "virtualLinkDescriptor update", help = "Updates the virtualLinkDescriptor")
	public String update(
			@CliOption(key = { "virtualLinkDescriptorFile" }, mandatory = true, help = "The virtualLinkDescriptor json file") final File virtualLinkDescriptor,
			@CliOption(key = { "id" }, mandatory = true, help = "The virtualLinkDescriptor id") final String id) {
		try {
			return "VLD UPDATED: " + virtualLinkRequest.update(mapper.<VirtualLinkDescriptor>fromJson(new InputStreamReader(new FileInputStream(virtualLinkDescriptor)), VirtualLinkDescriptor.class), id);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		}
	}

}
