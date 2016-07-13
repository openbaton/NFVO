package org.openbaton.nfvo.security.interfaces;

import org.openbaton.catalogue.security.Project;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;

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
  void delete(Project project) throws EntityInUseException;

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

  /**
   *
   * @param name
   * @return
   */
  Project queryByName(String name);

  Iterable<Project> queryForUser();

  boolean exist(String project);
}
