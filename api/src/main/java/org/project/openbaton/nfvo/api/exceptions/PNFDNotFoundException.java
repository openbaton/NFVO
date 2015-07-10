package org.project.openbaton.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PNFDNotFoundException extends RuntimeException {

	public PNFDNotFoundException(String Id) {
		super("Not found Physical Network Function Descriptor with id: " + Id);
	}
}