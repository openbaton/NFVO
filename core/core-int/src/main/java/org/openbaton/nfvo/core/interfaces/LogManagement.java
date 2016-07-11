package org.openbaton.nfvo.core.interfaces;

import org.openbaton.exceptions.NotFoundException;

import java.util.List;

/**
 * Created by lto on 17/05/16.
 */
public interface LogManagement {

  List<String> getLog(String nsrId, String vnfrName, String hostname) throws NotFoundException;
}
