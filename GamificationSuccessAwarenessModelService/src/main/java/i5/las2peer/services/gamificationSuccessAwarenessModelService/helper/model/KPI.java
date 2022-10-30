package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.fathzer.soft.javaluator.DoubleEvaluator;

/**
 *
 * Returns a Key Performance Indicator as visualization result.
 *
 * @author Peter de Lange
 *
 */
public class KPI implements Visualization {

	private String expression = "";
	//Using the "javaluator" for evaluating expressions
	//http://javaluator.sourceforge.net/en/home/
	private DoubleEvaluator evaluator;


	/**
	 *
	 * Constructor.
	 *
	 * @param expression a (sorted) map of Strings, containing the expression to calculate the KPI
	 *
	 */
	public KPI(String expression) {
		this.expression = expression;
		this.evaluator = new DoubleEvaluator();
	}


	public String visualize(Map<String, String> queries, Connection databaseConnection) throws Exception{
		LinkedList<String> values = new LinkedList<String>();
		Matcher m = null;
		try {
			m = Pattern.compile("([a-zA-Z]+)([-\\+\\*\\/]{1})([a-zA-Z]+)").matcher(expression);
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			throw new Exception("Could not parse expression. The expression contains an invalid character.");
		}

		if(m.matches()) {
			for(int i = 1; i <= m.groupCount(); i++) {
				values.add(m.group(i));
			}
		}

		//assume the queries map contains the measureName
		String expression = "";
		for(String op: values) {
			if(queries.containsKey(op)) {
				String query = queries.get(op);
				//Query!
				ResultSet resultSet;
				ResultSetMetaData resultSetMetaData;
				try{
					Statement stmn = databaseConnection.createStatement();
					stmn.execute(query);
					resultSet = stmn.getResultSet();
					resultSetMetaData = resultSet.getMetaData();
				} catch (SQLException e) {
					e.printStackTrace();
					return("(KPI Visualization) The query has lead to an error: " + e);
				}

				if(resultSetMetaData.getColumnCount() != 1){
					throw new Exception("KPI queries have to return a single value! " + query);
				}
				if(!resultSet.next()){
					throw new Exception("KPI result is empty! " +  query);
				}
				double queryResult = resultSet.getDouble(1);
				if(resultSet.next()){
					throw new Exception("KPI queries have to return a single value! " + query);
				}
				expression += queryResult;
			}
			else{
				expression += op;
			}
		}

		String returnString = "";
		Double returnValue = evaluator.evaluate(expression);
		if(!Double.isNaN(returnValue)){
			DecimalFormat formatter =   new DecimalFormat  ( ".##" );
			returnString = formatter.format(returnValue).toString();
		}
		//Probably division by zero (can happen with some (correctly formulated) query results); assuming correct result is mostly 0 then
		else{
			returnString = "0";
		}
		return returnString;
	}


}
