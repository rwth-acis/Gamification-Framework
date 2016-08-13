package i5.las2peer.services.gamificationApplicationService.helper;

import java.util.Map;

public class FormDataHeader {

	private final String name;
	private final Map<String, String> parameters;

	public FormDataHeader(String name, Map<String, String> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	/**
	 * Gets the name for this header, like 'Content-Disposition' or 'Content-Type'.
	 * 
	 * @return Returns the headers name or {@code null} if no name is set.
	 */
	public String getName() {
		return name;
	}
	
	public String getAll(){
		return parameters.toString();
	}

	/**
	 * Gets the value of the parameter in this header with the given name.
	 * 
	 * @param parameterName parameter name to get the value
	 * @return Returns the value for the given parameter
	 */
	public String getParameter(String parameterName) {
		return parameters.get(parameterName);
	}

	/**
	 * Counts the number of parameters in this header.
	 * 
	 * @return Returns the number of parameters.
	 */
	public int countParameter() {
		return parameters.size();
	}

	/**
	 * Gets the first parameter for this header.
	 * 
	 * @return Returns the first parameter.
	 */
	public String getFirstParameter() {
		if (countParameter() > 0) {
			//
			//Change the code from parameters.values().iterator().next();
			// The result pair is "image/png=null"
			return parameters.keySet().iterator().next();
		}
		return null;
	}

}
