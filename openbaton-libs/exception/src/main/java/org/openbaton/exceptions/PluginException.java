package org.openbaton.exceptions;

/**
 * Created by lto on 26/11/15.
 */
public class PluginException extends Exception
{
    public PluginException(Throwable cause) {
        super(cause);
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
