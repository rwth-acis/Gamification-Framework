﻿DROP DATABASE IF EXISTS gamification;

CREATE DATABASE gamification;

CREATE ROLE gamification;
GRANT CONNECT, TEMPORARY ON DATABASE gamification TO gamification;
GRANT ALL ON DATABASE gamification TO gamification;
--GRANT ALL ON DATABASE gamificationdb TO gameuser;
