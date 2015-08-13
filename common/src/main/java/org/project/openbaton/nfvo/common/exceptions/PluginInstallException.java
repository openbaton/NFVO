package org.project.openbaton.nfvo.common.exceptions;

/**
 * Created by lto on 21/07/15.
 */
public class PluginInstallException extends Exception{

    public PluginInstallException() {
    }

    public PluginInstallException(String message) {
        super(message);
    }

    public PluginInstallException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginInstallException(Throwable e) {
        super(e);
    }
}
