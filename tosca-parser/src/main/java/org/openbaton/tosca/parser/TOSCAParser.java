/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.tosca.parser;

import java.util.*;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.tosca.exceptions.NotFoundException;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.tosca.templates.RelationshipsTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP.CPNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU.VDUNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VL.VLNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF.VNFConfigurations;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF.VNFNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.TopologyTemplate;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.springframework.stereotype.Service;

/** Created by rvl on 17.08.16. */
@Service
public class TOSCAParser {

  public TOSCAParser() {}

  /**
   * Parser of the Virtual Link
   *
   * @param vlNodeTemplate
   * @return
   */
  private InternalVirtualLink parseVL(VLNodeTemplate vlNodeTemplate) {

    InternalVirtualLink vl = new InternalVirtualLink();
    vl.setName(vlNodeTemplate.getName());

    return vl;
  }

  /**
   * Parser of the Connection Point
   *
   * @param cpTemplate
   * @return
   */
  private VNFDConnectionPoint parseCPTemplate(CPNodeTemplate cpTemplate) {

    VNFDConnectionPoint cp = new VNFDConnectionPoint();
    cp.setVirtual_link_reference(cpTemplate.getRequirements().getVirtualLink().get(0));

    if (cpTemplate.getProperties() != null) {
      if (cpTemplate.getProperties().getFloatingIP() != null) {
        cp.setFloatingIp(cpTemplate.getProperties().getFloatingIP());
      }
    }

    return cp;
  }

  /**
   * Parser of the Virtual Deployment Unit
   *
   * @param vduTemplate
   * @param cps
   * @return
   */
  private VirtualDeploymentUnit parseVDUTemplate(
      VDUNodeTemplate vduTemplate, List<CPNodeTemplate> cps) {

    VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
    vdu.setName(vduTemplate.getName());

    // ADD Settings
    vdu.setScale_in_out(vduTemplate.getProperties().getScale_in_out());
    vdu.setVm_image(vduTemplate.getArtifacts());

    vdu.setVimInstanceName(vduTemplate.getProperties().getVim_instance_name());

    // ADD VNF Connection Points
    Set<VNFComponent> vnfComponents = new HashSet<>();
    VNFComponent vnfc = new VNFComponent();
    Set<VNFDConnectionPoint> connectionPoints = new HashSet<>();

    for (CPNodeTemplate cp : cps) {
      if (cp.getRequirements().getVirtualBinding().contains(vduTemplate.getName())) {
        connectionPoints.add(parseCPTemplate(cp));
      }
    }

    vnfc.setConnection_point(connectionPoints);
    vnfComponents.add(vnfc);
    vdu.setVnfc(vnfComponents);

    return vdu;
  }

  /**
   * Parser of the relationship template
   *
   * @param nsd
   * @param relationshipsTemplates
   */
  private void parseRelationships(
      NetworkServiceDescriptor nsd, Map<String, RelationshipsTemplate> relationshipsTemplates) {
    if (relationshipsTemplates == null) return;
    for (String key : relationshipsTemplates.keySet()) {
      VNFDependency vnfDependency = new VNFDependency();

      RelationshipsTemplate relationshipsTemplate = relationshipsTemplates.get(key);
      VirtualNetworkFunctionDescriptor vnfdSouce = new VirtualNetworkFunctionDescriptor();
      VirtualNetworkFunctionDescriptor vnfdTarget = new VirtualNetworkFunctionDescriptor();

      vnfdSouce.setName(relationshipsTemplate.getSource());
      vnfdTarget.setName(relationshipsTemplate.getTarget());

      vnfDependency.setSource(vnfdSouce);
      vnfDependency.setTarget(vnfdTarget);
      vnfDependency.setParameters(new HashSet<>(relationshipsTemplate.getParameters()));

      nsd.getVnf_dependency().add(vnfDependency);
    }
  }

