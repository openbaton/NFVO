DROP PROCEDURE IF EXISTS `?`;
DELIMITER //
CREATE PROCEDURE `?`
  (
  )
  BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;

    ALTER TABLE vnfm_manager_endpoint DROP COLUMN version;
    ALTER TABLE vnfrecord_dependency DROP COLUMN version;
    ALTER TABLE dependency_parameters DROP COLUMN version;
    ALTER TABLE vnfcdependency_parameters DROP COLUMN version;
    ALTER TABLE vnfpackage DROP COLUMN version;
    ALTER TABLE ip DROP COLUMN version;
    ALTER TABLE alarm_endpoint DROP COLUMN version;
    ALTER TABLE auto_scale_policy DROP COLUMN version;
    ALTER TABLE base_user DROP COLUMN version;
    ALTER TABLE configuration DROP COLUMN version;
    ALTER TABLE configuration_parameter DROP COLUMN version;
    ALTER TABLE connection_point DROP COLUMN version;
    ALTER TABLE costituentvdu DROP COLUMN version;
    ALTER TABLE costituentvnf DROP COLUMN version;
    ALTER TABLE criteria DROP COLUMN version;
    ALTER TABLE deployment_flavour DROP COLUMN version;
    ALTER TABLE event_endpoint DROP COLUMN version;
    ALTER TABLE fault_management_policy DROP COLUMN version;
    ALTER TABLE high_availability DROP COLUMN version;
    ALTER TABLE item DROP COLUMN version;
    ALTER TABLE lifecycle_event DROP COLUMN version;
    ALTER TABLE location DROP COLUMN version;
    ALTER TABLE manager_credentials DROP COLUMN version;
    ALTER TABLE monitoring_parameter DROP COLUMN version;
    ALTER TABLE network DROP COLUMN version;
    ALTER TABLE network_service_deployment_flavour DROP COLUMN version;
    ALTER TABLE nfvimage DROP COLUMN version;
    ALTER TABLE plugin_endpoint DROP COLUMN version;
    ALTER TABLE policy DROP COLUMN version;
    ALTER TABLE quota DROP COLUMN version;
    ALTER TABLE requires_parameters DROP COLUMN version;
    ALTER TABLE scaling_action DROP COLUMN version;
    ALTER TABLE scaling_alarm DROP COLUMN version;
    ALTER TABLE script DROP COLUMN version;
    ALTER TABLE security DROP COLUMN version;
    ALTER TABLE service_metadata DROP COLUMN version;
    ALTER TABLE subnet DROP COLUMN version;
    ALTER TABLE users DROP COLUMN version;
    ALTER TABLE vdudependency DROP COLUMN version;
    ALTER TABLE vim_instance DROP COLUMN version;
    ALTER TABLE virtual_deployment_unit DROP COLUMN version;
    ALTER TABLE vnfcinstance DROP COLUMN version;
    ALTER TABLE vnfdconnection_point DROP COLUMN version;
    ALTER TABLE vnfdependency DROP COLUMN version;
    ALTER TABLE vnfdeployment_flavour DROP COLUMN version;

  END //
DELIMITER ;
CALL `?`();
DROP PROCEDURE `?`;
