package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 29.10.15.
 */
public enum MonitoringParam {
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
    SYSTEM_SWAP,
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
