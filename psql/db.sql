-- user : gameadmin
-- usergroup : gameuser

CREATE SCHEMA manager AUTHORIZATION gamification;
CREATE SCHEMA global_leaderboard AUTHORIZATION gamification;
GRANT ALL ON SCHEMA manager TO gamification;

drop schema if exists public CASCADE;
CREATE SCHEMA public AUTHORIZATION gamification;
GRANT ALL ON SCHEMA public TO gamification;

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
	  achievement_id character varying(50) NOT NULL
	, name character varying(50) NOT NULL
	, description character varying(100)
	, point_value integer NOT NULL DEFAULT 0
	, badge_id character varying(50)
	, use_notification boolean
	, notif_message character varying(50)
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
	
	----------------------------STREAK TABLES START------------------------------------------------------------------------------------------------------

	EXECUTE 'CREATE TYPE ' || new_schema || '.streak_status AS ENUM (''ACTIVE'',''PAUSED'',''FAILED'',''UPDATED'');';
	
	EXECUTE 'CREATE TABLE ' || new_schema || '.streak (
	  streak_id character varying(20) NOT NULL
	, name character varying(20) NOT NULL
	, description character varying(100)
	, streak_level integer NOT NULL DEFAULT 1
	, status ' || new_schema || '.streak_status DEFAULT ''ACTIVE''
	, point_th integer NOT NULL
	, locked_date TIMESTAMP WITHOUT TIME ZONE NOT NULL
	, due_date TIMESTAMP WITHOUT TIME ZONE NOT NULL
	, period INTERVAL NOT NULL
	, use_notification boolean
	, notif_message character varying
	, CONSTRAINT streak_id PRIMARY KEY (streak_id)
	, CHECK (point_th >= 0)
	);';
	
	EXECUTE 'CREATE TABLE ' || new_schema || '.streak_badge (
	  streak_id character varying(20) NOT NULL
	, streak_level integer NOT NULL DEFAULT 1
	, badge_id character varying(20)
	, CONSTRAINT streak_level_b_pkey PRIMARY KEY (streak_id, streak_level)
	, CONSTRAINT badge_id FOREIGN KEY (badge_id)
	      REFERENCES ' || new_schema || '.badge (badge_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';
	
	EXECUTE 'CREATE TABLE ' || new_schema || '.streak_achievement (
	  streak_id character varying(20) NOT NULL
	, streak_level integer NOT NULL DEFAULT 1
	, achievement_id character varying(20)
	, CONSTRAINT streak_level_a_pkey PRIMARY KEY (streak_id, streak_level)
	, CONSTRAINT achievement_id FOREIGN KEY (achievement_id)
	      REFERENCES ' || new_schema || '.achievement (achievement_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';
	
	-- m to m
	-- unique relation (quest,action)
	EXECUTE 'CREATE TABLE ' || new_schema || '.streak_action (
	  streak_id character varying(20) NOT NULL
	, action_id character varying(20) NOT NULL
	, CONSTRAINT streak_action_pkey PRIMARY KEY (streak_id, action_id)
	, CONSTRAINT streak_id FOREIGN KEY (streak_id)
	      REFERENCES ' || new_schema || '.streak (streak_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT action_id FOREIGN KEY (action_id)
	      REFERENCES ' || new_schema || '.action (action_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';
	
	-- m to m
	-- unique relation (member, streak)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_streak (
	  member_id character varying(20) NOT NULL
	, streak_id character varying(20) NOT NULL
	, status ' || new_schema || '.streak_status DEFAULT ''ACTIVE''
	, locked_date TIMESTAMP WITHOUT TIME ZONE NOT NULL
	, due_date TIMESTAMP WITHOUT TIME ZONE NOT NULL
	, current_streak_level integer NOT NULL DEFAULT 1
	, highest_streak_level integer NOT NULL DEFAULT 1
	, CONSTRAINT member_streak_pkey PRIMARY KEY (member_id, streak_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT streak_id FOREIGN KEY (streak_id)
	      REFERENCES ' || new_schema || '.streak (streak_id) ON UPDATE CASCADE ON DELETE CASCADE 
	);';

	-- m to m
	-- not unique relation (member,quest,action)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_streak_action (
	  member_id character varying(20) NOT NULL
	, streak_id character varying(20) NOT NULL
	, action_id character varying(20) NOT NULL
	, completed boolean DEFAULT false
	, CONSTRAINT member_streak_action_pkey PRIMARY KEY (member_id, streak_id, action_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT streak_action_id FOREIGN KEY (streak_id, action_id)
	      REFERENCES ' || new_schema || '.streak_action (streak_id, action_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';
	
	
	-- m to m
	-- not unique relation (member,quest,action)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_streak_badge (
	  member_id character varying(20) NOT NULL
	, streak_id character varying(20) NOT NULL
	, badge_id character varying(20) NOT NULL
	, streak_level integer NOT NULL DEFAULT 1
	, active boolean DEFAULT false
	, CONSTRAINT member_streak_badge_pkey PRIMARY KEY (member_id, streak_id, badge_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT streak_badge_id FOREIGN KEY (streak_id, streak_level)
	      REFERENCES ' || new_schema || '.streak_badge (streak_id, streak_level) ON UPDATE CASCADE ON DELETE CASCADE
	);';
	
	
	-- m to m
	-- not unique relation (member,quest,action)
	EXECUTE 'CREATE TABLE ' || new_schema || '.member_streak_achievement (
	  member_id character varying(20) NOT NULL
	, streak_id character varying(20) NOT NULL
	, achievement_id character varying(20) NOT NULL
	, streak_level integer NOT NULL DEFAULT 1
	, unlocked boolean DEFAULT false
	, CONSTRAINT member_streak_achievement_pkey PRIMARY KEY (member_id, streak_id, achievement_id)
	, CONSTRAINT member_id FOREIGN KEY (member_id)
	      REFERENCES ' || new_schema || '.member (member_id) ON UPDATE CASCADE ON DELETE CASCADE
	, CONSTRAINT streak_achievement_id FOREIGN KEY (streak_id, streak_level)
	      REFERENCES ' || new_schema || '.streak_achievement (streak_id, streak_level) ON UPDATE CASCADE ON DELETE CASCADE
	);';
	-----------------------------STREAK TABLES END------------------------------------------------------------------------------------------------------------

	-----------------------------~SUCCESS AWARENESS MODEL TABLES START------------------------------------------------------------------------------------------------------------
	EXECUTE 'CREATE TABLE ' || new_schema || '.success_awareness_gamified_measure (
		measure_name character varying(100) NOT NULL,
		action_id  character varying(20) NOT NULL UNIQUE,
		game_id character varying(20) NOT NULL,
		member_id character varying(20) NOT NULL,
		measure_xml character varying(1000) NOT NULL,
		CONSTRAINT success_awareness_gamified_pkey PRIMARY KEY(measure_name,game_id),
		CONSTRAINT member_id FOREIGN KEY (game_id,member_id) REFERENCES manager.member_game (game_id,member_id) ON UPDATE CASCADE ON DELETE CASCADE,
		CONSTRAINT action_id FOREIGN KEY (action_id) REFERENCES ' || new_schema || '.action (action_id) ON UPDATE CASCADE ON DELETE CASCADE
	);';
	-----------------------------SUCCESS AWARENESS MODEL TABLES END------------------------------------------------------------------------------------------------------------

	-----------------------------DEVOPS MODEL TABLES START------------------------------------------------------------------------------------------------------------
	EXECUTE 'CREATE TABLE ' || new_schema || '.devops_model (
		scope character varying(100) NOT NULL,
		action_id  character varying(20) NOT NULL UNIQUE,
		rating float NOT NULL,
		game_id character varying(20) NOT NULL,
		member_id character varying(20) NOT NULL,
		CONSTRAINT devops_model_pkey PRIMARY KEY(game_id,action_id),
		CONSTRAINT devops_model_member_id FOREIGN KEY (game_id,member_id) REFERENCES manager.member_game (game_id,member_id) ON UPDATE CASCADE ON DELETE CASCADE,
		CONSTRAINT devops_model_action_id FOREIGN KEY (action_id) REFERENCES ' || new_schema || '.action (action_id) ON UPDATE CASCADE ON DELETE CASCADE,
		CONSTRAINT ck_devops_model_rating CHECK(rating >= 0 AND rating <= 5),
		CONSTRAINT ck_devops_model_scope CHECK(scope=''code'' OR scope=''build'' OR scope=''test'' OR scope=''feedback'' OR scope=''deploy'' OR scope=''operate'' OR scope=''monitor'')
	);';
	-----------------------------DEVOPS MODEL MODEL TABLES END------------------------------------------------------------------------------------------------------------



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
	EXECUTE 'SELECT create_trigger_streak_action_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_member_streak_action_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_member_streak_observer(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_update_streak_action_constraint(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_update_streak_badge_constraint(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_update_streak_achievement_constraint(' || quote_literal(new_schema) || ');';
	EXECUTE 'SELECT create_trigger_update_streak_constraint(' || quote_literal(new_schema) || ');';



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


CREATE OR REPLACE FUNCTION init_member_to_game(member_id text, game_id text) RETURNS void AS
$BODY$
DECLARE
st_level integer;
streaks record;
BEGIN
	EXECUTE 'DELETE FROM manager.member_game WHERE member_id = '|| quote_literal(member_id) ||' AND game_id = '|| quote_literal(game_id) ||';';
	EXECUTE 'DELETE FROM '|| game_id ||'.member WHERE member_id = '|| quote_literal(member_id) ||';';

	-- add and copy member info
	EXECUTE 'INSERT INTO manager.member_game (member_id, game_id) VALUES ( '|| quote_literal(member_id) ||', '|| quote_literal(game_id) ||');';
	EXECUTE 'INSERT INTO '|| game_id ||'.member (member_id, first_name, last_name, email)
		(SELECT member_id, first_name, last_name, email FROM manager.member_info WHERE member_id='||quote_literal(member_id)||');';
	
	-- initialize table member_point
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_point VALUES('|| quote_literal(member_id) ||',0);';
	
 	-- initialize table member_level
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_level VALUES('|| quote_literal(member_id) ||',0);';

 	-- initialize table member_quest
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_quest (member_id, quest_id, status)
	WITH tab1 as (SELECT * FROM '|| game_id ||'.member CROSS JOIN '|| game_id ||'.quest WHERE member_id='|| quote_literal(member_id) ||')
	SELECT  member_id, quest_id, status FROM tab1;
 	';
	-- initialize table member_quest_action
	EXECUTE 'INSERT INTO '|| game_id ||'.member_quest_action
		WITH newtab as (SELECT * FROM '|| game_id ||'.quest_action CROSS JOIN '|| game_id ||'.member)
		SELECT member_id, quest_id, action_id FROM newtab WHERE member_id='|| quote_literal(member_id) ||' ORDER BY member_id ;';
	
	-- initialize table member_streak
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak (member_id, streak_id, status, locked_date, due_date, current_streak_level, highest_streak_level)
	WITH tab1 as (SELECT * FROM '|| game_id ||'.member CROSS JOIN '|| game_id ||'.streak WHERE member_id='|| quote_literal(member_id) ||')
	SELECT  member_id, streak_id, status, locked_date, due_date, streak_level, streak_level FROM tab1;
 	';
	
	-- initialize table member_streak_action
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak_action
		WITH newtab as (SELECT * FROM '|| game_id ||'.streak_action CROSS JOIN '|| game_id ||'.member)
		SELECT member_id, streak_id, action_id FROM newtab WHERE member_id='|| quote_literal(member_id) ||' ORDER BY member_id ;';
		
	-- initialize table member_streak_achievement ::after new streak_achievement was inserted
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak_achievement
	WITH newtab as (SELECT * FROM '|| game_id ||'.streak_achievement CROSS JOIN '|| game_id ||'.member)
	SELECT member_id, streak_id, achievement_id, streak_level FROM newtab WHERE member_id='|| quote_literal(member_id) ||' ORDER BY member_id ;';
	
	-- initialize table member_streak_badge ::after new streak_badge was inserted
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak_badge
	WITH newtab as (SELECT * FROM '|| game_id ||'.streak_badge CROSS JOIN '|| game_id ||'.member)
	SELECT member_id, streak_id, badge_id, streak_level FROM newtab WHERE member_id='|| quote_literal(member_id) ||' ORDER BY member_id ;';
	
	For streaks in
		EXECUTE 'SELECT streak_id, current_streak_level FROM ' ||game_id||'.member_streak WHERE member_id='|| quote_literal(member_id)|| ''
		
	LOOP
		EXECUTE 'UPDATE ' ||game_id||'.member_streak_achievement SET unlocked=true WHERE member_id='|| quote_literal(member_id) ||' AND streak_id = ' ||quote_literal(streaks.streak_id)|| ' AND streak_level <= ' ||quote_literal(streaks.current_streak_level)||';';
		EXECUTE 'UPDATE ' ||game_id||'.member_streak_badge SET active=true WHERE member_id='|| quote_literal(member_id) ||'  AND streak_id = ' ||quote_literal(streaks.streak_id)|| ' AND streak_level <= ' ||quote_literal(streaks.current_streak_level)||';';
	END LOOP;
	
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
	EXECUTE 'DELETE FROM '|| game_id ||'.member_streak_action WHERE member_id = '|| quote_literal(member_id) ||';';
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

------------------------------------------------------------------------------------------------STREAK FUNCTIONS STARTS------------------------------------------------------------------------------------------------------
-- FUNK
-- function to validate streak_action activation
CREATE OR REPLACE FUNCTION update_member_streak_action_status() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
streak_id character varying(20);
streaks record;
l_date TIMESTAMP WITHOUT TIME ZONE;
d_date TIMESTAMP WITHOUT TIME ZONE;
now TIMESTAMP;
lstring character varying(20);
dstring character varying(20);
nstring character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	RAISE NOTICE 'MEMBER ACTION INSERT DETECTED';
	RAISE NOTICE 'INSERT ACTION %' ,quote_literal(NEW.action_id);
	RAISE NOTICE 'INSERT MEMBER %' ,quote_literal(NEW.member_id);
	
	-- get all streaks containing the new action
	For streaks IN
	  EXECUTE 'SELECT streak_id FROM '|| game_id ||'.member_streak_action WHERE action_id = '|| quote_literal(NEW.action_id) ||' and member_id = '|| quote_literal(NEW.member_id) ||''
	
	loop
		raise notice 'CURRENT STREAK %', quote_literal(streaks.streak_id);
		EXECUTE 'SELECT locked_date, due_date FROM '|| game_id || '.member_streak WHERE streak_id = '|| quote_literal(streaks.streak_id) ||' and member_id = '|| quote_literal(NEW.member_id) ||''INTO l_date, d_date ;	
		EXECUTE 'SELECT LOCALTIMESTAMP(0)' INTO now;
		lstring := to_char(l_date, 'YYYY-MM-DD HH24:MI:SS');
		dstring := to_char(d_date, 'YYYY-MM-DD HH24:MI:SS');
		nstring := to_char(now, 'YYYY-MM-DD HH24:MI:SS');
		RAISE NOTICE 'locked date is %', quote_literal(lstring);
		RAISE NOTICE 'due date is %', quote_literal(dstring);
		RAISE NOTICE 'now is %', quote_literal(nstring);
		
		IF l_date <= now AND now <= d_date then
			RAISE NOTICE 'VALID ACTION TIME';
		    EXECUTE 'UPDATE ' || game_id || '.member_streak_action SET completed=true WHERE streak_id = '|| quote_literal(streaks.streak_id) ||' and member_id = '|| quote_literal(NEW.member_id) ||' AND action_id = '|| quote_literal(NEW.action_id) ||' AND completed=false;';
	    -- if action performed after due_date, it means, the action has not performed in time and the streak is failed
		ELSIF d_date < now then
			RAISE NOTICE 'INVALID ACTION TIME';
		    EXECUTE 'UPDATE ' || game_id || '.member_streak SET status=''FAILED'' WHERE streak_id = '|| quote_literal(streaks.streak_id) ||' and member_id = '|| quote_literal(NEW.member_id) ||';';
		else
			RAISE NOTICE 'STREAK WITH ID % IS NOT READY TO BE PLAYED NOW. ACTION HAS NO EFFECT', quote_literal(streaks.streak_id);
		END IF;
	END LOOP;
	
	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
-- Action observer for streaks
CREATE OR REPLACE FUNCTION create_trigger_streak_action_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER streak_action_observer
		AFTER INSERT ON '|| game_id ||'.member_action
		FOR EACH ROW
		EXECUTE PROCEDURE update_member_streak_action_status();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
-- function to validate streak_completion
CREATE OR REPLACE FUNCTION update_member_streak_status() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
actions record;
current_point integer;
action_point integer;
threshold integer;
l_date TIMESTAMP WITHOUT TIME ZONE;
d_date TIMESTAMP WITHOUT TIME ZONE;
now TIMESTAMP;
lstring character varying(20);
dstring character varying(20);
nstring character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	RAISE NOTICE 'MEMEBER STREAK ACTION STATUS CHANGE DETECTED';
	RAISE NOTICE 'UPDATED MEMBER %' ,quote_literal(NEW.member_id);
	RAISE NOTICE 'UPDATED STREAK %' ,quote_literal(NEW.streak_id);
	RAISE NOTICE 'UPDATED ACTION %' ,quote_literal(NEW.action_id);
	EXECUTE 'SELECT locked_date, due_date FROM '|| game_id || '.member_streak WHERE streak_id = '|| quote_literal(NEW.streak_id) ||' and member_id = '|| quote_literal(NEW.member_id) ||''INTO l_date, d_date ;	
	EXECUTE 'SELECT LOCALTIMESTAMP(0)' INTO now;
	lstring := to_char(l_date, 'YYYY-MM-DD HH24:MI:SS');
	dstring := to_char(d_date, 'YYYY-MM-DD HH24:MI:SS');
	nstring := to_char(now, 'YYYY-MM-DD HH24:MI:SS');
	RAISE NOTICE 'locked date is %', quote_literal(lstring);
	RAISE NOTICE 'due date is %', quote_literal(dstring);
	RAISE NOTICE 'now is %', quote_literal(nstring);
	
	IF l_date <= now AND now <= d_date then
		current_point:=0;
	
		-- get all streaks containing the new action
		for actions IN
			EXECUTE 'SELECT action_id FROM ' || game_id || '.member_streak_action WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND  streak_id = '|| quote_literal(NEW.streak_id) ||' AND completed=true;'
		
		loop
			RAISE NOTICE 'CURRENT STREAK %', quote_literal(NEW.streak_id);
			RAISE NOTICE 'CURRENT ACTION %', quote_literal(actions.action_id);
			RAISE NOTICE 'CURRENT MEMBER %', quote_literal(NEW.member_id);
			EXECUTE 'SELECT point_value FROM '|| game_id || '.action WHERE action_id = '|| quote_literal(actions.action_id) ||';' INTO action_point;
			current_point:=current_point + action_point;
		END LOOP;
	
		EXECUTE 'SELECT point_th FROM ' || game_id ||'.streak WHERE streak_id= '|| quote_literal(NEW.streak_id) ||';' INTO threshold;
	
		IF current_point >= threshold then
			RAISE NOTICE 'ENOUGH POINT COLLECTED. NOW UPDATE STREAK STATUS';
			EXECUTE 'UPDATE ' || game_id || '.member_streak SET status=''UPDATED'' WHERE member_id = '|| quote_literal(NEW.member_id) ||' AND  streak_id = '|| quote_literal(NEW.streak_id) ||';';
		END IF;
	END IF;
	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
-- Action observer for streaks
CREATE OR REPLACE FUNCTION create_trigger_member_streak_action_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER member_streak_action_observer
		AFTER UPDATE ON '|| game_id ||'.member_streak_action
		FOR EACH ROW
		EXECUTE PROCEDURE update_member_streak_status();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
CREATE OR REPLACE FUNCTION handle_dates(game_id text, streak_id text, member_id text) RETURNS void AS
$BODY$
declare
t_period interval;
l_date TIMESTAMP WITHOUT TIME ZONE;
d_date TIMESTAMP WITHOUT TIME ZONE;
now TIMESTAMP;
begin
	RAISE NOTICE 'HANDLING DATE FUNKTION FOR STREAK %', quote_literal(streak_id);
	EXECUTE 'SELECT period FROM ' || game_id || '.streak WHERE streak_id = '|| quote_literal(streak_id) ||'' into t_period;
	EXECUTE 'SELECT locked_date FROM '|| game_id || '.member_streak WHERE streak_id = '|| quote_literal(streak_id) ||' and member_id = '|| quote_literal(member_id) ||''INTO l_date;
	EXECUTE 'SELECT due_date FROM '|| game_id || '.member_streak WHERE streak_id = '|| quote_literal(streak_id) ||' and member_id = '|| quote_literal(member_id) ||'' INTO d_date;
	l_date := d_date;
	d_date := d_date + t_period;
	EXECUTE 'SELECT LOCALTIMESTAMP(0)' INTO now;
	while d_date <= now loop
		l_date := d_date;
		d_date := d_date + t_period;
	end loop;
	EXECUTE 'UPDATE ' || game_id || '.member_streak  SET status=''ACTIVE'', locked_date = '|| quote_literal(to_char(l_date, 'YYYY-MM-DD HH24:MI:SS')) ||' , due_date = ' || quote_literal(to_char(d_date, 'YYYY-MM-DD HH24:MI:SS')) || ' WHERE streak_id = '|| quote_literal(streak_id) ||' AND member_id = '|| quote_literal(member_id) ||';';
	RAISE NOTICE 'UPDATED DATES FOR STREAK % ', quote_literal(streak_id);
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
CREATE OR REPLACE FUNCTION handle_representation(game_id text, streak_id text, member_id text, st_level integer) RETURNS void AS
$BODY$
begin
	RAISE NOTICE 'HANDLING REPRESENTATION FUNKTION FOR STREAK %', quote_literal(streak_id);
	EXECUTE 'UPDATE ' || game_id || '.member_streak_badge SET active=false WHERE streak_id = '|| quote_literal(streak_id) ||' AND member_id = '|| quote_literal(member_id) ||' AND active=true;';
	EXECUTE 'UPDATE ' || game_id || '.member_streak_badge SET active=true WHERE streak_id = '|| quote_literal(streak_id) ||' AND member_id = '|| quote_literal(member_id) ||' AND streak_level= (SELECT MAX(streak_level) FROM ' ||game_id||'.member_streak_badge WHERE streak_id='||quote_literal(streak_id)||' AND member_id= '||quote_literal(member_id)||' AND streak_level <= '||st_level||');';
	RAISE NOTICE 'HANDELED REPRESENTATION ';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
CREATE OR REPLACE FUNCTION handle_rewards(game_id text, streak_id text, member_id text, st_level integer) RETURNS void AS
$BODY$
DECLARE
current_point integer;
comp_result integer;
ach record;
point_obtained integer;
achievement_id character varying(20);
BEGIN
		RAISE NOTICE 'HANDLING REWARD FUNKTION FOR STREAK %', quote_literal(streak_id);
		RAISE NOTICE 'GAME IS %', quote_literal(game_id);
		
		EXECUTE 'DROP TABLE IF EXISTS '|| game_id ||'.temp;';
		EXECUTE 'CREATE TABLE '|| game_id ||'.temp (type '|| game_id ||'.notification_type);';
		EXECUTE 'INSERT INTO '|| game_id ||'.temp VALUES(''STREAK'');';
		
		EXECUTE 'INSERT INTO '|| game_id ||'.notification (member_id, type_id, use_notification, message, type)
			WITH res as (SELECT member_id, '|| game_id ||'.streak.streak_id,use_notification,notif_message FROM '|| game_id ||'.member_streak INNER JOIN '|| game_id ||'.streak
			ON ('|| game_id ||'.member_streak.streak_id = '|| game_id ||'.streak.streak_id) WHERE member_id = '|| quote_literal(member_id) ||' AND '|| game_id ||'.member_streak.streak_id = '|| quote_literal(streak_id) ||') SELECT * FROM res CROSS JOIN '|| game_id ||'.temp ;';
		
		-- Get the achievement, which have not been unlocked yet, belonging to to the member of the updated streak
		
		FOR ach IN
			EXECUTE 'SELECT achievement_id FROM '|| game_id ||'.member_streak_achievement WHERE streak_id = '|| quote_literal(streak_id) || ' AND member_id = ' || quote_literal(member_id) || ' AND unlocked=false AND streak_level <= ' || st_level
		
		loop
			RAISE NOTICE 'CURRENT ACHIEVEMENT IS  %', quote_literal(ach.achievement_id);
			-- insert to member_achievement
			EXECUTE 'INSERT INTO '|| game_id ||'.member_achievement (member_id, achievement_id) VALUES ('|| quote_literal(member_id) ||','|| quote_literal(ach.achievement_id) ||');';
			-- insert to member_badge
			EXECUTE 'INSERT INTO '|| game_id ||'.member_badge (member_id, badge_id) SELECT member_id,badge_id FROM '|| game_id ||'.member_achievement,'|| game_id ||'.achievement WHERE '|| game_id ||'.achievement.achievement_id = '|| quote_literal(ach.achievement_id) ||' AND '|| game_id ||'.achievement.badge_id IS NOT NULL
			AND '|| game_id ||'.member_achievement.member_id = '|| quote_literal(member_id) ||' AND '|| game_id ||'.member_achievement.achievement_id = '|| quote_literal(ach.achievement_id) ||';';
			-- get point obtained
			EXECUTE 'SELECT point_value FROM '|| game_id ||'.achievement WHERE achievement_id = '|| quote_literal(ach.achievement_id) ||';' INTO point_obtained;
			-- get current point value
			EXECUTE 'SELECT point_value FROM '|| game_id ||'.member_point WHERE member_id = '|| quote_literal(member_id) ||'' INTO current_point;
			-- check point if less than 0
			comp_result = current_point + point_obtained;
			IF comp_result < 0 THEN
				EXECUTE 'UPDATE '|| game_id ||'.member_point SET point_value = 0 WHERE member_id = '||quote_literal(member_id) ||';';
    		ELSE
			-- add point
				EXECUTE 'UPDATE '|| game_id ||'.member_point SET point_value =  '|| comp_result ||' WHERE member_id = '|| quote_literal(member_id) ||';';
			END IF;

		END LOOP;
	EXECUTE 'UPDATE ' || game_id || '.member_streak_achievement SET unlocked=true WHERE streak_id = '|| quote_literal(streak_id)  ||' AND member_id = '|| quote_literal(member_id) ||' AND streak_level <= '|| st_level||';';
	RAISE NOTICE 'REWARDS UPDATED FOR STREAK %', quote_literal(streak_id);
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


CREATE OR REPLACE FUNCTION handle_streak_reset(game_id text, streak_id text, member_id text) RETURNS void AS
$BODY$
begin
	RAISE NOTICE 'HANDLING RESET FUNKTION FOR STREAK %', quote_literal(streak_id);
	EXECUTE 'UPDATE ' || game_id || '.member_streak_action SET completed=false WHERE streak_id = '|| quote_literal(streak_id) ||' AND member_id = '|| quote_literal(member_id) ||';';
	RAISE NOTICE 'HANDLED RESET';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


-- function to handle streak_updates
CREATE OR REPLACE FUNCTION update_member_streak() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
streaks record;
curr integer;
high integer;
BEGIN
	game_id = TG_TABLE_SCHEMA;

	RAISE NOTICE 'MEMBER STREAK STATUS UPDATE DETECTED';
	-- get all streaks, that have changed
	FOR streaks IN
	    EXECUTE 'SELECT streak_id, status FROM ' || game_id || '.member_streak WHERE streak_id = '|| quote_literal(NEW.streak_id) ||' AND member_id = '|| quote_literal(NEW.member_id) ||';'
		
	-- Loop over all these streaks and execute funtions based on new state
	loop
	RAISE NOTICE 'CURRENT STREAK IS %', quote_literal(streaks.streak_id);
	RAISE NOTICE 'CURRENT STREAK STATUS IS %', quote_literal(streaks.status);
		IF  streaks.status = 'PAUSED' then
		    EXECUTE 'SELECT handle_dates('||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) || ', ' || quote_literal(NEW.member_id) ||');';
			
		ELSIF  streaks.status = 'UPDATED' then
			EXECUTE 'SELECT current_streak_level, highest_streak_level FROM ' || game_id || '.member_streak WHERE streak_id = ' || quote_literal(NEW.streak_id) ||' AND member_id = '|| quote_literal(NEW.member_id) ||'' into curr, high;
		   	curr:=curr + 1;
		   	high:=GREATEST(curr,high);
			EXECUTE 'SELECT handle_streak_reset('||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) || ', ' || quote_literal(NEW.member_id) ||');';
		    EXECUTE 'SELECT handle_dates('||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) || ', ' || quote_literal(NEW.member_id) ||');';
			EXECUTE 'UPDATE ' || game_id || '.member_streak  SET highest_streak_level = ' || high ||' , current_streak_level = ' || curr ||' WHERE streak_id = '|| quote_literal(NEW.streak_id) ||' AND member_id = '|| quote_literal(NEW.member_id) ||';';
			EXECUTE 'SELECT handle_representation('||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) ||', '|| quote_literal(NEW.member_id) ||' , '|| quote_literal(curr) ||');';
			EXECUTE 'SELECT handle_rewards(' ||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) ||',' || quote_literal(NEW.member_id) || ',' || quote_literal(curr) ||');';
			
		ELSIF  streaks.status = 'FAILED' then
			EXECUTE 'SELECT current_streak_level FROM ' || game_id || '.member_streak WHERE streak_id = ' || quote_literal(NEW.streak_id) ||' AND member_id = '|| quote_literal(NEW.member_id) ||'' into curr;
			curr:=1;
			EXECUTE 'SELECT handle_streak_reset('||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) || ', ' || quote_literal(NEW.member_id) ||');';
		    EXECUTE 'SELECT handle_dates('||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) || ', ' || quote_literal(NEW.member_id) ||');';
			EXECUTE 'UPDATE ' || game_id || '.member_streak  SET current_streak_level= '|| curr || ' WHERE streak_id = '|| quote_literal(NEW.streak_id) ||' AND member_id = '|| quote_literal(NEW.member_id) ||';';
			EXECUTE 'SELECT handle_representation('||quote_literal(game_id)||', '|| quote_literal(NEW.streak_id) ||', '|| quote_literal(NEW.member_id) ||' , '|| quote_literal(curr)||');';
			
		ELSIF  streaks.status = 'ACTIVE' then
			RAISE NOTICE 'NOTHING TO BE DONE';
		END IF;
	END LOOP;
	
	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


