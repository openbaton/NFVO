
CREATE TABLE IF NOT EXISTS openstack_vim_instance LIKE vim_instance;
CREATE TABLE IF NOT EXISTS docker_vim_instance LIKE vim_instance;
CREATE TABLE IF NOT EXISTS generic_vim_instance LIKE vim_instance;

ALTER TABLE generic_vim_instance DROP COLUMN key_pair;
ALTER TABLE generic_vim_instance DROP COLUMN password;
ALTER TABLE generic_vim_instance DROP COLUMN tenant;
ALTER TABLE generic_vim_instance DROP COLUMN username;

INSERT openstack_vim_instance SELECT * FROM vim_instance where type="openstack";
INSERT docker_vim_instance SELECT * FROM vim_instance where type="docker";
INSERT generic_vim_instance SELECT id, hb_version, project_id, shared, active, auth_url, name, type, location_id FROM vim_instance where type!="docker" AND type!="openstack";

DROP TABLE IF EXISTS vim_instance_flavours, vim_instance_images, vim_instance_networks, vim_instance_security_groups;
DROP TABLE IF EXISTS vim_instance;