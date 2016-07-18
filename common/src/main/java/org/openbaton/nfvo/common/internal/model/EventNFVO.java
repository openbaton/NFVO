package org.openbaton.nfvo.common.internal.model;

import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.springframework.context.ApplicationEvent;

/**
 * Created by lto on 30/09/15.
 */
public class EventNFVO extends ApplicationEvent {
  private ApplicationEventNFVO eventNFVO;

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public EventNFVO(Object source) {
    super(source);
  }

  public ApplicationEventNFVO getEventNFVO() {
    return eventNFVO;
  }

  public void setEventNFVO(ApplicationEventNFVO eventNFVO) {
    this.eventNFVO = eventNFVO;
  }
}
