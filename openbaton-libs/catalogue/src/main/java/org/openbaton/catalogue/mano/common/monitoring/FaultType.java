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
public enum FaultType {

  //Compute faults
  COMPUTE,
  COMPUTE_HOGS,
  COMPUTE_HOGS_CPU,
  COMPUTE_HOGS_MEMORY,
  COMPUTE_CRASH,
  COMPUTE_CODE_CORRUPTION,
  COMPUTE_DATA_CORRUPTION,

  //IO faults
  IO,

  IO_NETWORK_FRAME_RECEIVE,
  IO_NETWORK_FRAME_RECEIVE_CORRUPTION,
  IO_NETWORK_FRAME_RECEIVE_DROP,
  IO_NETWORK_FRAME_RECEIVE_DELAY,
  IO_NETWORK_FRAME_TRANSMIT,
  IO_NETWORK_FRAME_TRANSMIT_CORRUPTION,
  IO_NETWORK_FRAME_TRANSMIT_DROP,
  IO_NETWORK_FRAME_TRANSMIT_DELAY,

  IO_STORAGE_BLOCK_READS_CORRUPTION,
  IO_STORAGE_BLOCK_READS_DROP,
  IO_STORAGE_BLOCK_READS_DELAY,
  IO_STORAGE_BLOCK_WRITE_CORRUPTION,
  IO_STORAGE_BLOCK_WRITE_DROP,
  IO_STORAGE_BLOCK_WRITE_DELAY,

  //SERVICE FAULT
  VNF_NOT_AVAILABLE
}
