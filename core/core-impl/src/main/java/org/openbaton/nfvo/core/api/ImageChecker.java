/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.core.api;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.openbaton.catalogue.nfvo.ImageStatus;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

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
  public void checkImageStatus(BaseVimInstance vimInstance)
      throws InterruptedException, IOException, VimException, PluginException, ExecutionException {

    for (int i = 0; i < imageStatusTimeout; i++) {

      java.lang.Thread.sleep(imageStatusTimeoutDelay * 1000);

      boolean allImagesActive = true;
      Set<BaseNfvImage> images = vimManagement.queryImagesDirectly(vimInstance);

      for (BaseNfvImage image : images) {
        if (image instanceof NFVImage) {
          NFVImage nfvImage = (NFVImage) image;
          if (nfvImage.getStatus().equals(ImageStatus.QUEUED)
              || nfvImage.getStatus().equals(ImageStatus.SAVING)) {

            log.debug("Image " + nfvImage.getName() + " is still not active");
            allImagesActive = false;
          }
        }
      }

      if (allImagesActive) {
        log.info("All images are active");
        vimManagement.refresh(vimInstance, false).get();
        return;
      }
    }
    vimManagement.refresh(vimInstance, false).get();
    throw new VimException("Not all images are active even after timeout!");
  }
}
