package i5.las2peer.services.gamificationSuccessAwarenessModelService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.AgentAccessDeniedException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.connectors.ConnectorException;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.execution.ExecutionContext;
import i5.las2peer.logging.NodeObserver;
import i5.las2peer.logging.monitoring.MonitoringMessage;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.MonitoringAgent;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.serialization.MalformedXMLException;
import i5.las2peer.serialization.XmlTools;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.database.DatabaseManager;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.AchievementGamifiedMeasure;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.ActionGamifiedMeasure;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.GamifiedMeasure;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.Pair;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Chart;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Chart.ChartType;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.KPI;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Measure;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Value;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Visualization;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

/**
 * Gamification Success Awareness Model
 * 
 * This service gamifies a given success awareness model. 
 * @author David Almeida
 */
@Api
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Gamification Success Awareness Model Service",
				version = "1.0.0",
				description = "A las2peer Service for gamifying a success awareness model.",
				contact = @Contact(
						name = "David Almeida",
						email = "david.almeida@rwth-aachen.com"),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com")))
@ManualDeployment
@ServicePath("gamification/successawarenessmodel")
public class GamificationSuccessAwarenessModelService extends RESTService implements NodeObserver{


	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

	private String gamificationJdbcDriverClassName;
	private String gamificationJdbcLogin;
	private String gamificationJdbcPass;	
	private String gamificationJdbcUrl;
	private String gamificationJdbcSchema;

	private String successAwarenessModelJdbcDriverClassName;
	private String successAwarenessModelJdbcLogin;
	private String successAwarenessModelJdbcPass;
	private String successAwarenessModelJdbcUrl;
	private String successAwarenessModelJdbcSchema;

	private DatabaseManager gamificationDb;
	private DatabaseManager successAwarenessModelDb;


	public static final String GAMIFICATION_MEMBER_ID = "success_awareness";
	private static String gamificationMemberFirstName;
	private static String gamificationMemberLastName;
	private static String gamificationMemberEmail;

	public GamificationSuccessAwarenessModelService(){
		setFieldValues(); // This sets the values of the configuration file

		successAwarenessModelDb = new DatabaseManager(
				successAwarenessModelJdbcDriverClassName,
				successAwarenessModelJdbcLogin,
				successAwarenessModelJdbcPass,
				successAwarenessModelJdbcUrl,
				successAwarenessModelJdbcSchema
				);

		gamificationDb = new DatabaseManager(
				gamificationJdbcDriverClassName,
				gamificationJdbcLogin,
				gamificationJdbcPass,
				gamificationJdbcUrl,
				gamificationJdbcSchema);
	}

