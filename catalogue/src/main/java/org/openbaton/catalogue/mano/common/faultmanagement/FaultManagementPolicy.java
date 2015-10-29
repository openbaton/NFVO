package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by mob on 29.10.15.
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class FaultManagementPolicy implements Serializable{
    @Id
    protected String id;
    @Version
    protected int version = 0;
    protected String name;
    protected int cooldown;
    protected int period;
    protected PerceivedSeverity severity;

    @OneToMany
    private Set<Criteria> criteriaSet;

    @PrePersist
    public void ensureId(){
        id= IdGenerator.createUUID();
    }

    public void setName(String name) {
        this.name = name;
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

    public Set<Criteria> getCriteriaSet() {
        return criteriaSet;
    }

    public void setCriteriaSet(Set<Criteria> criteriaSet) {
        this.criteriaSet = criteriaSet;
    }



}
