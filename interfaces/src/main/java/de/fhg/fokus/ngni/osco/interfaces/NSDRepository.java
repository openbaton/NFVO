package de.fhg.fokus.ngni.osco.interfaces;

import de.fhg.fokus.ngni.nfvo.repository.mano.descriptor.NetworkServiceDescriptor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lto on 17/04/15.
 */
public interface NSDRepository {

    List findAll();

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    NetworkServiceDescriptor create(NetworkServiceDescriptor networkServiceDescriptor);

    NetworkServiceDescriptor find(String id);

    void delete(NetworkServiceDescriptor nsd);

    NetworkServiceDescriptor update(NetworkServiceDescriptor nsd);
}