	private Response handleGamifiySuccessModel(String gameId,
			String member, String contentType, 
			byte[] formData) throws ConnectorException, InterruptedException {
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/successawareness/"+gameId, true);

		JSONObject objResponse = new JSONObject();
		//try to add the game member to the gamification framework
		if(!GamificationSuccessAwarenessModelServiceState.getInstance().getGameIds().contains(gameId)) {
			try(Statement stmn = gamificationDb.getConnection().createStatement()) {
				ClientResponse response = GamificationSuccessAwarenessModelServiceState.getInstance().sendRequest("POST"
						, "gamification/games/data/" + gameId + "/" + GAMIFICATION_MEMBER_ID);
				if(response.getHttpCode() != 200) {
					objResponse.put("message", response.getResponse());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.serverError().entity(objResponse).build();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.serverError().entity(objResponse).build();
			}finally {
				try {
					gamificationDb.getConnection().close();
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.serverError().entity(objResponse).build();
				}
			}	
		}

		/*
		 * 1o mapa:
		 * -xml
		 * -mapas com chave da measure name: (2o mapa)
		 * 	-service
		 *  -gamification object
		 */
		Set<GamifiedMeasure> receivedMeasures = new HashSet<>();
		try {
			String json = new String(formData, StandardCharsets.UTF_8);
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject)parser.parse(json);

			Map<String, Measure> measures =  getMeasuresFromXml((String)jsonObj.get("catalog"));

			Map<String,Map<String,Object>> content =  (Map<String,Map<String,Object>>) jsonObj.get("content");


			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			String gamificationContentType = "multipart/form-data; boundary=" + boundary;
			Map<String, String> gamificationHeaders = new HashMap<>();

			gamificationHeaders.put("Accept-Encoding","gzip, deflate");
			gamificationHeaders.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");

			for(String measureName: content.keySet()) {
				Map<String,Object> action = content.get(measureName);

				Map<String, String> gamificationObject = (Map<String, String>) action.get("gamificationObject");				

				GamifiedMeasure gamifiedMeasure = null;
				String service = (String)action.get("service");
				double value = Double.parseDouble((String)action.get("valueToTrigger"));
				Measure measure = measures.get(measureName);

				switch(service) {
				case "action":
					gamifiedMeasure = new ActionGamifiedMeasure(measure, value);
					break;
				case "achievement":
					gamifiedMeasure = new AchievementGamifiedMeasure(measure, value);
				}

				//add the measure to the temp set to add them later
				receivedMeasures.add(gamifiedMeasure);

				//create the gamification object
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				builder.setBoundary(boundary);

				for(String key: gamificationObject.keySet()) {
					builder.addPart(key,new StringBody(gamificationObject.get(key), ContentType.TEXT_PLAIN));
				}

				HttpEntity data = builder.build();
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				data.writeTo(out);

				//add gamification object
				Map<String, i5.las2peer.services.gamificationActionService.helper.FormDataPart> parts = i5.las2peer.services.gamificationActionService.helper.MultipartHelper.getParts(out.toString().getBytes(), gamificationContentType);

				ClientResponse response = GamificationSuccessAwarenessModelServiceState.getInstance().sendRequest("POST",gamifiedMeasure.getServiceRootUrl() + gameId, out.toString(), gamificationContentType, gamificationHeaders);
				String body = (String)((JSONObject)parser.parse(response.getResponse())).get("message").toString();
				if(response.getHttpCode() == 201) {
					String gamificationId = body.substring(body.indexOf("(") + 1,body.lastIndexOf(")"));

					//now insert into the gamification table
					try(PreparedStatement stmn = gamificationDb.getConnection().prepareStatement(
							"INSERT INTO " + gameId + ".success_awareness_gamified_measure VALUES(?,?,?,?,?)")) {
						stmn.setString(1, measureName);
						stmn.setString(2, gamificationId);
						stmn.setString(3, gameId);
						stmn.setString(4, member);
						stmn.setString(5, measure.getXml());
						if(stmn.executeUpdate() != 1)
							throw new SQLException("error adding success awareness model to db");
					}catch(SQLException e) {
						e.printStackTrace();

						//since an error occurred delete all the previously added ones
						deleteGamifiedMeasures(gameId,receivedMeasures);

						objResponse.put("message", e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse).build();
					}

					long randomLong = new Random().nextLong(); //To be able to match 
					gamifiedMeasure.setGamificationId(body.substring(body.indexOf("(") + 1,body.lastIndexOf(")")));
					Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24, ""+measureName, true);
					Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, ""+gameId, true);	

				}else {
					//since an error occurred delete all the previously added ones
					deleteGamifiedMeasures(gameId,receivedMeasures);

					objResponse.put("message", body);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse).build();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();

			//since an error occurred delete all the previously added ones
			deleteGamifiedMeasures(gameId,receivedMeasures);

			objResponse.put("message", e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.serverError().entity(objResponse).build();
		}

		//everything went well, add things
		for(GamifiedMeasure measure: receivedMeasures) {	
			GamificationSuccessAwarenessModelServiceState.getInstance().addGamifiedMeasure(gameId, measure);
		}
		objResponse.put("message", "Everything was gamified succefully");
		return Response.ok().entity(objResponse).build();
	}


