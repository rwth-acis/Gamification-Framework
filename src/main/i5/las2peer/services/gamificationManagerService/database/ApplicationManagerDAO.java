package i5.las2peer.services.gamificationManagerService.database;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import i5.las2peer.services.gamificationManagerService.badge.BadgeModel;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Class to maintain the model that are used in the application management
 * 
 */

public class ApplicationManagerDAO {
	
	
	PreparedStatement stmt;
	Connection conn;
	
	public ApplicationManagerDAO( Connection conn){
		this.conn = conn;
	}
	
	/**
	 * Adding an application information to database
	 * 
	 * @param app_id application model
	 * @return true if success
	 */
	public boolean createApplicationDB(String app_id){
		// Copy template schema
		Statement statement;
		try {
			// Turn transactions off.
			statement = this.conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT create_new_application('"+ app_id +"')");
			if(rs.next()){
				return true;				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return false;
	}

	public boolean deleteApplicationDB(String app_id) {
		try {
			stmt = conn.prepareStatement("DROP SCHEMA " +app_id+ " CASCADE");
			stmt.executeUpdate();
			return true;
		} catch (SQLException e1){
		}
		return false;
	}
	
	
	public boolean addNewApplicationInfo(ApplicationModel app){

		try {
			stmt = conn.prepareStatement("INSERT INTO manager.application_info(app_id, description, community_type) VALUES(?, ?, ?)");
			stmt.setString(1, app.getId());
			stmt.setString(2, app.getDescription());
			stmt.setString(3, app.getCommType());
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();

			System.out.println(e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Delete application information form application_info table
	 * as well as drop the application database
	 * 
	 * @param app_id application id
	 * @return true if removed
	 */
	public boolean removeApplicationInfo(String app_id){

		try {
			stmt = conn.prepareStatement("DELETE FROM manager.application_info WHERE app_id = ?");
			stmt.setString(1, app_id);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		return false;
	}
	
	public void addNewApplication(ApplicationModel app) throws SQLException{
		PreparedStatement managerSt = null, dbcreationSt = null;
		try {
			conn.setAutoCommit(false);
			managerSt = conn.prepareStatement("INSERT INTO manager.application_info(app_id, description, community_type) VALUES(?, ?, ?)");
			dbcreationSt = conn.prepareStatement("SELECT create_new_application('"+ app.getId() +"')");
			managerSt.setString(1, app.getId());
			managerSt.setString(2, app.getDescription());
			managerSt.setString(3, app.getCommType());
			managerSt.executeUpdate();
			ResultSet rs = dbcreationSt.executeQuery();
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			conn.rollback();
		} finally {
			conn.setAutoCommit(true);
		}
	}
	
	/**
	 * Update an application information
	 * 
	 * @param app application model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateApplication(ApplicationModel app) throws SQLException {

			stmt = conn.prepareStatement("UPDATE manager.application_info SET description = ?, community_type = ? WHERE app_id = ?");
			stmt.setString(1, app.getDescription());
			stmt.setString(2, app.getCommType());
			stmt.setString(3, app.getId());
			stmt.executeUpdate();


	}

	/**
	 * Check whether the application id is already exist
	 * 
	 * @param app_id application id
	 * @return true app_id is already exist
	 */
	public boolean isAppIdExist(String app_id) {
		
			try {
				stmt = conn.prepareStatement("SELECT app_id FROM manager.application_info WHERE app_id=?");
				stmt.setString(1, app_id);
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
					if(rs.getString("app_id").equals(app_id)){
						return true;
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
	}


	/**
	 * Get an application with specific id
	 * 
	 * @param appId application id
	 * @return Application
	 * @throws SQLException exception
	 */
	public ApplicationModel getApplicationWithId(String appId) throws SQLException {
		try {
			stmt = conn.prepareStatement("SELECT community_type,description FROM manager.application_info WHERE app_id = ?");
			stmt.setString(1, appId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				return new ApplicationModel(appId, rs.getString("community_type"), rs.getString("description"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get all of applications' information
	 * 
	 * @return list of all applications
	 * @throws SQLException sql exception
	 */
	public List<ApplicationModel> getAllApplications() throws SQLException{
		// TODO Auto-generated method stub
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();

			stmt = conn.prepareStatement("SELECT * FROM manager.application_info");
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ApplicationModel appmodel = new ApplicationModel(rs.getString("app_id"), rs.getString("description"), rs.getString("community_type"));
				apps.add(appmodel);
			}

		return apps;
	}
	
	/**
	 * Get total number of applications
	 * 
	 * @return total number of applications
	 * @throws SQLException sql exception
	 */
	public int getNumberOfApplications() throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("SELECT count(*) FROM manager.application_info");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	/**
	 * Get applications per batch
	 * 
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @return list of applications
	 * @throws SQLException sql exception
	 */
	public List<ApplicationModel> getApplicationsWithOffset(int offset, int window_size) throws SQLException {
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();
		stmt = conn.prepareStatement("SELECT * FROM manager.application_info ORDER BY app_id LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ApplicationModel model = new ApplicationModel(rs.getString("app_id"), rs.getString("description"), rs.getString("community_type"));
			apps.add(model);
		}
		return apps;
	}
	
	/**
	 * Get all of applications of a member with specified member id.
	 * Return the list of applications belongs to  the specific member and the list of applications that is not belongs to the member
	 * 
	 * @param member_id member id
	 * @return List of applications belongs to a member and List of other applications
	 * @throws SQLException sql exception
	 */
	public List<List<ApplicationModel>> getSeparateApplicationsWithMemberId(String member_id) throws SQLException{
		// TODO Auto-generated method stub
		List<ApplicationModel> allApps = this.getAllApplications();
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();
		List<String> appIds = new ArrayList<String>();
		stmt = conn.prepareStatement("SELECT * FROM manager.member_application WHERE member_id = ?");
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			appIds.add(rs.getString("app_id"));
		}
		
		for(String appId: appIds){
			stmt = conn.prepareStatement("SELECT * FROM manager.application_info WHERE app_id = ?");
			stmt.setString(1, appId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ApplicationModel appmodel = new ApplicationModel(appId, rs.getString("description"), rs.getString("community_type"));
				apps.add(appmodel);
			}
		}
		
		List<ApplicationModel> toBeRemovedApps = new ArrayList<ApplicationModel>(apps);
		
		// Filter all apps	
		for(int i = 0; i < toBeRemovedApps.size(); i++){
			if(allApps.size()!=0){
				for (Iterator<ApplicationModel> iterator = allApps.iterator(); iterator.hasNext(); ) {
					ApplicationModel app = iterator.next();
					if(app.getId().equals(toBeRemovedApps.get(i).getId())){
						allApps.remove(app);
						//idxDeleted.add(i);
						//toBeRemovedApps.remove(app);
						break;
					}
				}
			}
		}
		
		List<List<ApplicationModel>> combinedApps = new ArrayList<List<ApplicationModel>>();
		combinedApps.add(allApps);
		combinedApps.add(apps);
		
		return combinedApps;
	}

	
	
	//---------- Member manager
	/**
	 * Check whether the member is already registered
	 * 
	 * @param member_id member id
	 * @return true member is already registered
	 * @throws SQLException exception
	 */
	public boolean isMemberRegistered(String member_id) throws SQLException {
		
		try {
			stmt = conn.prepareStatement("SELECT member_id,first_name,last_name,email FROM manager.member_info WHERE member_id=?");
			stmt.setString(1, member_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Check whether a member is registered in an application
	 * 
	 * @param member_id member id
	 * @param app_id application id
	 * @return member registered in app
	 * @throws SQLException sql exception
	 */
	public boolean isMemberRegisteredInApp(String member_id, String app_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT * FROM manager.member_application WHERE member_id=? AND app_id=?");
		stmt.setString(1, member_id);
		stmt.setString(2, app_id);
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
	 * Register a new member
	 * 
	 * @param member member model
	 * @throws SQLException sql exception
	 */
	public void registerMember(MemberModel member) throws SQLException{

			stmt = conn.prepareStatement("INSERT INTO manager.member_info (member_id, first_name, last_name, email) VALUES (?, ?, ?, ?)");
			stmt.setString(1, member.getId());
			stmt.setString(2, member.getFirstName());
			stmt.setString(3, member.getLastName());
			stmt.setString(4, member.getEmail());
			stmt.executeUpdate();
	}

	/**
	 * Get all applications belong to a member
	 * 
	 * @param member_id member id
	 * @return list of applications
	 * @throws SQLException sql exception
	 */
	public List<ApplicationModel> getAllApplicationsOfMember(String member_id) throws SQLException{
		// TODO Auto-generated method stub
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();
		List<String> appIds = new ArrayList<String>();
		stmt = conn.prepareStatement("SELECT * FROM manager.member_application WHERE member_id = ?");
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			appIds.add(rs.getString("app_id"));
		}
		
		for(String appId: appIds){
			stmt = conn.prepareStatement("SELECT * FROM manager.application_info WHERE app_id = ?");
			stmt.setString(1, appId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ApplicationModel appmodel = new ApplicationModel(appId, rs.getString("description"), rs.getString("community_type"));
				apps.add(appmodel);
			}
		}
		return apps;
	}
	
	/**
	 * Get total number of users applications
	 * 
	 * @param member_id member id
	 * @return number of user applications
	 * @throws SQLException sql exception
	 */
	public int getNumberOfUsersApplications(String member_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT count(*) FROM manager.member_application WHERE member_id = ?");
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			return rs.getInt(1);
		}
		else{
			return 0;
		}
	}
	
	/**
	 * Get applications per batch
	 * 
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param member_id member id
	 * @return list of applications
	 * @throws SQLException sql exception
	 */
	public List<ApplicationModel> getUsersApplicationsWithOffset(int offset, int window_size, String member_id) throws SQLException {
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();
		stmt = conn.prepareStatement("WITH TEMP AS (SELECT app_id FROM manager.member_application WHERE member_id = ?) SELECT * FROM TEMP, manager.application_info WHERE TEMP.app_id = manager.application_info.app_id ORDER BY app_id LIMIT "+window_size+" OFFSET "+offset);
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			ApplicationModel model = new ApplicationModel(rs.getString("app_id"), rs.getString("description"), rs.getString("community_type"));
			apps.add(model);
		}
		return apps;
	}

	/**
	 * Remove and unregister member form an application
	 * 
	 * @param member_id member id
	 * @param app_id application id
	 * @throws SQLException sql exception
	 */
	public void removeMemberFromApp(String member_id, String app_id) throws SQLException {
		
		PreparedStatement statement1 = null, statement2 = null;
			
			statement1 = conn.prepareStatement("DELETE FROM manager.member_application WHERE member_id=? AND app_id =?");
			statement2 = conn.prepareStatement("DELETE FROM "+app_id+".member WHERE member_id=?");
			statement1.setString(1, member_id);
			statement1.setString(2, app_id);
			statement1.executeUpdate();
			
			statement2.setString(1, member_id);
			statement2.executeUpdate();

	}
	
	/**
	 * Add member to application, inserting member and app id to member-application table
	 * 
	 * @param app_id application id
	 * @param member_id member id
	 * @throws SQLException sql exception
	 */
	public void addMemberToApp(String app_id, String member_id) throws SQLException{
		PreparedStatement managerStatement = null;
		PreparedStatement gameStatement = null;
			
			managerStatement = conn.prepareStatement("INSERT INTO manager.member_application (member_id, app_id) VALUES ( ?, ?);");
			gameStatement = conn.prepareStatement("INSERT INTO "+app_id+".member (member_id, first_name, last_name, email) (SELECT member_id, first_name, last_name, email FROM manager.member_info WHERE member_id=?)");
			managerStatement.setString(1, member_id);
			managerStatement.setString(2, app_id);
			managerStatement.executeUpdate();
			gameStatement.setString(1, member_id);
			gameStatement.executeUpdate();

	}
	
	
	
}
