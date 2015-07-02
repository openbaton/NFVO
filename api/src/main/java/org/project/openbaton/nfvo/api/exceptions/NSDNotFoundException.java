package org.project.openbaton.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Network Service Descriptor not found")
public class NSDNotFoundException extends RuntimeException {
	
	private Errors errors;

	public NSDNotFoundException(String Id, Errors errors) {
		super("Not foud Network Service Descriptor with id: " + Id);
		this.setErrors(errors);
	}

	public NSDNotFoundException(String Id) {
		super("Not foud Network Service Descriptor with id: " + Id);
	}

	public Errors getErrors() {
		return errors;
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

}