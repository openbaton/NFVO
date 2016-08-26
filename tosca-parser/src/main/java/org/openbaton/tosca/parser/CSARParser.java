package org.openbaton.tosca.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.validator.routines.UrlValidator;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.tosca.exceptions.NotFoundException;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by rvl on 26.08.16.
 */
public class CSARParser {

  private String pathUnzipFiles = "/tmp/files";
  private Set<String> vnfPackagesPaths;
  private TOSCAParser toscaParser;
  Logger log = LoggerFactory.getLogger(this.getClass());

  private String author = null;
  private String version = null;
  private String entryDefinitions = null;

  public CSARParser() {
    this.toscaParser = new TOSCAParser();
  }

  private void readMetaData() throws IOException {

    FileInputStream fstream =
        new FileInputStream(this.pathUnzipFiles + "/TOSCA-Metadata/TOSCA.meta");
    DataInputStream in = new DataInputStream(fstream);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String strLine;

    String entryDefinition = "Entry-Definitions:";
    String author = "Created-By:";
    String version = "CSAR-Version:";

    while ((strLine = br.readLine()) != null) {

      if (strLine.contains(author))
        this.author = strLine.substring(author.length(), strLine.length()).trim();
      if (strLine.contains(version))
        this.version = strLine.substring(version.length(), strLine.length()).trim();

      if (strLine.contains(entryDefinition)) {
        this.entryDefinitions =
            strLine.substring(entryDefinition.length(), strLine.length()).trim();
      }
    }

    in.close();
  }

  public List<String> getFileList(InputStream zipFile) throws Exception {

    ZipInputStream zipStream = new ZipInputStream(zipFile);

    File dir = new File(this.pathUnzipFiles);
    UrlValidator urlValidator = new UrlValidator();
    if (dir.exists()) dir.delete();
    dir.mkdir();

    List<String> fileList = new ArrayList<>();
    ZipEntry entry;

    Set<Script> scripts = new HashSet<>();

    try {
      while ((entry = zipStream.getNextEntry()) != null) {
        String currentEntry = entry.getName();

        fileList.add(currentEntry.trim());
        File destFile = new File(this.pathUnzipFiles + '/' + currentEntry, currentEntry);
        destFile = new File(this.pathUnzipFiles, destFile.getName());
        log.debug(destFile.getAbsolutePath());

        File destinationParent = destFile.getParentFile();
        String pathEntry = "" + destinationParent + '/' + currentEntry;

        if (entry.isDirectory()) {
          new File(pathEntry).mkdir();
        }

        if (!entry.isDirectory()) {
          destFile = new File(pathEntry);
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          FileOutputStream fos = new FileOutputStream(destFile.getAbsoluteFile());

          int count;
          byte[] buffer = new byte[1024];
          while ((count = zipStream.read(buffer)) != -1) {
            baos.write(buffer, 0, count);
            fos.write(buffer, 0, count);
          }

          String filename = entry.getName();
          if (destFile.isFile()
              && (!destFile.getName().endsWith(".txt")
                  && !destFile.getName().endsWith(".meta")
                  && !destFile.getName().endsWith(".yaml"))) {

            Script script = new Script();
            fileList.add(filename);
            String[] splittedName = filename.split("/");
            if (splittedName.length > 2) {
              String scriptName =
                  splittedName[splittedName.length - 2]
                      + "_"
                      + splittedName[splittedName.length - 1];
              script.setName(scriptName);

            } else script.setName(splittedName[splittedName.length - 1]);
            script.setPayload(baos.toByteArray());
            scripts.add(script);
          }

          fos.close();
          baos.close();
          zipStream.closeEntry();
        }
      }
    } finally {
      zipStream.close();
    }

    log.info("Files into CSAR: " + String.valueOf(fileList));
    if (fileList.contains("TOSCA-Metadata/TOSCA.meta"))
      log.debug("Found: /TOSCA-Metadata/TOSCA.meta");
    else throw new NotFoundException("In the csar file is missing the TOSCA-Metadata/TOSCA.meta");

    readMetaData();

    if (this.entryDefinitions == null || !fileList.contains(this.entryDefinitions))
      throw new FileNotFoundException("Error not found the file:" + this.entryDefinitions);

    //TODO:
    NSDTemplate nsdTemplate =
        Utils.fileToNSDTemplate(this.pathUnzipFiles + '/' + this.entryDefinitions);
    NetworkServiceDescriptor nsd = toscaParser.parseNSDTemplate(nsdTemplate);
    System.out.println(craeteVNFPackages(nsd));

    return fileList;
  }

  private Set<String> craeteVNFPackages(NetworkServiceDescriptor nsd) throws IOException {
    this.vnfPackagesPaths = new HashSet<>();
    for (VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()) {

      Metadata metadata = new Metadata();
      metadata.setName(vnfd.getType());
      Image image = metadata.getImage();
      image.setUpload("false");

      Set<VirtualDeploymentUnit> vdus = vnfd.getVdu();
      for (VirtualDeploymentUnit vdu : vdus) {
        for (String imageString : vdu.getVm_image())
          if (!image.getNames().contains(imageString)) image.getNames().add(imageString);
      }
      metadata.setImage(image);

      Constructor constructor = new Constructor(Metadata.class);
      TypeDescription typeDescription = new TypeDescription(Metadata.class);
      constructor.addTypeDescription(typeDescription);

      Writer jsonWriter = new FileWriter(this.pathUnzipFiles + "/vndf.json");
      Gson gson = new GsonBuilder().create();
      gson.toJson(vnfd, jsonWriter);
      jsonWriter.close();

      Utils.addFileToFolder(this.pathUnzipFiles + "/vndf.json", vnfd.getName() + "/");
      Yaml yaml = new Yaml(constructor);
      Writer metadataFile = new FileWriter(this.pathUnzipFiles + "/Metadata.yaml");
      yaml.dump(metadata, metadataFile);
      metadataFile.close();
      Utils.addFileToFolder(this.pathUnzipFiles + "/Metadata.yaml", vnfd.getName() + "/");

      for (LifecycleEvent lifecycleEvents : vnfd.getLifecycle_event()) {

        for (String event : lifecycleEvents.getLifecycle_events()) {
          Utils.addFileToFolder(
              this.pathUnzipFiles + "/Scripts/" + event, vnfd.getName() + "/scripts/");
          System.out.println(this.pathUnzipFiles + "/" + event);
        }
      }

      File directory = new File(vnfd.getName());
      File tar = new File(vnfd.getName() + ".tar");
      Utils.createTar(directory, tar);
      //            log.debug(String.valueOf(this.vnfPackagesPaths));
      this.vnfPackagesPaths.add(tar.getAbsolutePath());
    }
    return this.vnfPackagesPaths;
  }
}
