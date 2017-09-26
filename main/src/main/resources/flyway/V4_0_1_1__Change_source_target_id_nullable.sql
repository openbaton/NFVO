
ALTER TABLE vnfdependency ADD source varchar(255);
ALTER TABLE vnfdependency ADD target varchar(255);

DROP PROCEDURE IF EXISTS dependency;
DELIMITER ;;


CREATE PROCEDURE dependency()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE dep_id VARCHAR(255);
    DECLARE src_id VARCHAR(255);
    DECLARE trg_id VARCHAR(255);

    DECLARE cursor_i CURSOR FOR SELECT id, source_id, target_id FROM vnfdependency;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cursor_i;

    read_loop: LOOP
      FETCH cursor_i INTO dep_id, src_id, trg_id;
      IF done THEN
        LEAVE read_loop;
      END IF;

      SET @source_name = (select name from virtual_network_function_descriptor where id=src_id);
      SET @target_name = (select name from virtual_network_function_descriptor where id=trg_id);

#       SELECT @source_name;

      UPDATE vnfdependency SET source=@source_name,target=@target_name where id=dep_id;

    END LOOP;
    CLOSE cursor_i;
  END;
;;
DELIMITER ;

CALL dependency();

ALTER TABLE vnfdependency MODIFY source_id varchar(255) NULL;
ALTER TABLE vnfdependency MODIFY target_id varchar(255) NULL;