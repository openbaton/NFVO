package org.openbaton.catalogue.mano.common.monitoring;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mob on 21.01.16.
 */
@Entity
public class VNFAlarm extends Alarm {

  private String vnfrId;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> vnfcIds;

  private String vimName;

  public VNFAlarm() {
    this.alarmType = AlarmType.VIRTUAL_NETWORK_FUNCTION;
  }

  @Override
  public AlarmType getAlarmType() {
    return alarmType;
  }

  @Override
  public void setAlarmType(AlarmType alarmType) {
    this.alarmType = alarmType;
  }

  public String getVnfrId() {
    return vnfrId;
  }

  public void setVnfrId(String vnfrId) {
    this.vnfrId = vnfrId;
  }

  public List<String> getVnfcIds() {
    return vnfcIds;
  }

  public void addVnfcId(String vnfcId) {
    if (vnfcIds == null) vnfcIds = new ArrayList<>();
    vnfcIds.add(vnfcId);
  }

  public void setVnfcIds(List<String> vnfcIds) {
    this.vnfcIds = vnfcIds;
  }

  public String getVimName() {
    return vimName;
  }

  public void setVimName(String vimName) {
    this.vimName = vimName;
  }

  @Override
  public String toString() {
    return "VNFAlarm{"
        + "vnfrId='"
        + vnfrId
        + '\''
        + ", vnfcIds="
        + vnfcIds
        + ", vimName='"
        + vimName
        + '\''
        + "} "
        + super.toString();
  }
}
