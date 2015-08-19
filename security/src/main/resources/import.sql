insert into users (id, username, password, first_name, last_name, admin) values (1, 'admin', '$2a$12$iuHxv41fDHvrmEDloQGrNO7gLp0buMTaJ3eqZLc/ZS15iDizCxz.O','Super','Admin',true);
insert into users (id, username, password, first_name, last_name, admin) values (2, 'guest', '$2a$12$ctVDIw/LkWIMzb8uEegzb.2FPPRU30ImYjJ53tkXFHiC9vlufFC.q','Guest','Guest',false);

-- Username | Password | Is	Admin
-- guest      guest       No
-- Admin      openbaton   Yes
-- BCrypt for encryption the passwords

