/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by lto on 22/07/15.
 */
@Entity
public class VNFPackage implements Serializable {

    @Id
    private String id;
    @Version
    private int version = 0;

    //Name of the Package
    private String name;

    //URL to the image's location
    private String imageLink;

    //URL to the scripts' location
    private String scriptsLink;

    //NFVImage used by this VNFPackage
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, orphanRemoval = true)
    private NFVImage image;

    //Set of scripts to execute
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Script> scripts;

    public VNFPackage() {
    }

    @Override
    public String toString() {
        return "VNFPackage{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", imageLink='" + imageLink + '\'' +
                ", scriptsLink='" + scriptsLink + '\'' +
                ", image=" + image +
                ", scripts=" + scripts +
                '}';
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public String getScriptsLink() {
        return scriptsLink;
    }

    public void setScriptsLink(String scriptsLink) {
        this.scriptsLink = scriptsLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Script> getScripts() {
        return scripts;
    }

    public void setScripts(Set<Script> scripts) {
        this.scripts = scripts;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NFVImage getImage() {
        return image;
    }

    public void setImage(NFVImage image) {
        this.image = image;
    }
}
