package i5.las2peer.services.gamificationStreakService.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StreakDAO {
	private PreparedStatement stmt;

	/**
	 * Check whether the game id is already exist
	 * 
	 * @param conn    database connection
	 * @param game_id game id
	 * @return true game_id is already exist
	 * @throws SQLException
	 */
	public boolean isGameIdExist(Connection conn, String gameId) throws SQLException {
		stmt = conn.prepareStatement("SELECT game_id FROM manager.game_info WHERE game_id=?");
		stmt.setString(1, gameId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param conn     database connection
	 * @param gameId
	 * @param streakId
	 * @return true if streak does exist in game
	 * @throws SQLException
	 */
	public boolean isStreakIdExist(Connection conn, String gameId, String streakId) throws SQLException {
		stmt = conn.prepareStatement("SELECT streak_id FROM " + gameId + ".streak WHERE streak_id=? LIMIT 1");
		stmt.setString(1, streakId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return true;
		}
		return false;
	}

	/**
	 * Add a new streak
	 * 
	 * @param conn   database connection
	 * @param gameId game id
	 * @param streak streak model
	 * @throws SQLException
	 */
	public void addNewStreak(Connection conn, String gameId, StreakModel streak) throws SQLException {
		stmt = conn.prepareStatement("INSERT INTO " + gameId
				+ ".streak (streak_id, name, description, status, achievement_id, streak_flag, streak_id_completed, point_flag, point_value, use_notification , notif_message)  VALUES (?, ?, ?, ?::"
				+ gameId + ".streak_status, ?, ?, ?, ?, ?, ?, ?)");

	}

	/**
	 * Get an streak with specific id
	 * 
	 * @param conn     database connection
	 * @param gameId   game id
	 * @param streakId streak id
	 * @return StreakModel
	 * @throws IOException
	 * @throws SQLException
	 */
	public StreakModel getStreakWithId(Connection conn, String gameId, String streakId)
			throws IOException, SQLException {

		stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak WHERE streak_id = ?");
		stmt.setString(1, streakId);
		return null;
	}

	/**
	 * Get streaks with search parameter
	 * 
	 * @param conn         database connection
	 * @param gameId       game id
	 * @param offset       offset
	 * @param window_size  windowSize
	 * @param searchPhrase search phrase
	 * @return list of streaks
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<StreakModel> getStreaksWithOffsetAndSearchPhrase(Connection conn, String gameId, int offset,
			int window_size, String searchPhrase) throws SQLException, IOException {
		List<StreakModel> qs = new ArrayList<StreakModel>();
		return qs;
	}

	/**
	 * Get total number of streak
	 * 
	 * @param conn   database connection
	 * @param gameId game id
	 * @return total number of streak
	 * @throws SQLException
	 */
	public int getNumberOfStreaks(Connection conn, String gameId) throws SQLException {

		stmt = conn.prepareStatement("SELECT count(*) FROM " + gameId + ".streak");
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return rs.getInt(1);
		} else {
			return 0;
		}
	}

	/**
	 * Update a streak information
	 * 
	 * @param conn   database connection
	 * @param gameId game id
	 * @param streak model to be updated
	 * @throws SQLException
	 */
	public void updateStreak(Connection conn, String gameId, StreakModel streak) throws SQLException {
		stmt = conn.prepareStatement("UPDATE " + gameId + ".streak SET name = ?, description = ?, status = ?::" + gameId
				+ ".streak_status, achievement_id = ?, streak_flag = ?, streak_id_completed = ?, point_flag = ?, point_value = ?, use_notification = ?, notif_message = ? WHERE streak_id = ?");
	}

	/**
	 * Delete a specific streak
	 * 
	 * @param conn      database connection
	 * @param gameId    game id
	 * @param streak_id streak id
	 * @throws SQLException
	 */
	public void deleteStreak(Connection conn, String gameId, String streak_id) throws SQLException {
		stmt = conn.prepareStatement("DELETE FROM " + gameId + ".streak WHERE streak_id = ?");
		stmt.setString(1, streak_id);
		stmt.executeUpdate();
	}
}
