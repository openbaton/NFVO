package org.project.openbaton.nfvo.common.interfaces;

import org.project.openbaton.catalogue.nfvo.PluginAnswer;

import javax.jms.JMSException;
import java.io.Serializable;

/**
 * Created by tce on 14.08.15.
 */
public interface Receiver {
    Serializable receive(String destination) throws JMSException;
}
