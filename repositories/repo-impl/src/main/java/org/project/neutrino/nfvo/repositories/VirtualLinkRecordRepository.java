package org.project.neutrino.nfvo.repositories;

import org.project.neutrino.nfvo.abstract_repositories.DatabaseRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lto on 06/05/15.
 */
@Repository
@Transactional(readOnly = true)
@Scope("singleton")
public class VirtualLinkRecordRepository<VirtualLinkRecord> extends DatabaseRepository<VirtualLinkRecord>{
}
