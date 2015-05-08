package org.project.neutrino.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Network Service Descriptor not found")
public class NSDNotFoundException extends RuntimeException {

	public NSDNotFoundException(String Id) {
		super("Not foud Network Service Descriptor with id: " + Id);
	}
}