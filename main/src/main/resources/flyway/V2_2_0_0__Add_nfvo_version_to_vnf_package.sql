ALTER TABLE vnfpackage ADD nfvo_version varchar(255);
UPDATE vnfpackage SET nfvo_version = '2.2.1' WHERE (nfvo_version = '' OR nfvo_version is NULL);
