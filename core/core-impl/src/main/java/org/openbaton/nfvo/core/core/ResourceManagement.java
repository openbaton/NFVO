/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.core.core;

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.VnfPlacementManagement;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Created by lto on 11/06/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties
public class ResourceManagement implements org.openbaton.nfvo.core.interfaces.ResourceManagement {

  //TODO get from RabbitConfiguration
  private final static String exchangeName = "openbaton-exchange";
  private static final Pattern PATTERN =
      Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

  @Value("${nfvo.rabbit.brokerIp:127.0.0.1}")
  private String brokerIp;

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${nfvo.ems.queue.autodelete:true}")
  private boolean emsAutodelete;

  @Value("${nfvo.monitoring.ip:}")
  private String monitoringIp;

  @Value("${nfvo.ems.queue.heartbeat:60}")
  private int emsHeartbeat;

  @Value("${nfvo.ems.version:0.15}")
  private String emsVersion;

  @Value("${nfvo.timezone:UTC}") // set timezone=UTC if the timezone property is not set
  private String timezone;

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private VimBroker vimBroker;
  @Autowired private VimRepository vimInstanceRepository;

  @Autowired private VnfPlacementManagement vnfPlacementManagement;

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getEmsHeartbeat() {
    return emsHeartbeat;
  }

  public void setEmsHeartbeat(int emsHeartbeat) {
    this.emsHeartbeat = emsHeartbeat;
  }

  public boolean isEmsAutodelete() {
    return emsAutodelete;
  }

  public void setEmsAutodelete(boolean emsAutodelete) {
    this.emsAutodelete = emsAutodelete;
  }

  public String getEmsVersion() {
    return emsVersion;
  }

  public void setEmsVersion(String emsVersion) {
    this.emsVersion = emsVersion;
  }

  public String getMonitoringIp() {
    return monitoringIp;
  }

  public void setMonitoringIp(String monitoringIp) {
    this.monitoringIp = monitoringIp;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getBrokerIp() {
    return brokerIp;
  }

  public void setBrokerIp(String brokerIp) {
    this.brokerIp = brokerIp;
  }

  @Override
  @Async
  public Future<List<String>> allocate(
      VirtualDeploymentUnit virtualDeploymentUnit,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VimInstance vimInstance,
      String userdata)
      throws VimException, VimDriverException, ExecutionException, InterruptedException,
          PluginException {
    List<Future<VNFCInstance>> instances = new ArrayList<>();
    org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim;
    vim = vimBroker.getVim(vimInstance.getType());
    log.debug("Executing allocate with Vim: " + vim.getClass().getSimpleName());
    log.debug("NAME: " + virtualNetworkFunctionRecord.getName());
    log.debug("ID: " + virtualDeploymentUnit.getId());
    String hostname = virtualNetworkFunctionRecord.getName().replaceAll("_", "-");
    log.debug("Hostname is: " + hostname);
    virtualDeploymentUnit.setHostname(hostname);
    for (VNFComponent component : virtualDeploymentUnit.getVnfc()) {
      //            String userData = getUserData(virtualNetworkFunctionRecord.getEndpoint());
      log.trace("UserData is: " + userdata);
      Map<String, String> floatingIps = new HashMap<>();
      for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
        if (connectionPoint.getFloatingIp() != null)
          floatingIps.put(
              connectionPoint.getVirtual_link_reference(), connectionPoint.getFloatingIp());
      }
      log.info("FloatingIp chosen are: " + floatingIps);
      Future<VNFCInstance> added =
          vim.allocate(
              vimInstance,
              virtualDeploymentUnit,
              virtualNetworkFunctionRecord,
              component,
              userdata,
              floatingIps);
      instances.add(added);
    }
    List<String> ids = new ArrayList<>();
    for (Future<VNFCInstance> futureInstance : instances) {
      VNFCInstance instance = futureInstance.get();
      virtualDeploymentUnit.getVnfc_instance().add(instance);
      ids.add(instance.getVc_id());
      log.debug("Launched VM with id: " + instance.getVc_id());
      Map<String, String> floatingIps = new HashMap<>();
      for (VNFDConnectionPoint connectionPoint : instance.getVnfComponent().getConnection_point()) {
        if (connectionPoint.getFloatingIp() != null)
          floatingIps.put(
              connectionPoint.getVirtual_link_reference(), connectionPoint.getFloatingIp());
      }
      if (floatingIps.size() != instance.getFloatingIps().size()) {
        log.warn("NFVO wasn't able to all associate FloatingIPs. Is there enough available?");
        log.debug("Expected FloatingIPs: " + floatingIps);
        log.debug("Real FloatingIPs: " + instance.getFloatingIps());
      }
    }
    log.info("Finished deploying VMs with external ids: " + ids);
    return new AsyncResult<>(ids);
  }

