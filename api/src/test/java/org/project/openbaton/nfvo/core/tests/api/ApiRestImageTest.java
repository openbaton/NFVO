package org.project.openbaton.nfvo.core.tests.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.common.catalogue.nfvo.NFVImage;
import org.project.openbaton.nfvo.api.RestImage;
import org.project.openbaton.nfvo.core.interfaces.NFVImageManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


public class ApiRestImageTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestImage restImage;

	@Mock
	NFVImageManagement mock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testImageFindAll() {

		log.info("" + mock.query());
		List<NFVImage> list = mock.query();
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
