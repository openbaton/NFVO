# DROP PROCEDURE IF EXISTS `?`;
# DELIMITER //
# CREATE PROCEDURE `?`
#   (
#   )
#   BEGIN
#     DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
#
#
#     ALTER TABLE criteria DROP COLUMN version;
#     ALTER TABLE monitoring_parameter DROP COLUMN version;
#     ALTER TABLE fault_management_policy DROP COLUMN version;
#     ALTER TABLE fault_management_policy DROP COLUMN dtype;
#     ALTER TABLE high_availability DROP COLUMN geo_redundancy;
#
#   END //
# DELIMITER ;
# CALL `?`();
# DROP PROCEDURE `?`;

CREATE TABLE openstack_vim_instance LIKE vim_instance;
CREATE TABLE docker_vim_instance LIKE vim_instance;
CREATE TABLE generic_vim_instance LIKE vim_instance;
INSERT openstack_vim_instance SELECT * FROM vim_instance where type="openstack";
INSERT docker_vim_instance SELECT * FROM vim_instance where type="docker";
INSERT docker_vim_instance SELECT * FROM vim_instance where type!="docker" AND type!="openstack";
# RENAME TABLE vim_instance TO ;