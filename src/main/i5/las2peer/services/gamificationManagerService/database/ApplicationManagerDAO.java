package i5.las2peer.services.gamificationManagerService.database;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.sun.media.jfxmedia.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	 * Create a new database with specific app_id
	 * 
	 * @param app_id application id
	 * @return true database is created
	 */
	public boolean createNewApplicationDB(String app_id){
		String dbname = "db"+app_id;
		Statement statement;
		try {
			statement = this.conn.createStatement();
			statement.executeUpdate("CREATE DATABASE " +  dbname);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Add member to application, inserting member and app id to member-application table
	 * 
	 * @param app_id application id
	 * @param member_id member id
	 */
	public void addMemberToApp(String app_id, String member_id) throws SQLException{
		stmt = conn.prepareStatement("INSERT INTO member_application (member_id, app_id) VALUES ( ?, ?)");
		stmt.setString(1, member_id);
		stmt.setString(2,  app_id);
		stmt.executeUpdate();
	}

	/**
	 * Check whether the application id is already exist
	 * 
	 * @param app_id application id
	 * @return true app_id is already exist
	 */
	public boolean isAppIdExist(String app_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT app_id,description,community_type FROM application_info WHERE app_id=?");
		stmt.setString(1, app_id);
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
	 * Adding an application information to database
	 * 
	 * @param app application model
	 */
	public void addAppToDB(ApplicationModel app) throws SQLException{
		stmt = conn.prepareStatement("INSERT INTO application_info (app_id, description, community_type) VALUES (?, ?, ?)");
		stmt.setString(1, app.getId());
		stmt.setString(2, app.getAppDescription());
		stmt.setString(3, app.getCommType());
		stmt.executeUpdate();
	}

	/**
	 * Get all of applications' information
	 * 
	 * @return list of all applications
	 */
	public List<ApplicationModel> getAllApplications() throws SQLException{
		// TODO Auto-generated method stub
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();

			stmt = conn.prepareStatement("SELECT * FROM application_info");
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ApplicationModel appmodel = new ApplicationModel(rs.getString("app_id"), rs.getString("description"), rs.getString("community_type"));
				apps.add(appmodel);
			}

		return apps;
	}
	
	/**
	 * Get all of applications of a member with specified member id.
	 * Return the list of applications belongs to  the specific member and the list of applications that is not belongs to the member
	 * 
	 * @param member_id member id
	 * @return List of applications belongs to a member and List of other applications
	 */
	public List<List<ApplicationModel>> getSeparateApplicationsWithMemberId(String member_id) throws SQLException{
		// TODO Auto-generated method stub
		List<ApplicationModel> allApps = this.getAllApplications();
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();
		List<String> appIds = new ArrayList<String>();
		stmt = conn.prepareStatement("SELECT * FROM member_application WHERE member_id = ?");
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			appIds.add(rs.getString("app_id"));
		}
		
		for(String appId: appIds){
			stmt = conn.prepareStatement("SELECT * FROM application_info WHERE app_id = ?");
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

	/**
	 * Delete application information form application_info table
	 * as well as drop the application database
	 * 
	 * @param appid application id
	 */
	public boolean deleteApp(String appid){
		// TODO Auto-generated method stub
			String dbname = "db"+appid;
			Statement statement;
			try {
				statement = this.conn.createStatement();
				statement.executeUpdate("DROP DATABASE " +  dbname);
				stmt = conn.prepareStatement("DELETE FROM application_info WHERE app_id = ?");
				stmt.setString(1, appid);
				stmt.executeUpdate();
				System.out.println("Badge id " + appid + " is deleted from database");
				return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
	}
	
	//---------- Member manager
	/**
	 * Check whether the member is already registered
	 * 
	 * @param member_id member id
	 * @return true member is already registered
	 */
	public boolean isMemberRegistered(String member_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT member_id,first_name,last_name,email FROM member_info WHERE member_id=?");
		stmt.setString(1, member_id);
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
	 * Check whether a member is registered in an application
	 * 
	 * @param member_id member id
	 * @param app_id application id
	 */
	public boolean isRegisteredInApp(String member_id, String app_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT * FROM member_application WHERE member_id=? AND app_id=?");
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
	 */
	public void registerMember(MemberModel member) throws SQLException{
		stmt = conn.prepareStatement("INSERT INTO member_info (member_id, first_name, last_name, email) VALUES (?, ?, ?, ?)");
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
	 */
	public List<ApplicationModel> getAllApplicationsOfMember(String member_id) throws SQLException{
		// TODO Auto-generated method stub
		List<ApplicationModel> apps = new ArrayList<ApplicationModel>();
		List<String> appIds = new ArrayList<String>();
		stmt = conn.prepareStatement("SELECT * FROM member_application WHERE member_id = ?");
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			appIds.add(rs.getString("app_id"));
		}
		
		for(String appId: appIds){
			stmt = conn.prepareStatement("SELECT * FROM application_info WHERE app_id = ?");
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
	 * Remove and unregister member form an application
	 * 
	 * @param member_id member id
	 * @param app_id application id
	 */
	public void removeMemberFromApp(String member_id, String app_id) throws SQLException{
		stmt = conn.prepareStatement("DELETE FROM member_application WHERE member_id=? AND app_id =?");
		stmt.setString(1, member_id);
		stmt.setString(2, app_id);
		stmt.executeUpdate();
	}
	
}
