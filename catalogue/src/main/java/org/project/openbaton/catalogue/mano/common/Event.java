package org.project.openbaton.catalogue.mano.common;

/**
 * Created by lto on 06/02/15.
 */

public enum Event {
	GRANTED,
	ALLOCATE,
	SCALE,
	RELEASE,
	ERROR,

	INSTANTIATE,TERMINATE,
	CONFIGURE,
	START,STOP,
	SCALE_OUT,SCALE_IN,
	SCALE_UP,SCALE_DOWN,
	UPDATE,UPDATE_ROLLBACK,
	UPGRADE,UPGRADE_ROLLBACK,
	RESET,

}
