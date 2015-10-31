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
    private String vdu_selector;
    private Metric parameter_ref;
    private String statistic;
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

    public Metric getParameter_ref() {
        return parameter_ref;
    }

    public void setParameter_ref(Metric parameter_ref) {
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

    public String getVdu_selector() {
        return vdu_selector;
    }

    public void setVdu_selector(String vdu_selector) {
        this.vdu_selector = vdu_selector;
    }

    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    @Override
    public String toString() {
        return "Criteria{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", vdu_selector='" + vdu_selector + '\'' +
                ", parameter_ref=" + parameter_ref +
                ", statistic='" + statistic + '\'' +
                ", comparison_operator='" + comparison_operator + '\'' +
                ", threshold='" + threshold + '\'' +
                '}';
    }

}
