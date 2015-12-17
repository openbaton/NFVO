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

package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class MonitoringParameter {

    @Id
    private String id;
    @Version
    private int version = 0;

    private Metric metric;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String,String> params;

    public MonitoringParameter(){}
    
    @PrePersist
    public void ensureId(){
        id= IdGenerator.createUUID();
    }
    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "MonitoringParameter{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", metric=" + metric +
                ", params=" + params +
                '}';
    }
}
