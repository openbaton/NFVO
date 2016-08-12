package org.openbaton.nfvo.security.authorization;

import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.Role.RoleEnum;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.ProjectRepository;
import org.openbaton.nfvo.repositories.UserRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 24/05/16.
 */
@Service
public class ProjectManagement implements org.openbaton.nfvo.security.interfaces.ProjectManagement {

  @Autowired private org.openbaton.nfvo.security.interfaces.UserManagement userManagement;

  @Autowired private ProjectRepository projectRepository;
  @Autowired private VimRepository vimRepository;
  @Autowired private NetworkServiceDescriptorRepository networkServiceDescriptorRepository;
  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private UserRepository userRepository;

  @Override
  public Project add(Project project) {
    User currentUser = getCurrentUser();
    if (currentUser != null) {
      if (currentUser.getRoles().iterator().next().getRole().ordinal() == RoleEnum.ADMIN.ordinal())
        return projectRepository.save(project);
    } else {
      return projectRepository.save(project);
    }
    throw new UnauthorizedUserException("Sorry only ADMIN can add project");
  }

  @Override
  public void delete(Project project) throws EntityInUseException, NotAllowedException {
    int size = 0;
    for (Project ignored : projectRepository.findAll()) {
      size++;
    }

    if (size == 1) {
      throw new NotAllowedException("You are not allowed to remove the last project");
    }

    Project projectToDelete = projectRepository.findFirstById(project.getId());
    User user = getCurrentUser();
    if (user.getRoles().iterator().next().getRole().ordinal() == RoleEnum.ADMIN.ordinal()) {

      if (projectIsNotUsed(projectToDelete)) {

        projectRepository.delete(projectToDelete);
        return;
      }
      throw new EntityInUseException(
          "Project " + projectToDelete.getName() + " has still some resources allocated");
    }
    for (Role role : user.getRoles())
      if (role.getProject().equals(projectToDelete.getName())) {
        if (projectIsNotUsed(projectToDelete)) {
          projectRepository.delete(projectToDelete);
          return;
        }
        throw new EntityInUseException(
            "Project " + projectToDelete.getName() + " has still some resources allocated");
      }
    throw new UnauthorizedUserException(
        "Project not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  private boolean projectIsNotUsed(Project projectToDelete) {
    if (!vimRepository.findByProjectId(projectToDelete.getId()).isEmpty()) return false;
    if (!vnfPackageRepository.findByProjectId(projectToDelete.getId()).isEmpty()) return false;
    if (!networkServiceDescriptorRepository.findByProjectId(projectToDelete.getId()).isEmpty())
      return false;
    for (User user : userRepository.findAll()) {
      for (Role role : user.getRoles()) {
        if (role.getProject().equals(projectToDelete.getName())) return false;
      }
    }
    return networkServiceRecordRepository.findByProjectId(projectToDelete.getId()).isEmpty();
  }

  @Override
  public Project update(Project new_project) {
    Project project = projectRepository.findFirstById(new_project.getId());
    User user = getCurrentUser();
    if (user.getRoles().iterator().next().getRole().ordinal() == RoleEnum.ADMIN.ordinal())
      return projectRepository.save(new_project);
    for (Role role : user.getRoles())
      if (role.getProject().equals(project.getName())) return projectRepository.save(new_project);
    throw new UnauthorizedUserException(
        "Project not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  @Override
  public Iterable<Project> query() {
    return projectRepository.findAll();
  }

  @Override
  public Project query(String id) {
    Project project = projectRepository.findFirstById(id);
    User user = getCurrentUser();
    if (user.getRoles().iterator().next().getRole().ordinal() == RoleEnum.ADMIN.ordinal())
      return project;
    for (Role role : user.getRoles())
      if (role.getProject().equals(project.getName())) return project;
    throw new UnauthorizedUserException(
        "Project not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  @Override
  public Project queryByName(String name) {
    return projectRepository.findFirstByName(name);
  }

  @Override
  public Iterable<Project> queryForUser() {

    List<Project> projects = new ArrayList<>();
    User user = getCurrentUser();
    if (user.getRoles().iterator().next().getRole().ordinal() == RoleEnum.ADMIN.ordinal()
        || (user.getRoles().iterator().next().getRole().ordinal() == RoleEnum.GUEST.ordinal()
            && user.getRoles().iterator().next().getProject().equals("*")))
      return projectRepository.findAll();
    for (Role role : user.getRoles()) projects.add(this.queryByName(role.getProject()));

    return projects;
  }

  @Override
  public boolean exist(String id) {
    return projectRepository.exists(id);
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) return null;
    String currentUserName = authentication.getName();
    return userManagement.queryDB(currentUserName);
  }
}
