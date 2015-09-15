package org.project.openbaton.catalogue.nfvo.messages.Interfaces;

import org.project.openbaton.catalogue.nfvo.Action;

import java.io.Serializable;

/**
 * Created by mob on 14.09.15.
 */
public interface NFVMessage extends Serializable {
    Action getAction();
}
