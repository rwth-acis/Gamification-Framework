package i5.las2peer.services.gamificationActionService.helper;

import i5.las2peer.api.Context;
import java.net.HttpURLConnection;
import javax.ws.rs.core.MediaType;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.api.logging.MonitoringEvent;
import javax.ws.rs.core.Response;
import net.minidev.json.JSONObject;

public class ErrorResponse {

	public static Response Unauthorized(Object classObject, L2pLogger logger, JSONObject objResponse){
		Context.getCurrent().monitorEvent(classObject, MonitoringEvent.SERVICE_ERROR, "Unauthorized anonymous access.");
		objResponse.put("message", "You are not authorized");
		logger.info("[UNAUTHORIZED]" + objResponse.toJSONString());
		return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
	}
	
	public static Response InternalError(Object classObject, L2pLogger logger, Exception e, JSONObject objResponse){
		Context.getCurrent().monitorEvent(classObject, MonitoringEvent.SERVICE_ERROR, e.toString());
		logger.info("[INTERNAL ERROR] " + objResponse.toJSONString());
		return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
	}

	public static Response BadRequest(Object classObject, L2pLogger logger, Exception e, JSONObject objResponse){
		Context.getCurrent().monitorEvent(classObject, MonitoringEvent.SERVICE_ERROR, e.toString());
		logger.info("[BAD REQUEST] " + objResponse.toJSONString());
		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
	}
}
