package i5.las2peer.services.gamificationGameService.database;


import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.gamificationGameService.database.MemberModel;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to maintain the model that are used in the game management
 * 
 */

public class GameDAO {
	
	
	PreparedStatement stmt;
	
	public GameDAO(){
	}
	
	/**
	 * Adding an game information to database
	 * 
	 * @param conn database connection
	 * @param game_id game model
	 * @return true if success
	 */
	public boolean createGameDB(Connection conn,String game_id){
		// Copy template schema
		Statement statement;
		try {
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT create_new_game('"+ game_id +"')");
			if(rs.next()){
				return true;				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return false;
	}

	public boolean deleteGameDB(Connection conn,String game_id) {
		Statement statement;
		try {
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT delete_game('"+ game_id +"')");
			if(rs.next()){
				return true;				
			}
		} catch (SQLException e1){
		}
		return false;
	}
	
	
	public boolean addNewGameInfo(Connection conn,GameModel game){

		try {
			stmt = conn.prepareStatement("INSERT INTO manager.game_info(game_id, description, community_type) VALUES(?, ?, ?)");
			stmt.setString(1, game.getId());
			stmt.setString(2, game.getDescription());
			stmt.setString(3, game.getCommType());
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();

			System.out.println(e.getSQLState());
		}
		return false;
	}
	
//	/**
//	 * Delete game information form game_info table
//	 * as well as drop the game database
//	 * 
//	 * @param app_id game id
//	 * @return true if removed
//	 */
//	public boolean removeGameInfo(String app_id){
//
//		try {
//			stmt = conn.prepareStatement("DELETE FROM manager.game_info WHERE app_id = ?");
//			stmt.setString(1, app_id);
//			stmt.executeUpdate();
//			return true;
//		} catch (SQLException e) {
//			System.out.println("SQLException " + e.getMessage());
//		}
//		return false;
//	}
	
	public void addNewGame(Connection conn,GameModel game) throws SQLException{
		PreparedStatement managerSt = null, dbcreationSt = null;
		try {
			conn.setAutoCommit(false);
			managerSt = conn.prepareStatement("INSERT INTO manager.game_info(game_id, description, community_type) VALUES(?, ?, ?)");
			dbcreationSt = conn.prepareStatement("SELECT create_new_game('"+ game.getId() +"')");
			managerSt.setString(1, game.getId());
			managerSt.setString(2, game.getDescription());
			managerSt.setString(3, game.getCommType());
			managerSt.executeUpdate();
			ResultSet rs = dbcreationSt.executeQuery();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			conn.rollback();
		} finally {
			conn.setAutoCommit(true);
		}
	}
	
	/**
	 * Update an game information
	 * 
	 * @param conn database connection
	 * @param game game model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateGame(Connection conn,GameModel game) throws SQLException {

			stmt = conn.prepareStatement("UPDATE manager.game_info SET description = ?, community_type = ? WHERE game_id = ?");
			stmt.setString(1, game.getDescription());
			stmt.setString(2, game.getCommType());
			stmt.setString(3, game.getId());
			stmt.executeUpdate();


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
	 * Get an game with specific id
	 * 
	 * @param conn database connection
	 * @param gameId game id
	 * @return Game
	 * @throws SQLException exception
	 */
	public GameModel getGameWithId(Connection conn,String gameId) throws SQLException {
		try {
			stmt = conn.prepareStatement("SELECT community_type,description FROM manager.game_info WHERE game_id = ?");
			stmt.setString(1, gameId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				return new GameModel(gameId, rs.getString("community_type"), rs.getString("description"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get all of games' information with member's games information. Return the list of all existing games in gamification framework
	 *  with additional flag information "memberHas" that indicates the member has that game if it is true
	 * 
	 * @param conn database connection
	 * @param memberId member Id
	 * @return list of all games
	 * @throws SQLException sql exception
	 */
	public JSONArray getAllGamesWithMemberInformation(Connection conn, String memberId) throws SQLException{
		JSONArray arrayResult = new JSONArray();
		
		List<String> gameIds = new ArrayList<String>();
		stmt = conn.prepareStatement("SELECT game_id FROM manager.member_game WHERE member_id = ?");
		stmt.setString(1, memberId);
		ResultSet rsgameId = stmt.executeQuery();
		while (rsgameId.next()) {
			gameIds.add(rsgameId.getString("game_id"));
		}
		
			stmt = conn.prepareStatement("SELECT * FROM manager.game_info");
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				obj.put("game_id", rs.getString("game_id"));
				obj.put("description", rs.getString("description"));
				obj.put("community_type", rs.getString("community_type"));
				obj.put("memberHas", false);
				
				for(int i=0; i < gameIds.size();i++){
					if(gameIds.get(i).equals(rs.getString("game_id"))){
						obj.replace("memberHas", true);
						gameIds.remove(i);
					}
				}
				arrayResult.add(obj);
			}

			return arrayResult;
	}
	
	/**
	 * Get all of games' information
	 * 
	 * @param conn database connection
	 * @return list of all games
	 * @throws SQLException sql exception
	 */
	public List<GameModel> getAllGames(Connection conn) throws SQLException{
		// TODO Auto-generated method stub
		List<GameModel> games = new ArrayList<GameModel>();
			
			stmt = conn.prepareStatement("SELECT * FROM manager.game_info");
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				GameModel gamemodel = new GameModel(rs.getString("game_id"), rs.getString("description"), rs.getString("community_type"));
				games.add(gamemodel);
			}

		return games;
	}
	
	/**
	 * Get total number of games
	 * 
	 * @param conn database connection
	 * @return total number of games
	 * @throws SQLException sql exception
	 */
	public int getNumberOfGames(Connection conn) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("SELECT count(*) FROM manager.game_info");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	

	/**
	 * Get games per batch
	 * 
	 * @param offset offset
	 * @param conn database connection
	 * @param window_size number of fetched data
	 * @return list of games
	 * @throws SQLException sql exception
	 */
	public List<GameModel> getGamesWithOffset(Connection conn,int offset, int window_size) throws SQLException {
		List<GameModel> games = new ArrayList<GameModel>();
		stmt = conn.prepareStatement("SELECT * FROM manager.game_info ORDER BY game_id LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			GameModel model = new GameModel(rs.getString("game_id"), rs.getString("description"), rs.getString("community_type"));
			games.add(model);
		}
		return games;
	}
	
	/**
	 * Get all of games of a member with specified member id.
	 * Return the list of games belongs to  the specific member and the list of games that is not belongs to the member
	 * 
	 * @param member_id member id
	 * @param conn database connection
	 * @return List of games belongs to a member and List of other games
	 * @throws SQLException sql exception
	 */
	public List<List<GameModel>> getSeparateGamesWithMemberId(Connection conn,String member_id) throws SQLException{
		// TODO Auto-generated method stub
		List<GameModel> otherGames = new ArrayList<GameModel>();
		List<GameModel> games = new ArrayList<GameModel>();
		List<String> gameIds = new ArrayList<String>();
		stmt = conn.prepareStatement("SELECT game_id FROM manager.member_game WHERE member_id = ?");
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			gameIds.add(rs.getString("game_id"));
		}
		for(String gameId: gameIds){
			stmt = conn.prepareStatement("SELECT * FROM manager.game_info WHERE game_id = ?");
			stmt.setString(1, gameId);
			ResultSet rs2 = stmt.executeQuery();
			while (rs2.next()) {
				GameModel gamemodel = new GameModel(gameId, rs2.getString("description"), rs2.getString("community_type"));
				games.add(gamemodel);
			}
		}
		gameIds.clear();
		stmt = conn.prepareStatement("WITH tab AS (SELECT game_id FROM manager.member_game WHERE member_id=?) SELECT game_id FROM manager.game_info EXCEPT SELECT game_id from tab;");
		stmt.setString(1, member_id);
		ResultSet rs3 = stmt.executeQuery();
		while (rs3.next()) {
			gameIds.add(rs3.getString("game_id"));
		}
		
		for(String gameId: gameIds){
			stmt = conn.prepareStatement("SELECT * FROM manager.game_info WHERE game_id = ?");
			stmt.setString(1, gameId);
			ResultSet rs4 = stmt.executeQuery();
			while (rs4.next()) {
				GameModel gamemodel = new GameModel(gameId, rs4.getString("description"), rs4.getString("community_type"));
				otherGames.add(gamemodel);
			}
		}

		List<List<GameModel>> combinedGames = new ArrayList<List<GameModel>>();
		combinedGames.add(otherGames);
		combinedGames.add(games);
		
		return combinedGames;
	}
	
	
	//---------- Member manager
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Register a new member
	 * 
	 * @param member member model
	 * @param conn database connection
	 * @throws SQLException sql exception
	 */
	public void registerMember(Connection conn,MemberModel member) throws SQLException{

			stmt = conn.prepareStatement("INSERT INTO manager.member_info (member_id, first_name, last_name, email) VALUES (?, ?, ?, ?)");
			stmt.setString(1, member.getId());
			stmt.setString(2, member.getFirstName());
			stmt.setString(3, member.getLastName());
			stmt.setString(4, member.getEmail());
			stmt.executeUpdate();
	}

	/**
	 * Get all games belong to a member
	 * 
	 * @param member_id member id
	 * @param conn database connection
	 * @return list of games
	 * @throws SQLException sql exception
	 */
	public List<GameModel> getAllGamesOfMember(Connection conn,String member_id) throws SQLException{
		// TODO Auto-generated method stub
		List<GameModel> games = new ArrayList<GameModel>();
		List<String> gameIds = new ArrayList<String>();
		stmt = conn.prepareStatement("SELECT * FROM manager.member_game WHERE member_id = ?");
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			gameIds.add(rs.getString("game_id"));
		}
		
		for(String gameId: gameIds){
			stmt = conn.prepareStatement("SELECT * FROM manager.game_info WHERE game_id = ?");
			stmt.setString(1, gameId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				GameModel gamemodel = new GameModel(gameId, rs.getString("description"), rs.getString("community_type"));
				games.add(gamemodel);
			}
		}
		return games;
	}
	
	/**
	 * Get total number of users games
	 * 
	 * @param member_id member id
	 * @param conn database connection
	 * @return number of user games
	 * @throws SQLException sql exception
	 */
	public int getNumberOfUsersGames(Connection conn,String member_id) throws SQLException {
		stmt = conn.prepareStatement("SELECT count(*) FROM manager.member_game WHERE member_id = ?");
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
	 * Get games per batch
	 * 
	 * @param offset offset
	 * @param conn database connection
	 * @param window_size number of fetched data
	 * @param member_id member id
	 * @return list of games
	 * @throws SQLException sql exception
	 */
	public List<GameModel> getUsersGamesWithOffset(Connection conn,int offset, int window_size, String member_id) throws SQLException {
		List<GameModel> games = new ArrayList<GameModel>();
		stmt = conn.prepareStatement("WITH TEMP AS (SELECT game_id FROM manager.member_game WHERE member_id = ?) SELECT * FROM TEMP, manager.game_info WHERE TEMP.game_id = manager.game_info.game_id ORDER BY game_id LIMIT "+window_size+" OFFSET "+offset);
		stmt.setString(1, member_id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			GameModel model = new GameModel(rs.getString("game_id"), rs.getString("description"), rs.getString("community_type"));
			games.add(model);
		}
		return games;
	}

	/**
	 * Remove and unregister member form an game
	 * 
	 * @param member_id member id
	 * @param conn database connection
	 * @param game_id game id
	 * @return member removerd true
	 * @throws SQLException sql exception
	 */
	public boolean removeMemberFromGame(Connection conn,String member_id, String game_id) throws SQLException {
		
		Statement statement;

		statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("SELECT remove_member_from_game('"+ member_id +"','"+ game_id +"')");
		if(rs.next()){
			return true;				
		}
		return false;

	}
	
	/**
	 * Add member to game, inserting member and game id to member-game table
	 * 
	 * @param game_id game id
	 * @param conn database connection
	 * @param member_id member id
	 * @return member added true
	 * @throws SQLException sql exception
	 */
	public boolean addMemberToGame(Connection conn,String game_id, String member_id) throws SQLException{
		Statement statement;

		statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("SELECT init_member_to_game('"+ member_id +"','"+ game_id +"')");
		if(rs.next()){
			return true;				
		}
		return false;
	}
	
	
	
}
