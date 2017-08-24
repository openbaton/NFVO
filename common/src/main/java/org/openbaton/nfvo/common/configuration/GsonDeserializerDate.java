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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties
public class GsonDeserializerDate implements JsonDeserializer<Date> {

  @Value("${nfvo.api.serializer.date.format:timestamp}")
  private String dateFormat;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Date result;
    if (dateFormat == null) {
      dateFormat = "timestamp";
    }
    switch (dateFormat) {
      case "string":
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a z");
        try {
          result = formatter.parse(json.getAsString());
          break;
        } catch (ParseException | ClassCastException e) {
          log.error("Not able to parse string date format, trying using timestamp", e);
        }
      default:
        result = new Date(json.getAsLong());
    }

    return result;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }
}
