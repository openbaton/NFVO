package org.openbaton.tosca.Metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rvl on 29.08.16.
 */
public class ImageConfig {

  private String name;
  private String diskFormat;
  private String containerFormat;
  private int minCPU;
  private int minDisk;
  private int minRam;
  private boolean isPublic;

  public ImageConfig() {

    this.name = "ubuntu-14.04-server-cloudimg-amd64-disk1";
    this.diskFormat = "QCOW2";
    this.containerFormat = "BARE";
    this.minCPU = 2;
    this.minDisk = 0;
    this.minRam = 1024;
    this.isPublic = true;
  }

  public boolean getIsPublic() {
    return isPublic;
  }

  public void setIsPublic(boolean aPublic) {
    isPublic = aPublic;
  }

  public int getMinRam() {
    return minRam;
  }

  public void setMinRam(int minRam) {
    this.minRam = minRam;
  }

  public int getMinDisk() {
    return minDisk;
  }

  public void setMinDisk(int minDisk) {
    this.minDisk = minDisk;
  }

  public int getMinCPU() {
    return minCPU;
  }

  public void setMinCPU(int minCPU) {
    this.minCPU = minCPU;
  }

  public String getContainerFormat() {
    return containerFormat;
  }

  public void setContainerFormat(String containerFormat) {
    this.containerFormat = containerFormat;
  }

  public String getDiskFormat() {
    return diskFormat;
  }

  public void setDiskFormat(String diskFormat) {
    this.diskFormat = diskFormat;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> toHashMap() {

    Map<String, Object> data = new HashMap<>();

    data.put("name", name);
    data.put("diskFormat", diskFormat);
    data.put("isPublic", isPublic);
    data.put("minDisk", minDisk);
    data.put("minCPU", minCPU);
    data.put("containerFormat", containerFormat);
    data.put("diskFormat", diskFormat);
    data.put("minRam", minRam);

    return data;
  }
}
