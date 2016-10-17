package org.openbaton.nfvo.core.interfaces;

import org.openbaton.exceptions.NotFoundException;

/**
 * Created by lto on 17/10/16.
 */
public interface HistoryManagement {

  void addAction(String method, String path, String result) throws NotFoundException;
}
