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

package org.openbaton.catalogue.nfvo;

import java.io.Serializable;

/** Created by lto on 03/06/15. */

/** The internal Event containing the action that triggered this and the payload of the event */
public class ApplicationEventNFVO {
  private Action action;
  private Serializable payload;

  public ApplicationEventNFVO(Action action, Serializable payload) {
    this.action = action;
    this.payload = payload;
  }

  public ApplicationEventNFVO() {}

  public Serializable getPayload() {
    return payload;
  }

  public void setPayload(Serializable payload) {
    this.payload = payload;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  @Override
  public String toString() {
    return "ApplicationEventNFVO{"
        + "action="
        + action
        + ", payload="
        + payload.getClass().getSimpleName()
        + '}';
  }
}
