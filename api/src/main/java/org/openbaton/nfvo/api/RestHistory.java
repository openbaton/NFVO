package org.openbaton.nfvo.api;

import org.openbaton.nfvo.repositories.HistoryEntityRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by lto on 17/10/16.
 */
@RestController
@RequestMapping("/api/v1/history")
public class RestHistory {

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public HistoryEntity[]
}
