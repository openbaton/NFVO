package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.nfvo.VimInstance;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 10/03/16.
 */
public interface VnfPlacementManagement {
  VimInstance choseRandom(Collection<String> vimInstanceName);
}
