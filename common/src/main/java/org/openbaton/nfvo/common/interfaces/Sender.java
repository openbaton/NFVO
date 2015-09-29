package org.openbaton.nfvo.common.interfaces;


import javax.jms.JMSException;
import java.io.Serializable;

public interface Sender {

    void send(String destination, Serializable message);

    Serializable receiveObject(String destination) throws JMSException;

    String receiveText(String destination) throws JMSException;
}
