package org.openbaton.nfvo.api;

import org.openbaton.catalogue.security.HistoryEntity;
import org.openbaton.nfvo.core.interfaces.HistoryManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lto on 17/10/16.
 */
@RestController
@RequestMapping("/api/v1/history")
public class RestHistory {

  @Autowired private HistoryManagement historyManagement;

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public HistoryEntity[] getHistory() {
    return historyManagement.getAll();
  }

  @RequestMapping(
    value = "/{actions}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public HistoryEntity[] getHistory(@PathVariable("actions") int actions) {
    return historyManagement.getAll(actions);
  }
}
