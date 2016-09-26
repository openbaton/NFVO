package org.openbaton.nfvo.system;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.sql.DataSource;

@Configuration
@ConfigurationProperties
public class FlywayConfig {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private DataSource dataSource;

  @Bean(initMethod = "migrate")
  Flyway flyway() {
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource);
    flyway.setLocations("classpath:/flyway");
    flyway.setBaselineVersion(MigrationVersion.fromVersion("2.2.0.0"));
    try {
      flyway.baseline();
    } catch (FlywayException e) {
      log.warn("Database is already baselined with flyway");
    }
    return flyway;
  }
}

@Entity
class schema_version implements Serializable {
  @Id private int id;

  private String version;

  private String description;

  private String type;

  private String script;

  private int checksum;

  private int installed_by;

  private Date installed_on;

  private int execution_time;

  private int success;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public int getChecksum() {
    return checksum;
  }

  public void setChecksum(int checksum) {
    this.checksum = checksum;
  }

  public int getInstalled_by() {
    return installed_by;
  }

  public void setInstalled_by(int installed_by) {
    this.installed_by = installed_by;
  }

  public Date getInstalled_on() {
    return installed_on;
  }

  public void setInstalled_on(Date installed_on) {
    this.installed_on = installed_on;
  }

  public int getExecution_time() {
    return execution_time;
  }

  public void setExecution_time(int execution_time) {
    this.execution_time = execution_time;
  }

  public int getSuccess() {
    return success;
  }

  public void setSuccess(int success) {
    this.success = success;
  }

  @Override
  public String toString() {
    return "schema_version{"
        + "id="
        + id
        + ", version='"
        + version
        + '\''
        + ", description='"
        + description
        + '\''
        + ", type='"
        + type
        + '\''
        + ", script='"
        + script
        + '\''
        + ", checksum="
        + checksum
        + ", installed_by="
        + installed_by
        + ", installed_on="
        + installed_on
        + ", execution_time="
        + execution_time
        + ", success="
        + success
        + '}';
  }
}
