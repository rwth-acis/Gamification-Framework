-- user : gameadmin
-- usergroup : gameuser

CREATE SCHEMA manager AUTHORIZATION gamification;
CREATE SCHEMA global_leaderboard AUTHORIZATION gamification;
GRANT ALL ON SCHEMA manager TO gamification;

CREATE TABLE manager.game_info
(
  game_id character varying(20) NOT NULL,
  community_type character varying(20),
  description character varying(100),
  CONSTRAINT game_id PRIMARY KEY (game_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE manager.game_info OWNER TO gamification;

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
ALTER TABLE manager.member_info OWNER TO gamification;

CREATE TABLE manager.member_game
(
  member_id character varying(20) NOT NULL,
  game_id character varying(20) NOT NULL,
  CONSTRAINT member_game_pk PRIMARY KEY (game_id, member_id),
  CONSTRAINT game_id FOREIGN KEY (game_id)
      REFERENCES manager.game_info (game_id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT member_id FOREIGN KEY (member_id)
      REFERENCES manager.member_info (member_id) ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE manager.member_game
  OWNER TO gamification;

-- Index: manager.fki_member_id

CREATE INDEX fki_member_id
  ON manager.member_game
  USING btree
  (member_id COLLATE pg_catalog."default");

-- Functions

CREATE OR REPLACE FUNCTION create_new_game(new_schema text) RETURNS void AS
$BODY$
DECLARE
  completed text;
  revealed text;
  hidden text;
  comm_type text;
BEGIN
	EXECUTE 'CREATE SCHEMA ' || new_schema ||';';

	EXECUTE 'CREATE TABLE ' || new_schema || '.member (
	  member_id character varying(20) NOT NULL
	, first_name character varying(20)
	, last_name character varying(20)
	, email character varying(50)
	, CONSTRAINT member_id PRIMARY KEY (member_id)
	);';

	EXECUTE 'CREATE TABLE ' || new_schema || '.badge (
	  badge_id character varying(20) NOT NULL
	, name character varying(20) NOT NULL
	, description character varying(100)
	, use_notification boolean
	, notif_message character varying
	, CONSTRAINT badge_id PRIMARY KEY (badge_id)
	);';

	EXECUTE 'CREATE TABLE ' || new_schema || '.achievement (
	  achievement_id character varying(20) NOT NULL
	, name character varying(20) NOT NULL
	, description character varying(100)
	, point_value integer NOT NULL DEFAULT 0
	, badge_id character varying(20)
	, use_notification boolean
	, notif_message character varying
	, CONSTRAINT achievement_id PRIMARY KEY (achievement_id)
	, CONSTRAINT badge_id FOREIGN KEY (badge_id)
	      REFERENCES '|| new_schema ||'.badge (badge_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';

	  completed := 'COMPLETED';
	  revealed := 'REVEALED';
	  hidden := 'HIDDEN';

	EXECUTE 'CREATE TYPE ' || new_schema || '.quest_status AS ENUM ('|| quote_literal(completed) ||','|| quote_literal(revealed) ||','|| quote_literal(hidden) ||');';


	EXECUTE 'CREATE TABLE ' || new_schema || '.quest (
	  quest_id character varying(20) NOT NULL
	, name character varying(20) NOT NULL
	, description character varying(100)
	, status ' || new_schema || '.quest_status DEFAULT ''REVEALED''
	, achievement_id character varying(20)
	, quest_flag boolean DEFAULT false
	, quest_id_completed character varying(20) NULL
	, point_flag boolean DEFAULT false
	, point_value integer DEFAULT 0
	, use_notification boolean
	, notif_message character varying
	, CONSTRAINT quest_id PRIMARY KEY (quest_id)
	, CONSTRAINT achievement_id FOREIGN KEY (achievement_id)
	      REFERENCES ' || new_schema || '.achievement (achievement_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT quest_id_completed FOREIGN KEY (quest_id_completed)
	      REFERENCES ' || new_schema || '.quest (quest_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CHECK (point_value >= 0)
	);';
	

	EXECUTE 'CREATE TABLE ' || new_schema || '.level (
	  level_num integer NOT NULL
	, name character varying(20) NOT NULL
	, point_value integer NOT NULL DEFAULT 0
	, use_notification boolean
	, notif_message character varying
	, CONSTRAINT level_num PRIMARY KEY (level_num)
	, CHECK (point_value >= 0)
	);

 	INSERT INTO ' || new_schema || '.level VALUES (0,''START'',0);
	';

	EXECUTE 'CREATE TABLE ' || new_schema || '.action (
	  action_id character varying(20) NOT NULL
	, name character varying(20) NOT NULL
	, description character varying(100)
	, point_value integer NOT NULL DEFAULT 0
	, use_notification boolean
	, notif_message character varying
	, CONSTRAINT action_id PRIMARY KEY (action_id)
	);';

	-- one to one
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_level (
	  member_id character varying(20) NOT NULL
	, level_num integer NOT NULL DEFAULT 0
	, CONSTRAINT member_level_pkey PRIMARY KEY (member_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT level_num FOREIGN KEY (level_num)
	      REFERENCES ' || new_schema || '.level (level_num) ON UPDATE CASCADE ON DELETE CASCADE  -- explicit pk
	);';


	-- one to one
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_point (
	  member_id character varying(20) NOT NULL
	, point_value integer NOT NULL DEFAULT 0
	, CONSTRAINT member_point_pkey PRIMARY KEY (member_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CHECK (point_value >= 0)
	);';


	-- m to m
	-- unique relation (member, badge)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_badge (
	  member_id character varying(20) NOT NULL
	, badge_id character varying(20) NOT NULL
	, CONSTRAINT member_badge_pkey PRIMARY KEY (member_id, badge_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES '|| new_schema ||'.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT badge_id FOREIGN KEY (badge_id)
	      REFERENCES '|| new_schema ||'.badge (badge_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
	);';

	-- m to m
	-- unique relation (member, achievement)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_achievement (
	  member_id character varying(20) NOT NULL
	, achievement_id character varying(20) NOT NULL
	, CONSTRAINT member_achievement_pkey PRIMARY KEY (member_id, achievement_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT achievement_id FOREIGN KEY (achievement_id)
	      REFERENCES ' || new_schema || '.achievement (achievement_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
	);';


	-- m to m
	-- unique relation (member, quest)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_quest (
	  member_id character varying(20) NOT NULL
	, quest_id character varying(20) NOT NULL
	, status ' || new_schema || '.quest_status DEFAULT ''REVEALED''
	, CONSTRAINT member_quest_pkey PRIMARY KEY (member_id, quest_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT quest_id FOREIGN KEY (quest_id)
	      REFERENCES ' || new_schema || '.quest (quest_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
	);';
	

	-- m to m
	-- not unique relation (member,action)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_action (
	  id serial NOT NULL
	, member_id character varying(20) NOT NULL
	, action_id character varying(20) NOT NULL
	, CONSTRAINT member_action_pkey PRIMARY KEY (id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT action_id FOREIGN KEY (action_id)
	      REFERENCES ' || new_schema || '.action (action_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
	);';

	-- m to m
	-- unique relation (quest,action)
	-- times > 0
	EXECUTE 'CREATE TABLE ' || new_schema || '.quest_action (
	  quest_id character varying(20) NOT NULL
	, action_id character varying(20) NOT NULL
	, times integer NOT NULL DEFAULT 1
	, CONSTRAINT quest_action_pkey PRIMARY KEY (quest_id, action_id)
	, CONSTRAINT quest_id FOREIGN KEY (quest_id)
	      REFERENCES ' || new_schema || '.quest (quest_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT action_id FOREIGN KEY (action_id)
	      REFERENCES ' || new_schema || '.action (action_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CHECK (times > 0)
	);';

	-- m to m
	-- not unique relation (member,quest,action)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_quest_action (
	  member_id character varying(20) NOT NULL
	, quest_id character varying(20) NOT NULL
	, action_id character varying(20) NOT NULL
	, completed boolean DEFAULT false
	, CONSTRAINT member_quest_action_pkey PRIMARY KEY (member_id, quest_id, action_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT quest_action_id FOREIGN KEY (quest_id, action_id)
	      REFERENCES ' || new_schema || '.quest_action (quest_id, action_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';
--, CONSTRAINT action_id FOREIGN KEY (action_id)
--	      REFERENCES ' || new_schema || '.quest_action (action_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk

	EXECUTE 'CREATE TYPE ' || new_schema || '.notification_type AS ENUM (''BADGE'',''ACHIEVEMENT'',''QUEST'',''LEVEL'',''STREAK'');';
	EXECUTE 'CREATE TABLE ' || new_schema || '.notification (
	  member_id character varying(20) NOT NULL
	, type ' || new_schema || '.notification_type
	, type_id character varying (20) NOT NULL
	, use_notification boolean
	, message character varying
	, CONSTRAINT notification_pkey PRIMARY KEY (member_id , type_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';

	-- Create global leaderboard  community type table
	EXECUTE 'SELECT community_type FROM manager.game_info WHERE game_id = '||quote_literal(new_schema)||'' INTO comm_type;
	EXECUTE 'CREATE TABLE IF NOT EXISTS global_leaderboard.'||comm_type||'(
	  member_id character varying(20) NOT NULL
	, point_value integer NOT NULL DEFAULT 0
	, CONSTRAINT '||comm_type||'_pkey PRIMARY KEY (member_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES manager.member_info (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CHECK (point_value >= 0)
	);';
	
	
	-- TODO
	  active := 'ACTIVE';
	  paused := 'PAUSED';
	  failed := 'FAILED';

	EXECUTE 'CREATE TYPE ' || new_schema || '.streak_status AS ENUM ('|| quote_literal(active) ||','|| quote_literal(paused) ||','|| quote_literal(failed) ||');';
	
	EXECUTE 'CREATE TABLE ' || new_schema || '.streak (
	  streak_id character varying(20) NOT NULL
	, name character varying(20) NOT NULL
	, description character varying(100)
	, status ' || new_schema || '.streak_status DEFAULT ''ACTIVE''
	, achievement_id character varying(20)
	, point_flag boolean DEFAULT false
	, point_value integer DEFAULT 0
	, use_notification boolean
	, notif_message character varying
	, CONSTRAINT streak_id PRIMARY KEY (streak_id)
	, CONSTRAINT achievement_id FOREIGN KEY (achievement_id)
	      REFERENCES ' || new_schema || '.achievement (achievement_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CHECK (point_value >= 0)
	);';
	
	
	-- TODO
	-- m to m
	-- unique relation (member, streak)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_streak (
	  member_id character varying(20) NOT NULL
	, streak_id character varying(20) NOT NULL
	, status ' || new_schema || '.streak_status DEFAULT ''ACTIVE''
	, CONSTRAINT member_streak_pkey PRIMARY KEY (member_id, streak_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT streak_id FOREIGN KEY (streak_id)
	      REFERENCES ' || new_schema || '.streak (streak_id) ON UPDATE CASCADE ON DELETE CASCADE   -- explicit pk
	);';


	-- trigger

	EXECUTE 'SELECT create_trigger_pointquest_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_action_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_rewards_completed_quest(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_member_achievement_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_member_badge_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_member_level_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_global_leaderboard_table_update(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_update_quest_constraint(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_update_quest_action_constraint(' || quote_literal(new_schema) || ');';
	-- TODO
	EXECUTE 'SELECT create_trigger_streak_observer(' || quote_literal(new_schema) || ');';

END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION delete_game(game_id text) RETURNS void AS
$BODY$
DECLARE
comm_type text;
_found int;
member_rec record;
BEGIN
	EXECUTE 'SELECT community_type FROM manager.game_info WHERE game_id = '||quote_literal(game_id)||'' INTO comm_type;
	RAISE NOTICE 'Community type : %', comm_type;

	-- remove member one by one to sync with global leaderboard
	FOR member_rec IN
		EXECUTE 'SELECT member_id FROM manager.member_game WHERE game_id = '||quote_literal(game_id)||''
	LOOP
		EXECUTE 'SELECT remove_member_from_game('||quote_literal(member_rec.member_id)||', '||quote_literal(game_id)||');';
	END LOOP;
	
	-- check comm type table, delete table if no game with specific community type
	EXECUTE 'DROP SCHEMA IF EXISTS '|| game_id ||' CASCADE;';
	-- drop table if no game with community type
	EXECUTE format($f$SELECT 1 FROM manager.game_info WHERE  community_type = '%s'$f$, comm_type);
	GET DIAGNOSTICS _found = ROW_COUNT;
	IF _found > 0 THEN
		-- There is still an game with the community type comm_type, do nothing
		--EXECUTE 'UPDATE global_leaderboard.'|| comm_type ||' SET point_value = '|| NEW.point_value ||' WHERE member_id = '|| quote_literal(NEW.member_id) ||';';
	ELSE
		RAISE NOTICE 'Found zero --> %', comm_type;
		-- No more game with the community type comm_type, drop table
		EXECUTE 'DROP TABLE global_leaderboard.'|| comm_type ||';';
	END IF;
	EXECUTE 'DELETE FROM manager.game_info WHERE game_id = '||quote_literal(game_id)||';';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


CREATE OR REPLACE FUNCTION add_mock_data(schema text) RETURNS void AS
$BODY$
BEGIN
-- 	-- Populate tables with mock data
	DELETE FROM manager.member_info WHERE member_id = 'user1';
	DELETE FROM manager.member_info WHERE member_id = 'user2';
	DELETE FROM manager.member_info WHERE member_id = 'user3';
	DELETE FROM manager.member_info WHERE member_id = 'user4';

	DELETE FROM manager.member_game WHERE member_id = 'user1';
	DELETE FROM manager.member_game WHERE member_id = 'user2';
	DELETE FROM manager.member_game WHERE member_id = 'user3';
	DELETE FROM manager.member_game WHERE member_id = 'user4';

	DELETE FROM manager.game_info WHERE game_id = 'test';

	INSERT INTO manager.member_info VALUES ('user1','User','One','user1@example.com');
	INSERT INTO manager.member_info VALUES ('user2','User','One','user1@example.com');
	INSERT INTO manager.member_info VALUES ('user3','User','One','user1@example.com');
	INSERT INTO manager.member_info VALUES ('user4','User','One','user1@example.com');

	INSERT INTO manager.game_info VALUES ('test','desc','commtype');

	EXECUTE 'SELECT create_new_game('|| quote_literal(schema) ||');';

 	EXECUTE '
	INSERT INTO '|| schema ||'.badge VALUES (''badge1'',''Badge 1'',''The badge number 1'',true,''badge1messagethisis'');
	INSERT INTO '|| schema ||'.badge VALUES (''badge2'',''Badge 2'',''The badge number 2'',true,''badge2messagethisis'');
	INSERT INTO '|| schema ||'.badge VALUES (''badge3'',''Badge 3'',''The badge number 3'',false,''badge3messagethisis'');
	INSERT INTO '|| schema ||'.badge VALUES (''badge4'',''Badge 4'',''The badge number 4'',false,''badge4messagethisis'');
	INSERT INTO '|| schema ||'.badge VALUES (''badge5'',''Badge 5'',''The badge number 5'',false,''badge5messagethisis'');

	INSERT INTO '|| schema ||'.achievement VALUES (''achievement1'',''achievement 1'',''The achievement number 1'',1,''badge1'',false,''achievement1messagethisis'');
	INSERT INTO '|| schema ||'.achievement VALUES (''achievement2'',''achievement 2'',''The achievement number 2'',2,''badge2'',false,''achievement2messagethisis'');
	INSERT INTO '|| schema ||'.achievement VALUES (''achievement3'',''achievement 3'',''The achievement number 3'',3,''badge3'',true,''achievement3messagethisis'');
	INSERT INTO '|| schema ||'.achievement VALUES (''achievement4'',''achievement 4'',''The achievement number 4'',4,''badge4'',true,''achievement4messagethisis'');
	INSERT INTO '|| schema ||'.achievement VALUES (''achievement5'',''achievement 5'',''The achievement number 5'',5,''badge5'',true,''achievement5messagethisis'');

	INSERT INTO '|| schema ||'.level VALUES (1,''level 1'', 1,true,''level1messagethisis'');
	INSERT INTO '|| schema ||'.level VALUES (2,''level 2'', 2,false,''level2messagethisis'');
	INSERT INTO '|| schema ||'.level VALUES (3,''level 3'', 3,true,''level3messagethisis'');
	INSERT INTO '|| schema ||'.level VALUES (4,''level 4'', 4,false,''level4messagethisis'');
	INSERT INTO '|| schema ||'.level VALUES (5,''level 5'', 5,true,''level5messagethisis'');

	INSERT INTO '|| schema ||'.action VALUES (''action1'',''action1'',''The action number 1'',1,false,''action1messagethisis'');
	INSERT INTO '|| schema ||'.action VALUES (''action2'',''action2'',''The action number 2'',2,true,''action2messagethisis'');
	INSERT INTO '|| schema ||'.action VALUES (''action3'',''action3'',''The action number 3'',3,true,''action3messagethisis'');
	INSERT INTO '|| schema ||'.action VALUES (''action4'',''action4'',''The action number 4'',4,true,''action4messagethisis'');
	INSERT INTO '|| schema ||'.action VALUES (''action5'',''action5'',''The action number 5'',5,false,''action5messagethisis'');
	INSERT INTO '|| schema ||'.action VALUES (''actionquestid'',''actionquestid'',''The action quest id'',5,false,''the action quest id notification'');

	INSERT INTO ' || schema || '.quest VALUES (''quest1'',''Quest 1'',''The quest number 1'',''COMPLETED'',''achievement1'',false,NULL,false,5,true,''quest1messagethisis'');
	INSERT INTO ' || schema || '.quest VALUES (''quest2'',''Quest 2'',''The quest number 2'',''HIDDEN'',''achievement2'',true,''quest1'',true,10,true,''quest2messagethisis'');
	INSERT INTO ' || schema || '.quest VALUES (''quest4'',''Quest 4'',''The quest number 4'',''HIDDEN'',''achievement4'',false,NULL,true,20,false,''quest3messagethisis'');
	INSERT INTO ' || schema || '.quest VALUES (''quest3'',''Quest 3'',''The quest number 3'',''HIDDEN'',''achievement3'',false,NULL,true,15,false,''quest4messagethisis'');
	INSERT INTO ' || schema || '.quest VALUES (''quest5'',''Quest 5'',''The quest number 5'',''HIDDEN'',''achievement5'',true,''quest3'',false,0,true,''quest5messagethisis'');

	INSERT INTO ' || schema || '.quest_action VALUES (''quest1'',''action1'',1);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest1'',''action2'',2);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest1'',''action3'',3);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest2'',''action2'',2);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest2'',''action3'',1);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest3'',''action5'',1);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest4'',''action1'',2);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest4'',''action2'',2);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest4'',''action3'',2);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest5'',''action1'',1);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest5'',''action2'',1);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest5'',''action3'',1);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest5'',''action4'',1);
	INSERT INTO ' || schema || '.quest_action VALUES (''quest5'',''action5'',1);
	';

	EXECUTE 'SELECT init_member_to_game(''user1'','|| quote_literal(schema) ||');';
	EXECUTE 'SELECT init_member_to_game(''user2'','|| quote_literal(schema) ||');';
	EXECUTE 'SELECT init_member_to_game(''user3'','|| quote_literal(schema) ||');';
	EXECUTE 'SELECT init_member_to_game(''user4'','|| quote_literal(schema) ||');';

	EXECUTE '
	INSERT INTO ' || schema || '.member_achievement VALUES (''user1'',''achievement1'');
	INSERT INTO ' || schema || '.member_achievement VALUES (''user2'',''achievement2'');
	INSERT INTO ' || schema || '.member_achievement VALUES (''user3'',''achievement3'');
	INSERT INTO ' || schema || '.member_achievement VALUES (''user4'',''achievement4'');

	INSERT INTO ' || schema || '.member_badge VALUES (''user1'',''badge1'');
	INSERT INTO ' || schema || '.member_badge VALUES (''user2'',''badge2'');
	INSERT INTO ' || schema || '.member_badge VALUES (''user3'',''badge3'');
	INSERT INTO ' || schema || '.member_badge VALUES (''user4'',''badge4'');

	';

END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION init_member_to_game(member_id text, game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'DELETE FROM manager.member_game WHERE member_id = '|| quote_literal(member_id) ||' AND game_id = '|| quote_literal(game_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member WHERE member_id = '|| quote_literal(member_id) ||';';

	-- add and copy member info
	EXECUTE 'INSERT INTO manager.member_game (member_id, game_id) VALUES ( '|| quote_literal(member_id) ||', '|| quote_literal(game_id) ||');';
	EXECUTE 'INSERT INTO '|| game_id ||'.member (member_id, first_name, last_name, email)
		(SELECT member_id, first_name, last_name, email FROM manager.member_info WHERE member_id='||quote_literal(member_id)||');';
	-- initialize relation table
	-- Point
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_point VALUES('|| quote_literal(member_id) ||',0);';
-- 	-- Level
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_level VALUES('|| quote_literal(member_id) ||',0);';
-- 	-- Quest
-- 	-- Cross join member_id with (quest_ids and statuses)
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_quest (member_id, quest_id, status)
	WITH tab1 as (SELECT * FROM '|| game_id ||'.member CROSS JOIN '|| game_id ||'.quest WHERE member_id='|| quote_literal(member_id) ||')
	SELECT  member_id, quest_id, status FROM tab1;
 	';
	-- Badge
	-- Added in the runtime

	-- Action
	-- initialize table member_quest_action
	EXECUTE 'INSERT INTO '|| game_id ||'.member_quest_action
		WITH newtab as (SELECT * FROM '|| game_id ||'.quest_action CROSS JOIN '|| game_id ||'.member)
		SELECT member_id, quest_id, action_id FROM newtab WHERE member_id='|| quote_literal(member_id) ||' ORDER BY member_id ;';
		
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak (member_id, streak_id, status)
	WITH tab1 as (SELECT * FROM '|| game_id ||'.member CROSS JOIN '|| game_id ||'.quest WHERE member_id='|| quote_literal(member_id) ||')
	SELECT  member_id, quest_id, status FROM tab1;
 	';

	-- Clean up notification initialization
	EXECUTE 'DELETE FROM '|| game_id ||'.notification WHERE type_id = ''0'';';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION remove_member_from_game(member_id text, game_id text) RETURNS void AS
$BODY$
DECLARE
comm_type text;
BEGIN
	EXECUTE 'SELECT community_type FROM manager.game_info WHERE game_id = '||quote_literal(game_id)||'' INTO comm_type;
	RAISE NOTICE 'Community type : %', comm_type;
	EXECUTE 'DELETE FROM global_leaderboard.'|| comm_type ||' WHERE member_id = '||quote_literal(member_id)||';';

	
	EXECUTE 'DELETE FROM manager.member_game WHERE member_id = '|| quote_literal(member_id) ||' AND game_id = '|| quote_literal(game_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_point WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_achievement WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_action WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_badge WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_level WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_point WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_quest WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_streak WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member_quest_action WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member WHERE member_id = '|| quote_literal(member_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.notification WHERE member_id = '|| quote_literal(member_id) ||';';


END;
$BODY$
LANGUAGE plpgsql VOLATILE;


-- Trigger to reveal quest if some point reached
CREATE OR REPLACE FUNCTION update_quest_status_with_point() RETURNS trigger AS
$BODY$
DECLARE
p_quest record;
game_id character varying(20);
current_level integer;
BEGIN
	game_id = TG_TABLE_SCHEMA;
	-- Only point constraint

	FOR p_quest IN
		EXECUTE 'SELECT quest_id FROM '|| game_id ||'.quest WHERE point_flag=true AND quest_flag=false AND '|| NEW.point_value ||' >= point_value'
	LOOP
		RAISE NOTICE 'myplpgsqlval is currently %', p_quest;       -- either this

		EXECUTE 'UPDATE '|| game_id ||'.member_quest SET status=''REVEALED'' WHERE '|| game_id ||'.member_quest.quest_id='|| quote_literal(p_quest.quest_id) ||' AND '|| game_id ||'.member_quest.status=''HIDDEN'' AND '|| game_id ||'.member_quest.member_id = '|| quote_literal(NEW.member_id)||';';
	END LOOP;

	-- -- Point and quest constraint
	FOR p_quest IN
		EXECUTE 'WITH temp AS (SELECT quest_id FROM '|| game_id ||'.member_quest WHERE status = ''COMPLETED'' AND member_id = '|| quote_literal(NEW.member_id) ||')
		SELECT '|| game_id ||'.quest.quest_id FROM '|| game_id ||'.quest INNER JOIN temp ON ('|| game_id ||'.quest.quest_id_completed = temp.quest_id) WHERE '|| NEW.point_value ||' >= '|| game_id ||'.quest.point_value AND
		'|| game_id || '.quest.point_flag=true AND '|| game_id || '.quest.quest_flag=true'
	LOOP
		raise notice 'Value: %', p_quest;       -- either this
		EXECUTE 'UPDATE '|| game_id ||'.member_quest SET status=''REVEALED'' WHERE '|| game_id ||'.member_quest.quest_id='|| quote_literal(p_quest.quest_id) ||' AND '|| game_id ||'.member_quest.status=''HIDDEN'' AND '|| game_id ||'.member_quest.member_id = '|| quote_literal(NEW.member_id) ||';' ;
	END LOOP;

	-- check level, change if point reach the treshold
	EXECUTE 'SELECT level_num FROM '|| game_id || '.level WHERE point_value <= '|| NEW.point_value ||' ORDER BY level_num DESC LIMIT 1;' INTO current_level;
	raise notice 'Value level: %', current_level;
	EXECUTE 'UPDATE '|| game_id || '.member_level SET level_num = '|| current_level ||' WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND level_num != '|| current_level ||';';

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- Trigger to reveal quest if other quest completed
CREATE OR REPLACE FUNCTION update_quest_status_with_quest() RETURNS trigger AS
$BODY$
DECLARE
p_quest record;
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;
	-- Only point constraint

	FOR p_quest IN
		EXECUTE 'SELECT quest_id FROM '|| game_id ||'.quest WHERE point_flag=false AND quest_flag=true AND quest_id_completed = '|| quote_literal(NEW.quest_id) ||' AND '|| quote_literal(NEW.status) ||' = ''COMPLETED'' '
	LOOP
		RAISE NOTICE 'myplpgsqlval is currently %', NEW;       -- either this

		EXECUTE 'UPDATE '|| game_id ||'.member_quest SET status=''REVEALED'' WHERE '|| game_id ||'.member_quest.quest_id='|| quote_literal(p_quest.quest_id) ||' AND '|| game_id ||'.member_quest.member_id = '|| quote_literal(NEW.member_id)||';';
	END LOOP;

	-- -- Point and quest constraint
	FOR p_quest IN
		EXECUTE 'WITH temp as (SELECT * FROM '|| game_id ||'.quest WHERE '|| game_id ||'.quest.quest_id_completed = '|| quote_literal(NEW.quest_id) ||' AND
										'|| game_id ||'.quest.point_flag = true AND
										'|| game_id ||'.quest.quest_flag = true AND
										'|| quote_literal(NEW.status) ||' = ''COMPLETED'')
		SELECT quest_id FROM temp WHERE (SELECT point_value FROM '|| game_id ||'.member_point WHERE member_id='|| quote_literal(NEW.member_id) ||' LIMIT 1) >= point_value;'
	LOOP
		raise notice 'Value: %', p_quest;       -- either this
		EXECUTE 'UPDATE '|| game_id ||'.member_quest SET status=''REVEALED'' WHERE '|| game_id ||'.member_quest.quest_id='|| quote_literal(p_quest.quest_id) ||' AND '|| game_id ||'.member_quest.status=''HIDDEN'' AND '|| game_id ||'.member_quest.member_id = '|| quote_literal(NEW.member_id) ||';' ;
	END LOOP;

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_pointquest_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER point_threshold
		AFTER UPDATE ON '|| game_id ||'.member_point
		FOR EACH ROW
		EXECUTE PROCEDURE update_quest_status_with_point();';

	EXECUTE 'CREATE TRIGGER quest_threshold
		AFTER UPDATE ON '|| game_id ||'.member_quest
		FOR EACH ROW
		EXECUTE PROCEDURE update_quest_status_with_quest();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- Action observer
CREATE OR REPLACE FUNCTION update_quest_status_with_action() RETURNS trigger AS
$BODY$
DECLARE
p_quest record;
action_count integer;
comp_result integer;
current_point integer;
point_action integer;
quests_use_action character varying(15);
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	RAISE NOTICE 'ACTION TRIGGERED';
	-- count how many action_id user has performed
	-- get quests affected by the performed action
	FOR p_quest IN
		EXECUTE '
		SELECT quest_id FROM '|| game_id ||'.quest_action WHERE action_id = '|| quote_literal(NEW.action_id) ||'
		AND (SELECT count(action_id) FROM '|| game_id ||'.member_action WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND action_id='|| quote_literal(NEW.action_id) ||') >= times'

	LOOP
		EXECUTE 'UPDATE '|| game_id ||'.member_quest_action SET completed=true WHERE quest_id='|| quote_literal(p_quest.quest_id) ||'
			AND member_id = '|| quote_literal(NEW.member_id)||' AND action_id='|| quote_literal(NEW.action_id) || ';';
		--check completed quest
		EXECUTE 'UPDATE '|| game_id ||'.member_quest SET status=''COMPLETED'' WHERE  member_id='|| quote_literal(NEW.member_id) ||'
			AND quest_id='||quote_literal(p_quest.quest_id)||' AND '|| game_id ||'.member_quest.status = ''REVEALED''
			AND (SELECT bool_and(completed) FROM '|| game_id ||'.member_quest_action WHERE quest_id='|| quote_literal(p_quest.quest_id) ||' AND member_id='|| quote_literal(NEW.member_id) ||');';
	END LOOP;
  -- Update member point
  EXECUTE 'SELECT point_value FROM '|| game_id ||'.action WHERE action_id = '|| quote_literal(NEW.action_id) ||'' INTO point_action;
  -- get current point value
	EXECUTE 'SELECT point_value FROM '|| game_id ||'.member_point WHERE member_id = '|| quote_literal(NEW.member_id) ||';' INTO current_point;
	-- check point if less than 0
	comp_result := current_point + point_action;
	RAISE NOTICE 'comp_result : %', comp_result;
	IF comp_result < 0 THEN
			EXECUTE 'UPDATE '|| game_id ||'.member_point SET point_value = 0 WHERE member_id = '|| quote_literal(NEW.member_id) ||';';
	ELSE
		-- add point
			EXECUTE 'UPDATE '|| game_id ||'.member_point SET point_value = '|| comp_result ||' WHERE member_id = '|| quote_literal(NEW.member_id) ||';';
	END IF;
 
	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_action_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER action_observer
		AFTER INSERT ON '|| game_id ||'.member_action
		FOR EACH ROW
		EXECUTE PROCEDURE update_quest_status_with_action();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

--TODO

CREATE OR REPLACE FUNCTION create_trigger_streak_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER streak_observer
		AFTER INSERT ON '|| game_id ||'.member_streak
		FOR EACH ROW
		EXECUTE PROCEDURE update_streak_status_with_action();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- TODO gives rewards when streak increased

-- Trigger if a quest completed then the achievement rewards are given as well as notification

CREATE OR REPLACE FUNCTION give_rewards_when_quest_completed() RETURNS trigger AS
$BODY$
DECLARE
current_point integer;
comp_result integer;
ach record;
point_obtained integer;
achievement_id character varying(20);
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	IF (NEW.status = 'COMPLETED') THEN
		-- insert quest into notification table
		EXECUTE 'DROP TABLE IF EXISTS '|| game_id ||'.temp;';
		EXECUTE 'CREATE TABLE '|| game_id ||'.temp (type '|| game_id ||'.notification_type);';
		EXECUTE 'INSERT INTO '|| game_id ||'.temp VALUES(''QUEST'');';
		EXECUTE 'INSERT INTO '|| game_id ||'.notification (member_id, type_id, use_notification, message, type)
			WITH res as (SELECT member_id, '|| game_id ||'.quest.quest_id,use_notification,notif_message FROM '|| game_id ||'.member_quest INNER JOIN '|| game_id ||'.quest
			ON ('|| game_id ||'.member_quest.quest_id = '|| game_id ||'.quest.quest_id) WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND '|| game_id ||'.member_quest.status = ''COMPLETED'' AND '|| game_id ||'.member_quest.quest_id = '|| quote_literal(NEW.quest_id) ||') SELECT * FROM res CROSS JOIN '|| game_id ||'.temp ;';
		--
		-- Get the achievement of a quest
		FOR ach IN
			EXECUTE 'SELECT achievement_id FROM '|| game_id ||'.quest WHERE quest_id = '|| quote_literal(NEW.quest_id)

		LOOP
			-- insert to member_achievement
			EXECUTE 'INSERT INTO '|| game_id ||'.member_achievement (member_id, achievement_id) VALUES ('|| quote_literal(NEW.member_id) ||','|| quote_literal(ach.achievement_id) ||');';
			-- insert to member_badge
			EXECUTE 'INSERT INTO '|| game_id ||'.member_badge (member_id, badge_id) SELECT member_id,badge_id FROM '|| game_id ||'.member_achievement,'|| game_id ||'.achievement WHERE '|| game_id ||'.achievement.achievement_id = '|| quote_literal(ach.achievement_id) ||' AND '|| game_id ||'.achievement.badge_id IS NOT NULL
			AND '|| game_id ||'.member_achievement.member_id = '|| quote_literal(NEW.member_id) ||' AND '|| game_id ||'.member_achievement.achievement_id = '|| quote_literal(ach.achievement_id) ||';';
			-- get point obtained
			EXECUTE 'SELECT point_value FROM '|| game_id ||'.achievement WHERE achievement_id = '|| quote_literal(ach.achievement_id) ||';' INTO point_obtained;
			-- get current point value
			EXECUTE 'SELECT point_value FROM '|| game_id ||'.member_point WHERE member_id = '|| quote_literal(NEW.member_id) ||';' INTO current_point;
			-- check point if less than 0
			comp_result = current_point + point_obtained;
			IF comp_result < 0 THEN
				EXECUTE 'UPDATE '|| game_id ||'.member_point SET point_value = 0 WHERE member_id = '|| quote_literal(NEW.member_id) ||';';
    		ELSE
			-- add point
				EXECUTE 'UPDATE '|| game_id ||'.member_point SET point_value =  '|| comp_result ||' WHERE member_id = '|| quote_literal(NEW.member_id) ||';';
			END IF;

		END LOOP;
	END IF;
	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_rewards_completed_quest(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER rewards_completed_quest
		AFTER UPDATE ON '|| game_id ||'.member_quest
		FOR EACH ROW
		EXECUTE PROCEDURE give_rewards_when_quest_completed();';
		
		-- TODO execute funtion strek- rewards
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- trigger member_badge observer

CREATE OR REPLACE FUNCTION member_badge_observer_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	-- Insert badge to notification table
	EXECUTE 'DROP TABLE IF EXISTS '|| game_id ||'.temp;';
	EXECUTE 'CREATE TABLE '|| game_id ||'.temp (type '|| game_id ||'.notification_type);';
	EXECUTE 'INSERT INTO '|| game_id ||'.temp VALUES(''BADGE'');';
	EXECUTE 'INSERT INTO '|| game_id ||'.notification (member_id, type_id, use_notification, message, type)
	WITH res as (SELECT member_id, '|| game_id ||'.member_badge.badge_id, use_notification,notif_message FROM '|| game_id ||'.member_badge INNER JOIN '|| game_id ||'.badge
	ON ('|| game_id ||'.member_badge.badge_id = '|| game_id ||'.badge.badge_id) WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND '|| game_id ||'.member_badge.badge_id = '|| quote_literal(NEW.badge_id) ||') SELECT * FROM res CROSS JOIN '|| game_id ||'.temp ;';


	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_member_badge_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER member_badge_observer
		AFTER INSERT ON '|| game_id ||'.member_badge
		FOR EACH ROW
		EXECUTE PROCEDURE member_badge_observer_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


-- trigger member_achievement observer
CREATE OR REPLACE FUNCTION member_achievement_observer_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	-- Insert badge to notification table
	EXECUTE 'DROP TABLE IF EXISTS '|| game_id ||'.temp;';
	EXECUTE 'CREATE TABLE '|| game_id ||'.temp (type '|| game_id ||'.notification_type);';
	EXECUTE 'INSERT INTO '|| game_id ||'.temp VALUES(''ACHIEVEMENT'');';
	EXECUTE 'INSERT INTO '|| game_id ||'.notification (member_id, type_id, use_notification, message, type)
	WITH res as (SELECT member_id, '|| game_id ||'.member_achievement.achievement_id, use_notification,notif_message FROM '|| game_id ||'.member_achievement INNER JOIN '|| game_id ||'.achievement
	ON ('|| game_id ||'.member_achievement.achievement_id = '|| game_id ||'.achievement.achievement_id) WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND '|| game_id ||'.member_achievement.achievement_id = '|| quote_literal(NEW.achievement_id) ||') SELECT * FROM res CROSS JOIN '|| game_id ||'.temp ;';


	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_member_achievement_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER member_achievement_observer
		AFTER INSERT ON '|| game_id ||'.member_achievement
		FOR EACH ROW
		EXECUTE PROCEDURE member_achievement_observer_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- trigger member_level observer
CREATE OR REPLACE FUNCTION member_level_observer_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	-- Insert level to notification table
	EXECUTE 'DROP TABLE IF EXISTS '|| game_id ||'.temp;';
	EXECUTE 'CREATE TABLE '|| game_id ||'.temp (type '|| game_id ||'.notification_type);';
	EXECUTE 'INSERT INTO '|| game_id ||'.temp VALUES(''LEVEL'');';
	EXECUTE 'INSERT INTO '|| game_id ||'.notification (member_id, type_id, use_notification, message, type)
	WITH res as (SELECT member_id, '|| game_id ||'.member_level.level_num, name, use_notification,notif_message FROM '|| game_id ||'.member_level INNER JOIN '|| game_id ||'.level
	ON ('|| game_id ||'.member_level.level_num = '|| game_id ||'.level.level_num) WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND '|| game_id ||'.member_level.level_num = '|| NEW.level_num ||')
	SELECT member_id, name, use_notification, notif_message,type FROM res CROSS JOIN '|| game_id ||'.temp ;';


	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_member_level_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER member_level_observer
		AFTER UPDATE ON '|| game_id ||'.member_level
		FOR EACH ROW
		EXECUTE PROCEDURE member_level_observer_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- GLOBAL LEADERBOARD

CREATE OR REPLACE FUNCTION global_leaderboard_table_update_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
comm_type text;
_found int;
BEGIN
	game_id = TG_TABLE_SCHEMA;
	-- Get community type
	EXECUTE 'SELECT community_type FROM manager.game_info WHERE game_id = '||quote_literal(game_id)||'' INTO comm_type;

	-- assume the table of community type is exist

	-- Check if the member already registered in global leaderboard
	EXECUTE format($f$SELECT 1 FROM global_leaderboard.%s WHERE  member_id = '%s'$f$, comm_type, NEW.member_id);
	GET DIAGNOSTICS _found = ROW_COUNT;
-- 	IF EXISTS EXECUTE '(SELECT * FROM global_leaderboard.'|| comm_type ||' WHERE member_id = '||quote_literal(NEW.member_id)||')' THEN
	IF _found > 0 THEN
		-- Update
		EXECUTE 'UPDATE global_leaderboard.'|| comm_type ||' SET point_value = '|| NEW.point_value ||' WHERE member_id = '|| quote_literal(NEW.member_id) ||';';
	ELSE
		-- Insert
		EXECUTE 'INSERT INTO global_leaderboard.'|| comm_type ||' VALUES ('|| quote_literal(NEW.member_id) ||','|| NEW.point_value ||');';
	END IF;

	-- sort the table

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_global_leaderboard_table_update(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER global_leaderboard_table_update
		AFTER UPDATE ON '|| game_id ||'.member_point
		FOR EACH ROW
		EXECUTE PROCEDURE global_leaderboard_table_update_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;



CREATE OR REPLACE FUNCTION update_quest_constraint_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	RAISE NOTICE 'GameId : %', game_id;
-- 	-- Quest
-- 	-- Cross join member_id with (quest_ids and statuses)
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_quest (member_id, quest_id, status)
	WITH tab1 as (SELECT * FROM '|| game_id ||'.member CROSS JOIN '|| game_id ||'.quest WHERE quest_id='|| quote_literal(NEW.quest_id) ||')
	SELECT  member_id, quest_id, status FROM tab1;';

	

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_update_quest_constraint(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER update_quest_constraint
		AFTER INSERT ON '|| game_id ||'.quest
		FOR EACH ROW
		EXECUTE PROCEDURE update_quest_constraint_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION update_quest_action_constraint_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	-- Action
	-- initialize table member_quest_action
	EXECUTE 'INSERT INTO '|| game_id ||'.member_quest_action
	WITH newtab as (SELECT * FROM '|| game_id ||'.quest_action CROSS JOIN '|| game_id ||'.member)
	SELECT member_id, quest_id, action_id FROM newtab WHERE quest_id='|| quote_literal(NEW.quest_id) ||' AND action_id='|| quote_literal(NEW.action_id) ||' ORDER BY member_id ;';


	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION create_trigger_update_quest_action_constraint(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER update_quest_action_constraint
		AFTER INSERT ON '|| game_id ||'.quest_action
		FOR EACH ROW
		EXECUTE PROCEDURE update_quest_action_constraint_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP SCHEMA IF EXISTS test CASCADE;
SELECT add_mock_data('test');
--SELECT init_member_to_game('user1','test');
--SELECT init_member_to_game('user2','test');
