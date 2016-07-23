-- user : gameadmin
-- usergroup : gameuser

CREATE SCHEMA manager AUTHORIZATION gameadmin;

GRANT ALL ON SCHEMA manager TO gameadmin;
GRANT ALL ON SCHEMA manager TO gameuser;

CREATE TABLE manager.application_info
(
  app_id character varying(20) NOT NULL,
  community_type character varying(10),
  description character varying(50),
  CONSTRAINT app_id PRIMARY KEY (app_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE manager.application_info OWNER TO gameadmin;

CREATE TABLE manager.member_info
(
  member_id character varying(20) NOT NULL,
  first_name character varying(20),
  last_name character varying(20),
  email character varying(50),
  CONSTRAINT member_id PRIMARY KEY (member_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE manager.member_info OWNER TO gameadmin;

CREATE TABLE manager.member_application
(
  member_id character varying(20) NOT NULL,
  app_id character varying(20) NOT NULL,
  CONSTRAINT member_application_pk PRIMARY KEY (app_id, member_id),
  CONSTRAINT app_id FOREIGN KEY (app_id)
      REFERENCES manager.application_info (app_id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES manager.member_info (member_id) ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE manager.member_application
  OWNER TO gameadmin;

-- Index: manager.fki_member_id

CREATE INDEX fki_member_id
  ON manager.member_application
  USING btree
  (member_id COLLATE pg_catalog."default");

-- Template schema

CREATE SCHEMA template AUTHORIZATION gameadmin;

CREATE TABLE template.member (
  member_id character varying(20) NOT NULL
, first_name character varying(20)
, last_name character varying(20)
, email character varying(50)
, CONSTRAINT member_id PRIMARY KEY (member_id)
);

CREATE TABLE template.badge (
  badge_id character varying(15) NOT NULL
, name character varying(15) NOT NULL
, description character varying(50)
, image_path character varying
, CONSTRAINT badge_id PRIMARY KEY (badge_id)
);

CREATE TABLE template.achievement (
  achievement_id character varying(15) NOT NULL
, name character varying(15) NOT NULL
, description character varying(50)
, point_value integer NOT NULL
, badge_id character varying(15)
, CONSTRAINT achievement_id PRIMARY KEY (achievement_id)
, CONSTRAINT badge_id FOREIGN KEY (badge_id)
      REFERENCES template.badge (badge_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TYPE template.quest_status AS ENUM ('COMPLETED', 'REVEALED', 'HIDDEN');
CREATE TYPE template.quest_constraint AS (
    quest_flag boolean NOT NULL,
    quest_id_completed character varying(15),
    point_flag boolean NOT NULL,
    point_value integer,
    CONSTRAINT achievement_id FOREIGN KEY (achievement_id)
      REFERENCES template.achievement (achievement_id) ON UPDATE CASCADE ON DELETE CASCADE

);

CREATE TABLE template.quest (
  quest_id character varying(15) NOT NULL
, name character varying(15) NOT NULL
, description character varying(50)
, status template.quest_status
, achievement_id character varying(15)

, CONSTRAINT quest_id PRIMARY KEY (quest_id)
, CONSTRAINT achievement_id FOREIGN KEY (achievement_id)
      REFERENCES template.achievement (achievement_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE template.level (
  level_num integer NOT NULL
, name character varying(15) NOT NULL
, point_value integer NOT NULL
, CONSTRAINT level_num PRIMARY KEY (level_num)
);

CREATE TABLE template.action (
  action_id character varying(15) NOT NULL
, name character varying(15) NOT NULL
, description character varying(50)
, point_value integer NOT NULL
, CONSTRAINT action_id PRIMARY KEY (action_id)
);

CREATE TABLE template.member_level (
  member_id character varying(15) NOT NULL
, level_num character varying(15) NOT NULL
, UNIQUE (member_id)
, CONSTRAINT member_level_pkey PRIMARY KEY (member_id, level_num)
, CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES template.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
, CONSTRAINT level_num FOREIGN KEY (level_num)
      REFERENCES template.level (level_num) ON UPDATE CASCADE ON DELETE CASCADE  -- explicit pk
);

CREATE TABLE template.member_point (
  member_id character varying(15) NOT NULL
, point_value integer NOT NULL
, UNIQUE (member_id)
, CONSTRAINT member_point_pkey PRIMARY KEY (member_id)
, CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES template.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE template.member_badge (
  member_id character varying(15) NOT NULL
, badge_id character varying(15) NOT NULL
, CONSTRAINT member_badge_pkey PRIMARY KEY (member_id, badge_id)
, CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES template.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
, CONSTRAINT badge_id FOREIGN KEY (badge_id)
      REFERENCES template.badge (badge_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
);

CREATE TABLE template.member_achievement (
  member_id character varying(15) NOT NULL
, achievement_id character varying(15) NOT NULL
, CONSTRAINT member_achievement_pkey PRIMARY KEY (member_id, achievement_id)
, CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES template.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
, CONSTRAINT achievement_id FOREIGN KEY (achievement_id)
      REFERENCES template.achievement (achievement_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
);

CREATE TABLE template.member_quest (
  member_id character varying(15) NOT NULL
, quest_id character varying(15) NOT NULL
, CONSTRAINT member_quest_pkey PRIMARY KEY (member_id, quest_id)
, CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES template.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
, CONSTRAINT quest_id FOREIGN KEY (quest_id)
      REFERENCES template.quest (quest_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
);

CREATE TABLE template.member_action (
  member_id character varying(15) NOT NULL
, action_id character varying(15) NOT NULL
, CONSTRAINT member_action_pkey PRIMARY KEY (member_id, action_id)
, CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES template.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
, CONSTRAINT action_id FOREIGN KEY (action_id)
      REFERENCES template.action (action_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
);

CREATE TABLE template.quest_action (
  quest_id character varying(15) NOT NULL
, action_id character varying(15) NOT NULL
, CONSTRAINT quest_action_pkey PRIMARY KEY (quest_id, action_id)
, CONSTRAINT quest_id FOREIGN KEY (quest_id)
      REFERENCES template.quest (quest_id) ON UPDATE CASCADE ON DELETE CASCADE
, CONSTRAINT action_id FOREIGN KEY (action_id)
      REFERENCES template.action (action_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
);


CREATE OR REPLACE FUNCTION clone_schema(source_schema text, dest_schema text) RETURNS void AS
$BODY$
DECLARE 
  objeto text;
  buffer text;
BEGIN
    EXECUTE 'CREATE SCHEMA ' || dest_schema ;
 
    FOR objeto IN
        SELECT TABLE_NAME::text FROM information_schema.TABLES WHERE table_schema = source_schema
    LOOP        
        buffer := dest_schema || '.' || objeto;
        EXECUTE 'CREATE TABLE ' || buffer || ' (LIKE ' || source_schema || '.' || objeto || ' INCLUDING CONSTRAINTS INCLUDING INDEXES INCLUDING DEFAULTS)';
        EXECUTE 'INSERT INTO ' || buffer || '(SELECT * FROM ' || source_schema || '.' || objeto || ')';
    END LOOP;
 
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- Populate tables with mock data
INSERT INTO template.badge VALUES ('badge1','Badge 1','The badge number 1','http://badge1.example.com');
INSERT INTO template.badge VALUES ('badge2','Badge 2','The badge number 2','http://badge2.example.com');
INSERT INTO template.badge VALUES ('badge3','Badge 3','The badge number 3','http://badge3.example.com');
INSERT INTO template.badge VALUES ('badge4','Badge 4','The badge number 4','http://badge4.example.com');
INSERT INTO template.badge VALUES ('badge5','Badge 5','The badge number 5','http://badge5.example.com');

INSERT INTO template.achievement VALUES ('achievement1','achievement 1','The achievement number 1',1,'badge1');
INSERT INTO template.achievement VALUES ('achievement2','achievement 2','The achievement number 2',2,'badge2');
INSERT INTO template.achievement VALUES ('achievement3','achievement 3','The achievement number 3',3,'badge3');
INSERT INTO template.achievement VALUES ('achievement4','achievement 4','The achievement number 4',4,'badge4');
INSERT INTO template.achievement VALUES ('achievement5','achievement 5','The achievement number 5',5,'badge5');

INSERT INTO template.quest VALUES ('quest1','Quest 1','The quest number 1','Completed','achievement1');
INSERT INTO template.quest VALUES ('quest2','Quest 2','The quest number 2','Completed','achievement2');
INSERT INTO template.quest VALUES ('quest3','Quest 3','The quest number 3','Revealed','achievement3');
INSERT INTO template.quest VALUES ('quest4','Quest 4','The quest number 4','Hidden','achievement4');
INSERT INTO template.quest VALUES ('quest5','Quest 5','The quest number 5','Hidden','achievement5');

INSERT INTO template.level VALUES (1,'level 1', 1);
INSERT INTO template.level VALUES (2,'level 2', 2);
INSERT INTO template.level VALUES (3,'level 3', 3);
INSERT INTO template.level VALUES (4,'level 4', 4);
INSERT INTO template.level VALUES (5,'level 5', 5);

INSERT INTO template.action VALUES ('action1','action1','The action number 1',1);
INSERT INTO template.action VALUES ('action2','action2','The action number 2',2);
INSERT INTO template.action VALUES ('action3','action3','The action number 3',3);
INSERT INTO template.action VALUES ('action4','action4','The action number 4',4);
INSERT INTO template.action VALUES ('action5','action5','The action number 5',5);
