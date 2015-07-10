package org.project.openbaton.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NSDNotFoundException extends RuntimeException {

	public NSDNotFoundException(String Id) {
		super("Not found Network Service Descriptor with id: " + Id);
	}

}