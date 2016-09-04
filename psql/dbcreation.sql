DROP DATABASE IF EXISTS gamificationdb;

CREATE DATABASE gamificationdb;

GRANT CONNECT, TEMPORARY ON DATABASE gamificationdb TO public;
GRANT ALL ON DATABASE gamificationdb TO gameadmin;
GRANT ALL ON DATABASE gamificationdb TO gameuser;