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
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by lto on 30/04/15.
 */
@Transactional
public abstract class DatabaseRepository<T> implements GenericRepository {

	@PersistenceContext
	protected EntityManager entityManager;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<T> findAll() {
		TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
		};
		Type type = typeToken.getType();
		log.trace("TYPE: " + type.toString());
		return this.entityManager.createQuery("FROM " + type.toString()).getResultList();
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Object create(Object entity) {
		this.entityManager.persist(entity);
		return entity;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Object merge(Object entity) {
		return this.entityManager.merge(entity);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void remove(Object entity) {
		this.entityManager.remove(entityManager.merge(entity));
	}

	@Override
	public Object find(String id) throws NoResultException {
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