	@POST
	@Path("/setup")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Setup completed successfully") })
	@ApiOperation(
			value = "setup",
			notes = "This method setups the success awareness model service")
	/**
	 * 
	 * @return 200 ok if the setup was done successfully
	 */
	public Response setup(){
		try(FileWriter writer = new FileWriter(new File("outputtest.txt"))) {
			if(!GamificationSuccessAwarenessModelServiceState.getInstance().isObsever()) {
				writer.append("1");
				((ExecutionContext)Context.getCurrent()).getCallerContext().getLocalNode().addObserver(this);
				GamificationSuccessAwarenessModelServiceState.getInstance().setIsObserver(true);
				writer.append("2");
				try(Statement stmn = gamificationDb.getConnection().createStatement()) {
					writer.append("3");
					int res = stmn.executeUpdate("INSERT INTO manager.member_info (member_id,first_name,last_name,email) "
							+ "VALUES (\'" + GAMIFICATION_MEMBER_ID + "\',\'" + gamificationMemberFirstName + "\',\'" + gamificationMemberLastName + "\',\'" + gamificationMemberEmail + "\')"
							+ "ON CONFLICT DO NOTHING");
					writer.append("4");
					if(res != 1) {
						writer.append("5 a");
						return Response.serverError().entity("success awareness member already existed").build();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					writer.append("5 b");
					return Response.serverError().entity(e.getMessage()).build();
				}
				writer.append("5 c");
				return Response.ok().entity("Setup completed successfully").build();
			}
			writer.append("5 d");
			return Response.serverError().entity("Already is an observer").build();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return Response.serverError().entity("Ok").build();
	}

	/**
	 * Adds a success awareness model to be gamified.
	 * 
	 * @param gameId the id of the game
	 * @param member the member that is gamifying the model
	 * @param formData the data of the model and the action
	 * @return Returns an HTTP response with 200 if everything was performed successfully, or 500 if something 
	 * unexpected happened.
	 * @throws InterruptedException 
	 * @throws ConnectorException 
	 */
	@POST
	@Path("/{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Added success awareness model successfully") })
	@ApiOperation(
			value = "addSuccessModel",
			notes = "This method gamifies a success awareness model")
	public Response gamifiySuccessModel(@PathParam("gameId") String gameId,
			@ApiParam(value = "member") @QueryParam("member") String member,
			@ApiParam(value = "action data in multiple/form-data type", required = true)
	@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
	byte[] formData) throws ConnectorException, InterruptedException {		
		Response res = handleGamifiySuccessModel(gameId, member, contentType, formData);
		try {
			GamificationSuccessAwarenessModelServiceState.getInstance().stop();
		} catch (ConnectorException e) {
			e.printStackTrace();
		}
		return res;
	}

	private void deleteGamifiedMeasures(String gameId, Set<GamifiedMeasure> measures) throws ConnectorException, InterruptedException {
		for(GamifiedMeasure measure: measures) {
			removeMeasureFromGame(gameId, measure.getMeasure().getName());
		}
	}

	private static String getElementXml(Element element) {
		Document document = element.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document
				.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		return serializer.writeToString(element);
	}

	private static Map<String, Measure> getMeasuresFromXml(String xml) throws MalformedXMLException {		
		Map<String, Measure> measures = new TreeMap<>();
		Element root;
		root = XmlTools.getRootElement(xml, "Catalog");
		NodeList children = root.getChildNodes();
		for (int measureNumber = 0; measureNumber < children.getLength(); measureNumber++) {
			if (children.item(measureNumber).getNodeType() == Node.ELEMENT_NODE) {
				Element measureElement = (Element) children.item(measureNumber);
				Map<String, String> queries = new HashMap<>();
				Visualization visualization = null;
				String description = null;

				if (!measureElement.hasAttribute("name")) {
					throw new MalformedXMLException(
							"Catalog contains a measure without a name!");
				}
				String measureName = measureElement.getAttribute("name");
				if (measures.containsKey("measureName")) {
					throw new MalformedXMLException(
							"Catalog already contains a measure " + measureName + "!");
				}

				NodeList mChildren = measureElement.getChildNodes();
				for (int measureChildCount = 0; measureChildCount < mChildren.getLength(); measureChildCount++) {
					if (mChildren.item(measureChildCount).getNodeType() == Node.ELEMENT_NODE) {
						Element measureChild = (Element) mChildren.item(measureChildCount);
						String childType = measureChild.getNodeName();

						if (childType.equals("query")) {
							String queryName = measureChild.getAttribute("name");
							String query = measureChild.getFirstChild().getTextContent();
							// Replace escape characters with their correct values (seems like the simple
							// XML Parser
							// does not do
							// that)
							query = query
									.replaceAll("&amp;&", "&")
									.replaceAll("&lt;", "<")
									.replaceAll("&lt;", "<")
									.replaceAll("&gt;", ">")
									.replaceAll("&lt;", "<");
							queries.put(queryName, query);
						} else if (childType.equals("visualization")) {
							if (visualization != null) {
								throw new MalformedXMLException(
										"Measure " +
												measureName +
										" is broken, duplicate 'Visualization' entry!");
							}
							visualization = readVisualization(measureChild);
						} else if (childType.equals("description")) {
							description = measureChild
									.getTextContent()
									.replaceAll("&amp;&", "&")
									.replaceAll("&lt;", "<")
									.replaceAll("&lt;", "<")
									.replaceAll("&gt;", ">")
									.replaceAll("&lt;", "<");
						} else {
							throw new MalformedXMLException(
									"Measure " +
											measureName +
											" is broken, illegal node " +
											childType +
									"!");
						}
					}
				}

				if (visualization == null) {
					throw new MalformedXMLException(
							"Measure " + measureName + " is broken, no visualization element!");
				}
				if (queries.isEmpty()) {
					if (measureElement.hasAttribute("sid")) {
						if (!measureElement.hasAttribute("title")) {
							throw new MalformedXMLException(
									"Measure " + measureName + " is broken, Limesurvey measures require a title!");
						}
					} else {
						throw new MalformedXMLException(
								"Measure " + measureName + " is broken, no query element!");
					}

				}
				measures.put(
						measureName,
						new Measure(measureName, queries, visualization, description,
								getElementXml(measureElement)));
			}
		}


		return measures;
	}

