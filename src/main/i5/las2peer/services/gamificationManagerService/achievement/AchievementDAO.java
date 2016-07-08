package i5.las2peer.services.gamificationManagerService.achievement;


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
	 * Get all achievements in the database
	 * 
	 * @param appId application id
	 * @return list of achievements
	 * @throws SQLException sql exception
	 */
	public List<AchievementModel> getAllAchievements(String appId) throws SQLException{
		// TODO Auto-generated method stub
		List<AchievementModel> achs = new ArrayList<AchievementModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".achievement");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			AchievementModel bmodel = new AchievementModel(rs.getString("achievement_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getString("badge_id"));
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
			// TODO Auto-generated catch block
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
	 */
	public AchievementModel getAchievementWithId(String appId, String achievement_id) {
		try {
			stmt = conn.prepareStatement("SELECT * FROM "+appId+".achievement WHERE achievement_id = ?");
			stmt.setString(1, achievement_id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				return new AchievementModel(achievement_id, rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getString("badge_id"));
			}
			else{
				return null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
			AchievementModel a = new AchievementModel(rs.getString("achievement_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getString("badge_id"));
			achs.add(a);
		}
		return achs;
	}
	
	/**
	 * Update a achievement information
	 * 
	 * @param appId application id
	 * @param achievement model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateAchievement(String appId, AchievementModel achievement) throws SQLException {
		// check whether the badgeid exist or not
		stmt = conn.prepareStatement("SELECT badge_id FROM "+appId+".badge WHERE badge_id=?");
		stmt.setString(1, achievement.getBadgeId());
		try {
			ResultSet rs = stmt.executeQuery();
			if(!rs.next()){
				achievement.setBadgeId(null);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stmt = conn.prepareStatement("UPDATE "+appId+".achievement SET name = ?, description = ?, point_value = ?, badge_id = ? WHERE achievement_id = ?");
		stmt.setString(1, achievement.getName());
		stmt.setString(2, achievement.getDescription());
		stmt.setInt(3, achievement.getPointValue());
		stmt.setString(4, achievement.getBadgeId());
		stmt.setString(5, achievement.getId());
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

			stmt = conn.prepareStatement("INSERT INTO "+appId+".achievement (achievement_id, name, description, point_value, badge_id) VALUES (?, ?, ?, ?, ?)");
			stmt.setString(1, achievement.getId());
			stmt.setString(2, achievement.getName());
			stmt.setString(3, achievement.getDescription());
			stmt.setInt(4, achievement.getPointValue());
			stmt.setString(5, achievement.getBadgeId());
			stmt.executeUpdate();
			
	}
	
}
