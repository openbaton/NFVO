package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;

/**
 * Created by lto on 19/09/16.
 */
public interface QuotaManagement {
  Quota getAllQuota(String projectId) throws PluginException, VimException;

  Quota getLeftQuota(String projectId) throws VimException, PluginException;
}
