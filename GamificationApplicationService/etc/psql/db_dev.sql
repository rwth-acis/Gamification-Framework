DROP SCHEMA test CASCADE;
SELECT create_new_application('test');
SELECT add_mock_data('test');
SELECT init_member_to_app('user1','test');