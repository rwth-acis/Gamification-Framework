package i5.las2peer.services.gamificationManagerService.level;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.sun.media.jfxmedia.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LevelDAO {
	
	
	PreparedStatement stmt;
	Connection conn;
	
	public LevelDAO( Connection conn){
		this.conn = conn;
	}
	
	/**
	 * Get all levels in the database
	 * 
	 * @param appId application id
	 * @return list of levels
	 * @throws SQLException sql exception
	 */
	public List<LevelModel> getAllLevels(String appId) throws SQLException{
		// TODO Auto-generated method stub
		List<LevelModel> levs = new ArrayList<LevelModel>();
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".level");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			LevelModel model = new LevelModel(rs.getInt("level_num"), rs.getString("name"), rs.getInt("point_value"));
			levs.add(model);
		}

		return levs;
	}

	/**
	 * Check whether a level with level num is already exist
	 * 
	 * @param appId application id
	 * @param level_num level id
	 * @return true if the level is already exist
	 * @throws SQLException sql exception
	 */
	public boolean isLevelNumExist(String appId, int level_num) throws SQLException {
		stmt = conn.prepareStatement("SELECT level_num FROM "+appId+".level WHERE level_num=?");
		stmt.setInt(1, level_num);
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
	 * Get a level with specific id
	 * 
	 * @param appId application id
	 * @param level_num level id
	 * @return LevelModel
	 */
	public LevelModel getLevelWithNumber(String appId, int level_num) {
		try {
			stmt = conn.prepareStatement("SELECT * FROM "+appId+".level WHERE level_num = ?");
			stmt.setInt(1, level_num);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				return new LevelModel(rs.getInt("level_num"), rs.getString("name"), rs.getInt("point_value"));
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
	 * Get total number of level
	 * 
	 * @param appId application id
	 * @return total number of level
	 * @throws SQLException sql exception
	 */
	public int getNumberOfLevels(String appId) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("SELECT count(*) FROM "+appId+".level");
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			else{
				return 0;
			}
	}
	
	/**
	 * Get levels with search parameter
	 * 
	 * @param appId application id
	 * @param offset offset
	 * @param window_size number of fetched data
	 * @param searchPhrase search phrase
	 * @return list of levels
	 * @throws SQLException sql exception
	 */
	public List<LevelModel> getLevelsWithOffsetAndSearchPhrase(String appId, int offset, int window_size, String searchPhrase) throws SQLException {
		List<LevelModel> achs= new ArrayList<LevelModel>();
		String pattern = "%"+searchPhrase+"%";
		stmt = conn.prepareStatement("SELECT * FROM "+appId+".level WHERE CAST(level_num AS text) LIKE '"+pattern+"' ORDER BY level_num LIMIT "+window_size+" OFFSET "+offset);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			LevelModel a = new LevelModel(rs.getInt("level_num"), rs.getString("name"), rs.getInt("point_value"));
			achs.add(a);
		}
		return achs;
	}
	
	/**
	 * Update a level information
	 * 
	 * @param appId application id
	 * @param level model to be updated
	 * @throws SQLException sql exception
	 */
	public void updateLevel(String appId, LevelModel level) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("UPDATE "+appId+".level SET name = ?, point_value = ? WHERE level_num = ?");
			stmt.setString(1, level.getName());
			stmt.setInt(2, level.getPointValue());
			stmt.setInt(3, level.getNumber());
			stmt.executeUpdate();
	}

	/**
	 * Delete a specific level
	 * 
	 * @param appId application id
	 * @param level_num level num
	 * @throws SQLException sql exception
	 */
	public void deleteLevel(String appId, int level_num) throws SQLException {
		// TODO Auto-generated method stub
			stmt = conn.prepareStatement("DELETE FROM "+appId+".level WHERE level_num = ?");
			stmt.setInt(1, level_num);
			stmt.executeUpdate();
	}

	/**
	 * Add a new level
	 * 
	 * @param appId application id
	 * @param level level model
	 * @throws SQLException sql exception
	 */
	public void addNewLevel(String appId, LevelModel level) throws SQLException {
		// TODO Auto-generated method stub

			stmt = conn.prepareStatement("INSERT INTO "+appId+".level (level_num, name, point_value) VALUES (?, ?, ?)");
			stmt.setInt(1, level.getNumber());
			stmt.setString(2, level.getName());
			stmt.setInt(3, level.getPointValue());
			stmt.executeUpdate();
			
	}
	
}
