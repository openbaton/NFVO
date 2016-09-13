package org.openbaton.tosca.parser;

import com.google.gson.Gson;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.tosca.Metadata.Metadata;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.openbaton.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by rvl on 12.09.16.
 */
@Service
public class CSARParser {

  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VNFPackageManagement vnfPackageManagement;

  private TOSCAParser toscaParser;

  private Set<Script> scripts = new HashSet<>();
  private ByteArrayOutputStream metadata;
  private ByteArrayOutputStream template;
  private ArrayList<String> image_names = new ArrayList<>();

  private String author = null;
  private String version = null;
  private String entryDefinitions = null;

  public CSARParser() {
    this.toscaParser = new TOSCAParser();
  }

  /*
   *
   * Helper functions - Reading a csar and creating a proper vnf package
   *
   */

  private void readFiles(InputStream csar_file) throws IOException {

    ZipInputStream zipStream = new ZipInputStream(csar_file);
    ZipEntry entry;
    this.scripts.clear();

    while ((entry = zipStream.getNextEntry()) != null) {

      if (!entry.isDirectory()) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int count;
        byte[] buffer = new byte[1024];
        while ((count = zipStream.read(buffer)) != -1) {
          baos.write(buffer, 0, count);
        }

        String file_name = entry.getName();

        if (file_name.endsWith(".meta")) {
          this.metadata = baos;
        } else if (file_name.endsWith(".yaml")) {
          this.template = baos;
        } else {

          Script script = new Script();
          String[] splittedName = file_name.split("/");
          if (splittedName.length > 2) {
            String scriptName =
                splittedName[splittedName.length - 2] + "_" + splittedName[splittedName.length - 1];
            script.setName(scriptName);

          } else script.setName(splittedName[splittedName.length - 1]);

          script.setPayload(baos.toByteArray());
          this.scripts.add(script);
        }
      }
    }
  }

  private void readMetaData() throws IOException {

    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(this.metadata.toByteArray())));
    String strLine;

    String entryDefinition = "Entry-Definitions:";
    String author = "Created-By:";
    String version = "CSAR-Version:";
    String image = "image:";

    image_names.clear();

    while ((strLine = br.readLine()) != null) {

      if (strLine.contains(author))
        this.author = strLine.substring(author.length(), strLine.length()).trim();
      if (strLine.contains(version))
        this.version = strLine.substring(version.length(), strLine.length()).trim();

      if (strLine.contains(entryDefinition)) {
        this.entryDefinitions =
            strLine.substring(entryDefinition.length(), strLine.length()).trim();
      }

      if (strLine.contains(image)) {
        this.image_names.add(strLine.substring(image.length(), strLine.length()).trim());
      }
    }
  }

  private void writeMetadata(Metadata metadata, ArchiveOutputStream my_tar_ball)
      throws IOException {

    File tar_input_file = File.createTempFile("Metadata", null);
    Map<String, Object> data = new HashMap<>();

    data.put("image", metadata.getImage().toHashMap());
    //data.put("image-config", metadata.getImage_config().toHashMap());
    data.put("name", metadata.getName());
    //data.put("scripts_link", metadata.getScripts_link());

    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Yaml yaml = new Yaml(dumperOptions);

    Writer metadataFile = new FileWriter(tar_input_file);
    yaml.dump(data, metadataFile);
    TarArchiveEntry tar_file = new TarArchiveEntry(tar_input_file, "Metadata.yaml");
    tar_file.setSize(tar_input_file.length());
    my_tar_ball.putArchiveEntry(tar_file);
    IOUtils.copy(new FileInputStream(tar_input_file), my_tar_ball);

    /* Close Archieve entry, write trailer information */
    my_tar_ball.closeArchiveEntry();

    metadataFile.close();
  }

  private ByteArrayOutputStream createVNFPackage(
      VirtualNetworkFunctionDescriptor vnfd, Set<Script> scripts)
      throws IOException, ArchiveException {

    ByteArrayOutputStream tar_output = new ByteArrayOutputStream();
    ArchiveOutputStream my_tar_ball =
        new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, tar_output);

    Metadata metadata = new Metadata(vnfd, this.image_names);
    writeMetadata(metadata, my_tar_ball);

    File tar_input_file = File.createTempFile("vnfd", null);
    TarArchiveEntry tar_file = new TarArchiveEntry(tar_input_file, "vnfd.json");
    Writer writer = new FileWriter(tar_input_file);
    Gson gson = new Gson();
    String vnfdJson = gson.toJson(vnfd);
    writer.write(vnfdJson);
    writer.close();
    tar_file.setSize(tar_input_file.length());
    my_tar_ball.putArchiveEntry(tar_file);
    IOUtils.copy(new FileInputStream(tar_input_file), my_tar_ball);
    /* Close Archieve entry, write trailer information */
    my_tar_ball.closeArchiveEntry();

    for (Script script : scripts) {

      tar_input_file = File.createTempFile("script", null);
      tar_file = new TarArchiveEntry(tar_input_file, "scripts/" + script.getName());
      FileOutputStream outputStream = new FileOutputStream(tar_input_file);
      outputStream.write(script.getPayload());
      outputStream.close();
      tar_file.setSize(tar_input_file.length());
      my_tar_ball.putArchiveEntry(tar_file);
      IOUtils.copy(new FileInputStream(tar_input_file), my_tar_ball);
      my_tar_ball.closeArchiveEntry();
    }

    //close tar
    my_tar_ball.finish();
    /* Close output stream, our files are zipped */
    tar_output.close();

    return tar_output;
  }

  /*
   *
   * MAIN FUNCTIONS
   *
   */

  public void parseVNFCSAR(String vnfd_csar) throws Exception {

    InputStream csar = new FileInputStream(vnfd_csar);
    readFiles(csar);

    readMetaData();

    VNFDTemplate vnfdTemplate = Utils.bytesToVNFDTemplate(this.template);
    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);
  }

  public VirtualNetworkFunctionDescriptor parseVNFDCSARFromByte(byte[] bytes, String projectId)
      throws Exception {

    File temp = File.createTempFile("CSAR", null);
    FileOutputStream fos = new FileOutputStream(temp);
    fos.write(bytes);
    InputStream input = new FileInputStream(temp);

    readFiles(input);

    readMetaData();

    VNFDTemplate vnfdTemplate = Utils.bytesToVNFDTemplate(this.template);
    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);

    ByteArrayOutputStream byteArray = createVNFPackage(vnfd, this.scripts);

    return vnfPackageManagement.onboard(byteArray.toByteArray(), projectId);
  }

  public NetworkServiceDescriptor parseNSDCSAR(String nsd_csar) throws Exception {

    InputStream input = new FileInputStream(new File(nsd_csar));
    readFiles(input);

    readMetaData();
    ArrayList<ByteArrayOutputStream> vnfpList = new ArrayList<>();

    NSDTemplate nsdTemplate = Utils.bytesToNSDTemplate(this.template);
    NetworkServiceDescriptor nsd = toscaParser.parseNSDTemplate(nsdTemplate);

    return nsd;
  }

  public NetworkServiceDescriptor parseNSDCSARFromByte(byte[] bytes, String projectId)
      throws Exception {

    File temp = File.createTempFile("CSAR", null);
    ArrayList<ByteArrayOutputStream> vnfpList = new ArrayList<>();

    FileOutputStream fos = new FileOutputStream(temp);
    fos.write(bytes);
    InputStream input = new FileInputStream(temp);

    readFiles(input);
    readMetaData();

    NSDTemplate nsdTemplate = Utils.bytesToNSDTemplate(this.template);
    NetworkServiceDescriptor nsd = toscaParser.parseNSDTemplate(nsdTemplate);

    for (VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()) {
      vnfpList.add(createVNFPackage(vnfd, scripts));
    }

    nsd.getVnfd().clear();

    for (ByteArrayOutputStream byteArray : vnfpList) {

      String id = "";
      String vnfPackageLocation =
          vnfPackageManagement.onboard(byteArray.toByteArray(), projectId).getVnfPackageLocation();

      Iterable<VirtualNetworkFunctionDescriptor> vnfds = vnfdRepository.findByProjectId(projectId);
      for (VirtualNetworkFunctionDescriptor vnfd : vnfds) {
        if (vnfd.getVnfPackageLocation().equals(vnfPackageLocation)) {

          id = vnfd.getId();
        }
      }

      VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
      vnfd.setId(id);
      nsd.getVnfd().add(vnfd);
    }

    return nsd;
  }
}
