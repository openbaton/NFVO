package org.openbaton.nfvo.core.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by lto on 22/07/15.
 */
@Service
@Scope
public class VNFPackageManagement implements org.openbaton.nfvo.core.interfaces.VNFPackageManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Gson mapper = new GsonBuilder().create();

    @Autowired
    private NSDUtils nsdUtils;

    @Autowired
    private VnfPackageRepository vnfPackageRepository;

    @Autowired
    private VNFDRepository vnfdRepository;

    @Autowired
    private VimBroker vimBroker;

    @Override
    public VNFPackage onboard(byte[] pack) throws IOException, VimException, NotFoundException, SQLException {
        VNFPackage vnfPackage = new VNFPackage();
        vnfPackage.setScripts(new HashSet<Script>());
        Map<String, Object> metadata = null;
        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = null;
        byte[] imageFile = null;
        NFVImage image = new NFVImage();

        InputStream tarStream;
        ArchiveInputStream myTarFile;
        try {
            tarStream = new ByteArrayInputStream(pack);
            myTarFile = new ArchiveStreamFactory().createArchiveInputStream("tar", tarStream);
        } catch (ArchiveException e) {
            e.printStackTrace();
            throw new IOException();
        }
        TarArchiveEntry entry;
        while ((entry = (TarArchiveEntry) myTarFile.getNextEntry()) != null) {
            /* Get the name of the file */
            if (entry.isFile() && !entry.getName().startsWith("./._")) {
                log.debug("file inside tar: " + entry.getName());
                byte[] content = new byte[(int) entry.getSize()];
                myTarFile.read(content, 0, content.length);
                if (entry.getName().equals("Metadata.yaml")) {
                    YamlJsonParser yaml = new YamlJsonParser();
                    metadata = yaml.parseMap(new String(content));
                    //Get configuration for NFVImage
                    String[] REQUIRED_PACKAGE_KEYS = new String[]{"name", "image"};
                    for (String requiredKey : REQUIRED_PACKAGE_KEYS) {
                        if (!metadata.containsKey(requiredKey)) {
                            throw new NotFoundException("Not found " + requiredKey + " of VNFPackage in Metadata.yaml");
                        }
                        if (metadata.get(requiredKey) == null) {
                            throw new NullPointerException("Not defined " + requiredKey + " of VNFPackage in Metadata.yaml");
                        }
                    }
                    vnfPackage.setName((String) metadata.get("name"));
                    if (metadata.containsKey("scripts-link"))
                        vnfPackage.setScriptsLink((String) metadata.get("scripts-link"));
                    if (metadata.containsKey("image-link"))
                        vnfPackage.setImageLink((String) metadata.get("image-link"));

                    Map<String, Object> imageConfig = (Map<String, Object>) metadata.get("image");
                    //Check if all required keys are available
                    String[] REQUIRED_IMAGE_KEYS = new String[]{"name", "diskFormat", "containerFormat", "minCPU", "minDisk", "minRam", "isPublic"};
                    for (String requiredKey : REQUIRED_IMAGE_KEYS) {
                        if (!imageConfig.containsKey(requiredKey)) {
                            throw new NotFoundException("Not found " + requiredKey + " of image in Metadata.yaml");
                        }
                        if (imageConfig.get(requiredKey) == null) {
                            throw new NullPointerException("Not defined " + requiredKey + " of image in Metadata.yaml");
                        }
                    }
                    image.setName((String) imageConfig.get("name"));
                    image.setDiskFormat((String) imageConfig.get("diskFormat"));
                    image.setContainerFormat((String) imageConfig.get("containerFormat"));
                    image.setMinCPU(Integer.toString((Integer) imageConfig.get("minCPU")));
                    image.setMinDiskSpace((Integer) imageConfig.get("minDisk"));
                    image.setMinRam((Integer) imageConfig.get("minRam"));
                    image.setIsPublic(Boolean.parseBoolean(Integer.toString((Integer) imageConfig.get("minRam"))));
                } else if (entry.getName().endsWith(".json")) {
                    //this must be the vnfd
                    //and has to be onboarded in the catalogue
                    String json = new String(content);
                    log.trace("Content of json is: " + json);
                    try {
                        virtualNetworkFunctionDescriptor = mapper.fromJson(json, VirtualNetworkFunctionDescriptor.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log.trace("Created VNFD: " + virtualNetworkFunctionDescriptor);
                    nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor);
                } else if (entry.getName().endsWith(".img")) {
                    //this must be the image
                    //and has to be upladed to the RIGHT vim
                    imageFile = content;
                    log.debug("imageFile is: " + entry.getName());
                } else if (entry.getName().startsWith("scripts/")) {
                    Script script = new Script();
                    script.setName(entry.getName().substring(8));
                    script.setPayload(content);
                    vnfPackage.getScripts().add(script);
                }
            }
        }
        if (metadata == null) {
            throw new NotFoundException("VNFPackageManagement: Not found Metadata.yaml");
        }
        List<String> vimInstances = new ArrayList<>();
        if (vnfPackage.getImageLink() == null) {
            if (imageFile == null) {
                throw new NotFoundException("VNFPackageManagement: Not found image file and image-link is null");
            } else {
                //imageStream = new FileInputStream(imageFile);
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
                    if (!vimInstances.contains(vdu.getVimInstance().getId())) { // check if we didn't already upload it
                        Vim vim = vimBroker.getVim(vdu.getVimInstance().getType());
                        image = vim.add(vdu.getVimInstance(), image, imageFile);
                        if (vdu.getVm_image() == null)
                            vdu.setVm_image(new HashSet<String>());
                        vdu.getVm_image().add(image.getName());
                        vimInstances.add(vdu.getVimInstance().getId());
                    }
                }
            }
        }
        vnfPackage.setImage(image);
        myTarFile.close();

        virtualNetworkFunctionDescriptor.setVnfPackage(vnfPackage);
        vnfdRepository.save(virtualNetworkFunctionDescriptor);
        log.trace("Persisted " + virtualNetworkFunctionDescriptor);
        return virtualNetworkFunctionDescriptor.getVnfPackage();
    }

    @Override
    public void disable() {
    }

    @Override
    public void enable() {
    }

    @Override
    public VNFPackage update(String id, VNFPackage pack_new) {
        VNFPackage old = vnfPackageRepository.findOne(id);
        old.setName(pack_new.getName());
        old.setImage(pack_new.getImage());
        return old;
    }

    @Override
    public VNFPackage query(String id) {
        return vnfPackageRepository.findOne(id);
    }

    @Override
    public Iterable<VNFPackage> query() {
        return vnfPackageRepository.findAll();
    }

    @Override
    public void delete(String id) {
        //TODO remove image in the VIM
        vnfPackageRepository.delete(vnfPackageRepository.findOne(id));
    }
}
