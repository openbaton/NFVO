package org.openbaton.nfvo.common.utils.viminstance;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openbaton.catalogue.nfvo.ImageStatus;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.DockerImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.DockerNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.networks.Subnet;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.DockerVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.BadRequestException;

public class VimInstanceUtils {
  public static void handlePrivateInfo(BaseVimInstance vim) {
    if (vim.getClass().getCanonicalName().equals(OpenstackVimInstance.class.getCanonicalName())) {
      ((OpenstackVimInstance) vim).setPassword("**********");
    } else if (vim.getClass()
        .getCanonicalName()
        .equals(DockerVimInstance.class.getCanonicalName())) {
      ((DockerVimInstance) vim).setCa("**********");
      ((DockerVimInstance) vim).setDockerKey("**********");
      ((DockerVimInstance) vim).setCert("**********");
    }
  }

  public static void updatePrivateInfo(BaseVimInstance vimNew, BaseVimInstance vimOld) {
    if (vimNew
        .getClass()
        .getCanonicalName()
        .equals(OpenstackVimInstance.class.getCanonicalName())) {
      ((OpenstackVimInstance) vimNew).setPassword(((OpenstackVimInstance) vimOld).getPassword());
    } else if (vimNew
        .getClass()
        .getCanonicalName()
        .equals(DockerVimInstance.class.getCanonicalName())) {
      ((DockerVimInstance) vimNew).setCa(((DockerVimInstance) vimOld).getCa());
      ((DockerVimInstance) vimNew).setDockerKey(((DockerVimInstance) vimOld).getDockerKey());
      ((DockerVimInstance) vimNew).setCert(((DockerVimInstance) vimOld).getCert());
    }
  }

  public static void updateNfvImage(BaseNfvImage nfvImageOld, BaseNfvImage nfvImageNew) {
    nfvImageOld.setCreated(nfvImageNew.getCreated());
    if (NFVImage.class.isInstance(nfvImageNew)) {
      NFVImage osImageNew = (NFVImage) nfvImageNew;
      NFVImage osImageOld = (NFVImage) nfvImageOld;
      osImageOld.setName(osImageNew.getName());
      osImageOld.setIsPublic(osImageNew.isPublic());
      osImageOld.setMinRam(osImageNew.getMinRam());
      osImageOld.setMinCPU(osImageNew.getMinCPU());
      osImageOld.setMinDiskSpace(osImageNew.getMinDiskSpace());
      osImageOld.setDiskFormat(osImageNew.getDiskFormat());
      osImageOld.setContainerFormat(osImageNew.getContainerFormat());

      osImageOld.setUpdated(osImageNew.getUpdated());
      ImageStatus imageStatus = osImageNew.getStatus();
      if (imageStatus != null) {
        osImageOld.setStatus(imageStatus.toString());
      } else {
        osImageOld.setStatus(ImageStatus.ACTIVE.toString());
      }
    } else if (DockerImage.class.isInstance(nfvImageNew)) {
      DockerImage dockerImageNew = (DockerImage) nfvImageNew;
      DockerImage dockerImageOld = (DockerImage) nfvImageOld;
      dockerImageOld.setTags(dockerImageNew.getTags());
    }
  }

  public static void updateBaseNetworks(BaseNetwork networkOld, BaseNetwork networkNew)
      throws BadRequestException {

    if (Network.class.isInstance(networkOld)) {
      Network osNetworkOld = (Network) networkOld;
      Network osNetworkNew = (Network) networkNew;
      osNetworkOld.setName(osNetworkNew.getName());
      osNetworkOld.setExternal(osNetworkNew.getExternal());
      osNetworkOld.setExtShared(osNetworkNew.getExternal());
      Set<Subnet> subnets_refreshed = new HashSet<>();
      Set<Subnet> subnetsNew = new HashSet<>();
      Set<Subnet> subnetsOld = new HashSet<>();
      if (osNetworkNew.getSubnets() == null) {
        throw new BadRequestException("New network: " + osNetworkNew.getName() + " has no subnets");
      } else if (osNetworkNew.getSubnets() == null) {
        osNetworkNew.setSubnets(new HashSet<Subnet>());
      }
      subnets_refreshed.addAll(osNetworkNew.getSubnets());
      if (osNetworkOld.getSubnets() == null) {
        osNetworkOld.setSubnets(new HashSet<Subnet>());
      }
      for (Subnet subnetNew : subnets_refreshed) {
        boolean found_subnet = false;
        for (Subnet subnetNfvo : osNetworkOld.getSubnets()) {
          if (subnetNfvo.getExtId().equals(subnetNew.getExtId())) {
            subnetNfvo.setName(subnetNew.getName());
            subnetNfvo.setNetworkId(subnetNew.getNetworkId());
            subnetNfvo.setGatewayIp(subnetNew.getGatewayIp());
            subnetNfvo.setCidr(subnetNew.getCidr());
            found_subnet = true;
            break;
          }
        }
        if (!found_subnet) {
          subnetsNew.add(subnetNew);
        }
      }
      for (Subnet subnetNfvo : osNetworkOld.getSubnets()) {
        boolean foundSubnet = false;
        for (Subnet subnet_new : subnets_refreshed) {
          if (subnetNfvo.getExtId().equals(subnet_new.getExtId())) {
            foundSubnet = true;
            break;
          }
        }
        if (!foundSubnet) {
          subnetsOld.add(subnetNfvo);
        }
      }
      osNetworkOld.getSubnets().addAll(subnetsNew);
      osNetworkOld.getSubnets().removeAll(subnetsOld);
    } else if (DockerNetwork.class.isInstance(networkOld)) {
      DockerNetwork dockerNetworkOld = (DockerNetwork) networkOld;
      DockerNetwork dockerNetworkNew = (DockerNetwork) networkNew;

      dockerNetworkOld.setDriver(dockerNetworkNew.getDriver());
      dockerNetworkOld.setDriver(dockerNetworkNew.getGateway());
      dockerNetworkOld.setScope(dockerNetworkNew.getScope());
      dockerNetworkOld.setSubnet(dockerNetworkNew.getSubnet());
    }
  }

  public static Collection<BaseNfvImage> findActiveImagesByName(
      BaseVimInstance vimInstance, String imageName) {
    Stream<? extends BaseNfvImage> stream =
        vimInstance.getImages().stream().filter(i -> i.getExtId().equals(imageName));
    if (stream.count() > 0) {
      return stream.collect(Collectors.toList());
    }

    if (vimInstance instanceof OpenstackVimInstance) {
      return ((OpenstackVimInstance) vimInstance)
          .getImages()
          .stream()
          .filter(i -> ((NFVImage) i).getName().equals(imageName))
          .collect(Collectors.toList());
    } else if (vimInstance instanceof DockerVimInstance) {
      return ((DockerVimInstance) vimInstance)
          .getImages()
          .stream()
          .filter(i -> ((DockerImage) i).getTags().contains(imageName))
          .collect(Collectors.toList());
    }
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
