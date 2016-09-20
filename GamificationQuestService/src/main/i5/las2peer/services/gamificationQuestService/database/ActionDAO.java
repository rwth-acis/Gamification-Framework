package i5.las2peer.services.gamificationQuestService.database;


import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActionDAO {
	
	public static enum NotificationType {
		BADGE,
		ACHIEVEMENT,
		QUEST,
		LEVEL,
	}
	
	PreparedStatement stmt;
	
	
	public ActionDAO(){
	}
	
	/**
	 * Check whether the application id is already exist
	 * 
	 * @param app_id application id
	 * @param conn database connection
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
	 * Get all actions in the database
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @return list of actions
	 * @throws SQLException sql exception
	 */
	public List<ActionModel> getAllActions(Connection conn,String appId) throws SQLException{

		List<ActionModel> acts = new ArrayList<ActionModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".action");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ActionModel bmodel = new ActionModel(rs.getString("action_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			acts.add(bmodel);
		}

		return acts;
	}

	/**
	 * Check whether an action with action id is already exist
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @param action_id action id
	 * @return true if the action is already exist
	 * @throws SQLException sql exception
	 */
	public boolean isActionIdExist(Connection conn,String appId, String action_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT action_id FROM "+appId+".action WHERE action_id=?");
		stmt.setString(1, action_id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;
	}
	
	/**
	 * Get an action with specific id
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @param action_id action id
	 * @return ActionModel
	 * @throws SQLException sql exception
	 */
	public ActionModel getActionWithId(Connection conn,String appId, String action_id) throws SQLException {
		
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".action WHERE action_id = ?");
		stmt.setString(1, action_id);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()){
			return new ActionModel(action_id, rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
		}
		return null;
	}

	/**
	 * Get total number of action
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @return total number of action
	 * @throws SQLException sql exception
	 */
	public int getNumberOfActions(Connection conn,String appId) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".action");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	/**
	 * Get actions with search parameter
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of actions
	 * @throws SQLException sql exception
	 */
	public List<ActionModel> getActionsWithOffsetAndSearchPhrase(Connection conn,String appId, int offset, int window_size, String searchPhrase) throws SQLException {
		List<ActionModel> achs= new ArrayList<ActionModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".action WHERE action_id LIKE '"+pattern+"' ORDER BY action_id LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ActionModel a = new ActionModel(rs.getString("action_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"), rs.getBoolean("use_notification"), rs.getString("notif_message"));
			achs.add(a);
		}
		return achs;
	}
	
	/**
	 * Update a action information
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @param action model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateAction(Connection conn,String appId, ActionModel action) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("UPDATE "+appId+".action SET name = ?, description = ?, point_value = ?, use_notification = ?, notif_message = ? WHERE action_id = ?");
			stmt.setString(1, action.getName());
			stmt.setString(2, action.getDescription());
			stmt.setInt(3, action.getPointValue());
			stmt.setBoolean(4, action.isUseNotification());
			stmt.setString(5, action.getNotificationMessage());
			stmt.setString(6, action.getId());
			stmt.executeUpdate();
	}

	/**
	 * Delete a specific action
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @param action_id action id
	 * @throws SQLException sql exception
	 */
	public void deleteAction(Connection conn,String appId, String action_id) throws SQLException {
		stmt = conn.prepareStatement("DELETE FROM "+appId+".action WHERE action_id = ?");
		stmt.setString(1, action_id);
		stmt.executeUpdate();
	}

	/**
	 * Add a new action
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @param action action model
	 * @throws SQLException sql exception
	 */
	public void addNewAction(Connection conn,String appId, ActionModel action) throws SQLException {
			stmt = conn.prepareStatement("INSERT INTO "+appId+".action (action_id, name, description, point_value, use_notification, notif_message) VALUES (?, ?, ?, ?, ?, ?)");
			stmt.setString(1, action.getId());
			stmt.setString(2, action.getName());
			stmt.setString(3, action.getDescription());
			stmt.setInt(4, action.getPointValue());
			stmt.setBoolean(5, action.isUseNotification());
			stmt.setString(6, action.getNotificationMessage());
			stmt.executeUpdate();		
	}
	
	/**
	 * trigger an action and fetch the notifications
	 * 
	 * @param appId application id
	 * @param conn database connection
	 * @param memberId member id
	 * @param actionId action id
	 * @throws SQLException sql exception
	 * @return JSONArray of notifications
	 */
	public JSONArray triggerAction(Connection conn,String appId, String memberId, String actionId) throws SQLException {
		
		JSONArray resArray = new JSONArray();
		System.out.println("data : " + appId + " " + memberId);
		// Submit action into member_action
		stmt = conn.prepareStatement("INSERT INTO "+appId+".member_action (member_id, action_id) VALUES (?, ?)");
		stmt.setString(1, memberId);
		stmt.setString(2, actionId);
		stmt.executeUpdate();	
		
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
		// Clean up notification for the member
		stmt = conn.prepareStatement("DELETE FROM "+appId+".notification WHERE member_id = ?");
		stmt.setString(1, memberId);
		stmt.executeUpdate();	
		
		return resArray;
	}
	
}
