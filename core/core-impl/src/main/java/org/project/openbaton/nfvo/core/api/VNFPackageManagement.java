package org.project.openbaton.nfvo.core.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.BoundedInputStream;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.catalogue.nfvo.VNFPackage;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.exceptions.VimException;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
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
    private VimBroker vimBroker;

    @Override
    public VNFPackage onboard(byte[] pack, String name, String diskFormat, String containerFromat, long minDisk, long minRam, boolean isPublic) throws IOException, VimException, NotFoundException {
        VNFPackage vnfPackage = new VNFPackage();
        vnfPackage.setName(name);

        ByteArrayInputStream imageStream = null;
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
        FileInputStream imageInputStream = null;
        TarArchiveEntry entry;
        while ((entry = (TarArchiveEntry)myTarFile.getNextEntry()) != null) {
            /* Get the name of the file */
            if (entry.isFile() && !entry.getName().startsWith("./._")) {
                String individualFile = entry.getName();
                log.debug("file inside tar: " + individualFile);
                File entryFile = (entry).getFile();
                log.debug("entryFile is: " + entryFile);
                log.debug("entry size is: " + entry.getSize() + " much more different from getRealSize: " + entry.getRealSize());
//                log.debug("file path is: " + entryFile.getAbsolutePath());
                if (individualFile.endsWith(".json")) {
                /*this must be the vnfd*/
                /*and has to be onboarded in the catalogue*/
                    boundedInputStream = new BoundedInputStream(myTarFile, entry.getSize());
                    virtualNetworkFunctionDescriptor = mapper.fromJson(new InputStreamReader(boundedInputStream), VirtualNetworkFunctionDescriptor.class);
                    nsdUtils.fetchVimInstances(virtualNetworkFunctionDescriptor);
                } else if (individualFile.endsWith(".ovf")) {
                /*this must be the image*/
                /*and has to be upladed to the RIGHT vim*/
                    File imageFile = entryFile;
                    log.debug("imageFile is: " + imageFile);
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
        for (VirtualDeploymentUnit vdu: virtualNetworkFunctionDescriptor.getVdu()){
            Vim vim = vimBroker.getVim(vdu.getVimInstance().getType());
            image = vim.add(vdu.getVimInstance(), image, imageStream);
            vdu.setVm_image(new HashSet<String>());
            vdu.getVm_image().add(image.getName());
        }
        vnfPackage.setImage(image);
        myTarFile.close();
        return vnfPackage;
    }

    @Override
    public void disable() {

    }

    @Override
    public void enable() {

    }

    @Override
    public VNFPackage update(String id, VNFPackage pack_new) {
        return pack_new;
    }

    @Override
    public VNFPackage query(String id) {
        return null;
    }

    @Override
    public List<VNFPackage> query() {
        return null;
    }

    @Override
    public void delete(String id) {

    }
}
