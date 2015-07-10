package org.project.openbaton.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Virtual Network Function Descriptor not found")
public class VNFDNotFoundException extends RuntimeException {

	public VNFDNotFoundException(String Id) {
		super("Not found Virtual Network Function Descriptor with id: " + Id);
	}
}