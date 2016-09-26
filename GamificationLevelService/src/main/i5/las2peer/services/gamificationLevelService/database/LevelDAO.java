package i5.las2peer.services.gamificationLevelService.database;


import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LevelDAO {
	
	
	PreparedStatement stmt;

	public LevelDAO(){
	}
	
	/**
	 * Check whether the application id is already exist
	 * 
	 * @param conn database connection
	 * @param app_id application id
	 * @return true app_id is already exist
	 * @throws SQLException SQL Exception
	 */
	public boolean isAppIdExist(Connection conn,String app_id) throws SQLException  {
			stmt = conn.prepareStatement("SELECT app_id FROM manager.application_info WHERE app_id=?");
			stmt.setString(1, app_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				//if(rs.getString("app_id").equals(app_id)){
					return true;
				//}
			}
			return false;
	}
	
	/**
	 * Get all levels in the database
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @return list of levels
	 * @throws SQLException sql exception
	 */
	public List<LevelModel> getAllLevels(Connection conn,String appId) throws SQLException{

		List<LevelModel> levs = new ArrayList<LevelModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".level");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			LevelModel model = new LevelModel(rs.getInt("level_num"), rs.getString("name"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			levs.add(model);
		}

		return levs;
	}

	/**
	 * Check whether a level with level num is already exist
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param level_num level id
	 * @return true if the level is already exist
	 * @throws SQLException sql exception
	 */
	public boolean isLevelNumExist(Connection conn,String appId, int level_num) throws SQLException {

		stmt = conn.prepareStatement("SELECT level_num FROM "+appId+".level WHERE level_num=?");
		stmt.setInt(1, level_num);

		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;

	}
	
	/**
	 * Get a level with specific id
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param level_num level id
	 * @return LevelModel
	 * @throws SQLException sql exception 
	 */
	public LevelModel getLevelWithNumber(Connection conn,String appId, int level_num) throws SQLException {
		
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".level WHERE level_num = ?");
		stmt.setInt(1, level_num);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()){
			return new LevelModel(rs.getInt("level_num"), rs.getString("name"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
		}
		return null;
	}

	/**
	 * Get total number of level
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @return total number of level
	 * @throws SQLException sql exception
	 */
	public int getNumberOfLevels(Connection conn,String appId) throws SQLException {

		stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".level");
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return rs.getInt(1);
		}
		else{
			return 0;
		}
	}
	
	/**
	 * Get levels with search parameter
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of levels
	 * @throws SQLException sql exception
	 */
	public List<LevelModel> getLevelsWithOffsetAndSearchPhrase(Connection conn,String appId, int offset, int window_size, String searchPhrase) throws SQLException {
		List<LevelModel> achs= new ArrayList<LevelModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".level WHERE CAST(level_num AS text) LIKE '"+pattern+"' ORDER BY level_num LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			LevelModel a = new LevelModel(rs.getInt("level_num"), rs.getString("name"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			achs.add(a);
		}
		return achs;
	}
	
	/**
	 * Update a level information
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param level model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateLevel(Connection conn,String appId, LevelModel level) throws SQLException {
		
		stmt = conn.prepareStatement("UPDATE "+appId+".level SET name = ?, point_value = ?, use_notification = ?, notif_message = ? WHERE level_num = ?");
		stmt.setString(1, level.getName());
		stmt.setInt(2, level.getPointValue());
		stmt.setBoolean(3, level.isUseNotification());
		stmt.setString(4, level.getNotificationMessage());
		stmt.setInt(5, level.getNumber());
		stmt.executeUpdate();
	}

	/**
	 * Delete a specific level
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param level_num level num
	 * @throws SQLException sql exception
	 */
	public void deleteLevel(Connection conn,String appId, int level_num) throws SQLException {

		stmt = conn.prepareStatement("DELETE FROM "+appId+".level WHERE level_num = ?");
		stmt.setInt(1, level_num);
		stmt.executeUpdate();
	}

	/**
	 * Add a new level
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param level level model
	 * @throws SQLException sql exception
	 */
	public void addNewLevel(Connection conn,String appId, LevelModel level) throws SQLException {
		
		stmt = conn.prepareStatement("INSERT INTO "+appId+".level (level_num, name, point_value, use_notification, notif_message ) VALUES (?, ?, ?, ?, ?)");
		stmt.setInt(1, level.getNumber());
		stmt.setString(2, level.getName());
		stmt.setInt(3, level.getPointValue());
		stmt.setBoolean(4, level.isUseNotification());
		stmt.setString(5, level.getNotificationMessage());
		stmt.executeUpdate();
			
	}
	
}
