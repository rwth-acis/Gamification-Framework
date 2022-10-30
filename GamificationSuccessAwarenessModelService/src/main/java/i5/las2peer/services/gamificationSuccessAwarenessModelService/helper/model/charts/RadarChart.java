package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts;


import java.util.Iterator;

/**
*
* Converts a {@link MethodResult} into a Google Radar-Chart.
*
*/
public class RadarChart {
	
	private String resultHTML = null;
	
	
	/**
	*
	* Constructor of a RadarChart.
	*
	* @param methodResult
	* @param visualizationParameters a String of parameters: [div-Id, title, height, width]
	*
	*/
	public RadarChart (MethodResult methodResult, String[] visualizationParameters){
		
		String[] columnNames = methodResult.getColumnNames();
		Integer[] columnTypes = methodResult.getColumnDatatypes();
		Iterator<Object[]> iterator = methodResult.getRowList().iterator();
		
		String divId = visualizationParameters[0];
		String title = visualizationParameters[1];
		String height = visualizationParameters[2];
		String width = visualizationParameters[3];
		
		int columnCount = columnTypes.length;
		
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
		resultHTML += "var data = google.visualization.arrayToDataTable([\n";
		
		//Column names
		resultHTML += "[";
		for(int i = 0; i < columnCount-1; i++){
			resultHTML += "'" + columnNames[i] + "', ";
		}
		
		resultHTML += "'" + columnNames[columnCount-1] + "'],\n";
		
		String[] currentRowEntries = new String[columnCount];
		while(iterator.hasNext()){
			Object[] currentRow = iterator.next();
			for(int i = 0; i < columnCount; i++){
				currentRowEntries[i] = currentRow[i].toString();
			}
			//First entry has to be a String
			resultHTML += "['" + currentRowEntries[0] + "', ";
			for(int j = 1; j < columnCount-1; j++){
				resultHTML += currentRowEntries[j] + ", ";
			}
			if(iterator.hasNext())
				resultHTML += currentRowEntries[columnCount-1] + "],\n";
			else
				//Last Entry
				resultHTML += currentRowEntries[columnCount-1] + "]\n";
		}
		resultHTML += "]);\n";
		
		//calculation of overall max value -> optionsScaleMax
		resultHTML += "var optionsScaleMax = 0;\n";
		resultHTML += "for(var i = 1, numOfCols=data.getNumberOfColumns(); i<numOfCols; i++){\n";
		resultHTML += "  optionsScaleMax = optionsScaleMax < data.getColumnRange(i).max ? data.getColumnRange(i).max : optionsScaleMax;\n";
		resultHTML += "}\n";
		resultHTML += "optionsScaleMax = Math.ceil(optionsScaleMax);\n";

		//add zeros if value does not exist in new data table
		resultHTML += "for(var i = 0, numOfRowsNew = data.getNumberOfRows(); i<numOfRowsNew; i++)\n";
		resultHTML += "for(var j = 1, numOfColsNew = data.getNumberOfColumns(); j<numOfColsNew; j++){\n";
		resultHTML += "  if(data.getValue(i,j) == null) data.setValue(i,j,0);\n";
		resultHTML += "}\n";	
		
		resultHTML += "    var optionsLabels = '0:';\n";
		resultHTML += "    var optionsScale = '';\n";
		resultHTML += "    var optionsLineWidth = '';\n";
		resultHTML += "    var optionsLegend = 'r|l';\n";
		resultHTML += "    var optionsLegendSize = '000000,12';\n";

		resultHTML += "    for(var i = 0, numOfRows = data.getNumberOfRows(); i<numOfRows; i++){\n";
		resultHTML += "      optionsLabels += '|' + data.getValue(i,0);\n";
		resultHTML += "    }\n";

		resultHTML += "    data.removeColumn(0);\n";

		resultHTML += "    for(var i = 0, numOfCols=data.getNumberOfColumns(); i<numOfCols; i++){\n";
		resultHTML += "        optionsScale += '0,'+optionsScaleMax+(i==numOfCols-1 ? '' : ',');\n";
		resultHTML += "        optionsLineWidth += '2' + (i==numOfCols-1 ? '' : '|');\n";
		resultHTML += "    }\n";
		
		resultHTML += "    optionsLegend = 't|a';\n";
		resultHTML += "    optionsLegendSize = '000000,10';\n";
			
		resultHTML += "   var options = {\n";
		resultHTML += "      cht: 'r',\n";
		resultHTML += "      chxr: '1,0,'+optionsScaleMax+','+optionsScaleMax/10,\n";
		resultHTML += "      chds: optionsScale,\n";
		resultHTML += "      chs: '450x320',\n";
		resultHTML += "      chls: optionsLineWidth,\n";
		resultHTML += "      chxt: 'x,y',\n";
		resultHTML += "      chxl: optionsLabels,\n";
		resultHTML += "      chdlp: optionsLegend,\n";
		resultHTML += "      chdls: optionsLegendSize,\n";
		resultHTML += "		 'title':'" + title  + "'\n";
		resultHTML += "    };\n";

		resultHTML += "    var view = new google.visualization.DataView(data);\n";
		resultHTML += "    view.setRows(view.getViewRows().concat([0]));\n";
		
		resultHTML += "	   var chart = new google.visualization.ImageChart(document.getElementById('" + divId + "'));\n";
		resultHTML += "    chart.draw(view.toDataTable(), options);\n";
				
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
