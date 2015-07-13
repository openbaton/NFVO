package org.project.openbaton.nfvo.core.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.sdk.NFVORequestor;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.project.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

/**
 * OpenBaton viminstance(datacenter)-related commands implementation using the spring-shell library.
 */
@Component
public class VimInstance implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

    private NFVORequestor requestor = new NFVORequestor("1");
    private AbstractRestAgent<org.project.openbaton.common.catalogue.nfvo.VimInstance> vimInstanceRequest;
    private Gson mapper = new Gson();

    @PostConstruct
    private void init(){
        vimInstanceRequest = requestor.getVimInstanceAgent();
    }
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
			return "DATACENTER CREATED: " + vimInstanceRequest.create(mapper.<org.project.openbaton.common.catalogue.nfvo.VimInstance>fromJson(new InputStreamReader(new FileInputStream(datacenter)), org.project.openbaton.common.catalogue.nfvo.VimInstance.class));
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "DATACENTER NOT CREATED";
		} catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

	/**
	 * Removes the Datacenter from the Datacenter repository
	 *
	 * @param id: The Datacenter's id to be deleted
	 */
	@CliCommand(value = "viminstance delete", help = "Removes the Datacenter from the Datacenter repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
        try {
            vimInstanceRequest.delete(id);
            return "DATACENTER DELETED";
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VIMINSTANCE NOT DELETED";
        }
	}

	/**
	 * Returns the Datacenter selected by id
	 * @param id: The Datacenter's id selected
	 * @return Datacenter: The Datacenter selected
	 */
	@CliCommand(value = "viminstance find", help = "Returns the Datacenter selected by id" )
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
        try {
            if (id != null) {
                return "FOUND DATACENTER: " + vimInstanceRequest.findById(id);
            } else {
                return "FOUND DATACENTERS: " + vimInstanceRequest.findAll();
            }
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NO VIMINSTANCE FOUND";
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
            return "DATACENTER UPDATED: " + vimInstanceRequest.update(mapper.<org.project.openbaton.common.catalogue.nfvo.VimInstance>fromJson(new InputStreamReader(new FileInputStream(datacenter)), VimInstance.class), id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VIMINSTANCE NOT UPDATED";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
