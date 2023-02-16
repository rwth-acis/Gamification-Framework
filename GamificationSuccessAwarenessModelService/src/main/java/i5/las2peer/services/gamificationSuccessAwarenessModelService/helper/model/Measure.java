package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * A measure contains a name, a number of queries and a {@link Visualization}.
 * It can be used to visualize these queries on a database.
 *
 * @author Peter de Lange
 *
 */
public class Measure {

  private String name;
  private Map<String, String> queries = new HashMap<String, String>();
  private Visualization visualization;
  private String description;
  private String xml;


  /**
   *
   * Constructor
   *
   * @param name the name of the measure
   * @param queries a map of queries
   * @param visualization the desired {@link Visualization} for this measure
   * @param description an optional description for the measure
   * @param xml the xml of the measure
   */
  public Measure(
    String name,
    Map<String, String> queries,
    Visualization visualization,
    String description,
    String xml
  ) {
    this.name = name;
    this.queries = queries;
    this.visualization = visualization;
    if (description == null) {
      this.description = "";
    } else this.description = description;
    this.xml = xml;
  }

  /**
   * Visualizes the measure.
   *
   * @param databaseConnection the database the queries should be executed on
   * @return the result as a String
   *
   * @throws Exception If something went wrong with the visualization (Database errors, wrong query results..)
   */
  public String visualize(Connection databaseConnection) throws Exception {
    return this.visualization.visualize(queries, databaseConnection);
  }

  /**
   *
   * Gets the queries of this measure.
   *
   * @return a map of queries
   *
   */
  public Map<String, String> getQueries() {
    return this.queries;
  }

  /**
   * Gets the name of this Measure.
   *
   * @return the measure name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the name of this Measure.
   *
   * @return the measure name
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * 
   * @return the xml of the measure
   */
  public String getXml() {
	  return xml;
  }

}
