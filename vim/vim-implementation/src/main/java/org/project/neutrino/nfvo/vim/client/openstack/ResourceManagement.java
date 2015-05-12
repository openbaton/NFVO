package org.project.neutrino.nfvo.vim.client.openstack;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Future;


/**
 * Created by mpa on 06.05.15.
 */
@Service
@Scope
public class ResourceManagement implements org.project.neutrino.nfvo.vim_interfaces.ResourceManagement {

    @Autowired
    private OpenstackClient osClient;

    public ResourceManagement() throws IOException{
        //TODO get info from the configuration
//        String [] args = new String[0];
//        BufferedReader reader = new BufferedReader(new InputStreamReader(
//                System.in));
//        String username = OpenstackClient.loadPropertyFromEnv(args, reader,
//                "OS_USERNAME", null);
//        String password = OpenstackClient.loadPropertyFromEnv(args, reader,
//                "OS_PASSWORD", null);
//        String authUrl = OpenstackClient.loadPropertyFromEnv(args, reader,
//                "OS_AUTH_URL", null);
//        String tenantName = OpenstackClient.loadPropertyFromEnv(args, reader,
//                "OS_TENANT_NAME", null);

//        osClient = new OpenstackClient(username, password, tenantName, authUrl);
    }

    @Override
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        String s = osClient.launch_instance("test_instance","cirros","m1.small", null, null, null, null);
        //System.out.println(osClient.getImageId("cirros"));
        return new AsyncResult<String>(s);
    }

    @Override
    public void query() {

    }

    @Override
    public void update(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void scale(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void migrate(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void operate(VirtualDeploymentUnit vdu, String operation) {

    }

    @Override
    public void release(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void createReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void queryReservation() {

    }

    @Override
    public void updateReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void releaseReservation(VirtualDeploymentUnit vdu) {

    }
}
