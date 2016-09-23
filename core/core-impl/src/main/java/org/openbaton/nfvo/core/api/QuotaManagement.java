package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 19/09/16.
 */
@Service
public class QuotaManagement implements org.openbaton.nfvo.core.interfaces.QuotaManagement {
  @Autowired private VimBroker vimBroker;
  @Autowired private VimRepository vimInstanceRepository;

  @Override
  public Quota getAllQuota(String projectId) throws PluginException, VimException {
    Quota result = new Quota();
    for (VimInstance vimInstance : vimInstanceRepository.findByProjectId(projectId)) {
      if (vimInstance.getType().equals("test")) {
        continue;
      }
      Quota tmp = vimBroker.getVim(vimInstance.getType()).getQuota(vimInstance);
      result.setCores(result.getCores() + tmp.getCores());
      result.setFloatingIps(result.getFloatingIps() + tmp.getFloatingIps());
      result.setInstances(result.getInstances() + tmp.getInstances());
      result.setRam(result.getRam() + tmp.getRam());
    }
    return result;
  }

  @Override
  public Quota getLeftQuota(String projectId) throws VimException, PluginException {
    Quota result = new Quota();
    for (VimInstance vimInstance : vimInstanceRepository.findByProjectId(projectId)) {
      if (vimInstance.getType().equals("test")) {
        continue;
      }
      Quota tmp = vimBroker.getLeftQuota(vimInstance);
      result.setCores(result.getCores() + tmp.getCores());
      result.setFloatingIps(result.getFloatingIps() + tmp.getFloatingIps());
      result.setInstances(result.getInstances() + tmp.getInstances());
      result.setRam(result.getRam() + tmp.getRam());
    }
    return result;
  }
}
