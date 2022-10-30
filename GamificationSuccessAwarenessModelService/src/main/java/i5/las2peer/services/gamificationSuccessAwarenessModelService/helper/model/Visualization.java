package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model;

import java.sql.Connection;
import java.util.Map;

/**
 *
 * Interface for visualizations of a measure.
 *
 * @author Peter de Lange
 *
 */
public interface Visualization {

	
	/**
	 *
	 * Executes the given database queries and visualizes the results according to the visualization.
	 *
	 * @param queries
	 * @param databaseConnection
	 *
	 * @return the visualization as a String
	 *
	 * @throws Exception
	 *
	 */
	public String visualize(Map<String, String> queries, Connection databaseConnection) throws Exception;
	
	
}
