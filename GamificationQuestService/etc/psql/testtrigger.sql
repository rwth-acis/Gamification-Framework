-- UPDATE test.member_point SET point_value=13 WHERE member_id='user1';
-- SELECT * FROM test.member_point;
-- SELECT * FROM test.member_quest ORDER BY member_id;

UPDATE test.member_quest SET status='COMPLETED' WHERE test.member_quest.quest_id='quest1' AND test.member_quest.member_id='user1';
SELECT * FROM test.member_quest ORDER BY member_id;