  /**
   * Parser of the VNF Node
   *
   * @param vnf
   * @param topologyTemplate
   * @return
   */
  private VirtualNetworkFunctionDescriptor parseVNFNode(
      VNFNodeTemplate vnf, TopologyTemplate topologyTemplate) throws NotFoundException {

    VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();

    vnfd.setName(vnf.getName());
    vnfd.setVendor(vnf.getProperties().getVendor());
    vnfd.setVersion((Double.toString(vnf.getProperties().getVersion())));

    vnfd.setDeployment_flavour(vnf.getProperties().getDeploymentFlavourConverted());
    vnfd.setVnfPackageLocation(vnf.getProperties().getVnfPackageLocation());
    if (vnf.getProperties().getEndpoint() == null)
      throw new NotFoundException("No endpoint specified in properties for VNF: " + vnfd.getName());
    vnfd.setEndpoint(vnf.getProperties().getEndpoint());
    if (vnf.getProperties().getType() == null)
      throw new NotFoundException("No type specified in properties for VNF: " + vnfd.getName());
    vnfd.setType(vnf.getProperties().getType());

    if (vnf.getProperties().getAuto_scale_policy() != null) {
      vnfd.setAuto_scale_policy(vnf.getProperties().getAuto_scale_policy().getAutoScalePolicySet());
    }

    ArrayList<String> vduList = vnf.getRequirements().getVDUS();

    // ADD VDUs
    Set<VirtualDeploymentUnit> vdus = new HashSet<>();

    for (VDUNodeTemplate vdu : topologyTemplate.getVDUNodes()) {
      if (vduList.contains(vdu.getName())) {
        vdus.add(parseVDUTemplate(vdu, topologyTemplate.getCPNodes()));
      }
    }
    vnfd.setVdu(vdus);

    Set<String> virtualLinkReferences = new HashSet<>();

    for (VirtualDeploymentUnit vdu : vdus) {
      for (VNFComponent vnfComponent : vdu.getVnfc()) {
        for (VNFDConnectionPoint cp : vnfComponent.getConnection_point()) {
          if (cp.getVirtual_link_reference() != null)
            virtualLinkReferences.add(cp.getVirtual_link_reference());
        }
      }
    }

    // ADD VLs
    //ArrayList<String> vl_list = vnf.getRequirements().getVirtualLinks();
    Set<InternalVirtualLink> vls = new HashSet<>();

    for (VLNodeTemplate vl : topologyTemplate.getVLNodes()) {

      if (virtualLinkReferences.contains(vl.getName())) {
        vls.add(parseVL(vl));
      }
    }
    vnfd.setVirtual_link(vls);
    vnfd.setLifecycle_event(vnf.getInterfaces().getOpLifecycle());

    //ADD CONFIGURATIONS
    if (vnf.getProperties().getConfigurations() != null) {
      Configuration configuration = new Configuration();
      configuration.setName(vnf.getProperties().getConfigurations().getName());

      Set<ConfigurationParameter> configurationParameters = new HashSet<>();

      for (HashMap<String, String> pair :
          vnf.getProperties().getConfigurations().getConfigurationParameters()) {

        ConfigurationParameter configurationParameter = new ConfigurationParameter();
        configurationParameter.setConfKey((String) pair.keySet().toArray()[0]);
        configurationParameter.setValue((String) pair.values().toArray()[0]);
        configurationParameters.add(configurationParameter);
      }

      configuration.setConfigurationParameters(configurationParameters);
      vnfd.setConfigurations(configuration);
    }

    return vnfd;
  }

