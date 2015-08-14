package org.project.openbaton.nfvo.common.exceptions;

import javax.jms.JMSException;

/**
 * Created by tce on 14.08.15.
 */
public class PluginInvokeException extends Exception {
    public PluginInvokeException(String message) {
        super(message);
    }

    public PluginInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginInvokeException(Throwable cause) {
        super(cause);
    }
}
