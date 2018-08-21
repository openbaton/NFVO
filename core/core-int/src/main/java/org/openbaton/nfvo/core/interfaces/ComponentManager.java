/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.core.interfaces;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.openbaton.catalogue.security.ServiceMetadata;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;

public interface ComponentManager {

  String registerService(String body)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException, NotFoundException;

  String createService(String serviceName, String projectId, List<String> projects)
      throws NotFoundException, MissingParameterException;

  String enableManager(String message);

  boolean isService(String token);

  Iterable<ServiceMetadata> listServices();

  void removeService(String id);

  void removeTokens();
}
