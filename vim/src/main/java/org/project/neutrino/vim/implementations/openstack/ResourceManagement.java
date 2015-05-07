package org.project.neutrino.vim.implementations.openstack;

import net.schmizz.sshj.transport.cipher.NoneCipher;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.vim.implementations.openstack.OpenstackClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by mpa on 06.05.15.
 */
public class ResourceManagement implements org.project.neutrino.vim.interfaces.ResourceManagement {

    private OpenstackClient osClient;

    public ResourceManagement() throws IOException{
        String [] args = new String[0];
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        String username = OpenstackClient.loadPropertyFromEnv(args, reader,
                "OS_USERNAME", null);
        String password = OpenstackClient.loadPropertyFromEnv(args, reader,
                "OS_PASSWORD", null);
        String authUrl = OpenstackClient.loadPropertyFromEnv(args, reader,
                "OS_AUTH_URL", null);
        String tenantName = OpenstackClient.loadPropertyFromEnv(args, reader,
                "OS_TENANT_NAME", null);

        osClient = new OpenstackClient(username, password, tenantName, authUrl);
    }

    @Override
    public void allocate(VirtualDeploymentUnit vdu) {
        String s = osClient.launch_instance("test_instance","cirros","m1.small", null, null, null, null);
        //System.out.println(osClient.getImageId("cirros"));
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
