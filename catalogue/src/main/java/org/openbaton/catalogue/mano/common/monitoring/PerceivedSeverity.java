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

package org.openbaton.catalogue.mano.common.monitoring;

/**
 * Created by mob on 26.10.15.
 */
public enum PerceivedSeverity {
  INDETERMINATE, // ordinal value: 0
  WARNING, // ordinal value: 1
  MINOR, // ordinal value: 2
  MAJOR, // ordinal value: 3
  CRITICAL // ordinal value: 4
}
