package org.openbaton.nfvo.security.authorization;

import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.Role.RoleEnum;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.ProjectRepository;
import org.openbaton.nfvo.repositories.UserRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.security.interfaces.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by lto on 24/05/16.
 */
@Service
public class ProjectManagement implements org.openbaton.nfvo.security.interfaces.ProjectManagement {

  @Autowired private UserManagement userManagement;

  @Autowired private ProjectRepository projectRepository;
  @Autowired private VimRepository vimRepository;
  @Autowired private NetworkServiceDescriptorRepository networkServiceDescriptorRepository;
  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public Project add(Project project) {
    log.trace("Adding new project " + project.getName());
    return projectRepository.save(project);
  }

  @Override
  public void delete(Project project)
      throws EntityInUseException, NotAllowedException, BadRequestException, NotFoundException {
    int size = 0;
    for (Project ignored : projectRepository.findAll()) {
      size++;
    }
    if (size == 1) {
      throw new NotAllowedException("You are not allowed to remove the last project");
    }

    Project projectToDelete = projectRepository.findFirstById(project.getId());

    if (!projectIsNotUsed(projectToDelete)) {
      throw new EntityInUseException(
          "Project " + projectToDelete.getName() + " has still some resources allocated");
    }
    for (User user : userManagement.query()) {
      Set<Role> rolesToRemove = new HashSet<>();
      for (Role role : user.getRoles()) {
        if (role.getProject().equals(projectToDelete.getName())) {
          rolesToRemove.add(role);
        }
      }
      if (!rolesToRemove.isEmpty()) {
        user.getRoles().removeAll(rolesToRemove);
        userManagement.update(user);
      }
    }
    projectRepository.delete(projectToDelete);
  }

  private boolean projectIsNotUsed(Project projectToDelete) {
    if (!vimRepository.findByProjectId(projectToDelete.getId()).isEmpty()) return false;
    if (!vnfPackageRepository.findByProjectId(projectToDelete.getId()).isEmpty()) return false;
    if (!networkServiceDescriptorRepository.findByProjectId(projectToDelete.getId()).isEmpty())
      return false;
    return networkServiceRecordRepository.findByProjectId(projectToDelete.getId()).isEmpty();
  }

  @Override
  public Project update(Project new_project) throws NotAllowedException, NotFoundException {
    Project project = projectRepository.findFirstById(new_project.getId());
    if (project == null) {
      throw new NotFoundException("Not found project " + new_project.getId());
    }
    if (!project.getName().equals(new_project.getName())) {
      throw new NotAllowedException("Forbidden to change the project name");
    }
    project.setDescription(new_project.getDescription());
    return projectRepository.save(new_project);
  }

  @Override
  public Iterable<Project> query() {
    return projectRepository.findAll();
  }

  @Override
  public Project query(String id) throws NotFoundException {
    Project project = projectRepository.findFirstById(id);
    if (project == null) throw new NotFoundException("Not found project with id " + id);
    return project;
  }

  @Override
  public Project queryByName(String name) {
    return projectRepository.findFirstByName(name);
  }

  @Override
  public Iterable<Project> query(User user) {
    List<Project> projects = new ArrayList<>();
    for (Role role : user.getRoles()) projects.add(this.queryByName(role.getProject()));
    return projects;
  }

  @Override
  public boolean exist(String id) {
    return projectRepository.exists(id);
  }
}
