/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
