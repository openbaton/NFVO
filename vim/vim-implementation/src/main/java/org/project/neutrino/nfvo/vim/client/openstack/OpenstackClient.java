package org.project.neutrino.nfvo.vim.client.openstack;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.v2_0.domain.Resource;
import org.project.neutrino.nfvo.catalogue.nfvo.Datacenter;
import org.project.neutrino.nfvo.client_interfaces.ClientInterfaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Created by mpa on 06.05.15.
 */
@Service
@Scope
class OpenstackClient implements ClientInterfaces {
    private NovaApi novaApi;
    private NeutronApi neutronApi;
    private Set<String> zones;
    private String defaultZone = null;

    public OpenstackClient() {
        //TODO get properties from configurations
        neutronApi = null;
        zones = null;
        novaApi = null;
    }

    public OpenstackClient(String user, String password, String tenant, String url) {
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
        Properties overrides = new Properties();
        overrides.setProperty(KeystoneProperties.CREDENTIAL_TYPE, CredentialTypes.PASSWORD_CREDENTIALS);
        overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");
        overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
        novaApi = ContextBuilder.newBuilder("openstack-nova").endpoint(url).credentials(tenant + ":" + user, password).modules(modules).overrides(overrides).buildApi(NovaApi.class);
        neutronApi = ContextBuilder.newBuilder("openstack-neutron").endpoint(url).credentials(tenant + ":" + user, password).modules(modules).overrides(overrides).buildApi(NeutronApi.class);
        zones = novaApi.getConfiguredRegions();
        if (null == defaultZone) {
            defaultZone = zones.iterator().next();
        }
    }

    public static String loadPropertyFromEnv(String args[],
                                             BufferedReader reader, String propertyName, String defaultValue)
            throws IOException {
        String property = System.getenv(propertyName);
        if (property == null)
            property = defaultValue;
        for (String s : args) {
            if (s.startsWith(propertyName + "=")) {
                property = s.substring(propertyName.length() + 1);
            }
        }
        while (property == null) {
            System.err.print("\nPlease provide " + propertyName + ": ");
            System.err.flush();
            property = reader.readLine();
        }
        return property;
    }

    public void setZone(String zone) {
        if (null != zone && "" == zone) {
            defaultZone = zone;
        }
    }

    public void createKeyPair(String name, String path) throws IOException {
        KeyPairApi keypairApi = this.novaApi.getKeyPairApi(
                this.defaultZone).get();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();
            keypairApi.createWithPublicKey(name, line);
        } catch (IOException e) {
            System.out.println("ERROR::Given file path is not valid.");
        } finally {
            br.close();
        }
    }

    public String launch_instance(String name, String image, String flavor,
                                  String keypair, String network, Iterable<String> secGroup,
                                  String userData) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        //CreateServerOptions options = CreateServerOptions.Builder
        //        .keyPairName(keypair).networks(network)
        //        .securityGroupNames(secGroup).userData(userData.getBytes());
        ServerCreated ser = serverApi.create(name, this.getImageId(image), this.getFlavorId(flavor));
        return ser.getId();
    }

    @Override
    public void init(Datacenter datacenter) {
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
        Properties overrides = new Properties();
        overrides.setProperty(KeystoneProperties.CREDENTIAL_TYPE, CredentialTypes.PASSWORD_CREDENTIALS);
        overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");
        overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
        novaApi = ContextBuilder.newBuilder("openstack-nova").endpoint(datacenter.getAuthUrl()).credentials(datacenter.getTenant() + ":" + datacenter.getUsername(), datacenter.getPassword()).modules(modules).overrides(overrides).buildApi(NovaApi.class);
        neutronApi = ContextBuilder.newBuilder("openstack-neutron").endpoint(datacenter.getAuthUrl()).credentials(datacenter.getTenant() + ":" + datacenter.getUsername(), datacenter.getPassword()).modules(modules).overrides(overrides).buildApi(NeutronApi.class);
        zones = novaApi.getConfiguredRegions();
        if (null == defaultZone) {
            defaultZone = zones.iterator().next();
        }
    }

    public void rebootServer(String server, RebootType type) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        serverApi.reboot(this.getServerId(server), type);
    }

    public void deleteServer(String server) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        serverApi.delete(this.getServerId(server));
    }

    public String getServerId(String server) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        try {
            Server serverObj = serverApi.get(server);
            return serverObj.getId();
        } catch (NullPointerException e) {
            for (Resource s : serverApi.list().concat()) {
                if (s.getName().equalsIgnoreCase(server))
                    return s.getId();
            }
        }
        throw new NullPointerException("Server not found");
    }

    public String getImageId(String image) {
        ImageApi imageApi = this.novaApi.getImageApi(this.defaultZone);
        try {
            Image imageObj = imageApi.get(image);
            return imageObj.getId();
        } catch (NullPointerException e) {
            for (Resource i : imageApi.list().concat()) {
                if (i.getName().equalsIgnoreCase(image))
                    return i.getId();
            }
        }
        throw new NullPointerException("Image not found");
    }

    public String getFlavorId(String flavor) {
        FlavorApi flavorApi = this.novaApi
                .getFlavorApi(this.defaultZone);
        try {
            Flavor flavorObj = flavorApi.get(flavor);
            return flavorObj.getId();
        } catch (NullPointerException e) {
            for (Resource f : flavorApi.list().concat()) {
                if (f.getName().equalsIgnoreCase(flavor))
                    return f.getId();
            }
        }
        throw new NullPointerException("Flavor not found");
    }

    public String getSecurityGroupId(String sg) {
        SecurityGroupApi securityGroupApi = novaApi
                .getSecurityGroupApi(defaultZone).get();
        try {
            SecurityGroup securityGroup = securityGroupApi.get(sg);
            return securityGroup.getId();
        } catch (Exception e) {
            Iterator<? extends SecurityGroup> sgList = securityGroupApi.list()
                    .iterator();
            while (sgList.hasNext()) {
                SecurityGroup group = sgList.next();
                if (group.getName().equalsIgnoreCase(sg))
                    return group.getId();
            }
        }
        throw new NullPointerException("Security Group not found");
    }
}