CREATE OR REPLACE FUNCTION create_trigger_member_streak_observer(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER member_streak_observer
		AFTER UPDATE ON '|| game_id ||'.member_streak
		FOR EACH ROW
		EXECUTE PROCEDURE update_member_streak();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


-- FUNK
-- function to initialize table member_streak_action
CREATE OR REPLACE FUNCTION update_streak_action_constraint_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	-- initialize table member_streak_action ::after new streak_action was inserted
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak_action
	WITH newtab as (SELECT * FROM '|| game_id ||'.streak_action CROSS JOIN '|| game_id ||'.member)
	SELECT member_id, streak_id, action_id FROM newtab WHERE streak_id='|| quote_literal(NEW.streak_id) ||' AND action_id='|| quote_literal(NEW.action_id) ||' ORDER BY member_id ;';

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
CREATE OR REPLACE FUNCTION create_trigger_update_streak_action_constraint(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER streak_action_observer
		AFTER INSERT ON '|| game_id ||'.streak_action
		FOR EACH ROW
		EXECUTE PROCEDURE update_streak_action_constraint_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
-- function to initialize table member_streak_badge
-- TODO
CREATE OR REPLACE FUNCTION update_streak_badge_constraint_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
st_level integer;
BEGIN
	game_id = TG_TABLE_SCHEMA;

	-- initialize table member_streak_badge ::after new streak_badge was inserted
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak_badge
	WITH newtab as (SELECT * FROM '|| game_id ||'.streak_badge CROSS JOIN '|| game_id ||'.member)
	SELECT member_id, streak_id, badge_id, streak_level FROM newtab WHERE streak_id='|| quote_literal(NEW.streak_id) ||' AND badge_id='|| quote_literal(NEW.badge_id) ||' ORDER BY member_id ;';
	
	EXECUTE 'SELECT streak_level FROM ' ||game_id||'.streak WHERE streak_id = '||quote_literal(NEW.streak_id) ||'' INTO st_level;
	
	EXECUTE 'UPDATE ' ||game_id||'.member_streak_badge SET active=true WHERE streak_id='|| quote_literal(NEW.streak_id) ||' AND streak_level <= ' ||st_level||';';

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
CREATE OR REPLACE FUNCTION create_trigger_update_streak_badge_constraint(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER streak_badge_observer
		AFTER INSERT ON '|| game_id ||'.streak_badge
		FOR EACH ROW
		EXECUTE PROCEDURE update_streak_badge_constraint_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
-- function to initialize table member_streak_achievement
CREATE OR REPLACE FUNCTION update_streak_achievement_constraint_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
st_level integer;
BEGIN
	game_id = TG_TABLE_SCHEMA;

	-- initialize table member_streak_achievement ::after new streak_achievement was inserted
	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak_achievement
	WITH newtab as (SELECT * FROM '|| game_id ||'.streak_achievement CROSS JOIN '|| game_id ||'.member)
	SELECT member_id, streak_id, achievement_id, streak_level FROM newtab WHERE streak_id='|| quote_literal(NEW.streak_id) ||' AND achievement_id='|| quote_literal(NEW.achievement_id) ||' ORDER BY member_id ;';
	
	EXECUTE 'SELECT streak_level FROM ' ||game_id||'.streak WHERE streak_id = '||quote_literal(NEW.streak_id) ||'' INTO st_level;
	
	EXECUTE 'UPDATE ' ||game_id||'.member_streak_achievement SET unlocked=true WHERE streak_id='|| quote_literal(NEW.streak_id) ||' AND streak_level <= ' ||st_level||';';

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
CREATE OR REPLACE FUNCTION create_trigger_update_streak_achievement_constraint(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER streak_achievement_observer
		AFTER INSERT ON '|| game_id ||'.streak_achievement
		FOR EACH ROW
		EXECUTE PROCEDURE update_streak_achievement_constraint_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
-- function to initialize table member_streak
CREATE OR REPLACE FUNCTION update_streak_constraint_function() RETURNS trigger AS
$BODY$
DECLARE
game_id character varying(20);
BEGIN
	game_id = TG_TABLE_SCHEMA;

	RAISE NOTICE 'GameId : %', game_id;

    -- initialize table member_streak ::after new streak was inserted
 	EXECUTE 'INSERT INTO '|| game_id ||'.member_streak (member_id, streak_id, status, locked_date, due_date, current_streak_level, highest_streak_level)
	WITH tab1 as (SELECT * FROM '|| game_id ||'.member CROSS JOIN '|| game_id ||'.streak WHERE streak_id='|| quote_literal(NEW.streak_id) ||')
	SELECT  member_id, streak_id, status, locked_date, due_date, streak_level, streak_level FROM tab1 ORDER BY member_id;';

	RETURN NULL;  -- result is ignored since this is an AFTER trigger
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- FUNK
CREATE OR REPLACE FUNCTION create_trigger_update_streak_constraint(game_id text) RETURNS void AS
$BODY$
BEGIN
	EXECUTE 'CREATE TRIGGER streak_observer
		AFTER INSERT ON '|| game_id ||'.streak
		FOR EACH ROW
		EXECUTE PROCEDURE update_streak_constraint_function();';
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


------------------------------------------------------------------------------------------------STREAK FUNCTIONS ENDS------------------------------------------------------------------------------------------------------



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