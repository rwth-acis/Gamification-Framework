package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts;

import java.util.Iterator;

/**
*
* Converts a {@link MethodResult} into a Google Pie-Chart.
* <br>
* <i>Two columns expected!</i>
*
*/
public class PieChart {
	
	private String resultHTML = null;
	
	/**
	*
	* Constructor of a PieChart.
	*
	* @param methodResult
	* @param visualizationParameters a String of parameters: [div-Id, title, height, width]
	*
	*/
	public PieChart(MethodResult methodResult, String[] visualizationParameters){
		
		String[] columnNames = methodResult.getColumnNames();
		Iterator<Object[]> iterator = methodResult.getRowList().iterator();
		
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
		
		resultHTML += "data.addColumn('string', '" + columnNames[0] + "');\n";
		resultHTML += "data.addColumn('number', '" + columnNames[1] + "');\n";
		resultHTML += "data.addRows([\n";
		
		while(iterator.hasNext()){
			Object[] currentRow = iterator.next();
			String firstCell = currentRow[0].toString();
			String secondCell = currentRow[1].toString();
			if(firstCell == null || firstCell.equals("null"))
				firstCell = "";
			if(iterator.hasNext()){
				resultHTML += "['" + firstCell + "'," + secondCell + "],\n";
			}
			else{ //Last Entry, close Array
				resultHTML += "['" + firstCell + "'," + secondCell + "]\n]";
			}
		}
		resultHTML += ");\n";
		
		resultHTML += "var options = {\n";
		resultHTML += "'title':'" + title  + "',\n";
		resultHTML += "};\n";
		
		resultHTML += "var chart = new google.visualization.PieChart(document.getElementById('" + divId + "'));\n";
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
