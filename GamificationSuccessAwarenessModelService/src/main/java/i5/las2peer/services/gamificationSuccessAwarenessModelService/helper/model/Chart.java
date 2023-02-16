package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts.BarChart;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts.LineChart;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts.MethodResult;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts.PieChart;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts.RadarChart;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts.TimelineChart;

/**
 *
 * Visualizes a query as a chart.
 *
 * @author Peter de Lange
 *
 */
public class Chart implements Visualization {
	
	
	/**
	 *
	 * This enumeration stores the type of a chart.
	 *
	 * @author Peter de Lange
	 *
	 */
	public static enum ChartType {
		BarChart,
		LineChart,
		PieChart,
		TimelineChart,
		RadarChart;
	}
	
	private ChartType chartType;
	private String[] parameters;
	
	
	/**
	 *
	 * Constructor, calls the {@link Visualization} constructor with "Chart".
	 *
	 * @param type the desired {@link ChartType}.
	 * @param parameters a String of parameters: [div-Id, title, height, width]
	 * 
	 * @throws Exception if not exactly four parameters are given
	 *
	 */
	public Chart(ChartType type, String[] parameters) throws Exception {
		this.chartType = type;
		if(parameters.length != 4)
			throw new Exception("Exactly four parameters needed!");
		this.parameters = parameters;
	}
	
	
	public String visualize(Map<String, String> queries, Connection databaseConnection) throws Exception{
		
		//Please note that no parameter check is done here.
		//Responsibility for valid parameters lies with the calling entity!
		if(queries.size() == 1){
			List<String> list = new ArrayList<String>(queries.values());
			ResultSet resultSet;
			try {
				Statement stmn = databaseConnection.createStatement();
				stmn.execute(list.get(0));
				resultSet = stmn.getResultSet();
			} catch (SQLException e) {
				throw new Exception("(Chart Visualization) The query has lead to an error: " + e);
			}
			MethodResult methodResult = new MethodResult(resultSet);
			if(methodResult.getRowList().isEmpty())
				return "Correct, but (temporary) empty query result, no visualization possible.";
			switch(chartType){
			case BarChart:
				BarChart barChart = new BarChart(methodResult, parameters);
				return barChart.getResultHTML();
			case LineChart:
				LineChart lineChart = new LineChart(methodResult, parameters);
				return lineChart.getResultHTML();
			case PieChart:
				PieChart pieChart = new PieChart(methodResult, parameters);
				return pieChart.getResultHTML();
			case TimelineChart:
				TimelineChart timelineChart = new TimelineChart(methodResult, parameters);
				return timelineChart.getResultHTML();
			case RadarChart:
				RadarChart radarChart = new RadarChart(methodResult, parameters);
				return radarChart.getResultHTML();
			}
		}
		throw new Exception("More than one query defined for chart visualization!");
	}
	
	
}
