package org.project.openbaton.nfvo.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.nfvo.api.RestVimInstances;
import org.project.openbaton.nfvo.exceptions.VimException;
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
 * OpenBaton viminstance(datacenter)-related commands implementation using the spring-shell library.
 */
@Component
public class VimInstance extends org.project.openbaton.catalogue.nfvo.VimInstance implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

    private Gson mapper = new Gson();

    @Autowired
    private RestVimInstances vimInstanceRequest;

    /**
	 * Adds a new datacenter to the datacenter repository
	 *
	 * @param datacenter
	 *            : Image to add
	 * @return datacenter: The datacenter filled with values from the core
	 */
	@CliCommand(value = "viminstance create", help = "Adds a new datacenter to the datacenter repository")
	public String create(
            @CliOption(key = { "datacenterFile" }, mandatory = true, help = "The viminstance json file") final File datacenter) {

        try {
			return "DATACENTER CREATED: " + vimInstanceRequest.create(mapper.<VimInstance>fromJson(new InputStreamReader(new FileInputStream(datacenter)), org.project.openbaton.catalogue.nfvo.VimInstance.class));
		} catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (VimException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

	/**
	 * Removes the Datacenter from the Datacenter repository
	 *
	 * @param id: The Datacenter's id to be deleted
	 */
	@CliCommand(value = "viminstance delete", help = "Removes the Datacenter from the Datacenter repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
            vimInstanceRequest.delete(id);
            return "DATACENTER DELETED";
	}

	/**
	 * Returns the Datacenter selected by id
	 * @param id: The Datacenter's id selected
	 * @return Datacenter: The Datacenter selected
	 */
	@CliCommand(value = "viminstance find", help = "Returns the Datacenter selected by id" )
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
            if (id != null) {
                return "FOUND DATACENTER: " + vimInstanceRequest.findById(id);
            } else {
                return "FOUND DATACENTERS: " + vimInstanceRequest.findAll();
            }
    }

	/**
	 * This operation updates the Datacenter
	 *
	 * @param datacenter
	 *            : the new datacenter to be updated to
	 * @param id
	 *            : the id of the old datacenter
	 * @return VimInstance: the VimInstance updated
	 */
	@CliCommand(value = "viminstance update", help = "Updates the Datacenter")
	public String update( @CliOption(key = { "datacenterFile" }, mandatory = true, help = "The viminstance json file") final File datacenter,     @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
        try {
            return "DATACENTER UPDATED: " + vimInstanceRequest.update(mapper.<VimInstance>fromJson(new InputStreamReader(new FileInputStream(datacenter)), VimInstance.class), id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (VimException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

}
