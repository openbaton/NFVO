package org.openbaton.nfvo.common.interfaces;

import javax.jms.JMSException;
import java.io.Serializable;

/**
 * Created by tce on 14.08.15.
 */
public interface Receiver {
    Serializable receive(String destination, String selector) throws JMSException;
}
