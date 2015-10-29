package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class FaultManagementPolicy implements Serializable{
    @Id
    private String id;
    @Version
    private int version = 0;
    private String name;
    private int vduSelector;
    private int cooldown;
    private int period;
    private PerceivedSeverity severity;
    private FaultManagementVNFCAction action;

    @OneToMany
    private Set<Criteria> criteriaSet;

    public FaultManagementPolicy(){}

    @PrePersist
    public void ensureId(){
        id= IdGenerator.createUUID();
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public int getVduSelector() {
        return vduSelector;
    }

    public void setVduSelector(int vduSelector) {
        this.vduSelector = vduSelector;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public PerceivedSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(PerceivedSeverity severity) {
        this.severity = severity;
    }

    public FaultManagementVNFCAction getAction() {
        return action;
    }

    public void setAction(FaultManagementVNFCAction action) {
        this.action = action;
    }

    public Set<Criteria> getCriteriaSet() {
        return criteriaSet;
    }

    public void setCriteriaSet(Set<Criteria> criteriaSet) {
        this.criteriaSet = criteriaSet;
    }



}
