/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.core.api;

import java.util.Arrays;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.security.HistoryEntity;
import org.openbaton.catalogue.security.User;
import org.openbaton.nfvo.repositories.HistoryEntityRepository;
import org.openbaton.nfvo.security.interfaces.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** Created by lto on 17/10/16. */
@Service
public class HistoryManagement implements org.openbaton.nfvo.core.interfaces.HistoryManagement {

  @Autowired private UserManagement userManagement;
  @Autowired private HistoryEntityRepository historyEntityRepository;

  @Value("${nfvo.history.max-entities:250}")
  private int maxHistoryEntities;

  @Value("${nfvo.history.level:1}")
  private int historyLevel;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.history.clear:false}")
  private boolean clearHistory;

  @PostConstruct
  private void init() {
    if (clearHistory) {
      log.warn("Cleaning history of actions");
      historyEntityRepository.deleteAll();
    }
  }

  @Override
  public void addAction(String method, String path, String result) {

    if (historyLevel > 0) {
      if ((historyLevel == 1 && (method.equalsIgnoreCase("get") || method.equalsIgnoreCase("put")))
          || (historyLevel == 2 && method.equalsIgnoreCase("get"))) {
        log.trace("skipping method get or put");
        return;
      }
      User user = null;
      try {
        user = userManagement.getCurrentUser();
      } catch (org.openbaton.exceptions.NotFoundException ex) {
        log.warn("Not storing requests from anonymousUser");
        return;
      }

      HistoryEntity historyEntity = new HistoryEntity();

      historyEntity.setUsername(user.getUsername());
      historyEntity.setMethod(method);
      historyEntity.setPath(path);
      historyEntity.setResult(result);
      historyEntity.setTimestamp(new Date().getTime());

      if (historyEntityRepository.count() >= maxHistoryEntities) {
        HistoryEntity entity =
            historyEntityRepository.findAll(
                    new Sort(new Sort.Order(Sort.Direction.ASC, "timestamp")))[
                0];
        historyEntityRepository.delete(entity.getId());
      }
      historyEntityRepository.save(historyEntity);
    }
  }

  @Override
  public HistoryEntity[] getAll() {
    return historyEntityRepository.findAll(
        new Sort(new Sort.Order(Sort.Direction.ASC, "timestamp")));
  }

  @Override
  public HistoryEntity[] getAll(int actions) {
    if (actions < 0) {
      actions = 0;
    }
    long count = historyEntityRepository.count();
    if (actions >= count) {
      actions = (int) (count - 1);
    }
    return Arrays.copyOfRange(
        historyEntityRepository.findAll(new Sort(new Sort.Order(Sort.Direction.ASC, "timestamp"))),
        0,
        actions);
  }
}
