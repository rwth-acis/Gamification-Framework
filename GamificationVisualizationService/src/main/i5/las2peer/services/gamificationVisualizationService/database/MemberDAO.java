package i5.las2peer.services.gamificationVisualizationService.database;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import i5.las2peer.services.gamificationVisualizationService.database.QuestModel.QuestStatus;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class to maintain the model that are used in the application management
 * 
 */

public class MemberDAO {
	
	
	PreparedStatement stmt;
	
	public MemberDAO(){
	}
	
	/**
	 * Get obtained badges of members
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param memberId member id
	 * @return list of badges
	 * @throws SQLException sql exception
	 */
	public List<BadgeModel> getObtainedBadges(Connection conn,String appId, String memberId) throws SQLException {
		List<BadgeModel> badges = new ArrayList<BadgeModel>();
		stmt = conn.prepareStatement("WITH bdg AS (SELECT badge_id FROM "+appId+".member_badge WHERE member_id=?) SELECT "+appId+".badge.badge_id, name, description, image_path, use_notification, notif_message FROM "+appId+".badge INNER JOIN bdg ON ("+appId+".badge.badge_id = bdg.badge_id)");
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
	 * @param appId application id
	 * @param memberId member id
	 * @param status quest status
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException io exception
	 */
	public List<QuestModel> getMemberQuestsWithStatus(Connection conn,String appId, String memberId, QuestStatus status) throws SQLException, IOException{
		List<QuestModel> qs = new ArrayList<QuestModel>();
		stmt = conn.prepareStatement("WITH qs AS (SELECT quest_id FROM "+appId+".member_quest WHERE member_id=? AND status=?::"+appId+".quest_status) SELECT "+appId+".quest.quest_id, name, description, status, achievement_id, quest_flag, quest_id_completed, point_flag, point_value, use_notification, notif_message FROM "+appId+".quest INNER JOIN qs ON ("+appId+".quest.quest_id = qs.quest_id)");
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
			stmt = conn.prepareStatement("SELECT action_id, times FROM "+appId+".quest_action WHERE quest_id=?");
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
	 * @param appId application id
	 * @param memberId member id
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException io exception
	 */
	public List<QuestModel> getMemberQuests(Connection conn,String appId, String memberId) throws SQLException, IOException{
		List<QuestModel> qs = new ArrayList<QuestModel>();
		stmt = conn.prepareStatement("WITH qs AS (SELECT quest_id,status FROM "+appId+".member_quest WHERE member_id=? AND status!='HIDDEN') SELECT "+appId+".quest.quest_id, name, description, qs.status, achievement_id, quest_flag, quest_id_completed, point_flag, point_value, use_notification, notif_message FROM "+appId+".quest INNER JOIN qs ON ("+appId+".quest.quest_id = qs.quest_id)");
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
			stmt = conn.prepareStatement("SELECT action_id, times FROM "+appId+".quest_action WHERE quest_id=?");
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
	 * @param appId application id
	 * @param memberId member id
	 * @param questId quest id
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException io exception
	 */
	public JSONObject getMemberQuestProgress(Connection conn,String appId, String memberId, String questId) throws SQLException, IOException{

		JSONArray resArr = new JSONArray();
		JSONObject outObj = new JSONObject();
		stmt = conn.prepareStatement("SELECT "+appId+".member_quest_action.action_id,completed,times FROM "+appId+".member_quest_action INNER JOIN "+appId+".quest_action ON ("+appId+".member_quest_action.quest_id = "+appId+".quest_action.quest_id AND "+appId+".member_quest_action.action_id = "+appId+".quest_action.action_id) WHERE "+appId+".member_quest_action.quest_id=? AND member_id=?");
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
			
			//check if action is completed
			if(rs.getBoolean("completed")){
				
				// Ignore how many times player has performed an action
				// number have performed = number max times
				resObj.put("times", rs.getInt("times"));
				truecount = truecount + rs.getInt("times");
			}
			else{
				// get how many times player has performed an action
				stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".member_action WHERE member_id = ? AND action_id = ?");
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
			resArr.add(resObj);
		}
		
		System.out.println("arr " + resArr.toJSONString());
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
	 * @param appId application id
	 * @param memberId member id
	 * @return list of achievements
	 * @throws SQLException sql exception
	 */
	public List<AchievementModel> getMemberAchievements(Connection conn,String appId, String memberId) throws SQLException{
		// TODO Auto-generated method stub
		List<AchievementModel> achs = new ArrayList<AchievementModel>();
		stmt = conn.prepareStatement("WITH ach AS (SELECT achievement_id FROM "+appId+".member_achievement WHERE member_id=?) SELECT "+appId+".achievement.achievement_id, name, description, point_value, badge_id, use_notification, notif_message FROM "+appId+".achievement INNER JOIN ach ON ("+appId+".achievement.achievement_id = ach.achievement_id)");
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
	 * @param appId application id
	 * @param memberId member id
	 * @param badgeId badge id
	 * @return true if member has a badge
	 * @throws SQLException sql exception
	 */
	public boolean isMemberHasBadge(Connection conn,String appId, String memberId, String badgeId) throws SQLException{
		stmt = conn.prepareStatement("SELECT badge_id FROM "+appId+".member_badge WHERE member_id=? AND badge_id=?");
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
	 * @param appId application id
	 * @param memberId member id
	 * @param achievementId achievement id
	 * @return true if member has an achievement
	 * @throws SQLException sql exception
	 */
	public boolean isMemberHasAchievement(Connection conn,String appId, String memberId, String achievementId) throws SQLException{
		stmt = conn.prepareStatement("SELECT achievement_id FROM "+appId+".member_achievement WHERE member_id=? AND achievement_id=?");
		stmt.setString(1, memberId);
		stmt.setString(2, achievementId);
	
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the member has completed a quest
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param memberId member id
	 * @param questId quest id
	 * @return true if member has a badge
	 * @throws SQLException sql exception
	 */
	public boolean isMemberHasCompletedQuest(Connection conn,String appId, String memberId, String questId) throws SQLException{
		stmt = conn.prepareStatement("SELECT badge_id FROM "+appId+".member_quest WHERE member_id=? AND quest_id=? AND status='COMPLETED'");
		stmt.setString(1, memberId);
		stmt.setString(2, questId);

		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;
	}
	
	/**
	 * Get point of a member
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param memberId member id
	 * @return member point
	 * @throws SQLException sql exception
	 */
	public Integer getMemberPoint(Connection conn,String appId, String memberId) throws SQLException{

		stmt = conn.prepareStatement("SELECT point_value FROM "+appId+".member_point WHERE member_id=?");
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
	 * @param appId application id
	 * @param memberId member id
	 * @return member point
	 * @throws SQLException sql exception
	 */
	public JSONObject getMemberStatus(Connection conn,String appId, String memberId) throws SQLException{
		Integer currentLevel = 0;
		Integer memberPoint = 0;
		JSONObject resObj = new JSONObject();
		stmt = conn.prepareStatement("SELECT level_num,point_value FROM "+appId+".member_level INNER JOIN "+appId+".member_point ON ("+appId+".member_level.member_id = "+appId+".member_point.member_id) WHERE "+appId+".member_level.member_id = ? LIMIT 1");
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
		stmt = conn.prepareStatement("SELECT level_num, level_name, point_value FROM "+appId+".level WHERE level_num >= ? LIMIT 2");
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
				resObj.put("nextLevel", null);
				resObj.put("nextLevelPoint", null);
				resObj.put("nextLevelName", "");
				resObj.put("progress", 100);
			}
			
			stmt = conn.prepareStatement("WITH sorted AS (SELECT *, row_number() OVER (ORDER BY point_value DESC) FROM "+appId+".member_point) SELECT * FROM sorted WHERE member_id = '"+memberId+"' LIMIT 1");
			ResultSet rs2 = stmt.executeQuery();
			if (rs2.next()) {
				resObj.put("rank", rs2.getInt("row_number"));
			}
			else{
				resObj.put("rank", "-");
			}
		}
		else{
			throw new SQLException("No level found");
		}

		return resObj;
	}
	
	/**
	 * Get local leaderboard of a member
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return JSONObject leaderboard
	 * @throws SQLException sql exception
	 */
	public JSONArray getMemberLocalLeaderboard(Connection conn,String appId, int offset, int window_size, String searchPhrase) throws SQLException{

		JSONArray arr = new JSONArray();
		String pattern = "%"+searchPhrase+"%";
		
		stmt = conn.prepareStatement("WITH sorted AS (SELECT *, row_number() OVER (ORDER BY point_value DESC) FROM "+appId+".member_point) SELECT * FROM sorted WHERE member_id LIKE '"+pattern+"' LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject obj = new JSONObject();
			obj.put("rank", rs.getInt("row_number"));
			obj.put("memberId", rs.getString("member_id"));
			obj.put("pointValue", rs.getInt("point_value"));
			arr.add(obj);
		}
		
		return arr;
	}
	
	/**
	 * Get global leaderboard of a member
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return JSONObject leaderboard
	 * @throws SQLException sql exception
	 */
	public JSONArray getMemberGlobalLeaderboard(Connection conn,String appId, int offset, int window_size, String searchPhrase) throws SQLException{

		JSONArray arr = new JSONArray();
		String pattern = "%"+searchPhrase+"%";
		String commType = null;
		
		// Get community type from an app
		stmt = conn.prepareStatement("SELECT community_type FROM manager.application_info WHERE app_id = ?");
		stmt.setString(1, appId);
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
			arr.add(obj);
		}
		
		return arr;
	}
	
	/**
	 * Get total number of members for leaderboard
	 * 
	 * @param conn database connection
	 * @param appId application id
	 * @return total number of members for leaderboard
	 * @throws SQLException sql exception
	 */
	public int getNumberOfMembers(Connection conn,String appId) throws SQLException {
		
			stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".member_point");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	public static enum NotificationType {
		BADGE,
		ACHIEVEMENT,
		QUEST,
		LEVEL,
	}
	
	public JSONArray getMemberNotification(Connection conn,String appId, String memberId) throws SQLException{
		JSONArray resArray = new JSONArray();

		// Fetch notifications caused by action
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".notification WHERE member_id = ?");
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
				resArray.add(resObj);
			}
		}
		
		return resArray;
	}
}