package i5.las2peer.services.gamificationStreakService.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.postgresql.util.PGInterval;

import i5.las2peer.services.gamificationStreakService.database.StreakModel.StreakSatstus;

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
				+ ".streak (streak_id, name, description, streak_level, status, point_th, locked_date, due_date, period, use_notification, notif_message)  VALUES (?,?,?,?,?,?,?,?,?,?,?)");
		stmt.setString(1, streak.getStreakId());
		stmt.setString(2, streak.getName());
		stmt.setString(3, streak.getDescription());
		stmt.setInt(4, streak.getStreakLevel());
		stmt.setObject(5, streak.getStatus().toString(), Types.OTHER);
		stmt.setInt(6, streak.getPointThreshold());
		stmt.setObject(7, streak.getLockedDate());
		stmt.setObject(8, streak.getDueDate());
		stmt.setObject(9, parsePeriodToInterval(streak.getPeriod()));
		stmt.setBoolean(10, streak.isNotificationCheck());
		stmt.setString(11, streak.getNotificationMessage());
		stmt.executeUpdate();

		List <String> actions = streak.getActions();
		if (actions != null && !actions.isEmpty()) {
			stmt = conn.prepareStatement("INSERT INTO " + gameId + ".streak_action (streak_id,  action_id) VALUES(?,?)");
			for (String action : actions) {
				stmt.setString(1, streak.getStreakId());
				stmt.setString(2, action);
				stmt.executeUpdate();
			}
		}
		
		Map<Integer, String> badges = streak.getBadges();
		if (badges != null && !(badges.isEmpty())) {
			stmt = conn.prepareStatement("INSERT INTO " + gameId + ".streak_badge (streak_level, badge_id, streak_id) VALUES(?,?,?)");
			for (Entry<Integer, String> entry : badges.entrySet()) {
				stmt.setInt(1, entry.getKey());
				stmt.setString(2, entry.getValue());
				stmt.setString(3, streak.getStreakId());
				stmt.executeUpdate();
			}
		}
		
		Map<Integer, String> achievements = streak.getAchievements();
		if (achievements != null && !(achievements.isEmpty())) {
			stmt = conn.prepareStatement("INSERT INTO " + gameId + ".streak_achievement (streak_level, achievement_id, streak_id) VALUES(?,?,?)");
			for (Entry<Integer, String> entry : achievements.entrySet()) {
				stmt.setInt(1, entry.getKey());
				stmt.setString(2, entry.getValue());
				stmt.setString(3, streak.getStreakId());
				stmt.executeUpdate();
			}
		}
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
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			StreakModel streak = new StreakModel();
			streak.setStreakId(rs.getString("streak_id"));
			streak.setName(rs.getString("name"));
			streak.setDescription(rs.getString("description"));
			streak.setStreakLevel(rs.getInt("streak_level"));
			streak.setStatus(StreakSatstus.valueOf(rs.getString("status")));
			streak.setPointThreshold(rs.getInt("point_th"));
			streak.setLockedDate(rs.getObject("locked_date", LocalDateTime.class));
			streak.setDueDate(rs.getObject("due_date", LocalDateTime.class));
			streak.setPeriod(parseIntervaltoPeriod(rs.getObject("period", PGInterval.class)));
			streak.setNotificationCheck(rs.getBoolean("use_notification"));
			streak.setNotificationMessage("notif_message");
			
			Map<Integer, String> badges = new HashMap<Integer, String>();
			stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak_badge WHERE streak_id = ?");
			stmt.setString(1, streakId);
			ResultSet rs2 = stmt.executeQuery();
			while (rs2.next()) {
				badges.put(rs2.getInt("streak_level"), rs2.getString("badge_id"));
			}
			streak.setBadges(badges);

			Map<Integer, String> achievements = new HashMap<Integer, String>();
			stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak_achievement WHERE streak_id = ?");
			stmt.setString(1, streakId);
			ResultSet rs3 = stmt.executeQuery();
			while (rs3.next()) {
				achievements.put(rs3.getInt("streak_level"), rs3.getString("achievement_id"));
			}
			streak.setAchievements(achievements);
			

			List<String> actions = new ArrayList<String>();
			stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak_action WHERE streak_id = ?");
			stmt.setString(1, streakId);
			ResultSet rs4 = stmt.executeQuery();
			while (rs4.next()) {
				actions.add(rs4.getString("action_id"));
			}
			streak.setActions(actions);
			
			return streak;
		}
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
		List<StreakModel> streaks = new ArrayList<StreakModel>();

		String pattern = "%" + searchPhrase + "%";
		stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak WHERE streak_id LIKE '" + pattern
				+ "' ORDER BY streak_id LIMIT " + window_size + " OFFSET " + offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			StreakModel streak = new StreakModel();
			streak.setStreakId(rs.getString("streak_id"));
			streak.setName(rs.getString("name"));
			streak.setDescription(rs.getString("description"));
			streak.setStreakLevel(rs.getInt("streak_level"));
			streak.setPointThreshold(rs.getInt("point_th"));
			streak.setLockedDate(rs.getObject("locked_date", LocalDateTime.class));
			streak.setDueDate(rs.getObject("due_date", LocalDateTime.class));
			streak.setPeriod(parseIntervaltoPeriod(rs.getObject("period", PGInterval.class)));
			streak.setNotificationCheck(rs.getBoolean("use_notification"));
			streak.setNotificationMessage("notif_message");
			streak.setStatus(StreakSatstus.valueOf(rs.getString("status")));
			streaks.add(streak);
		}
		if (streaks.equals(null)) {
			throw new IOException("Streak Model is null");
		}
		for (StreakModel streak : streaks) {
			Map<Integer, String> badges = new HashMap<Integer, String>();
			stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak_badge WHERE streak_id = ?");
			stmt.setString(1, streak.getStreakId());
			ResultSet rs2 = stmt.executeQuery();
			while (rs2.next()) {
				badges.put(rs2.getInt("streak_level"), rs2.getString("badge_id"));
			}
			streak.setBadges(badges);

			Map<Integer, String> achievements = new HashMap<Integer, String>();
			stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak_achievement WHERE streak_id = ?");
			stmt.setString(1, streak.getStreakId());
			ResultSet rs3 = stmt.executeQuery();
			while (rs3.next()) {
				achievements.put(rs3.getInt("streak_level"), rs3.getString("achievement_id"));
			}
			streak.setAchievements(achievements);
			
			List<String> actions = new ArrayList<String>();
			stmt = conn.prepareStatement("SELECT * FROM " + gameId + ".streak_action WHERE streak_id = ?");
			stmt.setString(1, streak.getStreakId());
			ResultSet rs4 = stmt.executeQuery();
			while (rs4.next()) {
				actions.add(rs4.getString("action_id"));
			}
			streak.setActions(actions);
		}
		return streaks;
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
		stmt = conn.prepareStatement("UPDATE " + gameId
				+ ".streak SET name = ?, description = ?, streak_level = ? ,status = ?, point_th = ?, locked_date = ?, due_date = ?, period = ?, use_notification = ?, notif_message = ? WHERE streak_id = ?");
		stmt.setString(1, streak.getName());
		stmt.setString(2, streak.getDescription());
		stmt.setInt(3, streak.getStreakLevel());
		stmt.setObject(4, streak.getStatus().toString(), Types.OTHER);
		stmt.setInt(5, streak.getPointThreshold());
		stmt.setObject(6, streak.getLockedDate());
		stmt.setObject(7, streak.getDueDate());
		stmt.setObject(8, parsePeriodToInterval(streak.getPeriod()));
		stmt.setBoolean(9, streak.isNotificationCheck());
		stmt.setString(10, streak.getNotificationMessage());
		stmt.setString(11, streak.getStreakId());
		stmt.executeUpdate();

		
		Map<Integer, String> badges = streak.getBadges();
		if (badges != null && !(badges.isEmpty())) {
			stmt = conn.prepareStatement("DELETE FROM " + gameId + " .streak_badge WHERE streak_id = ?");
			stmt.setString(1, streak.getStreakId());
			stmt.executeUpdate();

			stmt = conn.prepareStatement(
					"INSERT INTO " + gameId + ".streak_badge (streak_level, badge_id, streak_id) VALUES(?,?,?)");
			for (Entry<Integer, String> entry : badges.entrySet()) {
				stmt.setInt(1, entry.getKey());
				stmt.setString(2, entry.getValue());
				stmt.setString(3, streak.getStreakId());
				stmt.executeUpdate();
			}
		}

		Map<Integer, String> achievements = streak.getAchievements();
		if (achievements != null && !(achievements.isEmpty())) {
			stmt = conn.prepareStatement("DELETE FROM " + gameId + " .streak_achievement WHERE streak_id = ?");
			stmt.setString(1, streak.getStreakId());
			stmt.executeUpdate();

			stmt = conn.prepareStatement("INSERT INTO " + gameId
					+ ".streak_achievement (streak_level, achievement_id, streak_id) VALUES(?,?,?)");
			for (Entry<Integer, String> entry : achievements.entrySet()) {
				stmt.setInt(1, entry.getKey());
				stmt.setString(2, entry.getValue());
				stmt.setString(3, streak.getStreakId());
				stmt.executeUpdate();
			}
		}
		
		List <String> actions = streak.getActions();
		if (actions != null && !actions.isEmpty()) {
			stmt = conn.prepareStatement("DELETE FROM " + gameId + " .streak_action WHERE streak_id = ?");
			stmt.setString(1, streak.getStreakId());
			stmt.executeUpdate();
			
			stmt = conn.prepareStatement("INSERT INTO " + gameId + ".streak_action (streak_id,  action_id) VALUES(?,?)");
			for (String action : actions) {
				stmt.setString(1, streak.getStreakId());
				stmt.setString(2, action);
				stmt.executeUpdate();
			}
		}
	}

	/**
	 * Delete a specific streak
	 * 
	 * @param conn     database connection
	 * @param gameId   game id
	 * @param streakId streak id
	 * @throws SQLException
	 */
	public void deleteStreak(Connection conn, String gameId, String streakId) throws SQLException {
		stmt = conn.prepareStatement("DELETE FROM " + gameId + " .streak_badge WHERE streak_id = ?");
		stmt.setString(1, streakId);
		stmt.executeUpdate();

		stmt = conn.prepareStatement("DELETE FROM " + gameId + " .streak_achievement WHERE streak_id = ?");
		stmt.setString(1, streakId);
		stmt.executeUpdate();
		
		stmt = conn.prepareStatement("DELETE FROM " + gameId + " .streak_action WHERE streak_id = ?");
		stmt.setString(1, streakId);
		stmt.executeUpdate();

		stmt = conn.prepareStatement("DELETE FROM " + gameId + ".streak WHERE streak_id = ?");
		stmt.setString(1, streakId);
		stmt.executeUpdate();
	}

	private PGInterval parsePeriodToInterval(Period period) {
		return new PGInterval(period.getYears(), period.getMonths(), period.getDays(), 0, 0, 0);
	}

	private Period parseIntervaltoPeriod(PGInterval interval) {
		return Period.of(interval.getYears(), interval.getMonths(), interval.getDays());
	}
}
