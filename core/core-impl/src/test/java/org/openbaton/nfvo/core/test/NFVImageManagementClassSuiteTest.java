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

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.nfvo.core.api.NFVImageManagement;
import org.openbaton.nfvo.repositories.ImageRepository;

/** Created by lto on 20/04/15. */
public class NFVImageManagementClassSuiteTest {

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @InjectMocks private NFVImageManagement nfvImageManagement;

  @Mock private ImageRepository imageRepository;

  @Test
  public void nfvImageManagementNotNull() {
    Assert.assertNotNull(nfvImageManagement);
  }

  @Test
  public void nfvImageManagementUpdateTest() {
    NFVImage nfvImage_exp = createNfvImage();

    NFVImage nfvImage_new = createNfvImage();
    nfvImage_new.setName("UpdatedName");
    nfvImage_new.setMinRam(2046);
    when(imageRepository.save(any(NFVImage.class))).thenReturn(nfvImage_new);
    nfvImage_exp = nfvImageManagement.update(nfvImage_new, nfvImage_exp.getId());

    Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
    Assert.assertEquals(nfvImage_exp.getExtId(), nfvImage_new.getExtId());
    Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
  }

  @Test
  public void nfvImageManagementCopyTest() {
    exception.expect(UnsupportedOperationException.class);
    nfvImageManagement.copy();
  }

  @Test
  public void nfvImageManagementAddTest() {
    NFVImage nfvImage_exp = createNfvImage();
    when(imageRepository.save(any(NFVImage.class))).thenReturn(nfvImage_exp);
    NFVImage nfvImage_new = nfvImageManagement.add(nfvImage_exp);

    Assert.assertEquals(nfvImage_exp.getId(), nfvImage_new.getId());
    Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
    Assert.assertEquals(nfvImage_exp.getExtId(), nfvImage_new.getExtId());
    Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
  }

  @Test
  public void nfvImageManagementQueryTest() {
    when(imageRepository.findAll()).thenReturn(new ArrayList<>());

    Assert.assertEquals(false, nfvImageManagement.query().iterator().hasNext());

    NFVImage nfvImage_exp = createNfvImage();
    when(imageRepository.findOne(nfvImage_exp.getId())).thenReturn(nfvImage_exp);
    BaseNfvImage nfvImage_new = nfvImageManagement.query(nfvImage_exp.getId());
    Assert.assertEquals(nfvImage_exp.getId(), nfvImage_new.getId());
    //    Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
    Assert.assertEquals(nfvImage_exp.getExtId(), nfvImage_new.getExtId());
    //    Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
  }

  @Test
  public void nfvImageManagementDeleteTest() {
    BaseNfvImage nfvImage_exp = createNfvImage();
    when(imageRepository.findOne(nfvImage_exp.getId())).thenReturn(nfvImage_exp);
    nfvImageManagement.delete(nfvImage_exp.getId());
    when(imageRepository.findOne(nfvImage_exp.getId())).thenReturn(null);
    BaseNfvImage nfvImage_new = nfvImageManagement.query(nfvImage_exp.getId());
    Assert.assertNull(nfvImage_new);
  }

  private NFVImage createNfvImage() {
    NFVImage nfvImage = new NFVImage();
    nfvImage.setName("image_name");
    nfvImage.setExtId("ext_id");
    nfvImage.setMinCPU("1");
    nfvImage.setMinRam(1024);
    return nfvImage;
  }
}
