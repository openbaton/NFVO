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

package org.openbaton.common.vnfm_sdk.amqp;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.jms.*;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

/** Created by lto on 23/09/15. */
@Service
@Scope
public class VnfmSpringHelper extends VnfmHelper {

  private static final String nfvoQueue = "vnfm-core-actions";

  private ExecutorService executorService;
  private Properties properties;

  public JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  @Autowired private JmsTemplate jmsTemplate;
  @Autowired private ConnectionFactory connectionFactory;

  @PostConstruct
  private void init() throws IOException {
    properties = new Properties();
    properties.load(this.getClass().getResourceAsStream("/conf.properties"));
    executorService = Executors.newFixedThreadPool(20);
  }

  public void sendMessageToQueue(String sendToQueueName, final Serializable message) {
    log.trace("Sending message: " + message + " to Queue: " + sendToQueueName);

    MessageCreator messageCreator;

    if (message instanceof java.lang.String)
      messageCreator = getTextMessageCreator((String) message);
    else messageCreator = getObjectMessageCreator(message);

    jmsTemplate.setPubSubDomain(false);
    jmsTemplate.setPubSubNoLocal(false);
    jmsTemplate.send(sendToQueueName, messageCreator);
  }

  public MessageCreator getTextMessageCreator(final String string) {
    MessageCreator messageCreator =
        new MessageCreator() {
          @Override
          public Message createMessage(Session session) throws JMSException {
            TextMessage objectMessage = session.createTextMessage(string);
            return objectMessage;
          }
        };
    return messageCreator;
  }

  public MessageCreator getObjectMessageCreator(final Serializable message) {
    MessageCreator messageCreator =
        new MessageCreator() {
          @Override
          public Message createMessage(Session session) throws JMSException {
            ObjectMessage objectMessage = session.createObjectMessage(message);
            return objectMessage;
          }
        };
    return messageCreator;
  }

  /**
   * This method should be used for receiving text message from EMS
   *
   * <p>resp = { 'output': out, // the output of the command 'err': err, // the error outputs of the
   * commands 'status': status // the exit status of the command }
   *
   * @param queueName
   * @return
   * @throws JMSException
   */
  public String receiveTextFromQueue(String queueName)
      throws JMSException, ExecutionException, InterruptedException, VnfmSdkException {
    String res;

    Connection connection = connectionFactory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));
    connection.start();
    String scriptMaxTime = properties.getProperty("script-max-time");
    if (scriptMaxTime != null) {
      TextMessage textMessage = (TextMessage) consumer.receive(Long.parseLong(scriptMaxTime));
      if (textMessage != null) res = textMessage.getText();
      else
        throw new VnfmSdkException(
            "No message got from queue " + queueName + " after " + scriptMaxTime);
    } else res = ((TextMessage) consumer.receive()).getText();
    log.debug("Received Text from " + queueName + ": " + res);
    consumer.close();
    session.close();
    connection.close();
    return res;
  }

  @Override
  public void sendToNfvo(final NFVMessage nfvMessage) {
    sendMessageToQueue(nfvoQueue, nfvMessage);
  }

  @Override
  public NFVMessage sendAndReceive(NFVMessage message) throws Exception {
    Message response = this.jmsTemplate.sendAndReceive(nfvoQueue, getObjectMessageCreator(message));
    return (NFVMessage) ((ObjectMessage) response).getObject();
  }

  @Override
  public String sendAndReceive(String message, String queueName) throws Exception {
    throw new UnsupportedOperationException();
  }
}
