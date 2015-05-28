package org.project.neutrino.nfvo.api.exceptions;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Physical Network Function Descriptor not found")
public class PNFDNotFoundException extends RuntimeException {

	public PNFDNotFoundException(String Id) {
		super("Not foud Physical Network Function Descriptor with id: " + Id);
	}
}