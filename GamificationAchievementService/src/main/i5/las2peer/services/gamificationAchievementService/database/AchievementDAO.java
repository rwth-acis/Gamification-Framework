package i5.las2peer.services.gamificationAchievementService.database;


import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AchievementDAO {
	
	
	PreparedStatement stmt;
	Connection conn;
	
	public AchievementDAO( Connection conn){
		this.conn = conn;
	}
	
	/**
	 * Check whether the application id is already exist
	 * 
	 * @param app_id application id
	 * @return true app_id is already exist
	 * @throws SQLException SQL Exception
	 */
	public boolean isAppIdExist(String app_id) throws SQLException  {
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
	 * Get all achievements in the database
	 * 
	 * @param appId application id
	 * @return list of achievements
	 * @throws SQLException sql exception
	 */
	public List<AchievementModel> getAllAchievements(String appId) throws SQLException{

		List<AchievementModel> achs = new ArrayList<AchievementModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".achievement");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			AchievementModel bmodel = new AchievementModel(rs.getString("achievement_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getString("badge_id"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			achs.add(bmodel);
		}

		return achs;
	}

	/**
	 * Check whether an achievement with achievement id is already exist
	 * 
	 * @param appId application id
	 * @param achievement_id achievement id
	 * @return true if the achievement is already exist
	 * @throws SQLException sql exception
	 */
	public boolean isAchievementIdExist(String appId, String achievement_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT achievement_id FROM "+appId+".achievement WHERE achievement_id=?");
		stmt.setString(1, achievement_id);
		try {
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return true;
			}
			else{
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Get an achievement with specific id
	 * 
	 * @param appId application id
	 * @param achievement_id achievement id
	 * @return AchievementModel
	 * @throws SQLException sql exception
	 */
	public AchievementModel getAchievementWithId(String appId, String achievement_id) throws SQLException {
		
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".achievement WHERE achievement_id = ?");
		stmt.setString(1, achievement_id);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()){
			return new AchievementModel(achievement_id, rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getString("badge_id"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
		}
		return null;
	}

	/**
	 * Get total number of achievement
	 * 
	 * @param appId application id
	 * @return total number of achievement
	 * @throws SQLException sql exception
	 */
	public int getNumberOfAchievements(String appId) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".achievement");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	/**
	 * Get achievements with search parameter
	 * 
	 * @param appId application id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of achievements
	 * @throws SQLException sql exception
	 */
	public List<AchievementModel> getAchievementsWithOffsetAndSearchPhrase(String appId, int offset, int window_size, String searchPhrase) throws SQLException {
		List<AchievementModel> achs= new ArrayList<AchievementModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".achievement WHERE achievement_id LIKE '"+pattern+"' ORDER BY achievement_id LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			AchievementModel a = new AchievementModel(rs.getString("achievement_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getString("badge_id"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			achs.add(a);
		}
		return achs;
	}
	
	/**
	 * Update an achievement information
	 * 
	 * @param appId application id
	 * @param achievement model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateAchievement(String appId, AchievementModel achievement) throws SQLException {
		// check whether the badgeid exist or not

		stmt = conn.prepareStatement("UPDATE "+appId+".achievement SET name = ?, description = ?, point_value = ?, badge_id = ?, use_notification = ?, notif_message = ? WHERE achievement_id = ?");
		stmt.setString(1, achievement.getName());
		stmt.setString(2, achievement.getDescription());
		stmt.setInt(3, achievement.getPointValue());
		stmt.setString(4, achievement.getBadgeId());
		stmt.setBoolean(5, achievement.isUseNotification());
		stmt.setString(6, achievement.getNotificationMessage());
		stmt.setString(7, achievement.getId());
		stmt.executeUpdate();
	}

	/**
	 * Delete a specific achievement
	 * 
	 * @param appId application id
	 * @param achievement_id achievement id
	 * @throws SQLException sql exception
	 */
	public void deleteAchievement(String appId, String achievement_id) throws SQLException {
		// TODO Auto-generated method stub
			stmt = conn.prepareStatement("DELETE FROM "+appId+".achievement WHERE achievement_id = ?");
			stmt.setString(1, achievement_id);
			stmt.executeUpdate();
	}

	/**
	 * Add a new achievement
	 * 
	 * @param appId application id
	 * @param achievement achievement model
	 * @throws SQLException sql exception
	 */
	public void addNewAchievement(String appId, AchievementModel achievement) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("INSERT INTO "+appId+".achievement (achievement_id, name, description, point_value, badge_id, use_notification, notif_message) VALUES (?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, achievement.getId());
			stmt.setString(2, achievement.getName());
			stmt.setString(3, achievement.getDescription());
			stmt.setInt(4, achievement.getPointValue());
			stmt.setString(5, achievement.getBadgeId());
			stmt.setBoolean(6, achievement.isUseNotification());
			stmt.setString(7, achievement.getNotificationMessage());
			stmt.executeUpdate();
			
	}
	
}
