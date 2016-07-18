package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.nfvo.VimInstance;

import java.util.Collection;

/**
 * Created by lto on 10/03/16.
 */
public interface VnfPlacementManagement {
  VimInstance choseRandom(Collection<String> vimInstanceName);
}
