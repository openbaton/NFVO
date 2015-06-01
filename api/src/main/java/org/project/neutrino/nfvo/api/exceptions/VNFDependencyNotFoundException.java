package org.project.neutrino.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Virtual Network Function Dependency not found")
public class VNFDependencyNotFoundException extends RuntimeException {

	public VNFDependencyNotFoundException(String Id) {
		super("Not foud Virtual Network Function Dependency with id: " + Id);
	}
}