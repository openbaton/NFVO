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

package org.openbaton.common.vnfm_sdk.interfaces;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFFaultManagement {

	/**
	 * This operation allows collecting VNF application-layer fault information.
	 */
	void getFaultInformation();
	
	/**
	 * This operation allows providing application-layer fault notifications.
	 */
	void notifyFault();
}
