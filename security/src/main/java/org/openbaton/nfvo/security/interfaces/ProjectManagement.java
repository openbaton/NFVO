package org.openbaton.nfvo.security.interfaces;

import org.openbaton.catalogue.security.Project;

/**
 * Created by lto on 24/05/16.
 */
public interface ProjectManagement {
    /**
     *
     * @param project
     */
    Project add(Project project);

    /**
     *
     * @param project
     */
    void delete(Project project);

    /**
     *
     * @param new_project
     */
    Project update(Project new_project);

    /**
     */
    Iterable<Project> query();

    /**
     *
     * @param id
     */
    Project query(String id);
}
