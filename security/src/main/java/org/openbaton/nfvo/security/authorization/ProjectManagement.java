package org.openbaton.nfvo.security.authorization;

import org.openbaton.catalogue.security.Project;
import org.openbaton.nfvo.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 24/05/16.
 */
@Service
public class ProjectManagement implements org.openbaton.nfvo.security.interfaces.ProjectManagement {

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public Project add(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public void delete(Project project) {
        projectRepository.delete(project);
    }

    @Override
    public Project update(Project new_project) {
        return projectRepository.save(new_project);
    }

    @Override
    public Iterable<Project> query() {
        return projectRepository.findAll();
    }

    @Override
    public Project query(String id) {
        return projectRepository.findFirstById(id);
    }
}
