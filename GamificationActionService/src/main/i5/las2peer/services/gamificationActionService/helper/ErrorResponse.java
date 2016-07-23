package i5.las2peer.services.gamificationActionService.helper;

import java.net.HttpURLConnection;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.restMapper.HttpResponse;
import net.minidev.json.JSONObject;

public class ErrorResponse {

	public static HttpResponse Unauthorized(Object classObject, L2pLogger logger, JSONObject objResponse){
		L2pLogger.logEvent(classObject, Event.SERVICE_ERROR, "Unauthorized anonymous access.");
		objResponse.put("message", "You are not authorized");
		logger.info("[UNAUTHORIZED]" + objResponse.toJSONString());
		return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);
	}
	
	public static HttpResponse InternalError(Object classObject, L2pLogger logger, Exception e, JSONObject objResponse){
		L2pLogger.logEvent(classObject, Event.SERVICE_ERROR, e.toString());
		logger.info("[INTERNAL ERROR] " + objResponse.toJSONString());
		return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
	}

	public static HttpResponse BadRequest(Object classObject, L2pLogger logger, Exception e, JSONObject objResponse){
		L2pLogger.logEvent(classObject, Event.SERVICE_ERROR, e.toString());
		logger.info("[BAD REQUEST] " + objResponse.toJSONString());
		return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
	}
}
