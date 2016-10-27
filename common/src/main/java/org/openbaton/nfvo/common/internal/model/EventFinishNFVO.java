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

package org.openbaton.nfvo.common.internal.model;

import org.openbaton.catalogue.util.EventFinishEvent;
import org.springframework.context.ApplicationEvent;

/**
 * Created by lto on 30/09/15.
 */
public class EventFinishNFVO extends ApplicationEvent {
  private EventFinishEvent eventNFVO;

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public EventFinishNFVO(Object source) {
    super(source);
  }

  public EventFinishEvent getEventNFVO() {
    return eventNFVO;
  }

  public void setEventNFVO(EventFinishEvent eventNFVO) {
    this.eventNFVO = eventNFVO;
  }
}
