package i5.las2peer.services.gamificationBotWrapperService.database;

import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileDAO {

	PreparedStatement stmt;

	public ProfileDAO() {
	}

	/**
	 * Check whether the game id is already exist
	 * 
	 * @param conn    database connection
	 * @param game_id game id
	 * @return true game_id is already exist
	 * @throws SQLException SQL Exception
	 */
	public boolean setProfileBadge(Connection conn, String game_id, String member_id, String badge_id)
			throws SQLException {
		stmt = conn.prepareStatement("INSERT INTO  " + game_id + ".member_profile (member_id, badge_id) VALUES ('"
				+ member_id + "','" + badge_id + "');");
				System.out.println(stmt.toString());
		int success = stmt.executeUpdate();
		if (success == 1) {
			// if(rs.getString("game_id").equals(game_id)){
			return true;
			// }
		}
		return false;
	}

	/**
	 * Get all badges in the database
	 * 
	 * @param conn   database connection
	 * @param gameId game id
	 * @return list of badges
	 * @throws SQLException sql exception
	 */
	public String getProfileBadge(Connection conn, String gameId, String member_id) throws SQLException {


		stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".member_profile WHERE member_id='"+member_id+"'");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			return rs.getString("badge_id");
		}

		return "";
	}

}
