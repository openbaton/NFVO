package org.openbaton.monitoring.interfaces;

import org.openbaton.catalogue.mano.common.monitoring.*;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.plugin.PluginCaller;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lto on 25/11/15.
 */
@Service
@Scope("prototype")
public class MonitoringPluginCaller extends MonitoringPlugin {
    public MonitoringPluginCaller(String type) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor." + type, "localhost", "admin", "openbaton", 5672);
    }

    public MonitoringPluginCaller(String name, String type) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor." + type +"."+name, "localhost", "admin", "openbaton", 5672);
    }

    public MonitoringPluginCaller(String brokerIp, int port, String type) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor." + type, brokerIp, "admin", "openbaton", port);
    }

    public MonitoringPluginCaller(String brokerIp, String username, String password, String type) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor."+ type, brokerIp, username, password, 5672);
    }

    private PluginCaller pluginCaller;

    /*@Override
    public List<Item> getMeasurementResults(List<String> hostnames, List<String> metrics, String period) throws RemoteException, MonitoringException {

        List<Serializable> params = new ArrayList<>();
        params.add((Serializable) hostnames);
        params.add((Serializable) metrics);
        params.add(period);
        Serializable res = null;
        try {


            res = pluginCaller.executeRPC("getMeasurementResults", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<Item>) res;
    }

    @Override
    public void notifyResults() throws RemoteException, MonitoringException {

        try {
            pluginCaller.executeRPC("notifyResults", null, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }

    }*/

    public static void main(String[] args) throws IOException, TimeoutException, NotFoundException, ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        class Exec implements Callable<Object>{

            @Override
            public Object call() throws Exception {
                List<String> hosts = new ArrayList<>();
                hosts.add("hostname1");
                hosts.add("hostname2");
                return null/* new MonitoringPluginCaller("zabbix").getMeasurementResults(hosts, new ArrayList<String>(), "")*/;
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
        System.out.println("Time ---> " + (new Date().getTime() - time)/1000);

    }

    @Override
    public String subscribeForFault(AlarmEndpoint filter) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(filter);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("subscribeForFault", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
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
            res = pluginCaller.executeRPC("unsubscribeForFault", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (String) res;
    }

    @Override
    public void notifyFault(AlarmEndpoint endpoint, AbstractVirtualizedResourceAlarm event) throws MonitoringException {
        //It must not be called from a consumer
    }

    @Override
    public List<Alarm> getAlarmList(String vnfId, PerceivedSeverity perceivedSeverity) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(vnfId);
        params.add(perceivedSeverity);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("getAlarmList", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<Alarm>) res;
    }

    @Override
    public String createPMJob(ObjectSelection resourceSelector, List<String> performanceMetric, List<String> performanceMetricGroup, Integer collectionPeriod, Integer reportingPeriod) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(resourceSelector);
        params.add((Serializable) performanceMetric);
        params.add((Serializable) performanceMetricGroup);
        params.add(collectionPeriod);
        params.add(reportingPeriod);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("createPMJob", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (String) res;
    }

    @Override
    public List<String> deletePMJob(List<String> itemIdsToDelete) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add((Serializable)itemIdsToDelete);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("deletePMJob", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<String>) res;
    }

    @Override
    public List<Item> queryPMJob(List<String> hostnames, List<String> metrics, String period) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add((Serializable) hostnames);
        params.add((Serializable) metrics);
        params.add(period);
        Serializable res = null;
        try {


            res = pluginCaller.executeRPC("queryPMJob", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
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
    public String createThreshold(List<ObjectSelection> objectSelectors, String performanceMetric, ThresholdType thresholdType, ThresholdDetails thresholdDetails) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add((Serializable) objectSelectors);
        params.add(performanceMetric);
        params.add(thresholdType);
        params.add(thresholdDetails);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("createThreshold", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (String) res;
    }

    @Override
    public List<String> deleteThreshold(List<String> thresholdIds) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add((Serializable)thresholdIds);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("deleteThreshold", params, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<String>) res;
    }

    @Override
    public void queryThreshold(String queryFilter) {
        //not yet implemented
    }
}
