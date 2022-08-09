package i5.las2peer.services.gamificationAchievementService.database;


import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AchievementDAO {
	
	
	PreparedStatement stmt;
	
	public AchievementDAO( ){
	}
	
	/**
	 * Check whether the game id is already exist
	 * 
	 * @param game_id game id
	 * @param conn database connection
	 * @return true game_id is already exist
	 * @throws SQLException SQL Exception
	 */
	public boolean isGameIdExist(Connection conn, String game_id) throws SQLException  {
			stmt = conn.prepareStatement("SELECT game_id FROM manager.game_info WHERE game_id=?");
			stmt.setString(1, game_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				//if(rs.getString("game_id").equals(game_id)){
					return true;
				//}
			}
			return false;
	}
	
	/**
	 * Get all achievements in the database
	 * 
	 * @param gameId game id
	 * @param conn database connection
	 * @return list of achievements
	 * @throws SQLException SQL exception
	 */
	public List<AchievementModel> getAllAchievements(Connection conn,String gameId) throws SQLException{

		List<AchievementModel> achs = new ArrayList<AchievementModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+gameId+".achievement");
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
	 * @param gameId game id
	 * @param conn database connection
	 * @param achievement_id achievement id
	 * @return true if the achievement is already exist
	 * @throws SQLException SQL exception
	 */
	public boolean isAchievementIdExist(Connection conn,String gameId, String achievement_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT achievement_id FROM "+gameId+".achievement WHERE achievement_id=?");
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
	 * @param gameId game id
	 * @param conn database connection
	 * @param achievement_id achievement id
	 * @return {@link AchievementModel}
	 * @throws SQLException SQL exception
	 */
	public AchievementModel getAchievementWithId(Connection conn,String gameId, String achievement_id) throws SQLException {
		
		stmt = conn.prepareStatement("SELECT * FROM "+gameId+".achievement WHERE achievement_id = ?");
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
	 * @param gameId game id
	 * @param conn database connection
	 * @return total number of achievement
	 * @throws SQLException SQL exception
	 */
	public int getNumberOfAchievements(Connection conn,String gameId) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("SELECT count(*) FROM "+gameId+".achievement");
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
	 * @param gameId game id
	 * @param conn database connection
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of achievements
	 * @throws SQLException SQL exception
	 */
	public List<AchievementModel> getAchievementsWithOffsetAndSearchPhrase(Connection conn,String gameId, int offset, int window_size, String searchPhrase) throws SQLException {
		List<AchievementModel> achs= new ArrayList<AchievementModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+gameId+".achievement WHERE achievement_id LIKE '"+pattern+"' ORDER BY achievement_id LIMIT "+window_size+" OFFSET "+offset);
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
	 * @param gameId game id
	 * @param conn database connection
	 * @param achievement model to be updated
	 * @throws SQLException SQL exception
	 */
	public void updateAchievement(Connection conn,String gameId, AchievementModel achievement) throws SQLException {
		// check whether the badgeid exist or not

		stmt = conn.prepareStatement("UPDATE "+gameId+".achievement SET name = ?, description = ?, point_value = ?, badge_id = ?, use_notification = ?, notif_message = ? WHERE achievement_id = ?");
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
	 * @param gameId game id
	 * @param conn database connection
	 * @param achievement_id achievement id
	 * @throws SQLException SQL exception
	 */
	public void deleteAchievement(Connection conn,String gameId, String achievement_id) throws SQLException {
		// TODO Auto-generated method stub
			stmt = conn.prepareStatement("DELETE FROM "+gameId+".achievement WHERE achievement_id = ?");
			stmt.setString(1, achievement_id);
			stmt.executeUpdate();
	}

	/**
	 * Add a new achievement
	 * 
	 * @param gameId game id
	 * @param conn database connection
	 * @param achievement achievement model
	 * @throws SQLException SQL exception
	 */
	public void addNewAchievement(Connection conn,String gameId, AchievementModel achievement) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("INSERT INTO "+gameId+".achievement (achievement_id, name, description, point_value, badge_id, use_notification, notif_message) VALUES (?, ?, ?, ?, ?, ?, ?)");
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
