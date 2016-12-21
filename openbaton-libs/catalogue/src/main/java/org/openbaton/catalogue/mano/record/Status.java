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

package org.openbaton.catalogue.mano.record;

/**
 * Created by lto on 06/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
public enum Status {

  /** Error */
  ERROR(0),
  /** Null - */
  NULL(1),
  /** Instantiated - Not Configured */
  INITIALIZED(2),

  /** Inactive - Configured */
  INACTIVE(3),

  /*
   * Scaling
   */
  SCALING(4),

  /** Active - Configured */
  ACTIVE(5),

  /** Terminated */
  TERMINATED(6),

  /** Terminated */
  RESUMING(7);

  private int value;

  Status(int value) {
    this.value = value;
  }
}
