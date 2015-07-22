package org.project.openbaton.nfvo.core.api;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.project.openbaton.catalogue.nfvo.VNFPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

/**
 * Created by lto on 22/07/15.
 */
@Service
@Scope
public class VNFPackageManagement implements org.project.openbaton.nfvo.core.interfaces.VNFPackageManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());



    @Override
    public VNFPackage onboard(byte[] pack, String name) throws IOException {
        VNFPackage vnfPackage = new VNFPackage();
        vnfPackage.setName(name);

        File file = new File("/tmp/" + name);
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        stream.write(pack);
        stream.close();

        TarArchiveInputStream myTarFile = new TarArchiveInputStream(new FileInputStream(file));

        TarArchiveEntry entry;
        while ((entry = myTarFile.getNextTarEntry()) != null) {
                        /* Get the name of the file */
            String individualFile = entry.getName();
            log.debug("file inside tar: " + individualFile);
            if (individualFile.endsWith(".json")){
                /*this must be the vnfd*/
                /*and has to be onboarded in the catalogue*/

            }else {
                /*this must be the image*/
                /*and has to be upladed to the RIGHT vim*/
            }
        }

        file.delete();
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
