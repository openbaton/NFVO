package de.fhg.fokus.ngni.osco.repositories;

import de.fhg.fokus.ngni.nfvo.repository.mano.descriptor.NetworkServiceDescriptor;
import de.fhg.fokus.ngni.osco.interfaces.NSDRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;

/**
 * Created by lto on 17/04/15.
 */
@Repository
@Transactional(readOnly = true)
@Scope
public class NSDRepositoryImpl  implements NSDRepository {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    @Override
    public List findAll() {
        return entityManager.createQuery("FROM NetworkServiceDescriptor").getResultList();
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public NetworkServiceDescriptor create(NetworkServiceDescriptor networkServiceDescriptor){
        entityManager.persist(networkServiceDescriptor);
        log.trace("Persisted " + networkServiceDescriptor);
        return networkServiceDescriptor;
    }

    @Override
    public NetworkServiceDescriptor find(String id) {
        log.trace("Looking for NetworkServiceDescriptor with id " + id);
        return entityManager.find(NetworkServiceDescriptor.class, id);
    }

    @Override
    public void delete(NetworkServiceDescriptor nsd) {
        log.trace("Removing " + nsd);
        entityManager.remove(nsd);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public NetworkServiceDescriptor update(NetworkServiceDescriptor nsd) {
        log.trace("Updateing " + nsd);
        NetworkServiceDescriptor networkServiceDescriptor = entityManager.merge(nsd);
        entityManager.flush();
        return networkServiceDescriptor;
    }
}
