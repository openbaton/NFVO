package org.project.neutrino.nfvo.repositories;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lto on 30/04/15.
 */
@Repository
@Transactional(readOnly = true)
@Scope("singleton")
public class NSDRepository<NetworkServiceDescriptor> extends DatabaseRepository<NetworkServiceDescriptor> {
}
