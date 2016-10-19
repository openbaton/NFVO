package org.openbaton.nfvo.core.interfaces;

import java.io.IOException;

/**
 * Created by rvl on 19.10.16.
 */
public interface PluginManager {

  void downloadPlugin(String type, String name, String versio) throws IOException;

  void startPlugin(String path, String name) throws IOException;
}
