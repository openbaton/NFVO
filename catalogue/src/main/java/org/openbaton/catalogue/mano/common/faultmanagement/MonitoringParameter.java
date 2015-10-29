package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.List;

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
    private List<String> params;

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

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
