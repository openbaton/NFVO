package org.project.neutrino.nfvo.abstract_repositories;

import com.google.common.reflect.TypeToken;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
		TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
		};
		Type type = typeToken.getType();
		log.trace("TYPE: " + type.toString());
		return this.entityManager.createQuery("FROM " + type.toString())
				.getResultList();
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public T create(T entity) {
		this.entityManager.persist(entity);
		return entity;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public T merge(T entity) {
		return this.entityManager.merge(entity);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void remove(T entity) {
		this.entityManager.remove(entity);
	}

	@Override
	public T find(String id) throws NoResultException {
		TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
		};
		Type type = typeToken.getType();
		log.trace("Type is: " + type.toString());
		Object o = entityManager.createQuery(
				"FROM " + type.toString() + " WHERE id=\'" + id + "\'")
				.getSingleResult();
		if (o == null)
			throw new NoResultException(type.toString() + " with " + id
					+ " not found");
		return (T) o;
	}
}
