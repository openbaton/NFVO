/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

package org.openbaton.nfvo.core.tests.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.nfvo.api.RestImage;
import org.openbaton.nfvo.core.interfaces.NFVImageManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


public class ApiRestImageTest {

    @InjectMocks
    RestImage restImage;
    @Mock
    NFVImageManagement mock;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findAllImage() {
        log.info("" + mock.query());
        Iterable<NFVImage> list = mock.query();
        when(mock.query()).thenReturn(list);
        assertEquals(list, restImage.findAll());
    }

    @Test
    public void testImageCreate() {
        NFVImage image = new NFVImage();
        image.setId("123");
        image.setMinCPU("1");
        image.setMinRam(1000);
        image.setName("Image_test");
        when(mock.add(image)).thenReturn(image);
        log.info("" + restImage.create(image));
        NFVImage image2 = restImage.create(image);
        assertEquals(image, image2);
    }

    @Test
    public void testImageFindBy() {
        NFVImage image = new NFVImage();
        image.setId("123");
        image.setMinCPU("1");
        image.setMinRam(1000);
        image.setName("Image_test");
        when(mock.query(image.getId())).thenReturn(image);
        assertEquals(image, restImage.findById(image.getId()));
    }

    @Test
    public void testImageUpdate() {
        NFVImage image = new NFVImage();
        image.setId("123");
        image.setMinCPU("1");
        image.setMinRam(1000);
        image.setName("Image_test");
        when(mock.update(image, image.getId())).thenReturn(image);
        assertEquals(image, restImage.update(image, image.getId()));
    }

    @Test
    public void testImageDelete() {
        mock.delete("123");
        restImage.delete("123");
    }
}
