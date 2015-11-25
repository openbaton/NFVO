package org.openbaton.monitoring.interfaces;

import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.plugin.PluginCaller;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 25/11/15.
 */
@Service
@Scope("prototype")
public class MonitoringPluginCaller extends MonitoringPlugin {
    public MonitoringPluginCaller() throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor.test", "localhost", "admin", "openbaton", 5672);
    }

    private PluginCaller pluginCaller;

    @Override
    public List<Item> getMeasurementResults(List<String> hostnames, List<String> metrics, String period) throws RemoteException {

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
        }
        return (List<Item>) res;
    }

    @Override
    public void notifyResults() throws RemoteException {

    }

    public static void main(String[] args) throws IOException, TimeoutException, NotFoundException {
        List hosts = new ArrayList<String>();
        hosts.add("hostname");
        System.out.println(new MonitoringPluginCaller().getMeasurementResults(hosts, new ArrayList<String>(), ""));
    }
}