  /**
   * Parser of the VNF template
   *
   * @param VNFDTemplate
   * @return
   */
  public VirtualNetworkFunctionDescriptor parseVNFDTemplate(VNFDTemplate VNFDTemplate)
      throws NotFoundException {

    VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();

    // ADD SETTINGS
    if (VNFDTemplate.getMetadata() == null)
      throw new NotFoundException("The VNFD Template must have contain metadata child!");
    vnfd.setName(VNFDTemplate.getMetadata().getID());
    vnfd.setVendor(VNFDTemplate.getMetadata().getVendor());
    vnfd.setVersion(VNFDTemplate.getMetadata().getVersion());

    if (VNFDTemplate.getInputs() == null)
      throw new NotFoundException(
          "You should specify at least endpoint, deployment_flavour and type in inputs");
    vnfd.setDeployment_flavour(VNFDTemplate.getInputs().getDeploymentFlavourConverted());
    vnfd.setVnfPackageLocation(VNFDTemplate.getInputs().getVnfPackageLocation());
    if (VNFDTemplate.getInputs().getEndpoint() == null)
      throw new NotFoundException("No endpoint specified in inputs!");
    vnfd.setEndpoint(VNFDTemplate.getInputs().getEndpoint());
    if (VNFDTemplate.getInputs().getType() == null)
      throw new NotFoundException("No type specified in inputs!");
    vnfd.setType(VNFDTemplate.getInputs().getType());

    // ADD VDUs
    Set<VirtualDeploymentUnit> vdus = new HashSet<>();
    for (VDUNodeTemplate vdu : VNFDTemplate.getTopology_template().getVDUNodes()) {
      vdus.add(parseVDUTemplate(vdu, VNFDTemplate.getTopology_template().getCPNodes()));
    }
    vnfd.setVdu(vdus);

    // ADD VLs
    Set<InternalVirtualLink> vls = new HashSet<>();

    for (VLNodeTemplate vl : VNFDTemplate.getTopology_template().getVLNodes()) {

      vls.add(parseVL(vl));
    }

    vnfd.setVirtual_link(vls);
    vnfd.setLifecycle_event(VNFDTemplate.getInputs().getInterfaces().getOpLifecycle());

    //ADD CONFIGURATIONS
    if (VNFDTemplate.getInputs().getConfigurations() != null) {

      VNFConfigurations configurations = VNFDTemplate.getInputs().getConfigurations();

      Configuration configuration = new Configuration();
      configuration.setName(configurations.getName());

      Set<ConfigurationParameter> configurationParameters = new HashSet<>();

      for (HashMap<String, String> pair : configurations.getConfigurationParameters()) {

        ConfigurationParameter configurationParameter = new ConfigurationParameter();
        configurationParameter.setConfKey((String) pair.keySet().toArray()[0]);
        configurationParameter.setValue((String) pair.values().toArray()[0]);
        configurationParameters.add(configurationParameter);
      }

      configuration.setConfigurationParameters(configurationParameters);
      vnfd.setConfigurations(configuration);
    }

    return vnfd;
  }

  /**
   * Parser of the NSD template
   *
   * @param nsdTemplate
   * @return
   */
  public NetworkServiceDescriptor parseNSDTemplate(NSDTemplate nsdTemplate)
      throws NotFoundException {

    NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();

    if (nsdTemplate.getMetadata() == null)
      throw new NotFoundException("The NSD Template must have a metadata child!");
    nsd.setVersion(nsdTemplate.getMetadata().getVersion());
    nsd.setVendor(nsdTemplate.getMetadata().getVendor());
    nsd.setName(nsdTemplate.getMetadata().getID());

    // ADD VNFDS

    for (VNFNodeTemplate vnfNodeTemplate : nsdTemplate.getTopology_template().getVNFNodes()) {

      VirtualNetworkFunctionDescriptor vnf =
          parseVNFNode(vnfNodeTemplate, nsdTemplate.getTopology_template());

      nsd.getVnfd().add(vnf);
    }

    // ADD VLS
    nsd.setVld(new HashSet<VirtualLinkDescriptor>());

    for (VLNodeTemplate vlNode : nsdTemplate.getTopology_template().getVLNodes()) {

      VirtualLinkDescriptor vld = new VirtualLinkDescriptor();
      vld.setName(vlNode.getName());
      nsd.getVld().add(vld);
    }

    // ADD DEPENDENCIES
    parseRelationships(nsd, nsdTemplate.getRelationships_template());

    return nsd;
  }
}
