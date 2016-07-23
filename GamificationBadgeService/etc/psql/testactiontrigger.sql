
-- INITIALIZE member_quest_action

-- DELETE FROM test.member_quest_action;
-- INSERT INTO test.member_quest_action WITH newtab as (SELECT * FROM test.quest_action CROSS JOIN test.member) SELECT member_id, quest_id, action_id FROM newtab ORDER BY member_id;
-- SELECT * FROM test.member_quest_action;


-- Trigger test action

--DELETE FROM test.member_action WHERE member_id='user1';
INSERT INTO test.member_action (member_id,action_id) VALUES('user1','action4');
SELECT * FROM test.member_action WHERE member_id = 'user1';
--WITH counter AS (SELECT count(action_id) FROM test.member_action WHERE member_id = 'user1')
SELECT quest_id FROM test.quest_action WHERE action_id = 'action1' AND (SELECT count(action_id) FROM test.member_action WHERE member_id = 'user1' AND action_id='action1') >= times;
--SELECT * FROM test.member_quest_action WHERE member_id='user1' AND action_id='action1' AND quest_id='quest1';
--SELECT count(action_id) FROM test.member_action WHERE member_id = 'user1'
--SELECT bool_and(completed) FROM test.member_quest_action WHERE quest_id='quest1' AND member_id='user1';