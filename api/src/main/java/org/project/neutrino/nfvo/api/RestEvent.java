package org.project.neutrino.nfvo.api;

import org.project.neutrino.nfvo.catalogue.nfvo.Action;
import org.project.neutrino.nfvo.catalogue.nfvo.EventEndpoint;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.core.interfaces.EventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class RestEvent {

	//	TODO add log prints
//	private Logger log = LoggerFactory.getLogger(this.getClass());


	@Autowired
	private EventDispatcher eventDispatcher;

	/**
	 * Adds a new EventEndpoint to the EventEndpoint repository
	 * 
	 * @param endpoint
	 *            : Image to add
	 * @return image: The image filled with values from the core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public void register(@RequestBody @Valid EventEndpoint endpoint) {
		 eventDispatcher.register(endpoint);
	}

	/**
	 * Removes the EventEndpoint from the EventEndpoint repository
	 * 
	 * @param name
	 *            : The Image's id to be deleted
	 */
	@RequestMapping(value = "{name}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unregister(@PathVariable("id") String name) throws NotFoundException {
		eventDispatcher.unregister(name);
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<String> getEvents(){
		List<String> actions = new ArrayList<>();
		for (Action action : Action.values()){
			actions.add(action.toString());
		}
		return actions;
	}

}
