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
