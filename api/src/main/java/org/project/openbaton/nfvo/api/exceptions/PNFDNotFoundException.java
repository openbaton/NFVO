package org.project.openbaton.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Physical Network Function Descriptor not found")
public class PNFDNotFoundException extends RuntimeException {

	public PNFDNotFoundException(String Id) {
		super("Not foud Physical Network Function Descriptor with id: " + Id);
	}
}