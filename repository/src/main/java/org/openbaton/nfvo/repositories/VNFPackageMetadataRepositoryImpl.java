package org.openbaton.nfvo.repositories;

import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VNFPackageMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by mob on 24.05.17.
 */
public class VNFPackageMetadataRepositoryImpl implements VNFPackageMetadataRepositoryCustom {

    @Autowired VnfPackageRepository vnfPackageRepository;

    @Override
    @Transactional
    public VNFPackageMetadata setVNFPackageId(String vnfPackageId) {

        VNFPackage vnfPackage = vnfPackageRepository.findFirstById(vnfPackageId);
        VNFPackageMetadata  vnfPackageMetadata = vnfPackage.getVnfPackageMetadata();
        vnfPackageMetadata.setVnfPackageFatherId(vnfPackageId);
        return vnfPackageMetadata;
    }
}
