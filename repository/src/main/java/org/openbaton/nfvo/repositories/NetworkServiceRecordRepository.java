/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.repositories;

import java.util.List;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/** Created by mob on 03.09.15. */
public interface NetworkServiceRecordRepository
    extends CrudRepository<NetworkServiceRecord, String>, NetworkServiceRecordRepositoryCustom {
  NetworkServiceRecord findFirstById(String id);

  @Modifying
  // @Transactional is needed since using @Query the semantics are completely in the declaration
  // while other methods (CRUD operations) are transactional by default
  @Transactional
  @Query("update NetworkServiceRecord n set n.status = ?2 where n.id = ?1")
  void setStatus(String id, Status status);

  boolean existsByIdAndProjectIdAndStatus(String id, String projectId, Status status);

  NetworkServiceRecord findFirstByIdAndProjectId(String id, String projectId);

  List<NetworkServiceRecord> findByProjectId(String projectId);
}
