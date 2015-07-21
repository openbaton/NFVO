package org.project.openbaton.clients.concretes;

import org.project.openbaton.clients.interfaces.ClientInterfaces;

/**
 * Created by lto on 21/07/15.
 */
public abstract class AbstractOpenstack implements ClientInterfaces {
    protected static final String type = "openstack";

    public static String getType() {
        return type;
    }
}
