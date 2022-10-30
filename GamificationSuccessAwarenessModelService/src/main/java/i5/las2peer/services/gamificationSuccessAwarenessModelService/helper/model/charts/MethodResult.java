package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.charts;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;


/**
*
* A MethodResult is a helper class that stores a ResultSet returned from a database query in a more readable format.
* The original code was taken from the QueryVisualizationService, but it has been changed quite a bit.
*
* @author Peter de Lange
*
*/
public class MethodResult {
	private int rowLength;
	private String[] columnNames = null;
	private Integer[] columnDatatypes = null;
	private LinkedList<Object[]> rowList = null;
	
	
	/**
	*
	* Transforms a given ResultSet into a new MethodResult.
	*
	* @param resultSet a SQL-ResultSet the MethodResult shall be generated from
	*
	* @throws Exception SQL Exceptions if something with the ResultSet was not in order
	*
	*/
	public MethodResult(ResultSet resultSet) throws Exception {
		
		this.rowLength = -1;
		this.rowList = new LinkedList<Object[]>();
		
		if(resultSet == null) {
			throw new Exception("ResultSet is empty!");
		}
		
		//METADATA
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		int columnCount = resultSetMetaData.getColumnCount();
		
		//First row contains the column names.
		String[] columnNames = new String[columnCount];
		for (int i = 1; i <= columnCount; i++) {
			columnNames[i-1] = resultSetMetaData.getColumnName(i);
			if(columnNames[i-1] == null) {
				columnNames[i-1] = "";
			}
		}
		this.setColumnNames(columnNames);
		
		//The second row contains the data types.
		Integer[] columnTypes = new Integer[columnCount];
		for (int i = 1; i <= columnCount; i++) {
			columnTypes[i-1] = resultSetMetaData.getColumnType(i);
			
			if(columnNames[i-1] == null) {
				throw new Exception("Invalid SQL Datatype for column: " + i + ". Fallback to Object...");
			}
		}
		this.setColumnDatatypes(columnTypes);
		//END METADATA
		
		//DATA
		while(resultSet.next()) {
			Object[] currentRow = new Object[columnCount];
			
			for(int i = 1; i<=columnCount; i++) {
				switch(columnTypes[i-1]) {
					case Types.BOOLEAN:
						currentRow[i-1] = resultSet.getBoolean(i);
						break;
					case Types.DATE:
						currentRow[i-1] = resultSet.getDate(i);
						break;
					case Types.TIME:
					case Types.TIMESTAMP:
						currentRow[i-1] = resultSet.getTime(i);
						break;
					case Types.BIGINT:
						currentRow[i-1] = resultSet.getLong(i);
						break;
					case Types.DECIMAL:
					case Types.NUMERIC:
						currentRow[i-1] = resultSet.getBigDecimal(i);
						break;
					case Types.DOUBLE:
						currentRow[i-1] = resultSet.getDouble(i);
						break;
					case Types.REAL:
					case Types.FLOAT:
						currentRow[i-1] = resultSet.getFloat(i);
						break;
					case Types.INTEGER:
						currentRow[i-1] = resultSet.getInt(i);
						break;
					case Types.SMALLINT:
						currentRow[i-1] = resultSet.getShort(i);
						break;
					case Types.VARCHAR:
						currentRow[i-1] = resultSet.getString(i);
						break;
					default:
						currentRow[i-1] = resultSet.getObject(i).toString();
						break;
				}
				//In case the transformation fails we just construct an empty entry..
				if(currentRow[i-1] == null) {
					currentRow[i-1] = "";
				}
			}
			this.addRow(currentRow);
		}
		//END DATA
		
		resultSet.close();
	}
	
	
	/**
	*
	* Sets the column names.
	*
	* @param columnNames an array of column names
	*
	*/
	private void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
		this.rowLength = columnNames.length;
	}
	
	
	/**
	*
	* Sets the column data types according to {@link java.sql.Types}.
	*
	* @param columnDatatypes an array of Integers
	*
	*/
	private void setColumnDatatypes(Integer[] columnDatatypes) {
		this.columnDatatypes = columnDatatypes;
		this.rowLength = columnDatatypes.length;
	}
	
	
	/**
	*
	* Adds a row to the MethodResult.
	*
	* @param rowValueArray an array containing the row values
	*
	*/
	private void addRow(Object[] rowValueArray) {
		if(this.rowList != null && !(rowValueArray.length != this.rowLength))
			rowList.add(rowValueArray);
	}
	
	
	/**
	*
	* Gets the column names.
	*
	* @return an array of column names
	*
	*/
	public String[] getColumnNames() {
		return this.columnNames;
	}
	
	
	/**
	*
	* Gets the column data types according to {@link java.sql.Types}.
	*
	* @return an array of Integers
	*
	*/
	public Integer[] getColumnDatatypes() {
		return this.columnDatatypes;
	}
	
	
	/**
	*
	* Gets a list of all rows.
	*
	* @return the list
	*
	*/
	public LinkedList<Object[]> getRowList() {
		return this.rowList;
	}
	
	
	/**
	*
	* A basic string representation of the MethodResult.
	* To be used for debugging and error handling.
	*
	*/
	@Override
	public String toString() {
		try {
			if(this.rowLength < 0) {
				throw new Exception("Negative row length!");
			}
			if(this.columnNames == null || this.columnNames.length <0) {
				throw new Exception("Invalid column names!");
			}
			if(this.columnDatatypes == null || this.columnDatatypes.length <0) {
				throw new Exception("Invalid column datatypes!");
			}
			if(this.rowList == null) {
				throw new Exception("Invalid rowlist!");
			}
			if(this.columnNames.length != this.columnDatatypes.length) {
				throw new Exception("Column name count does not match the datatype count!");
			}
			
			String string = "Row count: " + this.rowLength;
			
			int columnCount = this.columnNames.length;
			for(int i=0; i<columnCount; i++) {
				string += "| " + this.columnNames[i];
			}
			
			for(int i=0; i<columnCount; i++) {
				string += "-";
			}
			string += "\n";
			
			columnCount = this.columnDatatypes.length;
			for(int i=0; i<columnCount; i++) {
				string += "| " + this.columnDatatypes[i];
			}
			
			for(int i=0; i<columnCount; i++) {
				string += "=";
			}
			string += "\n";
			
			Iterator<Object[]> rowIterator = this.getRowList().iterator();
			while(rowIterator.hasNext()) {
				Object[] currentRow = rowIterator.next();
				
				if(currentRow.length != columnCount) {
					throw new Exception("A row has an invalid number of columns!");
				}
				
				for(int i=0; i<currentRow.length; i++) {
					string += "| " + currentRow[i].toString();
				}
				string += "\n";
			}
			
			return string;
		}
		catch(Exception e) {
			return "toString failed: " + e.getMessage() + e.getStackTrace().toString();
		}
	}
	
	
}
