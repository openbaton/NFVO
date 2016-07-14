package org.openbaton.common.vnfm_sdk.interfaces;

import java.util.Set;

/**
 * Created by lto on 10/11/15.
 */
public interface EmsRegistrator {

  void register(String json);

  Set<String> getHostnames();

  void unregister(String s);
}
