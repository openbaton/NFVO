package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class Criteria implements Serializable {
    @Id
    private String id;
    @Version
    private int version = 0;

    private String name;
    private MonitoringParam parameter_ref;
    private String comparison_operator;
    private String threshold;

    public Criteria(){}

    @PrePersist
    public void ensureId(){
        id= IdGenerator.createUUID();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MonitoringParam getParameter_ref() {
        return parameter_ref;
    }

    public void setParameter_ref(MonitoringParam parameter_ref) {
        this.parameter_ref = parameter_ref;
    }

    public String getComparison_operator() {
        return comparison_operator;
    }

    public void setComparison_operator(String comparison_operator) {
        this.comparison_operator = comparison_operator;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }
}
