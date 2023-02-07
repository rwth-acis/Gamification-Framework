package i5.las2peer.services.gamificationVisualizationService.database;


import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang3.tuple.Pair;
import org.postgresql.util.PGInterval;

import com.fasterxml.jackson.annotation.JsonBackReference;

import i5.las2peer.services.gamificationVisualizationService.database.QuestModel.QuestStatus;
import i5.las2peer.services.gamificationVisualizationService.database.StreakModel.StreakSatstus;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Class to maintain the model that are used in the game management
 * 
 */

public class VisualizationDAO {
	
	
	PreparedStatement stmt;
	
	public VisualizationDAO(){
	}
	
	/**
	 * Check whether the game id is already exist
	 * 
	 * @param conn database connection
	 * @param game_id game id
	 * @return true game_id is already exist
	 * @throws SQLException SQL Exception
	 */
	public boolean isGameIdExist(Connection conn,String game_id) throws SQLException  {
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
	 * Check whether the member is already registered
	 * 
	 * @param member_id member id
	 * @param conn database connection
	 * @return true member is already registered
	 * @throws SQLException exception
	 */
	public boolean isMemberRegistered(Connection conn,String member_id) throws SQLException {
		
		try {
			stmt = conn.prepareStatement("SELECT member_id,first_name,last_name,email FROM manager.member_info WHERE member_id=?");
			stmt.setString(1, member_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return true;
			}
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Check whether a member is registered in an game
	 * 
	 * @param member_id member id
	 * @param conn database connection
	 * @param game_id game id
	 * @return member registered in game
	 * @throws SQLException sql exception
	 */
	public boolean isMemberRegisteredInGame(Connection conn,String member_id, String game_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT * FROM manager.member_game WHERE member_id=? AND game_id=?");
		stmt.setString(1, member_id);
		stmt.setString(2, game_id);
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
	
	// Member DAO
	/**
	 * Get point of a member
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @return member point
	 * @throws SQLException sql exception
	 */
	public Integer getMemberPoint(Connection conn,String gameId, String memberId) throws SQLException{

		stmt = conn.prepareStatement("SELECT point_value FROM "+gameId+".member_point WHERE member_id=?");
		stmt.setString(1, memberId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return rs.getInt("point_value");
		}

		return null;
	}
	
	/**
	 * Get status of a member
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @return member point
	 * @throws SQLException sql exception
	 */
	public JSONObject getMemberStatus(Connection conn,String gameId, String memberId) throws SQLException{
		Integer currentLevel = 0;
		Integer memberPoint = 0;
		JSONObject resObj = new JSONObject();
		stmt = conn.prepareStatement("SELECT level_num,point_value FROM "+gameId+".member_level INNER JOIN "+gameId+".member_point ON ("+gameId+".member_level.member_id = "+gameId+".member_point.member_id) WHERE "+gameId+".member_level.member_id = ? LIMIT 1");
		stmt.setString(1, memberId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			currentLevel = rs.getInt("level_num");
			memberPoint = rs.getInt("point_value");
		}
		else{
			throw new SQLException("Member ID not found");
		}
		//Get current and next level
		stmt = conn.prepareStatement("SELECT level_num, name, point_value FROM "+gameId+".level WHERE level_num >= ? order by level_num LIMIT 2");
		stmt.setInt(1, currentLevel);
		rs = stmt.executeQuery();
		if (rs.next()) {
			Integer currentLevelPointThreshold = rs.getInt("point_value");
			String currentLevelName = rs.getString("name");
			if(rs.next()){
				Integer nextLevelPointThreshold = rs.getInt("point_value");
				Integer nextLevelNum = rs.getInt("level_num");
				String nextLevelName = rs.getString("name");
				float progress = (float) (((float)(memberPoint - currentLevelPointThreshold)/(float)(nextLevelPointThreshold - currentLevelPointThreshold)) * 100.0);
				
				resObj.put("memberLevel", currentLevel);
				resObj.put("memberPoint", memberPoint);
				resObj.put("memberLevelName", currentLevelName);
				resObj.put("nextLevel", nextLevelNum);
				resObj.put("nextLevelPoint", nextLevelPointThreshold);
				resObj.put("nextLevelName", nextLevelName);
				resObj.put("progress", Math.round(progress));
				
			}
			else{
				//throw new SQLException("No next level found, you may be on the highest level");
				resObj.put("memberLevel", currentLevel);
				resObj.put("memberPoint", memberPoint);
				resObj.put("memberLevelName", currentLevelName);
				resObj.put("nextLevel", "");
				resObj.put("nextLevelPoint", "");
				resObj.put("nextLevelName", "");
				resObj.put("progress", 100);
			}
			
			stmt = conn.prepareStatement("WITH sorted AS (SELECT *, row_number() OVER (ORDER BY point_value DESC) FROM "+gameId+".member_point) SELECT * FROM sorted WHERE member_id = '"+memberId+"' LIMIT 1");
			ResultSet rs2 = stmt.executeQuery();
			if (rs2.next()) {
				resObj.put("rank", rs2.getInt("row_number"));
			}
			else{
				resObj.put("rank", "-");
			}
			stmt = conn.prepareStatement("Select count(*) FROM "+gameId+".member_achievement where member_id='"+memberId+"'");
			ResultSet rs3 = stmt.executeQuery();
			if (rs3.next()) {
				resObj.put("unlockedAchievements", rs3.getInt("count"));
			}
			else{
				resObj.put("unlockedAchievements", "0");
			}
			stmt = conn.prepareStatement("Select count(*) FROM "+gameId+".achievement");
			ResultSet rs4 = stmt.executeQuery();
			if (rs4.next()) {
				resObj.put("totalAchievements", rs4.getInt("count"));
			}
			else{
				resObj.put("totalAchievements", "0");
			}
			stmt = conn.prepareStatement("Select count(*) FROM "+gameId+".member_badge where member_id='"+memberId+"'");
			ResultSet rs5 = stmt.executeQuery();
			if (rs5.next()) {
				resObj.put("unlockedBadges", rs5.getInt("count"));
			}
			else{
				resObj.put("unlockedBadges", "0");
			}
			stmt = conn.prepareStatement("Select count(*) FROM "+gameId+".badge");
			ResultSet rs6 = stmt.executeQuery();
			if (rs6.next()) {
				resObj.put("totalBadges", rs6.getInt("count"));
			}
			else{
				resObj.put("totalBadges", "0");
			}
		}
		else{
			throw new SQLException("No level found");
		}

		return resObj;
	}
	
	/**
	 * Get obtained badges of members
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @return list of badges
	 * @throws SQLException sql exception
	 */
	public List<BadgeModel> getObtainedBadges(Connection conn,String gameId, String memberId) throws SQLException {
		List<BadgeModel> badges = new ArrayList<BadgeModel>();
		stmt = conn.prepareStatement("WITH bdg AS (SELECT badge_id FROM "+gameId+".member_badge WHERE member_id=?) SELECT "+gameId+".badge.badge_id, name, description, use_notification, notif_message FROM "+gameId+".badge INNER JOIN bdg ON ("+gameId+".badge.badge_id = bdg.badge_id)");
		stmt.setString(1, memberId);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			BadgeModel bmodel = new BadgeModel(rs.getString("badge_id"), rs.getString("name"), rs.getString("description"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			badges.add(bmodel);
		}
		return badges;
	}
	
	/**
	 * Get quests of members with status
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @param status quest status
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException io exception
	 */
	public List<QuestModel> getMemberQuestsWithStatus(Connection conn,String gameId, String memberId, QuestStatus status) throws SQLException, IOException{
		List<QuestModel> qs = new ArrayList<QuestModel>();
		stmt = conn.prepareStatement("WITH qs AS (SELECT quest_id FROM "+gameId+".member_quest WHERE member_id=? AND status=?::"+gameId+".quest_status) SELECT "+gameId+".quest.quest_id, name, description, status, achievement_id, quest_flag, quest_id_completed, point_flag, point_value, use_notification, notif_message FROM "+gameId+".quest INNER JOIN qs ON ("+gameId+".quest.quest_id = qs.quest_id)");
		stmt.setString(1, memberId);
		stmt.setString(2, status.toString());
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			QuestModel qmodel = new QuestModel(rs.getString("quest_id"), rs.getString("name"), rs.getString("description"), QuestStatus.valueOf(rs.getString("status")), rs.getString("achievement_id"), rs.getBoolean("quest_flag"), rs.getString("quest_id_completed"), rs.getBoolean("point_flag"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			qs.add(qmodel);
		}
		
		if(qs.isEmpty()){
			return null;
		}
		for(QuestModel q : qs){
			List<Pair<String, Integer>> action_ids = new ArrayList<Pair<String, Integer>>();
			stmt = conn.prepareStatement("SELECT action_id, times FROM "+gameId+".quest_action WHERE quest_id=?");
			stmt.setString(1, q.getId());
			ResultSet rs2 = stmt.executeQuery();
			while (rs2.next()) {
				action_ids.add(Pair.of(rs2.getString("action_id"), rs2.getInt("times")));
			}
			q.setActionIds(action_ids);
			action_ids.clear();
		}
		return qs;
	}
	
	/**
	 * Get all quests of members except hidden quests
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException io exception
	 */
	public List<QuestModel> getMemberQuests(Connection conn,String gameId, String memberId) throws SQLException, IOException{
		List<QuestModel> qs = new ArrayList<QuestModel>();
		stmt = conn.prepareStatement("WITH qs AS (SELECT quest_id,status FROM "+gameId+".member_quest WHERE member_id=? AND status!='HIDDEN') SELECT "+gameId+".quest.quest_id, name, description, qs.status, achievement_id, quest_flag, quest_id_completed, point_flag, point_value, use_notification, notif_message FROM "+gameId+".quest INNER JOIN qs ON ("+gameId+".quest.quest_id = qs.quest_id)");
		stmt.setString(1, memberId);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			QuestModel qmodel = new QuestModel(rs.getString("quest_id"), rs.getString("name"), rs.getString("description"), QuestStatus.valueOf(rs.getString("status")), rs.getString("achievement_id"), rs.getBoolean("quest_flag"), rs.getString("quest_id_completed"), rs.getBoolean("point_flag"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			qs.add(qmodel);
		}
		
		if(qs.isEmpty()){
			return null;
		}
		for(QuestModel q : qs){
			List<Pair<String, Integer>> action_ids = new ArrayList<Pair<String, Integer>>();
			stmt = conn.prepareStatement("SELECT action_id, times FROM "+gameId+".quest_action WHERE quest_id=?");
			stmt.setString(1, q.getId());
			ResultSet rs2 = stmt.executeQuery();
			while (rs2.next()) {
				action_ids.add(Pair.of(rs2.getString("action_id"), rs2.getInt("times")));
			}
			q.setActionIds(action_ids);
			action_ids.clear();
		}
		return qs;
	}
	
	/**
	 * Get revealed quests of members progress
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @param questId quest id
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException io exception
	 */
	public JSONObject getMemberQuestProgress(Connection conn,String gameId, String memberId, String questId) throws SQLException, IOException{

		JSONArray resArr = new JSONArray();
		JSONObject outObj = new JSONObject();
		stmt = conn.prepareStatement("SELECT "+gameId+".member_quest_action.action_id,status,completed,times,"+gameId+".achievement.name,"+gameId+".achievement.description,"+gameId+".achievement.point_value,"+gameId+".achievement.badge_id FROM "+gameId+".member_quest_action INNER JOIN "+gameId+".quest_action ON ("+gameId+".member_quest_action.quest_id = "+gameId+".quest_action.quest_id AND "+gameId+".member_quest_action.action_id = "+gameId+".quest_action.action_id) JOIN "+gameId+".quest ON ("+gameId+".member_quest_action.quest_id="+gameId+".quest.quest_id) JOIN "+gameId+".achievement ON ("+gameId+".quest.achievement_id="+gameId+".achievement.achievement_id) WHERE "+gameId+".member_quest_action.quest_id=? AND member_id=?");
		stmt.setString(1, questId);
		stmt.setString(2, memberId);
		ResultSet rs = stmt.executeQuery();

		Integer truecount = 0;
		Integer totalcount = 0;
		while (rs.next()) {
			JSONObject resObj = new JSONObject();
			resObj.put("maxTimes", rs.getInt("times"));
			resObj.put("action", rs.getString("action_id"));
			resObj.put("isCompleted", rs.getBoolean("completed"));
			resObj.put("description", rs.getString("description"));
			resObj.put("points", rs.getString("point_value"));
			resObj.put("badge", rs.getString("badge_id"));
			resObj.put("name",rs.getString("name"));
			resObj.put("status",rs.getString("status"));
			//check if action is completed
			if(rs.getBoolean("completed")){
				
				// Ignore how many times player has performed an action
				// number have performed = number max times
				resObj.put("times", rs.getInt("times"));
				truecount = truecount + rs.getInt("times");
			}
			else{
				// get how many times player has performed an action
				stmt = conn.prepareStatement("SELECT count(*) FROM "+gameId+".member_action WHERE member_id = ? AND action_id = ?");
				stmt.setString(1, memberId);
				stmt.setString(2, rs.getString("action_id"));
				ResultSet rs2 = stmt.executeQuery();

				Integer totalTimesPerformed = 0;
				if (rs2.next()) {
					totalTimesPerformed = rs2.getInt("count");
				}
				resObj.put("times", totalTimesPerformed);
				truecount = truecount + totalTimesPerformed;
			}
			totalcount = totalcount + rs.getInt("times");
			resArr.put(resObj);
		}
		
		System.out.println("arr " + resArr.toString());
		outObj.put("actionArray", resArr);
		float progress = (float) (((float)truecount/(float)totalcount) * 100.0);
		outObj.put("progress", Math.round(progress));

		System.out.println("count " + truecount);
		System.out.println("total " + totalcount);
		System.out.println("progress " + progress);
		return outObj;
	}
	
	/**
	 * Get achievements of the members
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @return list of achievements
	 * @throws SQLException sql exception
	 */
	public List<AchievementModel> getMemberAchievements(Connection conn,String gameId, String memberId) throws SQLException{
		List<AchievementModel> achs = new ArrayList<AchievementModel>();
		stmt = conn.prepareStatement("WITH ach AS (SELECT achievement_id FROM "+gameId+".member_achievement WHERE member_id=?) SELECT "+gameId+".achievement.achievement_id, name, description, point_value, badge_id, use_notification, notif_message FROM "+gameId+".achievement INNER JOIN ach ON ("+gameId+".achievement.achievement_id = ach.achievement_id)");
		stmt.setString(1, memberId);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			AchievementModel bmodel = new AchievementModel(rs.getString("achievement_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getString("badge_id"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			achs.add(bmodel);
		}

		return achs;
	}
	
	/**
	 * Check if the member has a badge
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @param badgeId badge id
	 * @return true if member has a badge
	 * @throws SQLException sql exception
	 */
	public boolean isMemberHasBadge(Connection conn,String gameId, String memberId, String badgeId) throws SQLException{
		stmt = conn.prepareStatement("SELECT badge_id FROM "+gameId+".member_badge WHERE member_id=? AND badge_id=?");
		stmt.setString(1, memberId);
		stmt.setString(2, badgeId);

		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;
	}
	

	/**
	 * Check if the member has an achievement
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param memberId member id
	 * @param achievementId achievement id
	 * @return true if member has an achievement
	 * @throws SQLException sql exception
	 */
	public boolean isMemberHasAchievement(Connection conn,String gameId, String memberId, String achievementId) throws SQLException{
		stmt = conn.prepareStatement("SELECT achievement_id FROM "+gameId+".member_achievement WHERE member_id=? AND achievement_id=?");
		stmt.setString(1, memberId);
		stmt.setString(2, achievementId);
	
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;
	}

	/**
	 * Get total number of members for leaderboard
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @return total number of members for leaderboard
	 * @throws SQLException sql exception
	 */
	public int getNumberOfMembers(Connection conn,String gameId) throws SQLException {
		
			stmt = conn.prepareStatement("SELECT count(*) FROM "+gameId+".member_point");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	/**
	 * Get local leaderboard of a member
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return JSONObject leaderboard
	 * @throws SQLException sql exception
	 */
	public JSONArray getMemberLocalLeaderboard(Connection conn,String gameId, int offset, int window_size, String searchPhrase) throws SQLException{

		JSONArray arr = new JSONArray();
		String pattern = "%"+searchPhrase+"%";
		
		stmt = conn.prepareStatement("WITH sorted AS (SELECT *, row_number() OVER (ORDER BY point_value DESC) FROM "+gameId+".member_point) SELECT * FROM sorted WHERE member_id LIKE '"+pattern+"' LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("rank", rs.getInt("row_number"));
			obj.put("memberId", rs.getString("member_id"));
			obj.put("pointValue", rs.getInt("point_value"));
			arr.put(obj);
		}
		
		return arr;
	}

	/**
	 * Get local leaderboard of a member
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @return JSONObject leaderboard
	 * @throws SQLException sql exception
	 */
	public JSONArray getMemberLocalLeaderboard(Connection conn,String gameId) throws SQLException{

		JSONArray arr = new JSONArray();
		
		stmt = conn.prepareStatement("WITH sorted AS (SELECT *, row_number() OVER (ORDER BY point_value DESC) FROM "+gameId+".member_point) SELECT * FROM sorted");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("rank", rs.getInt("row_number"));
			obj.put("memberId", rs.getString("member_id"));
			obj.put("pointValue", rs.getInt("point_value"));
			arr.put(obj);
		}
		
		return arr;
	}


	/**
	 * Get local leaderboard of a member over actions done
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @return JSONObject leaderboard
	 * @throws SQLException sql exception
	 */
	public JSONArray getMemberLocalLeaderboardOverAction(Connection conn,String gameId, String action_id) throws SQLException{

		JSONArray arr = new JSONArray();
		stmt = conn.prepareStatement("WITH sorted AS (SELECT member_action.member_id,member_action.action_id,member_action.object_id,member_profile.nickname from "+gameId+".member_action JOIN "+gameId+".member_profile ON (member_action.member_id= member_profile.member_id) where action_id='"+action_id+"') SELECT member_id,nickname,count(action_id), row_number() OVER (ORDER BY COUNT(action_id) DESC) from sorted GROUP BY member_id,nickname;");
		ResultSet rs = stmt.executeQuery();
		List<String> users = new ArrayList<String>();
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("rank", rs.getInt("row_number"));
			obj.put("memberId", rs.getString("member_id"));
			obj.put("nickname", rs.getString("nickname"));
			obj.put("actioncount", rs.getInt("count"));
			arr.put(obj);
			users.add(rs.getString("member_id"));
		}
		stmt = conn.prepareStatement("SELECT * from "+gameId+".member Natural join "+gameId+".member_profile;");
		ResultSet rs2 = stmt.executeQuery();
		int lastPlace = arr.length()+1;
		while(rs2.next()){
			String user = rs2.getString("member_id");
			if(!users.contains(user)){
				JSONObject obj = new JSONObject();
				obj.put("memberId",user);
				obj.put("rank",lastPlace);
				obj.put("nickname", rs2.getString("nickname"));
				lastPlace++;
				obj.put("actioncount",0);
				arr.put(obj);
			}
		}
		return arr;
	}
	
	/**
	 * Get global leaderboard of a member
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return JSONObject leaderboard
	 * @throws SQLException sql exception
	 */
	public JSONArray getMemberGlobalLeaderboard(Connection conn,String gameId, int offset, int window_size, String searchPhrase) throws SQLException{

		JSONArray arr = new JSONArray();
		String pattern = "%"+searchPhrase+"%";
		String commType = null;
		
		// Get community type from an game
		stmt = conn.prepareStatement("SELECT community_type FROM manager.game_info WHERE game_id = ?");
		stmt.setString(1, gameId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			commType = rs.getString("community_type");
		}
		
		stmt = conn.prepareStatement("WITH sorted AS (SELECT *, row_number() OVER (ORDER BY point_value DESC) FROM global_leaderboard."+commType+") SELECT * FROM sorted WHERE member_id LIKE '"+pattern+"' LIMIT "+window_size+" OFFSET "+offset);
		rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("rank", rs.getInt("row_number"));
			obj.put("memberId", rs.getString("member_id"));
			obj.put("pointValue", rs.getInt("point_value"));
			arr.put(obj);
		}
		
		return arr;
	}

		/**
	 * Get global leaderboard of a member
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @return JSONObject leaderboard
	 * @throws SQLException sql exception
	 */
	public JSONArray getMemberGlobalLeaderboard(Connection conn,String gameId) throws SQLException{

		JSONArray arr = new JSONArray();
		String commType = null;
		
		// Get community type from an game
		stmt = conn.prepareStatement("SELECT community_type FROM manager.game_info WHERE game_id = ?");
		stmt.setString(1, gameId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			commType = rs.getString("community_type");
		}
		
		stmt = conn.prepareStatement("WITH sorted AS (SELECT *, row_number() OVER (ORDER BY point_value DESC) FROM global_leaderboard."+commType+") SELECT * FROM sorted");
		rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("rank", rs.getInt("row_number"));
			obj.put("memberId", rs.getString("member_id"));
			obj.put("pointValue", rs.getInt("point_value"));
			arr.put(obj);
		}
		
		return arr;
	}
	
	public static enum NotificationType {
		BADGE,
		ACHIEVEMENT,
		QUEST,
		LEVEL,
		STREAK,
	}
	
	public JSONArray getMemberNotification(Connection conn,String gameId, String memberId) throws SQLException{
		JSONArray resArray = new JSONArray();

		// Fetch notifications caused by action
		stmt = conn.prepareStatement("SELECT * FROM "+gameId+".notification WHERE member_id = ?");
		stmt.setString(1, memberId);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()){
			if(rs.getBoolean("use_notification")){
				JSONObject resObj = new JSONObject();
				resObj.put("memberId", rs.getString("member_id"));
				resObj.put("type", NotificationType.valueOf(rs.getString("type")));
				resObj.put("typeId", rs.getString("type_id"));
				resObj.put("message", rs.getString("message"));
				resObj.put("otherMessage", rs.getString("other_message"));
				resArray.put(resObj);
			}
		}
		
		return resArray;
	}

	
	/**
	 * 
	 * @param conn     database connection
	 * @param memberId memberId
	 * @param gameId   gameId
	 * @param streakId streakId
	 * @return true if streak does exist in game
	 * @throws SQLException SQLException
	 */
	public boolean isMemberHasStreak(Connection conn, String gameId, String memberId, String streakId) throws SQLException {
		stmt = conn.prepareStatement("SELECT streak_id FROM " + gameId + ".member_streak WHERE streak_id=? AND member_id = ? LIMIT 1");
		stmt.setString(1, streakId);
		stmt.setString(2,memberId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return true;
		}
		return false;
	}
	
	/** Returns streak details for all streaks of a member in this game
	 * 
	 * @param conn dbConnection
	 * @param gameId gameId to prrocess
	 * @param memberId memberId of Member
	 * @return streak information in JSON
	 * @throws SQLException SQLException
	 * @throws IOException IOException
	 */
	public JSONArray getStreakList(Connection conn, String gameId, String memberId) throws SQLException, IOException{
		stmt =  conn.prepareStatement("SELECT streak_id FROM " + gameId +".member_streak WHERE member_id = ?");
		stmt.setString(1, memberId);
		ResultSet rs = stmt.executeQuery();
		JSONArray arr = new JSONArray();
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("streakId", rs.getString("streak_id"));
			arr.put(obj);
		}
		return arr;
	}
	
	/**
	 * 
	 * @param interval of type PGInterval
	 * @return Period Object converted from PGInterval
	 */
	private String parseIntervaltoPeriod(PGInterval interval) {
		return Period.of(interval.getYears(), interval.getMonths(), interval.getDays()).toString()+interval.getHours()+"H"+interval.getMinutes()+"M"+interval.getWholeSeconds()+"S";
	}

	/**
	 * 
	 * @param conn dbConnection
	 * @param gameId gameId
	 * @param memberId memberId
	 * @param streakId streakId
	 * @return streak progress details of member in JSON
	 * @throws SQLException SQLException
	 * @throws IOException  IOException
	 */
	public JSONObject getMemberStreakProgress(Connection conn, String gameId, String memberId, String streakId) throws SQLException, IOException{
		stmt =conn.prepareStatement("WITH stk AS (SELECT streak_id, due_date , locked_date, current_streak_level, highest_streak_level, status FROM "+gameId+".member_streak where member_id =? AND streak_id=?) SELECT stk.streak_id, stk.due_date , stk.locked_date, current_streak_level, highest_streak_level, name, description, use_notification, notif_message, point_th, stk.status, period FROM stk INNER JOIN "+gameId+".streak ON ("+gameId+".streak.streak_id = stk.streak_id);");
		stmt.setString(1, memberId);
		stmt.setString(2, streakId);
		ResultSet rs = stmt.executeQuery();
		JSONObject obj = new JSONObject();
		while (rs.next()) {
			obj.put("streakId", rs.getString("streak_id"));
			obj.put("name", rs.getString("name"));
			obj.put("description", rs.getString("description"));
			obj.put("status", i5.las2peer.services.gamificationVisualizationService.database.StreakModel.StreakSatstus.valueOf(rs.getString("status")));
			obj.put("pointTreshold", rs.getInt("point_th"));
			obj.put("period", parseIntervaltoPeriod(rs.getObject("period", PGInterval.class)).toString());
			obj.put("lockedDate", rs.getObject("locked_date", LocalDateTime.class));
			obj.put("dueDate", rs.getObject("due_date", LocalDateTime.class));
			obj.put("notificationCheck", rs.getBoolean("use_notification"));
			obj.put("notificationMessage", rs.getString("notif_message"));
			obj.put("currentStreakLevel", rs.getInt("current_streak_level"));
			obj.put("highestStreakLevel", rs.getInt("highest_streak_level"));
		}
		
		stmt = conn.prepareStatement("SELECT * FROM " + gameId+ ".member_streak_action WHERE streak_id = ? AND member_id = ?");
		stmt.setString(1, streakId);
		stmt.setString(2, memberId);
		ResultSet rs2 = stmt.executeQuery();
		JSONArray openActions = new JSONArray();
		JSONArray completedActions = new JSONArray();
		while (rs2.next()) {
			JSONObject actionObj= new JSONObject();
			actionObj.put("actionId", rs2.getString("action_id"));
			if(rs2.getBoolean("completed")) {
				completedActions.put(actionObj);
			}
			else {
				openActions.put(actionObj);
			}
		}
		obj.put("openActions", openActions);
		obj.put("completedActions", completedActions);
		
		stmt = conn. prepareStatement("SELECT badge_id FROM "+ gameId + ".member_streak_badge WHERE streak_id =? AND member_id = ? AND active = true;");
		stmt.setString(1, streakId);
		stmt.setString(2,memberId);
		ResultSet rs3 = stmt.executeQuery();
		if(rs3.next()) {
			obj.put("currentBadge",rs3.getString("badge_id"));
		}
		stmt = conn.prepareStatement("SELECT streak_level, achievement_id FROM "+ gameId+".member_streak_achievement WHERE streak_id =? AND member_id =? AND unlocked=false;");
		stmt.setString(1, streakId);
		stmt.setString(2,memberId);
		JSONArray lockedAchievements = new JSONArray();
		ResultSet rs4 = stmt.executeQuery();
		while (rs4.next()) {
			JSONObject achObj = new JSONObject();
			achObj.put("streakLevel", rs4.getInt("streak_level"));
			achObj.put("achievementId", rs4.getString("achievement_id"));
			lockedAchievements.put(achObj);
		}
		obj.put("lockedAchievements", lockedAchievements);
		return obj;
	}

	/**
	 * 
	 * @param conn dbConnection
	 * @param gameId gameId
	 * @param memberId memberId
	 * @return streak progress details of member in JSON
	 * @throws SQLException SQLException
	 * @throws IOException  IOException
	 */
	public JSONObject getMemberStreakProgressDetailed(Connection conn, String gameId, String memberId) throws SQLException, IOException{
		stmt =conn.prepareStatement("Select * from "+ gameId + ".member_streak JOIN "+ gameId + ".streak_achievement ON (member_streak.streak_id = streak_achievement.streak_id) JOIN "+ gameId + ".streak_action ON (streak_achievement.streak_id = streak_action.streak_id) WHERE member_id='"+memberId+"';");
		ResultSet rs = stmt.executeQuery();
		JSONArray arr = new JSONArray();
		JSONObject resp = new JSONObject();

		while (rs.next()) {
			JSONObject obj = new JSONObject();
		//	obj.put("streakId", rs.getString("streak_id"));
			//obj.put("name", rs.getString("name"));
			//obj.put("description", rs.getString("description"));
			obj.put("status", i5.las2peer.services.gamificationVisualizationService.database.StreakModel.StreakSatstus.valueOf(rs.getString("status")));
		//	obj.put("pointTreshold", rs.getInt("point_th"));
		//	obj.put("period", parseIntervaltoPeriod(rs.getObject("period", PGInterval.class)).toString());
			obj.put("lockedDate", rs.getObject("locked_date", LocalDateTime.class));
			obj.put("dueDate", rs.getObject("due_date", LocalDateTime.class));
		//	obj.put("notificationCheck", rs.getBoolean("use_notification"));
	//		obj.put("notificationMessage", rs.getString("notif_message"));
			obj.put("currentStreakLevel", rs.getInt("current_streak_level"));
			obj.put("highestStreakLevel", rs.getInt("highest_streak_level"));
			obj.put("achievement_id", rs.getString("achievement_id"));
			// this will cause rror
			obj.put("action_id", rs.getString("action_id"));
			stmt = conn.prepareStatement("Select * from "+gameId+".action WHERE action_id='"+rs.getString("action_id")+"'");
			ResultSet rs1 = stmt.executeQuery();
			JSONObject action = new JSONObject();
			if(rs1.next()){
				action.put("id",rs1.getString("action_id"));
				action.put("name",rs1.getString("name"));
				action.put("description",rs1.getString("description"));
				
			}
			obj.put("action", action);
			stmt = conn.prepareStatement("Select * from "+gameId+".achievement WHERE achievement_id='"+rs.getString("achievement_id")+"'");
			ResultSet rs2 = stmt.executeQuery();
			JSONObject achievement = new JSONObject();
			if(rs2.next()){
				achievement.put("id",rs2.getString("achievement_id"));
				achievement.put("name",rs2.getString("name"));
				achievement.put("description",rs2.getString("description"));
				achievement.put("badge_id",rs2.getString("badge_id"));
				achievement.put("point_value",rs2.getString("point_value"));
				
			}
			obj.put("achievement",achievement);
			arr.put(obj);
		}
		
		resp.put("streaks", arr);
		return resp;
	}
	
	
	
	public JSONObject getAccumulativeStreakData(Connection conn, String gameId, String memberId, String streakId) throws SQLException, IOException{
		JSONObject streak = getMemberStreakProgress(conn, gameId, memberId, streakId);
		
		JSONArray openActions = streak.getJSONArray("openActions");
		for (int i = 0; i < openActions.length(); i++) {
			JSONObject action = openActions.getJSONObject(i);
			stmt = conn.prepareStatement("SELECT point_value, name, description FROM " + gameId + ".action WHERE action_id =?");
			stmt.setString(1, action.getString("actionId"));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				action.put("pointValue", rs.getInt("point_value"));
				action.put("name", rs.getString("name"));
				action.put("description", rs.getString("description"));
			}
		}
		
		
		JSONArray completedActions = streak.getJSONArray("completedActions");
		int currentPoint = 0;
		for (int i = 0; i < completedActions.length(); i++) {
			JSONObject action = completedActions.getJSONObject(i);
			stmt = conn.prepareStatement("SELECT point_value, name, description FROM " + gameId + ".action WHERE action_id =?");
			stmt.setString(1, action.getString("actionId"));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				action.put("pointValue", rs.getInt("point_value"));
				action.put("name", rs.getString("name"));
				action.put("description", rs.getString("description"));
				currentPoint = currentPoint + rs.getInt("point_value");
			}
		}
		streak.put("currentPoint", currentPoint);
		
		JSONArray achievements = streak.getJSONArray("lockedAchievements");
		for (int i = 0; i < achievements.length(); i++) {
			JSONObject achievement = achievements.getJSONObject(i);
			stmt = conn.prepareStatement("SELECT name, badge_id, description FROM " + gameId + ".achievement WHERE achievement_id =?");
			stmt.setString(1, achievement.getString("achievementId"));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				achievement.put("name", rs.getString("name"));
				achievement.put("badgeId", rs.getString("badge_id"));
				achievement.put("description", rs.getString("description"));
			}
		}
		return streak;
	}

	public void pauseStreak(Connection conn, String gameId, String memberId, String streakId) throws SQLException {
		stmt = conn.prepareStatement("Update " + gameId + ".member_streak set status = ? WHERE member_id =? AND streak_id = ?");
		stmt.setObject(1, StreakSatstus.PAUSED.toString(), Types.OTHER);
		stmt.setString(2, memberId);
		stmt.setString(3, streakId);
		stmt.executeUpdate();
	}
}
