package org.openbaton.nfvo.core.api;

import java.io.IOException;
import java.util.Set;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/** Created by rvl on 10.05.17. */
@Service
@Scope
@EnableAsync
@ConfigurationProperties
public class ImageChecker {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.image.check.timeout:150}")
  private int imageStatusTimeout;

  @Value("${nfvo.image.check.timeout.delay:10}")
  private int imageStatusTimeoutDelay;

  @Autowired private VimManagement vimManagement;

  @Async
  public void checkImageStatus(VimInstance vimInstance)
      throws InterruptedException, AlreadyExistingException, IOException, BadRequestException,
          VimException, PluginException {

    for (int i = 0; i < imageStatusTimeout; i++) {

      java.lang.Thread.sleep(imageStatusTimeoutDelay * 1000);

      boolean allImagesActive = true;
      Set<NFVImage> images = vimManagement.queryImagesDirectly(vimInstance);

      for (NFVImage image : images) {
        if (image.getStatus().equals(NFVImage.ImageStatus.QUEUED)
            || image.getStatus().equals(NFVImage.ImageStatus.SAVING)) {

          log.debug("Image " + image.getName() + " is still not active");
          allImagesActive = false;
        }
      }

      if (allImagesActive) {
        log.info("All images are active");
        vimManagement.refresh(vimInstance);
        return;
      }
    }
    vimManagement.refresh(vimInstance);
    throw new VimException("Not all images are active even after timeout!");
  }
}
