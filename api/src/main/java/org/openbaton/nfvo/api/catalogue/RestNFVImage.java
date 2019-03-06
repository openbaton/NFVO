package org.openbaton.nfvo.api.catalogue;

import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.core.interfaces.NfvImageRepoManagement;
import org.openbaton.nfvo.security.config.OAuth2AuthorizationServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Created by tbr on 27.11.18. */
@RestController
@RequestMapping("/api/v1/nfvimage")
public class RestNFVImage {
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NfvImageRepoManagement nfvImageRepoManagement;

  @Autowired private OAuth2AuthorizationServerConfig securityServerConfig;

  @Value("${nfvo.nfvimagedir.path:/etc/openbaton/nfvImages/}")
  private String nfvImageDirPath;

  /**
   * Upload an NFVImage (OpenStack) to the NFVO image repository.
   *
   * @param imageFile the image file which will be uploaded; has to be null if an image URL is
   *     specified
   * @param name the image name
   * @param minRam the minimum amount of RAM in MB
   * @param minDiskSpace the minimum amount of storage in GB
   * @param minCPU the minimum amount of CPUs
   * @param isPublic shall the image be public or not when uploaded to a VIM (OpenStack)
   * @param diskFormat the disk format (e.g. QCOW2)
   * @param containerFormat the container format (e.g. BARE)
   * @param url a URL pointing to a location from where the image can be retrieved; has to be null
   *     if an image file is uploaded
   * @param projectId the project ID
   * @return the created NFVImage object
   * @throws BadRequestException thrown if request parameters are used incorrectly
   */
  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(
      value = "Adding an NFVImage to the NFVO's NFV image repository",
      notes = "POST request with NFVImage properties as request parameters")
  public NFVImage create(
      @RequestParam(value = "file", required = false) MultipartFile imageFile,
      @RequestParam(value = "name") String name,
      @RequestParam(value = "minRam") Long minRam,
      @RequestParam(value = "minDiskSpace") Long minDiskSpace,
      @RequestParam(value = "minCPU") String minCPU,
      @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,
      @RequestParam(value = "diskFormat") String diskFormat,
      @RequestParam(value = "containerFormat") String containerFormat,
      @RequestParam(value = "url", required = false) String url,
      @RequestHeader(value = "project-id") String projectId)
      throws BadRequestException, IOException, AlreadyExistingException {

    if (imageFile == null && url == null)
      throw new BadRequestException(
          "You have to specify a URL from where the image is available or upload an image file");
    if (imageFile != null && url != null)
      throw new BadRequestException(
          "Please either specify the image URL or upload an image file, but not both");

    NFVImage nfvImage = new NFVImage();

    nfvImage.setName(name);
    nfvImage.setMinRam(minRam);
    nfvImage.setMinDiskSpace(minDiskSpace);
    nfvImage.setMinCPU(minCPU);
    nfvImage.setIsPublic(isPublic);
    nfvImage.setDiskFormat(diskFormat);
    nfvImage.setContainerFormat(containerFormat);
    nfvImage.setUrl(url);
    nfvImage.setInImageRepo(true);
    nfvImage.setProjectId(projectId);
    nfvImage.setExtId(null);
    nfvImage.setId(null);
    nfvImage.setHbVersion(null);

    if (imageFile != null) {
      if (imageFile.isEmpty()) throw new BadRequestException("The provided image file is empty");
      byte[] bytes;
      try {
        bytes = imageFile.getBytes();
      } catch (IOException e) {
        throw new IOException("Unable to read from the provided image file: " + e.getMessage(), e);
      }
      return nfvImageRepoManagement.add(nfvImage, bytes, projectId);
    }

    return nfvImageRepoManagement.add(nfvImage, projectId);
  }

  @ApiOperation(
      value = "Get all NFVImages from a project",
      notes = "Returns all NFVImages onboarded in the project with the specified ID")
  @RequestMapping(method = RequestMethod.GET)
  public List<NFVImage> findAll(@RequestHeader(value = "project-id") String projectId) {
    return (List<NFVImage>) nfvImageRepoManagement.queryByProjectId(projectId);
  }

  @ApiOperation(
      value = "Delete an NFVImage from the NFV image repository",
      notes = "DELETE request for removing the NFVImage with the passed ID")
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    nfvImageRepoManagement.delete(id, projectId);
  }

  /**
   * This operation returns the image file associated with an NFVImage from the image repository.
   *
   * @param imageId of NFVImage
   * @return byte[]: the image file
   */
  @ApiOperation(value = "Get file associated to NFVImage", notes = "Returns the image file")
  @RequestMapping(
      value = "/file/{imageId}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public byte[] getImageFile(
      @PathVariable("imageId") String imageId, @RequestParam("token") String token)
      throws NotFoundException, IOException {

    if (!securityServerConfig.validateImageToken(token, imageId))
      throw new AccessDeniedException("Invalid token for retrieving the image file");
    return nfvImageRepoManagement.getImageFileOfNfvImage(imageId);
  }

  /**
   * This operation returns the NFVImage from the image repository specified by its ID.
   *
   * @param id of NFVImage
   * @return the NFVImage from the image repository
   */
  @ApiOperation(value = "Get an NFVImage from the image repository", notes = "Returns the NFVImage")
  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public NFVImage findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NFVImage image = nfvImageRepoManagement.queryByIdAndProjectId(id, projectId);
    if (image == null)
      throw new NotFoundException(
          "No NFVImage with ID " + id + " found in image repository (project: " + projectId + ")");
    return image;
  }
}
