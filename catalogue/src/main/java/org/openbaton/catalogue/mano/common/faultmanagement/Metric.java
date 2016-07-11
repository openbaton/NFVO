/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 29.10.15.
 */
public enum Metric {
  AGENT_PING,

  NET_DNS,
  NET_IF_COLLISION,
  NET_IF_IN,
  NET_IF_OUT,
  NET_IF_TOTAL,
  NET_TCP_LISTEN,
  NET_TCP_PORT,
  NET_TCP_SERVICE,
  NET_TCP_SERVICE_PERF,
  NET_UDP_LISTEN,

  PROC_MEM,
  PROC_NUM,
  SENSOR,
  SYSTEM_CPU_INTR,
  SYSTEM_CPU_LOAD,
  SYSTEM_CPU_NUM,
  SYSTEM_CPU_SWITCHES,
  SYSTEM_CPU_UTIL,

  SYSTEM_STAT,
  SYSTEM_SWAP_IN,
  SYSTEM_SWAP_OUT,
  SYSTEM_SWAP_SIZE,
  SYSTEM_UPTIME,
  SYSTEM_USERS_NUM,

  VFS_DEV_READ,
  VFS_DEV_WRITE,
  VFS_FILE_CKSUM,
  VFS_FILE_EXISTS,
  VFS_FILE_REGMATCH,
  VFS_FILE_SIZE,
  VFS_FS_INODE,
  VFS_FS_SIZE,

  VM_MEMORY_SIZE,
  WEB_PAGE_PERF
}
