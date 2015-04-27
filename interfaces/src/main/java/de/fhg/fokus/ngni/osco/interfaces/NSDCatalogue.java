package de.fhg.fokus.ngni.osco.interfaces;

import de.fhg.fokus.ngni.nfvo.repository.mano.descriptor.NetworkServiceDescriptor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lto on 17/04/15.
 */
public interface NSDCatalogue {
    public List<NetworkServiceDescriptor> findAll();

    @Transactional(readOnly = true)
    List<NetworkServiceDescriptor> find();
}
