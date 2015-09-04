package org.project.openbaton.nfvo.repositories;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.abstract_repositories.NetworkServiceRecordRepository;
import org.project.openbaton.nfvo.abstract_repositories.NetworkServiceRecordRepositoryCustom;
import org.project.openbaton.nfvo.abstract_repositories.NetworkServiceRepositoryInterface;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;

/**
 * Created by mob on 03.09.15.
 */
@Repository
//@Scope("singleton")
@Transactional(readOnly = true)
public class NetworkServiceRecordRepositoryImpl implements NetworkServiceRepositoryInterface {

    @PersistenceContext
    protected EntityManager entityManager;

    /*@Autowired
    private NetworkServiceRecordRepository repo;*/

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @Transactional
    public void addVnfr(VirtualNetworkFunctionRecord vnfr, String id) {
        entityManager.persist(vnfr);
        //NetworkServiceRecord nsr=entityManager.createQuery("SELECT n FROM " + NetworkServiceRecord.class.getSimpleName() + " AS n WHERE n.id = '" + id + "'", NetworkServiceRecord.class).getSingleResult();
        NetworkServiceRecord nsr = find(id);
        nsr.getVnfr().add(vnfr);
        entityManager.merge(nsr);
        //repo.findOne(id).getVnfr().add(vnfr);
    }

    @Override
    public List<NetworkServiceRecord> findAll() {
        return entityManager.createQuery("FROM " + NetworkServiceRecord.class.getSimpleName()).getResultList();
    }

    @Override
    public NetworkServiceRecord create(NetworkServiceRecord entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public NetworkServiceRecord merge(NetworkServiceRecord entity) {
        return entityManager.merge(entity);
    }

    @Override
    public void remove(NetworkServiceRecord entity) {
        entityManager.remove(entityManager.merge(entity));
    }

    @Override
    public NetworkServiceRecord find(String id) throws NoResultException {
        return entityManager.createQuery("SELECT n FROM " + NetworkServiceRecord.class.getSimpleName() + " AS n WHERE n.id = '" + id + "'", NetworkServiceRecord.class).getSingleResult();
    }
}
