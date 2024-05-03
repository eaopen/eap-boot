-- 语法
-- create database #databaseName;
-- create user '#userName'@'#host' identified by '#passWord';
-- grant #auth on #databaseName.#table to '#userName'@'#host';


create database eap-db;
create user 'eap'@'%' identified by 'eap';
grant all privileges on `eap-db` to 'eap'@'%';
