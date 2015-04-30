package org.project.neutrino.nfvo.repositories;

import com.google.common.reflect.TypeToken;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by lto on 30/04/15.
 */
public abstract class DatabaseRepository<T> implements GenericRepository<T> {

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<T> findAll() {
        TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
        Type type = typeToken.getType();
        log.debug("TYPE: " + type.toString());
        return this.entityManager.createQuery("FROM " + type.toString()).getResultList();
    }

    @Override
    public T create(T entity) {
        this.entityManager.persist(entity);
        return entity;
    }

    @Override
    public T merge(T entity) {
        return this.entityManager.merge(entity);
    }

    @Override
    public void remove(T entity) {
        this.entityManager.remove(entity);
    }

    @Override
    public T find(String id) {
        TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
        Type type = typeToken.getType();
        log.debug("Type is: " + type.toString());
        return (T) entityManager.createQuery("FROM " + type.toString() + " WHERE id=\'"+ id + "\'").getSingleResult();
    }
}
