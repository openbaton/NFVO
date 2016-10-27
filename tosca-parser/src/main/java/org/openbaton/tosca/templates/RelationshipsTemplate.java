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

package org.openbaton.tosca.templates;

import java.util.ArrayList;

/**
 * Created by rvl on 22.08.16.
 */
public class RelationshipsTemplate {

  private String type;
  private String source;
  private String target;
  private ArrayList<String> parameters;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public ArrayList<String> getParameters() {
    return parameters;
  }

  public void setParameters(ArrayList<String> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {

    return "Relationships: "
        + "\n"
        + "source: "
        + source
        + "\n"
        + "target: "
        + target
        + "\n"
        + "Parameters "
        + parameters
        + "\n";
  }
}
