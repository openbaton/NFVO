/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.nfvo.exceptions.NotFoundException;

/**
 * Created by lto on 01/07/15.
 */

/**
 * This class is in charge of registering and deregistering endpoint for event dispatching.
 *
 * An external application can register to a specific event sending an EndpointEvent through JMS or Rest.
 *
 */
public interface EventDispatcher {
    void register(EventEndpoint endpoint);

    void unregister(String name) throws NotFoundException;
}
