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

package org.openbaton.nfvo.common.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.DockerVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.GenericVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NfvoGsonSerializerVimInstance implements JsonSerializer<BaseVimInstance> {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public JsonElement serialize(
      BaseVimInstance src, Type typeOfSrc, JsonSerializationContext context) {
    if (src instanceof OpenstackVimInstance) {
      return gson.toJsonTree(src, OpenstackVimInstance.class);
    } else if (src instanceof DockerVimInstance)
      return gson.toJsonTree(src, DockerVimInstance.class);
    else return gson.toJsonTree(src, GenericVimInstance.class);
  }
}
