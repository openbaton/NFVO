package org.project.openbaton.nfvo.core.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.BoundedInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.catalogue.nfvo.Script;
import org.project.openbaton.catalogue.nfvo.VNFPackage;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.openbaton.exceptions.NotFoundException;
import org.project.openbaton.exceptions.VimException;
import org.project.openbaton.nfvo.repositories.VNFDRepository;
import org.project.openbaton.nfvo.repositories.VnfPackageRepository;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by lto on 22/07/15.
 */
@Service
@Scope
public class VNFPackageManagement implements org.project.openbaton.nfvo.core.interfaces.VNFPackageManagement {

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
    public VNFPackage onboard(byte[] pack, String name, String diskFormat, String containerFromat, long minDisk, long minRam, boolean isPublic) throws IOException, VimException, NotFoundException, SQLException {
        VNFPackage vnfPackage = new VNFPackage();
        vnfPackage.setScripts(new HashSet<Script>());
        vnfPackage.setName(name);

        InputStream imageStream = null;
        ArchiveInputStream myTarFile = null;
        try {
            imageStream = new ByteArrayInputStream(pack);
            myTarFile = new ArchiveStreamFactory().createArchiveInputStream("tar", imageStream);
        } catch (ArchiveException e) {
            e.printStackTrace();
            throw new IOException();
        }

        VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = null;
        BoundedInputStream boundedInputStream;
        File imageFile = null;
        InputStream imageInputStream = null;
        File folderScripts = null;
        TarArchiveEntry entry;
        while ((entry = (TarArchiveEntry)myTarFile.getNextEntry()) != null) {
            /* Get the name of the file */
            if (entry.isFile() && !entry.getName().startsWith("./._")) {
                String individualFile = entry.getName();
                log.debug("file inside tar: " + individualFile);
                byte[] content = new byte[(int) entry.getSize()];
                if (individualFile.endsWith(".json")) {
                /*this must be the vnfd*/
                /*and has to be onboarded in the catalogue*/
                    boundedInputStream = new BoundedInputStream(myTarFile, entry.getSize());
                    String json = convertStreamToString(boundedInputStream);
                    log.trace("Content of json is: " + json);
                    try {
                        virtualNetworkFunctionDescriptor = mapper.fromJson(json, VirtualNetworkFunctionDescriptor.class);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    log.trace("Created VNFD: " + virtualNetworkFunctionDescriptor);
                    nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor);
                } else if (individualFile.endsWith(".img")) {
                /*this must be the image*/
                /*and has to be upladed to the RIGHT vim*/
                    //imageFile = entryFile;
                    myTarFile.read(content, 0, content.length);
                    imageInputStream = new ByteArrayInputStream(content);
                    log.debug("imageFile is: " + name);
                }else if (individualFile.equals("scripts")){
                    boundedInputStream = new BoundedInputStream(myTarFile, entry.getSize());
                    String scriptLink = new BufferedReader(new InputStreamReader(boundedInputStream)).readLine();
                    vnfPackage.setScriptsLink(scriptLink);
                }else if (individualFile.equals("image")){
                    boundedInputStream = new BoundedInputStream(myTarFile, entry.getSize());
                    String imageLink = new BufferedReader(new InputStreamReader(boundedInputStream)).readLine();
                    vnfPackage.setImageLink(imageLink);
                }
            }
        }
        NFVImage image = new NFVImage();
        image.setName(name);
        image.setContainerFormat(containerFromat);
        image.setDiskFormat(diskFormat);
        image.setIsPublic(isPublic);
        image.setMinDiskSpace(minDisk);
        image.setMinRam(minRam);
        List<String> vimInstances = new ArrayList<>();
        if (imageInputStream == null) {
            throw new NullPointerException("VNFPackageManagement: Image file does not exist.");
        } else {
            //imageStream = new FileInputStream(imageFile);
            log.debug(imageInputStream.toString());
            for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
                if (!vimInstances.contains(vdu.getVimInstance().getId())) { // check if we didn't already upload it
                    Vim vim = vimBroker.getVim(vdu.getVimInstance().getType());
                    image = vim.add(vdu.getVimInstance(), image, imageInputStream);
                    if (vdu.getVm_image() == null)
                        vdu.setVm_image(new HashSet<String>());
                    vdu.getVm_image().add(image.getName());
                    vimInstances.add(vdu.getVimInstance().getId());
                }
            }
        }
        vnfPackage.setImage(image);

        myTarFile.close();

        virtualNetworkFunctionDescriptor.setVnfPackage(vnfPackage);
        vnfPackage.setVnfr(virtualNetworkFunctionDescriptor);
        vnfdRepository.save(virtualNetworkFunctionDescriptor);
        //log.trace("Persisted " + virtualNetworkFunctionDescriptor);

        //vnfPackageRepository.save(vnfPackage);
        //log.debug("Persisted " + vnfPackage);
        return vnfPackage;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
        old.setExtId(pack_new.getExtId());
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
