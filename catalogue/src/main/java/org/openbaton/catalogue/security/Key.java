package org.openbaton.catalogue.security;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
  name = "PublicKeys",
  uniqueConstraints = @UniqueConstraint(columnNames = {"name", "projectId"})
)
public class Key extends BaseKey {}
