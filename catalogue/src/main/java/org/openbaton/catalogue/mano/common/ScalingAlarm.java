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

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by mpa on 15/12/15. */
@Entity
public class ScalingAlarm extends BaseEntity {

  private String metric;
  private String statistic;
  private String comparisonOperator;
  private double threshold;
  private double weight;

  @Override
  public String toString() {
    return "ScalingAlarm{"
        + "metric='"
        + metric
        + '\''
        + ", statistic='"
        + statistic
        + '\''
        + ", comparisonOperator='"
        + comparisonOperator
        + '\''
        + ", threshold="
        + threshold
        + ", weight="
        + weight
        + "} "
        + super.toString();
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getStatistic() {
    return statistic;
  }

  public void setStatistic(String statistic) {
    this.statistic = statistic;
  }

  public String getComparisonOperator() {
    return comparisonOperator;
  }

  public void setComparisonOperator(String comparisonOperator) {
    this.comparisonOperator = comparisonOperator;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}
