DROP DATABASE IF EXISTS gamificationdb;

CREATE DATABASE gamificationdb
  WITH OWNER = gameadmin
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'English_United States.1252'
       LC_CTYPE = 'English_United States.1252'
       CONNECTION LIMIT = -1;

GRANT CONNECT, TEMPORARY ON DATABASE gamificationdb TO public;
GRANT ALL ON DATABASE gamificationdb TO gameadmin;
GRANT ALL ON DATABASE gamificationdb TO gameuser;