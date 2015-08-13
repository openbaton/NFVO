package org.project.openbaton.nfvo.common.interfaces;

import java.lang.reflect.Method;

/**
 * Created by tce on 13.08.15.
 */
public interface PluginAgent {
    void invokeMethod(Method method, Class inter, Object... parameters);
}
