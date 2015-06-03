/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.common;

import org.project.neutrino.nfvo.catalogue.nfvo.Action;
import org.springframework.context.ApplicationEvent;

/**
 * Created by lto on 06/02/15.
 */
public class Event extends ApplicationEvent {
 private Action action;
 public Event(Object source, Action action) {
  super(source);
  this.action = action;
 }

 @Override
 public String toString() {
  return "Event{" +
          "action=" + action +
          '}';
 }

 public Action getAction() {
  return action;
 }

 public void setAction(Action action) {
  this.action = action;
 }
}
