/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
 *
 */

package org.openbaton.nfvo.core.interfaces;

import java.io.IOException;
import java.util.Set;
import org.openbaton.exceptions.AlreadyExistingException;

/** Created by rvl on 19.10.16. */
public interface PluginManager {

  void downloadPlugin(String type, String name, String versio)
      throws IOException, AlreadyExistingException;

  void startPlugin(String path, String name) throws IOException;

  Set<String> listInstalledVimDrivers() throws IOException;
}
