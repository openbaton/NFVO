/*
 * #
 * # Copyright (c) 2015 Fraunhofer FOKUS
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #     http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * #
 *
 */

package org.openbaton.nfvo.repositories;

import java.util.Collection;
import org.openbaton.catalogue.nfvo.VNFPackageMetadata;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/** Created by lto on 06/05/15. */
public interface VNFPackageMetadataRepository extends CrudRepository<VNFPackageMetadata, String> {

  VNFPackageMetadata findFirstById(String id);

  @Query(
      "SELECT CASE WHEN COUNT(n) > 0 THEN 'true' ELSE 'false' END FROM VNFPackageMetadata n WHERE n.vendor = ?1 and n.name = ?2 and n.version = ?3 "
          + "and n.type = ?4 and n.vnfmType = ?5 ")
  Boolean existsByTypeAndNameAndVersion(
      String vendor, String name, String version, String type, String vnfmType);

  Iterable<VNFPackageMetadata>
      findAllByNameAndVendorAndVersionAndNfvoVersionAndVnfmTypeAndOsIdAndOsVersionAndOsArchitectureAndTagAndProjectId(
          String name,
          String vendor,
          String version,
          String nfvoVersion,
          String vnfmType,
          String osId,
          String osVersion,
          String osArchitecture,
          String tag,
          String projectId);

  Iterable<VNFPackageMetadata> findAllByNameAndVendor(String name, String vendor);

  Iterable<VNFPackageMetadata> findByDefaultFlagIsTrue();

  VNFPackageMetadata findByNameAndVendorAndDefaultFlagIsTrue(String name, String vendor);

  VNFPackageMetadata findByNameAndVendorAndVersionAndDefaultFlagIsTrue(
      String name, String vendor, String version);

  VNFPackageMetadata findByVendorAndNameAndVersionAndTypeAndVnfmType(
      String vendor, String name, String version, String type, String vnfmType);

  VNFPackageMetadata findByVendorAndNameAndVersionAndType(
      String vendor, String name, String version, String type);

  Collection<VNFPackageMetadata> findByVendorAndType(String vendor, String type);

  Collection<VNFPackageMetadata> findByVendorAndNameAndType(
      String vendor, String name, String type);

  Collection<VNFPackageMetadata> findByType(String type);

  Collection<VNFPackageMetadata> findByVnfmType(String vnfmType);

  void deleteById(String id);
}