  private String allocateVNFC(
      VimInstance vimInstance,
      VirtualDeploymentUnit virtualDeploymentUnit,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim,
      VNFComponent component,
      String userdata)
      throws InterruptedException, ExecutionException, VimException, VimDriverException {
    log.trace("UserData is: " + userdata);
    Map<String, String> floatinIps = new HashMap<>();
    for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
      floatinIps.put(connectionPoint.getVirtual_link_reference(), connectionPoint.getFloatingIp());
    }
    log.info("FloatingIp chosen are: " + floatinIps);
    VNFCInstance added =
        vim.allocate(
                vimInstance,
                virtualDeploymentUnit,
                virtualNetworkFunctionRecord,
                component,
                userdata,
                floatinIps)
            .get();
    virtualDeploymentUnit.getVnfc_instance().add(added);
    if (!floatinIps.isEmpty() && added.getFloatingIps().isEmpty())
      log.warn("NFVO wasn't able to associate FloatingIPs. Is there enough available");
    return added.getVim_id();
  }

  private String getUserData(String endpoint) throws VimException {
    log.debug("Broker ip is: " + brokerIp);
    log.debug("Monitoring ip is: " + monitoringIp);
    brokerIp = brokerIp.trim();
    if (brokerIp == null || brokerIp.equals("") || !PATTERN.matcher(brokerIp).matches()) {
      throw new VimException(
          "nfvo.rabbit.brokerIp is null, empty or not a valid ip please set a correct ip");
    }

    String result =
        "#!/bin/bash\n"
            + "adduser user\n"
            + "echo -e \"password\\npassword\" | (passwd user)\n"
            + "echo \"deb http://get.openbaton.org/repos/apt/debian/ ems main\" >> /etc/apt/sources.list\n"
            + "wget -O - http://get.openbaton.org/public.gpg.key | apt-key add -\n"
            + "apt-get update\n"
            + "cp /usr/share/zoneinfo/"
            + timezone
            + " /etc/localtime\n"
            + //synchronize the time with the timezone of the NFVO
            "apt-get install git -y\n";

    if (monitoringIp != null && !monitoringIp.equals("")) {
      result +=
          " echo \"Installing zabbix-agent for server at _address\"\n"
              + "sudo apt-get install -y zabbix-agent\n"
              + "sudo sed -i -e 's/ServerActive=127.0.0.1/ServerActive="
              + monitoringIp
              + ":10051/g' -e 's/Server=127.0.0.1/Server="
              + monitoringIp
              + "/g' -e 's/Hostname=Zabbix server/#Hostname=/g' /etc/zabbix/zabbix_agentd.conf\n"
              + "sudo service zabbix-agent restart\n"
              + "sudo rm zabbix-release_2.2-1+precise_all.deb\n"
              + "echo \"finished installing zabbix-agent!\"\n";
    }

    result +=
        //                "apt-get install -y python-pip\n" +
        "apt-get install -y ems-"
            + emsVersion
            + "\n"
            + "mkdir -p /etc/openbaton/ems\n"
            + "echo [ems] > /etc/openbaton/ems/conf.ini\n"
            + "echo orch_ip="
            + brokerIp
            + " >> /etc/openbaton/ems/conf.ini\n"
            + "echo username="
            + username
            + " >> /etc/openbaton/ems/conf.ini\n"
            + "echo password="
            + password
            + " >> /etc/openbaton/ems/conf.ini\n"
            + "echo exchange="
            + exchangeName
            + " >> /etc/openbaton/ems/conf.ini\n"
            + "echo heartbeat="
            + emsHeartbeat
            + " >> /etc/openbaton/ems/conf.ini\n"
            + "echo autodelete="
            + emsAutodelete
            + " >> /etc/openbaton/ems/conf.ini\n"
            + "export hn=`hostname`\n"
            + "echo \"type="
            + endpoint
            + "\" >> /etc/openbaton/ems/conf.ini\n"
            + "echo \"hostname=$hn\" >> /etc/openbaton/ems/conf.ini\n"
            + "echo orch_port=61613 >> /etc/openbaton/ems/conf.ini\n"
            + "service ems restart\n";

    return result;
  }

  @Override
  public List<Server> query(VimInstance vimInstance) throws VimException, PluginException {
    return vimBroker.getVim(vimInstance.getType()).queryResources(vimInstance);
  }

  @Override
  public void update(VirtualDeploymentUnit vdu) {}

  @Override
  public void scale(VirtualDeploymentUnit vdu) {}

  @Override
  public void migrate(VirtualDeploymentUnit vdu) {}

  @Override
  public void operate(VirtualDeploymentUnit vdu, String operation) {}

  @Override
  @Async
  public Future<Void> release(
      VirtualDeploymentUnit virtualDeploymentUnit, VNFCInstance vnfcInstance)
      throws VimException, ExecutionException, InterruptedException, PluginException {
    VimInstance vimInstance = vimInstanceRepository.findFirstById(vnfcInstance.getVim_id());
    org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim =
        vimBroker.getVim(vimInstance.getType());
    log.debug("Removing vnfcInstance: " + vnfcInstance);
    vim.release(vnfcInstance, vimInstance).get();
    virtualDeploymentUnit.getVnfc().remove(vnfcInstance.getVnfComponent());
    return new AsyncResult<>(null);
  }

  @Override
  public void createReservation(VirtualDeploymentUnit vdu) {}

  @Override
  public void queryReservation() {}

  @Override
  public void updateReservation(VirtualDeploymentUnit vdu) {}

  @Override
  public void releaseReservation(VirtualDeploymentUnit vdu) {}

  @Override
  @Async
  public Future<String> allocate(
      VirtualDeploymentUnit virtualDeploymentUnit,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent componentToAdd,
      VimInstance vimInstance,
      String userdata)
      throws InterruptedException, ExecutionException, VimException, VimDriverException,
          PluginException {
    org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim;
    vim = vimBroker.getVim(vimInstance.getType());
    log.debug("Executing allocate with Vim: " + vim.getClass().getSimpleName());
    log.debug("NAME: " + virtualNetworkFunctionRecord.getName());
    log.debug("ID: " + virtualDeploymentUnit.getId());
    String vnfc =
        allocateVNFC(
            vimInstance,
            virtualDeploymentUnit,
            virtualNetworkFunctionRecord,
            vim,
            componentToAdd,
            userdata);
    return new AsyncResult<>(vnfc);
  }
}
