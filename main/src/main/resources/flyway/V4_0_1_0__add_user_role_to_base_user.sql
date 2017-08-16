use openbaton;

CREATE TABLE IF NOT EXISTS base_user_roles (
  base_user_id varchar(255),
  roles_id VARCHAR(255),
  primary key (base_user_id , roles_id));


DROP PROCEDURE IF EXISTS ReeBAR;
DELIMITER ;;


CREATE PROCEDURE ReeBAR()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE usr_id VARCHAR(255);
  DECLARE rl_id VARCHAR(255);
  DECLARE cursor_i CURSOR FOR SELECT users_id, roles_id FROM users_roles;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN cursor_i;

  read_loop: LOOP
    FETCH cursor_i INTO usr_id, rl_id;
    IF done THEN
        LEAVE read_loop;
    END IF;
    insert into base_user_roles(base_user_id, roles_id) values(usr_id,rl_id);

  END LOOP;
  CLOSE cursor_i;
END;
;;
DELIMITER ;

CALL ReeBAR();
--
-- SET usr_id := (SELECT users_id INTO usr_id FROM users_roles);
-- SET rl_id := (SELECT roles_id INTO rl_id FROM users_roles);
-- insert into base_user_roles(base_user_id, roles_id) values(usr_id,rl_id);
