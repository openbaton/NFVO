ALTER TABLE network_service_descriptor ADD nfvo_version varchar(255);
ALTER TABLE virtual_network_function_descriptor ADD nfvo_version varchar(255);
UPDATE network_service_descriptor SET nfvo_version = '3.2.0' WHERE (nfvo_version = '' OR nfvo_version is NULL);
UPDATE virtual_network_function_descriptor SET nfvo_version = '3.2.0' WHERE (nfvo_version = '' OR nfvo_version is NULL);