	/**
	 * Helper method that reads a visualization object of the catalog file.
	 *
	 * @return a visualization object
	 * @throws MalformedXMLException
	 */
	private static Visualization readVisualization(Element visualizationElement)
			throws MalformedXMLException {
		String visualizationType = visualizationElement.getAttribute("type");
		if (visualizationType.equals("Value")) {
			return new Value();
		} else if (visualizationType.equals("KPI")) {
			String expression = "";
			NodeList children = visualizationElement.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
					expression += ((Element) children.item(i)).getAttribute("name");
				}
			}
			return new KPI(expression);
		} else if (visualizationType.equals("Chart")) {
			String type;
			ChartType chartType = null;
			String[] parameters = new String[4];
			NodeList children = visualizationElement.getChildNodes();
			Element[] elements = new Element[5];
			int j = 0;
			for (int i = 0; i < children.getLength(); ++i) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
					elements[j] = (Element) children.item(i);
					j++;
				}
				if (j >= 5) {
					break;
				}
			}
			type = elements[0].getFirstChild().getTextContent();
			for (int i = 0; i < 4; ++i) {
				parameters[i] = elements[i + 1].getFirstChild().getTextContent();
			}

			if (type.equals("BarChart")) {
				chartType = ChartType.BarChart;
			}
			if (type.equals("LineChart")) {
				chartType = ChartType.LineChart;
			}
			if (type.equals("PieChart")) {
				chartType = ChartType.PieChart;
			}
			if (type.equals("RadarChart")) {
				chartType = ChartType.RadarChart;
			}
			if (type.equals("TimelineChart")) {
				chartType = ChartType.TimelineChart;
			}

			try {
				return new Chart(chartType, parameters);
			} catch (Exception e) {
				throw new MalformedXMLException("Could not create chart: " + e);
			}
		}
		throw new MalformedXMLException(
				"Unknown visualization type: " + visualizationType);
	}

	private static final Object LOCK = new Object();

	@Override
	public void log(Long timestamp, MonitoringEvent event, String sourceNode, String sourceAgentId,
			String destinationNode, String destinationAgentId, String remarks) {
		synchronized (LOCK) {
			JSONObject json = new JSONObject();
			json.put("timestamp", timestamp);
			json.put("event", event.name());
			json.put("sourceNode", sourceNode);
			json.put("sourceAgentId", sourceAgentId);
			json.put("destinationNode", destinationNode);
			json.put("destinationAgentId", destinationAgentId);
			json.put("remarks", remarks);

			Map<String,String> headers = new HashMap<>();
			headers = new HashMap<>();

			headers.put("Accept-Encoding","gzip, deflate");
			headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			headers.put("Content-Type","application/json");

			try {
				GamificationSuccessAwarenessModelServiceState.getInstance().sendRequest("POST","gamification/successawarenessmodel/_listen",json.toJSONString(),
						"multipart/form-data",headers);
				//				GamificationSuccessAwarenessModelServiceState.getInstance().stop();
			} catch (ConnectorException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Template of a get function.
	 * 
	 * @return Returns an HTTP response with the username as string content.
	 */
	@POST
	@Path("/_listen")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response listenToMessage(byte[] formData) {		
		GamificationSuccessAwarenessModelServiceState state = GamificationSuccessAwarenessModelServiceState.getInstance();
		String json = new String(formData, StandardCharsets.UTF_8);
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = null;
		try {
			jsonObj = (JSONObject)parser.parse(json);
		} catch (ParseException e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}

		try {
			final String processingService = "i5.las2peer.services.mobsos.dataProcessing.MobSOSDataProcessingService";
			LocalNode node = state.getNode();

			// get monitoring agent (should create one)
			String agentId = (String) node.invoke((UserAgentImpl)Context.getCurrent().getMainAgent(), processingService, "getReceivingAgentId",
					new Serializable[] { "Test message." });

			//add agent to data processing service
			//String agentId = state.getDataProcessingService().getReceivingAgentId(GamificationSuccessAwarenessModelService.class.getName());
			MonitoringAgent agent = (MonitoringAgent) node.getAgent(agentId);
			agent.unlock("ProcessingAgentPass");
			//			agent = (MonitoringAgent) Context.getCurrent().fetchAgent(agentId);
			//			System.out.println(agentId);
			//			System.out.println(Context.getCurrent().getMainAgent().getIdentifier());

			//process message
			MonitoringMessage msg = new MonitoringMessage(Long.parseLong(jsonObj.get("timestamp").toString())
					, MonitoringEvent.valueOf((String)jsonObj.get("event")),(String)jsonObj.get("sourceNode"),
					(String)jsonObj.get("sourceAgentId"),  (String)jsonObj.get("destinationNode")
					, (String)jsonObj.get("destinationAgentId"), (String)jsonObj.get("remarks"));

			boolean messagePosted = (Boolean)state.getNode().invoke(agent, "i5.las2peer.services.mobsos.dataProcessing.MobSOSDataProcessingService", "getMessages"
					, new Serializable[] {new MonitoringMessage[] {msg}});
			if(messagePosted) {	
				//check if the service is still on
				if((msg.getEvent().toString().equals("SERVICE_SHUTDOWN") && msg.getRemarks().contains(this.getClass().getName()))
						|| msg.getEvent().toString().equals("NODE_SHUTDOWN")) {
					state.stopListening();
				}
				if(state.canListen()) {
					List<Pair<String,String>> measuresToRemove = new LinkedList<>();
					try(Connection conn = successAwarenessModelDb.getConnection()){
						for(String gameId: state.getGameIds()) {
							for(GamifiedMeasure measure: state.getGamifiedMeasures(gameId)) {
								double result = Double.parseDouble(measure.getMeasure().visualize(conn));
								if(result - measure.getValue() > 0.00001) {
									//trigger action
									Context.getCurrent().invoke("i5.las2peer.services.gamificationActionService.GamificationActionService"
											, "triggerActionRMI", gameId, GAMIFICATION_MEMBER_ID, measure.getGamificationId());

									//add the measure to be removed since it was already gamified
									measuresToRemove.add(new Pair<String,String>(gameId,measure.getMeasure().getName()));
								}
							}
						}						
					} catch (Exception e) {
						e.printStackTrace();
					}

					//remove the measures
					for(Pair<String,String> pair: measuresToRemove) {
						GamificationSuccessAwarenessModelServiceState.getInstance()
						.removeMeasureFromGame(pair.getFirst(),pair.getSecond());
					}
				}
			}
		} catch (AgentNotFoundException | AgentLockedException | ServiceInvocationException | AgentAccessDeniedException | AgentOperationFailedException e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}

		return Response.ok().build();
	}


	/**
	 * Deletes a measure from the gamified success awareness model.
	 * 
	 * @param gameId the id of the game
	 * @param measureName the measure to remove
	 * @return Returns an HTTP response with 200 if everything was performed successfully, or 500 if something 
	 * unexpected happened.
	 * @throws InterruptedException 
	 * @throws ConnectorException 
	 */
	@DELETE
	@Path("/{gameId}/{measureName}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Delete measaure from gamified success awareness model successfully") })
	@ApiOperation(
			value = "deleteSuccessModelMeasure",
			notes = "This removes a gamified measure from the previously gamified success awareness model")
	public Response removeMeasureFromGame(@PathParam("gameId") String gameId,
			@PathParam("measureName") String measureName) throws ConnectorException, InterruptedException {
		Response res = handleRemoveMeasureFromGame(gameId, measureName);
		try {
			GamificationSuccessAwarenessModelServiceState.getInstance().stop();
		} catch (ConnectorException e) {
			e.printStackTrace();
		}

		return res;
	}

	private Response handleRemoveMeasureFromGame(String gameId,
			String measureName) throws ConnectorException, InterruptedException {
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE" + "gamification/successawareness/" + gameId + "/" + measureName, true);

		JSONObject objResponse = new JSONObject();

		GamificationSuccessAwarenessModelServiceState state = GamificationSuccessAwarenessModelServiceState.getInstance();
		GamifiedMeasure measure = state.getGamifiedMeasure(gameId,measureName);
		if(measure == null) {
			objResponse.put("measure", "the measure or game does not exist");	
			return Response.status(404).entity(objResponse).build();
		}

		ClientResponse response = state.sendRequest("DELETE", measure.getServiceRootUrl() + gameId + "/" + measure.getGamificationId());
		if(response.getHttpCode() != 200) {
			objResponse.put("measure", response.getResponse());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.serverError().entity(objResponse).build();
		}
		state.removeMeasureFromGame(gameId,measureName);
		objResponse.put("measure", "Measure " + measureName + " deleted successfully from game " + gameId);
		return Response.ok().entity(objResponse).build();
	}

	/**
	 * Deletes all measures from the gamified success awareness model.
	 * 
	 * @param gameId the id of the game
	 * @return Returns an HTTP response with 200 if everything was performed successfully, or 500 if something 
	 * unexpected happened.
	 * @throws InterruptedException 
	 * @throws ConnectorException 
	 */
	@DELETE
	@Path("/{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Deleted gamified success awareness model successfully") })
	@ApiOperation(
			value = "deleteSuccessModelGame",
			notes = "This removes all gamified measures from the previously gamified success awareness model")
	public Response removeGame(@PathParam("gameId") String gameId) throws ConnectorException, InterruptedException {
		Response res = handleRemoveGame(gameId);
		try {
			GamificationSuccessAwarenessModelServiceState.getInstance().stop();
		} catch (ConnectorException e) {
			e.printStackTrace();
		}
		return res;
	}

	private Response handleRemoveGame(String gameId) throws ConnectorException, InterruptedException {
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE" + "gamification/successawareness/" + gameId, true);

		JSONObject objResponse = new JSONObject();

		GamificationSuccessAwarenessModelServiceState state = GamificationSuccessAwarenessModelServiceState.getInstance();
		for(GamifiedMeasure measure: state.getGamifiedMeasures(gameId)) {
			Response response = removeMeasureFromGame(gameId,measure.getMeasure().getName());
			if(response.getStatus() != 200) {
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) ((JSONObject)response.getEntity()).get("message"));
				return Response.serverError().entity(objResponse).build();
			}
		}
		state.removeGame(gameId);
		objResponse.put("measure", "All measures have been deleted successfully from game " + gameId);
		return Response.ok().entity(objResponse).build();
	}

	/**
	 * Deletes the success awareness model member from the gamification framework
	 * @param gameId the gameId of the game in which the member is suppossed to be removed  
	 * @return Returns an HTTP response with 200 if everything was performed successfully, or 500 if something 
	 * unexpected happened.
	 */
	@DELETE
	@Path("member/{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Delete game member from the gamified success awareness model successfully") })
	@ApiOperation(
			value = "deleteSuccessModelMember",
			notes = "This removes a gamified measure from the previously gamified success awareness model")
	public Response removeMember(@PathParam("gameId") String gameId) {
		Response res = handleRemoveMember(gameId);
		try {
			GamificationSuccessAwarenessModelServiceState.getInstance().stop();
		} catch (ConnectorException e) {
			e.printStackTrace();
		}
		return res;
	}

	private Response handleRemoveMember(String gameId) {
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE" + "gamification/successawareness/member/" + gameId, true);

		JSONObject objResponse = new JSONObject();

		try(PreparedStatement stmn = gamificationDb.getConnection().prepareStatement("DELETE FROM manager.member_game WHERE member_id = ? AND game_id = ?")) {
			stmn.setString(1, GAMIFICATION_MEMBER_ID);
			stmn.setString(2, gameId);
			int res = stmn.executeUpdate();
			if(res < 1) {
				objResponse.put("message", "game or member not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity(objResponse).build();
			}
		}catch(SQLException e) {
			e.printStackTrace();
			objResponse.put("message", e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.serverError().entity(objResponse).build();
		}
		objResponse.put("measure", "The success awareness member have been deleted successfully from game " + gameId);
		return Response.ok().entity(objResponse).build();
	}

	public DatabaseManager getGamificationDatabase() {
		return gamificationDb;
	}

	public DatabaseManager getSuccessAwarenessModelDatabase() {
		return successAwarenessModelDb;
	}

}
