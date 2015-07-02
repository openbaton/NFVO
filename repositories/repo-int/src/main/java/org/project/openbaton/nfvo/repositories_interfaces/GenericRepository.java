package org.project.openbaton.nfvo.repositories_interfaces;

import java.util.List;

import javax.persistence.NoResultException;

/**
 * Created by lto on 30/04/15.
 */
public interface GenericRepository<T>{
    List<T> findAll();
    T create(T entity);
    T merge(T entity);
    void remove(T entity);
    T find(String id) throws NoResultException;
}
