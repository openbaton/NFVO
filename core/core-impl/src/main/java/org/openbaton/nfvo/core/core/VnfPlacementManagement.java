package org.openbaton.nfvo.core.core;

import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.nfvo.repositories.VimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lto on 10/03/16.
 */
@Service
@Scope
public class VnfPlacementManagement
    implements org.openbaton.nfvo.core.interfaces.VnfPlacementManagement {

  @Autowired private VimRepository vimInstanceRepository;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public VimInstance choseRandom(Collection<String> vimInstanceName) {
    if (!vimInstanceName.isEmpty()) {
      String name =
          vimInstanceName.toArray(new String[0])[
              ((int) (Math.random() * 1000)) % vimInstanceName.size()];
      VimInstance vimInstance = vimInstanceRepository.findFirstByName(name);
      log.info("Chosen VimInstance: " + vimInstance.getName());
      return vimInstance;
    } else {
      Iterable<VimInstance> vimInstances = vimInstanceRepository.findAll();
      List<Iterable<VimInstance>> iterableList = Collections.singletonList(vimInstances);
      return iterableList.toArray(new VimInstance[0])[
          ((int) (Math.random() * 1000)) % iterableList.size()];
    }
  }
}
