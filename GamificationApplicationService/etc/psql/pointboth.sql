--point
--WITH temp AS (SELECT quest_id FROM test.member_quest WHERE status = 'COMPLETED' AND member_id = 'user1')
--SELECT * FROM test.quest INNER JOIN temp ON test.quest.quest_id_completed = temp.quest_id WHERE 15 >= test.quest.point_value AND test.quest.point_flag=true AND test.quest.quest_flag=true

--quest
WITH temp as (SELECT * FROM test.quest WHERE test.quest.quest_id_completed = 'quest1' AND test.quest.point_flag = true AND test.quest.quest_flag = true)
SELECT quest_id FROM temp WHERE (SELECT point_value FROM test.member_point WHERE member_id='user1' LIMIT 1) >= point_value;
--SELECT point_value FROM test.member_point WHERE member_id='user1' AND 'COMPLETED' = 'COMPLETED' LIMIT 1