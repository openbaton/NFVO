DROP PROCEDURE IF EXISTS `?`;
DELIMITER //
CREATE PROCEDURE `?`
  (
  )
  BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;


    ALTER TABLE criteria DROP COLUMN version;
    ALTER TABLE monitoring_parameter DROP COLUMN version;
    ALTER TABLE fault_management_policy DROP COLUMN version;
    ALTER TABLE fault_management_policy DROP COLUMN dtype;
    ALTER TABLE high_availability DROP COLUMN geo_redundancy;

  END //
DELIMITER ;
CALL `?`();
DROP PROCEDURE `?`;
