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

package org.openbaton.nfvo.core.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.nfvo.core.api.NfvImageRepoManagement;
import org.openbaton.nfvo.repositories.NFVImageRepository;

/**
 * These tests check the functionality of the NFVImage repository. That is the image repository to
 * which users can upload images which can then be uploaded to OpenStack by the VIM driver in case a
 * requested image is not available on a VIM.
 */
public class NfvImageRepoManagementClassSuiteTest {

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @InjectMocks private NfvImageRepoManagement nfvImageRepoManagement;

  @Mock private NFVImageRepository nfvImageRepository;

  @Test
  public void nfvImageManagementNotNull() {
    Assert.assertNotNull(nfvImageRepoManagement);
  }

  /** Try adding an NFVImage with property isInImageRepo == false. */
  @Test(expected = IllegalArgumentException.class)
  public void nfvImageManagementAddTest() throws IOException {
    NFVImage nfvImage = createNfvImage();
    nfvImage.setInImageRepo(false);
    when(nfvImageRepository.save(any(NFVImage.class))).thenReturn(nfvImage);
    nfvImageRepoManagement.add(nfvImage, new byte[2]);
  }

  @Test
  public void nfvImageManagementQueryTest() {
    when(nfvImageRepository.findAllByProjectIdAndIsInImageRepoIsTrue(any(String.class)))
        .thenReturn(new ArrayList<>());

    Assert.assertEquals(false, nfvImageRepoManagement.query().iterator().hasNext());

    NFVImage nfvImage = createNfvImage();
    when(nfvImageRepository.findOneByIdAndIsInImageRepoIsTrue(nfvImage.getId()))
        .thenReturn(nfvImage);
    NFVImage nfvImageNew = nfvImageRepoManagement.queryById(nfvImage.getId());
    Assert.assertEquals(nfvImage.getId(), nfvImageNew.getId());
    //    Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
    Assert.assertEquals(nfvImage.getExtId(), nfvImageNew.getExtId());
    //    Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
  }

  private NFVImage createNfvImage() {
    NFVImage nfvImage = new NFVImage();
    nfvImage.setId("imageId");
    nfvImage.setName("image_name");
    nfvImage.setExtId("ext_id");
    nfvImage.setMinCPU("1");
    nfvImage.setMinRam(1024);
    return nfvImage;
  }
}
