package org.openbaton.catalogue.security;

import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.VimInstance;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.Set;

/**
 * Created by lto on 24/05/16.
 */
@Entity
public class Project {
    @Id
    private String id;
    private String name;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<VimInstance> allowedVimInstances;
    private Quota quota;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<VimInstance> getAllowedVimInstances() {
        return allowedVimInstances;
    }

    public void setAllowedVimInstances(Set<VimInstance> allowedVimInstances) {
        this.allowedVimInstances = allowedVimInstances;
    }

    public Quota getQuota() {
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }
}
