package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts;

import java.sql.Date;
import java.sql.Time;
import java.sql.Types;
import java.util.Iterator;

/**
*
* Converts a {@link MethodResult} into a Google Timeline-Chart.
*
*/
public class TimelineChart {
	
	private String resultHTML = null;
	
	
	/**
	*
	* Constructor of a RadarChart.
	*
	* @param methodResult
	* @param visualizationParameters a String of parameters: [div-Id, title, height, width]
	*
	*/
	public TimelineChart(MethodResult methodResult, String[] visualizationParameters){
		
		String[] columnNames = methodResult.getColumnNames();
		Integer[] columnTypes = methodResult.getColumnDatatypes();
		Iterator<Object[]> iterator = methodResult.getRowList().iterator();
		int columnCount = columnTypes.length;
		
		String divId = visualizationParameters[0];
		String title = visualizationParameters[1];
		String height = visualizationParameters[2];
		String width = visualizationParameters[3];
		
		resultHTML = "<div id='" + divId + "' style='height: " + height + "; width: " + width + ";'></div>\n";
		resultHTML += "<script>\n";
		resultHTML += "var " + divId + "_script = document.createElement('script');\n";
		resultHTML += divId + "_script.src = 'https://www.google.com/jsapi?callback=" + divId + "_loadChart';\n";
		resultHTML += divId + "_script.type = 'text/javascript';\n";
		resultHTML += "document.getElementsByTagName('head')[0].appendChild(" + divId + "_script);\n";
		resultHTML += "function " + divId + "_loadChart(){\n";
		resultHTML += "google.load('visualization', '1', {packages: ['corechart'], callback: drawChart_" + divId + " });\n";
		resultHTML += "}\n";
		resultHTML += "function drawChart_" + divId + "() {\n";
		
		resultHTML += "var data = new google.visualization.DataTable();\n";
		
		//Column names and types
		String columnTypeString = "string";
		for(int i = 0; i < columnCount; i++){
			switch(columnTypes[i]) {
			case Types.BOOLEAN:
				columnTypeString = "boolean";
				break;
			case Types.DATE:
				columnTypeString = "date";
				break;
			case Types.TIME:
			case Types.TIMESTAMP:
				columnTypeString = "datetime";
				break;
			case Types.BIGINT:
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.INTEGER:
			case Types.NUMERIC:
			case Types.REAL:
			case Types.SMALLINT:
				columnTypeString = "number";
				break;
			default:
				// do nothing, just treat it as string
				break;
			}
			resultHTML += "data.addColumn('" + columnTypeString + "', '" + columnNames[i] + "');\n";
		}
		resultHTML += "data.addRows([\n";
		
		
		// add the individual rows
		while(iterator.hasNext()) {
			resultHTML += "[";
			
			Object[] currentRow = iterator.next();
			for(int i = 0; i < columnCount; i++) {
				if(i>0) resultHTML += ", ";
				switch(columnTypes[i]) {
					case Types.DATE:	
						resultHTML += " new Date(" + ((Date) currentRow[i]).getTime() + ")";
						break;
					case Types.TIME:
					case Types.TIMESTAMP:
						resultHTML += " new Date(" + ((Time) currentRow[i]).getTime() + ")";
						break;
					case Types.BOOLEAN:
					case Types.BIGINT:
					case Types.DECIMAL:
					case Types.NUMERIC:
					case Types.DOUBLE:
					case Types.REAL:
					case Types.FLOAT:
					case Types.INTEGER:
					case Types.SMALLINT:
						resultHTML += currentRow[i];
						break;
					default:
						String value = (String) currentRow[i];
						resultHTML += "\"" + value + "\"";
						break;
				}
			}
			if(iterator.hasNext()){
				resultHTML += "],\n";
			}
			else{
				resultHTML += "]\n]);\n"; //Last entry
			}
		}
		
		resultHTML += "var options = {\n";
		resultHTML += "displayAnnotations: true,\n";
		resultHTML += "'title':'" + title  + "'\n";
		resultHTML += "};\n";
		
		resultHTML += "var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('" + divId + "'));\n";
		resultHTML += "chart.draw(data, options);\n";
				
		resultHTML += "}\n</script>";

	}
	
	
	/**
	*
	* Gets the HTML representation of this chart.
	*
	* @return the HTML representation as a string
	*
	*/
	public String getResultHTML(){
		return this.resultHTML;
	}
	
	
}
