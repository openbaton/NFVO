/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.catalogue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.swagger.annotations.ApiOperation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.Valid;
import org.apache.commons.io.FileUtils;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongAction;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/vnf-packages")
@ConfigurationProperties(prefix = "nfvo.marketplace.privateip")
public class RestVNFPackage {
  private String ip;

  public String getIp() {
    return this.ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFPackageManagement vnfPackageManagement;
  /** Adds a new VNFPackage to the VNFPackages repository */
  @ApiOperation(
    value = "Adding a VNFPackage",
    notes =
        "The request parameter 'file' specifies an archive which is needed to instantiate a VNFPackage. "
            + "On how to create such an archive refer to: http://openbaton.github.io/documentation/vnfpackage/"
  )
  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public String onboard(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws IOException, VimException, NotFoundException, SQLException, PluginException,
          IncompatibleVNFPackage, AlreadyExistingException, NetworkServiceIntegrityException,
          BadRequestException {

    log.debug("Onboarding");
    if (!file.isEmpty()) {

      byte[] bytes = new byte[0];
      Random random = new Random();
      int ran = random.nextInt();
      String directory = "/tmp/random" + ran + "/";

      // write the file to the filesystem
      File tempDirectory = new File(directory);
      if (!tempDirectory.exists()) {
        tempDirectory.mkdir();
      }

      String name = file.getOriginalFilename();
      File tarFile = new File(directory + name);
      file.transferTo(tarFile);
      log.debug("File size is " + file.getSize());

      Runtime rt = Runtime.getRuntime();

      try {
        // untar the file
        Process p1 =
            rt.exec(new String[] {"bash", "-c", "tar -xf " + name}, new String[0], tempDirectory);
        p1.waitFor();

        // if there is an image file in the tar file find it's name and move it
        File metadata = new File(directory + "Metadata.yaml");
        if (!metadata.exists()) {
          log.debug("No Metadata.yaml found");
          throw new Exception("no Metadata.yaml file");
        }

        // get the name of the expected image
        String metadataStr = FileUtils.readFileToString(metadata, "UTF-8");
        log.debug("metadata: " + metadataStr);
        Pattern filenameRegex = Pattern.compile("(imageFile\\s*:\\s*)(\\S*)");
        Matcher matcher = filenameRegex.matcher(metadataStr);
        String filename = "";
        if (matcher.find()) {
          filename = matcher.group(2);
        }
        log.debug("filename before replaces: " + filename);
        // just in case there are spaces or quotations
        filename = filename.replaceAll("\"", "");
        filename = filename.replaceAll("\\s", "");

        log.debug("filename: " + filename);

        if ("" != filename) {
          // see if the file with that name exists
          String randomFileName = "";
          File imageFile = new File(directory + filename);
          String targetDir = "/tmp/openbaton/";
          if (imageFile.exists()) {
            // what the heck, use the same random number (could do something else here)
            int indexDot = filename.indexOf('.');
            randomFileName = filename.substring(0, indexDot) + ran + filename.substring(indexDot);
            log.debug("random filename is: " + randomFileName);

            // make sure the folder that we are moving to exists
            File targetDirFile = new File(targetDir);
            if (!targetDirFile.exists()) {
              targetDirFile.mkdir();
            }

            // figure out if there are more than 10 files and delete if so
            Process p2a =
                rt.exec(
                    new String[] {
                      "bash", "-c", "ls " + targetDir + "* -dt | sed -e '1,10d' | xargs -d '\n' rm"
                    });
            p2a.waitFor();

            // copy the relevant file, if we copy instead of move it updates the time stamp
            // so that we can delete all but the most recently loaded files
            log.debug("cp " + directory + filename + " " + targetDir + randomFileName);
            Process p1a =
                rt.exec(
                    new String[] {
                      "bash", "-c", "cp " + directory + filename + " " + targetDir + randomFileName
                    });
            p1a.waitFor();
          }

          // also replace the name in the Metatdata.yaml file
          Pattern oldFileName = Pattern.compile(filename);
          metadataStr = oldFileName.matcher(metadataStr).replaceAll(targetDir + randomFileName);
          FileUtils.write(metadata, metadataStr);
          log.debug("metadata is now " + metadataStr);
        }

        // create a list of excluded files and delete them
        String[] fileTypes = {"iso", "img", "qcow2", "zip", "gz", "raw"};
        for (String type : fileTypes) {
          Process p = rt.exec(new String[] {"bash", "-c", "rm " + directory + "*." + type});
          p.waitFor();
        }

        String tarCmd = "tar --xform s:'./':: -C " + directory + " -cf " + directory + name + " .";
        log.debug("tar command: " + tarCmd);
        Process p3 = rt.exec(new String[] {"bash", "-c", tarCmd}, new String[0], tempDirectory);
        p3.waitFor();

        log.debug("un-tar'd and re-tar'd file");

        // now reload the file
        File outFile = new File(directory + name);
        FileInputStream is = new FileInputStream(outFile);
        bytes = new byte[(int) outFile.length()];

        is.read(bytes);
        is.close();

      } catch (Exception ex) {
        // There was an error with trying to use the file system to untar and re-tar,
        // using original file instead
        log.error(ex.getMessage());
        bytes = FileUtils.readFileToByteArray(tarFile);
      }

      try {
        // now delete the temporary file and directory
        // we use this method rather than the java provided method because we can delete the
        // entire directory at once instead of having to delete each file and then deleting
        // the directory
        Process p4 = rt.exec(new String[] {"bash", "-c", "rm -rf " + directory});
        p4.waitFor();
      } catch (Exception ex) {
        // oh well, not much we can do at this point
        log.error("Unable to delete the temporary directory: " + directory);
      }
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
          vnfPackageManagement.onboard(bytes, projectId);
      return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
    } else throw new IOException("File is empty!");
  }

  @ApiOperation(
    value = "Adding a VNFPackage from the Open Baton marketplace",
    notes =
        "The JSON object in the request body contains a field named link, which holds the URL to the package on the Open Baton Marketplace"
  )
  @RequestMapping(
    value = "/marketdownload",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public String marketDownload(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws IOException, PluginException, VimException, NotFoundException, IncompatibleVNFPackage,
          AlreadyExistingException, NetworkServiceIntegrityException, BadRequestException {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(link, JsonObject.class);
    String downloadlink = jsonObject.getAsJsonPrimitive("link").getAsString();
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfPackageManagement.onboardFromMarket(downloadlink, projectId);
    return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
  }

  /**
   * Removes the VNFPackage from the VNFPackages repository
   *
   * @param id: id of the package to delete
   */
  @ApiOperation(
    value = "Remove a VNFPackage",
    notes = "The id of the package that has to be removed in in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws WrongAction {
    vnfPackageManagement.delete(id, projectId);
  }
  /**
   * Removes multiple VNFPackage from the VNFPackages repository
   *
   * @param ids: The List of the VNFPackage Id to be deleted
   * @throws NotFoundException, WrongAction
   */
  @ApiOperation(
    value = "Removing multiple VNFPackages",
    notes = "A list of VNF Package ids has to be provided in the Request Body"
  )
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, WrongAction {
    for (String id : ids) vnfPackageManagement.delete(id, projectId);
  }

  /**
   * Returns the list of the VNFPackages available
   *
   * @return List<VNFPackage>: The list of VNFPackages available
   */
  @ApiOperation(
    value = "Retrieve all VNFPackages",
    notes = "Returns all VNF Packages onboarded on the specified project"
  )
  @RequestMapping(method = RequestMethod.GET)
  public Iterable<VNFPackage> findAll(@RequestHeader(value = "project-id") String projectId) {
    return vnfPackageManagement.queryByProjectId(projectId);
  }

  @ApiOperation(
    value = "Retrieve a script from a VNF Package",
    notes = "The ids of the package and the script are provided in the URL"
  )
  @RequestMapping(
    value = "{id}/scripts/{scriptId}",
    method = RequestMethod.GET,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String getScript(
      @PathVariable("id") String id,
      @PathVariable("scriptId") String scriptId,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    VNFPackage vnfPackage = vnfPackageManagement.query(id, projectId);
    for (Script script : vnfPackage.getScripts()) {
      if (script.getId().equals(scriptId)) {
        return new String(script.getPayload());
      }
    }
    throw new NotFoundException(
        "Script with id " + scriptId + " was not found into package with id " + id);
  }

  @ApiOperation(
    value = "Update a script of a VNF Package",
    notes = "The updated script has to be passed in the Request Body"
  )
  @RequestMapping(
    value = "{id}/scripts/{scriptId}",
    method = RequestMethod.PUT,
    produces = MediaType.TEXT_PLAIN_VALUE,
    consumes = MediaType.TEXT_PLAIN_VALUE
  )
  public String updateScript(
      @PathVariable("id") String vnfPackageId,
      @PathVariable("scriptId") String scriptId,
      @RequestBody String scriptNew,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    VNFPackage vnfPackage = vnfPackageManagement.query(vnfPackageId, projectId);
    for (Script script : vnfPackage.getScripts()) {
      if (script.getId().equals(scriptId)) {
        script.setPayload(scriptNew.getBytes());
        script = vnfPackageManagement.updateScript(script, vnfPackageId);
        return new String(script.getPayload());
      }
    }
    throw new NotFoundException(
        "Script with id " + scriptId + " was not found into package with id " + vnfPackageId);
  }

  /**
   * Returns the VNFPackage selected by id
   *
   * @param id : The id of the VNFPackage
   * @return VNFPackage: The VNFPackage selected
   */
  @ApiOperation(
    value = "Retrieve a VNFPackage",
    notes = "Returns the VNF Package corresponding to the id specified in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public VNFPackage findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    return vnfPackageManagement.query(id, projectId);
  }

  /**
   * Updates the VNFPackage
   *
   * @param vnfPackage_new : The VNFPackage to be updated
   * @param id : The id of the VNFPackage
   * @return VNFPackage The VNFPackage updated
   */
  @ApiOperation(
    value = "Update a VNFPackage",
    notes = "The updated VNF Package is passed in the request body"
  )
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VNFPackage update(
      @RequestBody @Valid VNFPackage vnfPackage_new,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId) {
    return vnfPackageManagement.update(id, vnfPackage_new, projectId);
  }
}
