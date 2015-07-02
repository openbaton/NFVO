package org.project.openbaton.nfvo.security;

import org.project.openbaton.nfvo.abstract_repositories.DatabaseRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
@Scope("singleton")
public class UserRepository<User> extends DatabaseRepository<User>{
}
