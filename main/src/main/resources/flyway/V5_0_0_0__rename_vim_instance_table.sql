
CREATE TABLE openstack_vim_instance LIKE vim_instance;
CREATE TABLE docker_vim_instance LIKE vim_instance;
CREATE TABLE generic_vim_instance LIKE vim_instance;
INSERT openstack_vim_instance SELECT * FROM vim_instance where type="openstack";
INSERT docker_vim_instance SELECT * FROM vim_instance where type="docker";
INSERT docker_vim_instance SELECT * FROM vim_instance where type!="docker" AND type!="openstack";


DROP TABLE vim_instance_flavours, vim_instance_images, vim_instance_networks, vim_instance_security_groups;
DROP TABLE vim_instance;