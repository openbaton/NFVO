/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.catalogue.mano.common;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mpa on 15/12/15. */
@Entity
public class AutoScalePolicy implements Serializable {
  @Id private String id;
  @Version private int version = 0;

  private String name;

  private double threshold;

  private String comparisonOperator;

  private int period;

  private int cooldown;

  @Enumerated(EnumType.STRING)
  private ScalingMode mode;

  @Enumerated(EnumType.STRING)
  private ScalingType type;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<ScalingAlarm> alarms;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<ScalingAction> actions;

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public String getComparisonOperator() {
    return comparisonOperator;
  }

  public void setComparisonOperator(String comparisonOperator) {
    this.comparisonOperator = comparisonOperator;
  }

  public int getPeriod() {
    return period;
  }

  public void setPeriod(int period) {
    this.period = period;
  }

  public int getCooldown() {
    return cooldown;
  }

  public void setCooldown(int cooldown) {
    this.cooldown = cooldown;
  }

  public ScalingMode getMode() {
    return mode;
  }

  public void setMode(ScalingMode mode) {
    this.mode = mode;
  }

  public ScalingType getType() {
    return type;
  }

  public void setType(ScalingType type) {
    this.type = type;
  }

  public Set<ScalingAlarm> getAlarms() {
    return alarms;
  }

  public void setAlarms(Set<ScalingAlarm> alarms) {
    this.alarms = alarms;
  }

  public Set<ScalingAction> getActions() {
    return actions;
  }

  public void setActions(Set<ScalingAction> actions) {
    this.actions = actions;
  }

  @Override
  public String toString() {
    return "AutoScalePolicy{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", name='"
        + name
        + '\''
        + ", threshold='"
        + threshold
        + '\''
        + ", comparisonOperator='"
        + comparisonOperator
        + '\''
        + ", period="
        + period
        + ", cooldown="
        + cooldown
        + ", mode="
        + mode
        + ", type="
        + type
        + ", alarms="
        + alarms
        + ", actions="
        + actions
        + '}';
  }
}
