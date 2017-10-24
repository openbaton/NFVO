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

package org.openbaton.catalogue.mano.common.faultmanagement;

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by mob on 29.10.15. */
@Entity
public class Criteria extends BaseEntity {

  private String name;
  private String parameter_ref;
  private String function;
  private VNFCSelector vnfc_selector;
  private String comparison_operator;
  private String threshold;

  public Criteria() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getParameter_ref() {
    return parameter_ref;
  }

  public void setParameter_ref(String parameter_ref) {
    this.parameter_ref = parameter_ref;
  }

  public String getComparison_operator() {
    return comparison_operator;
  }

  public void setComparison_operator(String comparison_operator) {
    this.comparison_operator = comparison_operator;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public VNFCSelector getVnfc_selector() {
    return vnfc_selector;
  }

  public void setVnfc_selector(VNFCSelector vnfc_selector) {
    this.vnfc_selector = vnfc_selector;
  }

  public String getThreshold() {
    return threshold;
  }

  public void setThreshold(String threshold) {
    this.threshold = threshold;
  }

  @Override
  public String toString() {
    return "Criteria{"
        + "name='"
        + name
        + '\''
        + ", parameter_ref='"
        + parameter_ref
        + '\''
        + ", function='"
        + function
        + '\''
        + ", vnfc_selector="
        + vnfc_selector
        + ", comparison_operator='"
        + comparison_operator
        + '\''
        + ", threshold='"
        + threshold
        + '\''
        + "} "
        + super.toString();
  }
}
