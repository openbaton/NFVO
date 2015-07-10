package org.project.openbaton.nfvo.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

	private Errors errors;

	public EntityNotFoundException(String message, Errors errors) {
		super(message);
		this.setErrors(errors);
	}

	public EntityNotFoundException(String message) {
		super(message);
	}

	public Errors getErrors() {
		return errors;
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

}