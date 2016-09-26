package i5.las2peer.services.gamificationBadgeService.database;


import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BadgeDAO {
	
	
	PreparedStatement stmt;
	
	
	public BadgeDAO(){
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
	 * Get all badges in the database
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @return list of badges
	 * @throws SQLException sql exception
	 */
	public List<BadgeModel> getAllBadges(Connection conn,String appId) throws SQLException{

		List<BadgeModel> badges = new ArrayList<BadgeModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".badge");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			BadgeModel bmodel = new BadgeModel(rs.getString("badge_id"), rs.getString("name"), rs.getString("description"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			badges.add(bmodel);
		}

		return badges;
	}

	/**
	 * Get total number of badges
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @return total number of badges
	 * @throws SQLException sql exception
	 */
	public int getNumberOfBadges(Connection conn,String appId) throws SQLException {

			stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".badge");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	/**
	 * Get badges with search parameter
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of badges
	 * @throws SQLException sql exception
	 */
	public List<BadgeModel> getBadgesWithOffsetAndSearchPhrase(Connection conn,String appId, int offset, int window_size, String searchPhrase) throws SQLException {
		List<BadgeModel> badges = new ArrayList<BadgeModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".badge WHERE badge_id LIKE '"+pattern+"' ORDER BY badge_id LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			BadgeModel bmodel = new BadgeModel(rs.getString("badge_id"), rs.getString("name"), rs.getString("description"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			badges.add(bmodel);
		}
		return badges;
	}
	
	/**
	 * Check whether a badge with badge id is already exist
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param badge_id badge id
	 * @return true if the badge is already exist
	 * @throws SQLException sql exception
	 */
	public boolean isBadgeIdExist(Connection conn,String appId, String badge_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT badge_id FROM "+appId+".badge WHERE badge_id=?");
		stmt.setString(1, badge_id);
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
	 * Get a badge with specific id
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param badge_id badge id
	 * @return BadgeModel
	 * @throws SQLException sql exception
	 */
	public BadgeModel getBadgeWithId(Connection conn,String appId, String badge_id) throws SQLException {
			stmt = conn.prepareStatement("SELECT * FROM "+appId+".badge WHERE badge_id = ?");
			stmt.setString(1, badge_id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				return new BadgeModel(badge_id, rs.getString("name"), rs.getString("description"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			}
			return null;
	}

	/**
	 * Update a badge information
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param badge badge model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateBadge(Connection conn,String appId, BadgeModel badge) throws SQLException {

			stmt = conn.prepareStatement("UPDATE "+appId+".badge SET name = ?, description = ?, use_notification = ?, notif_message = ? WHERE badge_id = ?");
			stmt.setString(1, badge.getName());
			stmt.setString(2, badge.getDescription());
			stmt.setBoolean(3, badge.isUseNotification());
			stmt.setString(4, badge.getNotificationMessage());
			stmt.setString(5, badge.getId());
			stmt.executeUpdate();
			System.out.println("Badge id " + badge.getId() + " is updated");

	}

	/**
	 * Delete a specific badge
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param badge_id badge id
	 * @throws SQLException sql exception
	 */
	public void deleteBadge(Connection conn,String appId, String badge_id) throws SQLException {
		// TODO Auto-generated method stub
			stmt = conn.prepareStatement("DELETE FROM "+appId+".badge WHERE badge_id = ?");
			stmt.setString(1, badge_id);
			stmt.executeUpdate();
			System.out.println("Badge id " + badge_id + " is deleted from database");

	}

	/**
	 * Add a new badge
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param badge badge model
	 * @throws SQLException sql exception
	 */
	public void addNewBadge(Connection conn,String appId, BadgeModel badge) throws SQLException {

			stmt = conn.prepareStatement("INSERT INTO "+appId+".badge (badge_id, name, description, use_notification, notif_message) VALUES (?, ?, ?, ?, ?)");
			stmt.setString(1, badge.getId());
			stmt.setString(2, badge.getName());
			stmt.setString(3, badge.getDescription());
			stmt.setBoolean(4, badge.isUseNotification());
			stmt.setString(5, badge.getNotificationMessage());
			stmt.executeUpdate();
			
	}
	
}
