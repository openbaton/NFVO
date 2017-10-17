alter table vnfm_manager_endpoint drop column version;

ALTER TABLE network ADD COLUMN ext_shared BOOL;

DROP PROCEDURE IF EXISTS sharedChange;
DELIMITER ;;


CREATE PROCEDURE sharedChange()
    BEGIN
        DECLARE done INT DEFAULT FALSE;
        DECLARE oldShared BOOL;
        DECLARE net_id VARCHAR(255);

        DECLARE cursor_i CURSOR FOR SELECT id, shared FROM network;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

        OPEN cursor_i;

        read_loop: LOOP
            FETCH cursor_i INTO net_id, oldShared;
            IF done THEN
                LEAVE read_loop;
            END IF;

            UPDATE network SET ext_shared=oldShared where id=net_id;
            UPDATE network SET shared=false where id=net_id;

        END LOOP;
        CLOSE cursor_i;
    END;
;;
DELIMITER ;

CALL sharedChange();