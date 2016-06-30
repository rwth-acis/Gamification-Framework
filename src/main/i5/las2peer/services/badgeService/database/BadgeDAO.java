package i5.las2peer.services.badgeService.database;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.sun.media.jfxmedia.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BadgeDAO {
	
	
	PreparedStatement stmt;
	Connection conn;
	
	public BadgeDAO( Connection conn){
		this.conn = conn;
	}
	
	/**
	 * Get all badges in the database
	 * 
	 * @return list of badges
	 */
	public List<BadgeModel> getAllBadges() throws SQLException{
		// TODO Auto-generated method stub
		List<BadgeModel> badges = new ArrayList<BadgeModel>();

			stmt = conn.prepareStatement("SELECT * FROM badge");
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				BadgeModel bmodel = new BadgeModel(rs.getString("badge_id"), rs.getString("name"), rs.getString("description"), rs.getString("image_path"));
				badges.add(bmodel);
			}

		return badges;
	}

	/**
	 * Check whether a badge with badge id is already exist
	 * 
	 * @return true if the badge is already exist
	 */
	public boolean isBadgeIdExist(String badge_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT badge_id FROM badge WHERE badge_id=?");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Get a badge with specific id
	 * 
	 * @param badgeId badge id
	 */
	public BadgeModel getBadgeWithId(String badgeId) {
		try {
			stmt = conn.prepareStatement("SELECT name,description,image_path FROM badge WHERE badge_id = ?");
			stmt.setString(1, badgeId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				return new BadgeModel(badgeId, rs.getString("name"), rs.getString("description"), rs.getString("image_path"));
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
	 * Update a badge information
	 * 
	 * @param badge badge model to be updated
	 */
	public void updateBadge(BadgeModel badge) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("UPDATE badge SET name = ?, description = ?, image_path = ? WHERE badge_id = ?");
			stmt.setString(1, badge.getName());
			stmt.setString(2, badge.getDescription());
			stmt.setString(3, badge.getImagePath());
			stmt.setString(4, badge.getId());
			stmt.executeUpdate();
			System.out.println("Badge id " + badge.getId() + " is updated");

	}

	/**
	 * Delete a specific badge
	 * 
	 * @param badgeid badge id
	 */
	public void deleteBadge(String badgeid) throws SQLException {
		// TODO Auto-generated method stub
			stmt = conn.prepareStatement("DELETE FROM badge WHERE badge_id = ?");
			stmt.setString(1, badgeid);
			stmt.executeUpdate();
			System.out.println("Badge id " + badgeid + " is deleted from database");

	}

	/**
	 * Add a new badge
	 * 
	 * @param badge badge model
	 */
	public void addNewBadge(BadgeModel badge) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("INSERT INTO badge (badge_id, name, description, image_path) VALUES (?, ?, ?, ?)");
			stmt.setString(1, badge.getId());
			stmt.setString(2, badge.getName());
			stmt.setString(3, badge.getDescription());
			stmt.setString(4, badge.getImagePath());
			stmt.executeUpdate();
			
	}
	
}
