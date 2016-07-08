package i5.las2peer.services.gamificationManagerService.action;


import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActionDAO {
	
	
	PreparedStatement stmt;
	Connection conn;
	
	public ActionDAO( Connection conn){
		this.conn = conn;
	}
	
	/**
	 * Get all actions in the database
	 * 
	 * @param appId application id
	 * @return list of actions
	 * @throws SQLException sql exception
	 */
	public List<ActionModel> getAllActions(String appId) throws SQLException{
		// TODO Auto-generated method stub
		List<ActionModel> acts = new ArrayList<ActionModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".action");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ActionModel bmodel = new ActionModel(rs.getString("action_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"));
			acts.add(bmodel);
		}

		return acts;
	}

	/**
	 * Check whether an action with action id is already exist
	 * 
	 * @param appId application id
	 * @param action_id action id
	 * @return true if the action is already exist
	 * @throws SQLException sql exception
	 */
	public boolean isActionIdExist(String appId, String action_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT action_id FROM "+appId+".action WHERE action_id=?");
		stmt.setString(1, action_id);
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
	 * Get an action with specific id
	 * 
	 * @param appId application id
	 * @param action_id action id
	 * @return ActionModel
	 */
	public ActionModel getActionWithId(String appId, String action_id) {
		try {
			stmt = conn.prepareStatement("SELECT * FROM "+appId+".action WHERE action_id = ?");
			stmt.setString(1, action_id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				return new ActionModel(action_id, rs.getString("name"), rs.getString("description"), rs.getInt("point_value"));
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
	 * Get total number of action
	 * 
	 * @param appId application id
	 * @return total number of action
	 * @throws SQLException sql exception
	 */
	public int getNumberOfActions(String appId) throws SQLException {
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
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of actions
	 * @throws SQLException sql exception
	 */
	public List<ActionModel> getActionsWithOffsetAndSearchPhrase(String appId, int offset, int window_size, String searchPhrase) throws SQLException {
		List<ActionModel> achs= new ArrayList<ActionModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".action WHERE action_id LIKE '"+pattern+"' ORDER BY action_id LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ActionModel a = new ActionModel(rs.getString("action_id"), rs.getString("name"), rs.getString("description"), rs.getInt("point_value"));
			achs.add(a);
		}
		return achs;
	}
	
	/**
	 * Update a action information
	 * 
	 * @param appId application id
	 * @param action model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateAction(String appId, ActionModel action) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("UPDATE "+appId+".action SET name = ?, description = ?, point_value = ? WHERE action_id = ?");
			stmt.setString(1, action.getName());
			stmt.setString(2, action.getDescription());
			stmt.setInt(3, action.getPointValue());
			stmt.setString(4, action.getId());
			stmt.executeUpdate();
	}

	/**
	 * Delete a specific action
	 * 
	 * @param appId application id
	 * @param action_id action id
	 * @throws SQLException sql exception
	 */
	public void deleteAction(String appId, String action_id) throws SQLException {
		// TODO Auto-generated method stub
			stmt = conn.prepareStatement("DELETE FROM "+appId+".action WHERE action_id = ?");
			stmt.setString(1, action_id);
			stmt.executeUpdate();
	}

	/**
	 * Add a new action
	 * 
	 * @param appId application id
	 * @param action action model
	 * @throws SQLException sql exception
	 */
	public void addNewAction(String appId, ActionModel action) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("INSERT INTO "+appId+".action (action_id, name, description, point_value) VALUES (?, ?, ?, ?)");
			stmt.setString(1, action.getId());
			stmt.setString(2, action.getName());
			stmt.setString(3, action.getDescription());
			stmt.setInt(4, action.getPointValue());
			stmt.executeUpdate();
			
	}
	
}
