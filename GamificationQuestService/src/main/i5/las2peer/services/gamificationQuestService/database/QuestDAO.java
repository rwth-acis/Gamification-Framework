package i5.las2peer.services.gamificationQuestService.database;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import i5.las2peer.services.gamificationQuestService.database.QuestModel.QuestStatus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestDAO {
	
	
	PreparedStatement stmt;
	Connection conn;
	
	public QuestDAO( Connection conn){
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
	 * Get all quests in the database
	 * 
	 * @param appId application id
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException io exception
	 */
	public List<QuestModel> getAllQuests(String appId) throws SQLException, IOException{
		
		List<QuestModel> qs = new ArrayList<QuestModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".quest");
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
	 * Check whether a quest with quest id is already exist
	 * 
	 * @param appId application id
	 * @param quest_id quest id
	 * @return true if the quest is already exist
	 * @throws SQLException sql exception
	 */
	public boolean isQuestIdExist(String appId, String quest_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT quest_id FROM "+appId+".quest WHERE quest_id=? LIMIT 1");
		stmt.setString(1, quest_id);
		
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;
	}
	
	/**
	 * Get an quest with specific id
	 * 
	 * @param appId application id
	 * @param quest_id quest id
	 * @return QuestModel
	 * @throws IOException io exception
	 * @throws SQLException sql exception 
	 */
	public QuestModel getQuestWithId(String appId, String quest_id) throws IOException, SQLException {
		
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".quest WHERE quest_id = ?");
		stmt.setString(1, quest_id);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()){
			QuestModel qmodel = new QuestModel(rs.getString("quest_id"), rs.getString("name"), rs.getString("description"), QuestStatus.valueOf(rs.getString("status")), rs.getString("achievement_id"), rs.getBoolean("quest_flag"), rs.getString("quest_id_completed"), rs.getBoolean("point_flag"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));

			List<Pair<String, Integer>> action_ids = new ArrayList<Pair<String, Integer>>();
			stmt = conn.prepareStatement("SELECT action_id, times FROM "+appId+".quest_action WHERE quest_id=?");
			stmt.setString(1, qmodel.getId());
			ResultSet rs2 = stmt.executeQuery();
			while (rs2.next()) {
				action_ids.add(Pair.of(rs2.getString("action_id"), rs2.getInt("times")));
			}
			qmodel.setActionIds(action_ids);
			action_ids.clear();
			
			return qmodel;
		}
		return null;
	}

	/**
	 * Get total number of quest
	 * 
	 * @param appId application id
	 * @return total number of quest
	 * @throws SQLException sql exception
	 */
	public int getNumberOfQuests(String appId) throws SQLException {

			stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".quest");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	/**
	 * Get quests with search parameter
	 * 
	 * @param appId application id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of quests
	 * @throws SQLException sql exception
	 * @throws IOException  io exception
	 */
	public List<QuestModel> getQuestsWithOffsetAndSearchPhrase(String appId, int offset, int window_size, String searchPhrase) throws SQLException, IOException {
		List<QuestModel> qs= new ArrayList<QuestModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".quest WHERE quest_id LIKE '"+pattern+"' ORDER BY quest_id LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			QuestModel qmodel = new QuestModel(rs.getString("quest_id"), rs.getString("name"), rs.getString("description"), QuestStatus.valueOf(rs.getString("status")), rs.getString("achievement_id"), rs.getBoolean("quest_flag"), rs.getString("quest_id_completed"), rs.getBoolean("point_flag"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			qs.add(qmodel);
		}
		if(qs.equals(null)){
			throw new IOException("Quest Model is null");
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
	 * Update a quest information
	 * 
	 * @param appId application id
	 * @param quest model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateQuest(String appId, QuestModel quest) throws SQLException {
		stmt = conn.prepareStatement("UPDATE "+appId+".quest SET name = ?, description = ?, status = ?::"+appId+".quest_status, achievement_id = ?, quest_flag = ?, quest_id_completed = ?, point_flag = ?, point_value = ?, use_notification = ?, notif_message = ? WHERE quest_id = ?");
		stmt.setString(1, quest.getName());
		stmt.setString(2, quest.getDescription());
		stmt.setString(3, quest.getStatus().toString());
		stmt.setString(4, quest.getAchievementId());
		stmt.setBoolean(5, quest.getQuestFlag());
		stmt.setString(6, quest.getQuestIdCompleted());
		stmt.setBoolean(7, quest.getPointFlag());
		stmt.setInt(8, quest.getPointValue());
		stmt.setBoolean(9, quest.isUseNotification());
		stmt.setString(10, quest.getNotificationMessage());
		stmt.setString(11, quest.getId());
		stmt.executeUpdate();
		
		stmt = conn.prepareStatement("DELETE FROM "+appId+".quest_action WHERE quest_id=?");
		stmt.setString(1, quest.getId());
		stmt.executeUpdate();

		PreparedStatement batchstmt = null;
		//conn.setAutoCommit(false);
		
		for(Pair<String,Integer> a : quest.getActionIds()){

		      
			batchstmt = conn.prepareStatement("INSERT INTO "+appId+".quest_action (quest_id, action_id, times) VALUES ( ?, ?, ?)");
			System.out.println(quest.getId());
			batchstmt.setString(1, quest.getId());
			batchstmt.setString(2, a.getLeft());
			batchstmt.setInt(3, a.getRight());
//			batchstmt.addBatch();
			batchstmt.executeUpdate();
		}
//		batchstmt.executeBatch();
//		conn.commit();
//		conn.setAutoCommit(true);
	}

	/**
	 * Delete a specific quest
	 * 
	 * @param appId application id
	 * @param quest_id quest id
	 * @throws SQLException sql exception
	 */
	public void deleteQuest(String appId, String quest_id) throws SQLException {
			stmt = conn.prepareStatement("DELETE FROM "+appId+".quest WHERE quest_id = ?");
			stmt.setString(1, quest_id);
			stmt.executeUpdate();
			stmt = conn.prepareStatement("DELETE FROM "+appId+".quest_action WHERE quest_id = ?");
			stmt.setString(1, quest_id);
			stmt.executeUpdate();
	}

	/**
	 * Add a new quest
	 * 
	 * @param appId application id
	 * @param quest quest model
	 * @throws SQLException sql exception
	 */
	public void addNewQuest(String appId, QuestModel quest) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("INSERT INTO "+appId+".quest (quest_id, name, description, status, achievement_id, quest_flag, quest_id_completed, point_flag, point_value, use_notification , notif_message)  VALUES (?, ?, ?, ?::"+appId+".quest_status, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, quest.getId());
			stmt.setString(2, quest.getName());
			stmt.setString(3, quest.getDescription());
			stmt.setString(4, quest.getStatus().toString());
			stmt.setString(5, quest.getAchievementId());
			stmt.setBoolean(6, quest.getQuestFlag());
			stmt.setString(7, quest.getQuestIdCompleted());
			stmt.setBoolean(8, quest.getPointFlag());
			stmt.setInt(9, quest.getPointValue());
			stmt.setBoolean(10, quest.isUseNotification());
			stmt.setString(11, quest.getNotificationMessage());
			stmt.executeUpdate();
			
			PreparedStatement batchstmt = null;
			for(Pair<String,Integer> a : quest.getActionIds()){
				batchstmt = conn.prepareStatement("INSERT INTO "+appId+".quest_action (quest_id, action_id, times) VALUES ( ?, ?, ?)");
				batchstmt.setString(1, quest.getId());
				batchstmt.setString(2, a.getLeft());
				batchstmt.setInt(3, a.getRight());
				batchstmt.executeUpdate();
			}
			
	}
	
}
