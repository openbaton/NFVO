package org.openbaton.nfvo.repositories;

import java.util.List;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.springframework.data.repository.CrudRepository;

/**
 * This repository interface is used for dealing with NFVImages, which correspond to images used by
 * OpenStack. Do not confuse this interface with the general ImageRepository which works on all
 * types of images.
 */
public interface NFVImageRepository extends CrudRepository<NFVImage, String> {
  List<NFVImage> findAllByProjectIdAndIsInImageRepoIsTrue(String projectId);

  NFVImage findOneByNameAndProjectIdAndIsInImageRepoIsTrue(String name, String projectId);

  NFVImage findOneByIdAndProjectIdAndIsInImageRepoIsTrue(String id, String projectId);

  List<NFVImage> isInImageRepoIsTrue();

  NFVImage findOneByIdAndIsInImageRepoIsTrue(String id);
}
