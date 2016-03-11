package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.nfvo.VimInstance;

import java.util.List;

/**
 * Created by lto on 10/03/16.
 */
public interface VnfPlacementManagement {
    VimInstance choseRandom(List<String> vimInstanceName);
}
