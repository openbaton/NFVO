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

package org.openbaton.monitoring.interfaces;

import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.openbaton.catalogue.mano.common.monitoring.AbstractVirtualizedResourceAlarm;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.AlarmEndpoint;
import org.openbaton.catalogue.mano.common.monitoring.ObjectSelection;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.catalogue.mano.common.monitoring.ThresholdDetails;
import org.openbaton.catalogue.mano.common.monitoring.ThresholdType;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.plugin.utils.PluginCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 25/11/15. */
@Service
@Scope("prototype")
public class MonitoringPluginCaller extends MonitoringPlugin {

  private PluginCaller pluginCaller;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  public MonitoringPluginCaller(
      String brokerIp,
      String username,
      String password,
      int port,
      String type,
      String name,
      String managementPort,
      int pluginTimeout)
      throws IOException, TimeoutException, NotFoundException {
    log.trace("Creating PluginCaller");
    if (name == null) {
      name = "";
    }
    pluginCaller =
        new PluginCaller(
            "monitor." + type + "." + name,
            brokerIp,
            username,
            password,
            port,
            Integer.parseInt(managementPort),
            pluginTimeout);
  }

  public void stop() throws Exception {
    pluginCaller.close();
  }

  public static void main(String[] args)
      throws IOException, TimeoutException, NotFoundException, ExecutionException,
          InterruptedException {

    ExecutorService executor = Executors.newFixedThreadPool(3);

    class Exec implements Callable<Object> {

      @Override
      public Object call() throws Exception {
        List<String> hosts = new ArrayList<>();
        hosts.add("hostname1");
        hosts.add("hostname2");
        return null /* new MonitoringPluginCaller("zabbix").getMeasurementResults(hosts, new ArrayList<String>(), "")*/;
      }
    }

    long time = new Date().getTime();
    System.out.println("Time ---> " + time);
    Future fut1 = executor.submit(new Exec());
    Future fut2 = executor.submit(new Exec());

    System.out.println("2nd call");
    System.out.println(fut2.get());

    System.out.println("1st call");
    System.out.println(fut1.get());
    System.out.println("Time ---> " + (new Date().getTime() - time) / 1000);
  }

  @Override
  public String subscribeForFault(AlarmEndpoint filter) throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add(filter);

    Serializable res = null;
    try {
      res = pluginCaller.executeRPC("subscribeForFault", params, String.class);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (String) res;
  }

  @Override
  public String unsubscribeForFault(String alarmEndpointId) throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add(alarmEndpointId);

    Serializable res = null;
    try {
      res = pluginCaller.executeRPC("unsubscribeForFault", params, String.class);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (String) res;
  }

  @Override
  public void notifyFault(AlarmEndpoint endpoint, AbstractVirtualizedResourceAlarm event) {
    //It must not be called from a consumer
  }

  @Override
  public List<Alarm> getAlarmList(String vnfId, PerceivedSeverity perceivedSeverity)
      throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add(vnfId);
    params.add(perceivedSeverity);

    Serializable res = null;
    try {
      Type listType = new TypeToken<ArrayList<Alarm>>() {}.getType();
      res = pluginCaller.executeRPC("getAlarmList", params, listType);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (List<Alarm>) res;
  }

  @Override
  public String createPMJob(
      ObjectSelection resourceSelector,
      List<String> performanceMetric,
      List<String> performanceMetricGroup,
      Integer collectionPeriod,
      Integer reportingPeriod)
      throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add(resourceSelector);
    params.add((Serializable) performanceMetric);
    params.add((Serializable) performanceMetricGroup);
    params.add(collectionPeriod);
    params.add(reportingPeriod);

    Serializable res = null;
    try {
      res = pluginCaller.executeRPC("createPMJob", params, String.class);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (String) res;
  }

  @Override
  public List<String> deletePMJob(List<String> itemIdsToDelete) throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add((Serializable) itemIdsToDelete);

    Serializable res = null;
    Type listType = new TypeToken<ArrayList<String>>() {}.getType();
    try {
      res = pluginCaller.executeRPC("deletePMJob", params, listType);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (List<String>) res;
  }

  @Override
  public List<Item> queryPMJob(List<String> hostnames, List<String> metrics, String period)
      throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add((Serializable) hostnames);
    params.add((Serializable) metrics);
    params.add(period);
    Serializable res = null;
    try {

      Type listType = new TypeToken<ArrayList<Item>>() {}.getType();
      res = pluginCaller.executeRPC("queryPMJob", params, listType);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (List<Item>) res;
  }

  @Override
  public void subscribe() {
    //not yet implemented
  }

  @Override
  public void notifyInfo() {
    //not yet implemented
  }

  @Override
  public String createThreshold(
      ObjectSelection objectSelector,
      String performanceMetric,
      ThresholdType thresholdType,
      ThresholdDetails thresholdDetails)
      throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add(objectSelector);
    params.add(performanceMetric);
    params.add(thresholdType);
    params.add(thresholdDetails);

    Serializable res = null;
    try {
      res = pluginCaller.executeRPC("createThreshold", params, String.class);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (String) res;
  }

  @Override
  public List<String> deleteThreshold(List<String> thresholdIds) throws MonitoringException {
    List<Serializable> params = new ArrayList<>();
    params.add((Serializable) thresholdIds);

    Serializable res = null;
    try {
      Type listType = new TypeToken<ArrayList<String>>() {}.getType();
      res = pluginCaller.executeRPC("deleteThreshold", params, listType);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      throw new MonitoringException(e.getMessage());
    }
    return (List<String>) res;
  }

  @Override
  public void queryThreshold(String queryFilter) {
    //not yet implemented
  }
}
