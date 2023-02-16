package i5.las2peer.services.gamificationSuccessAwarenessModelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONObject;

public class SuccessAwarenessModelServiceJSONGenerator {


	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception{		
		JSONObject jsonObj = new JSONObject();

		try(Scanner sc = new Scanner(System.in)){
			JSONObject testingObj = new JSONObject();

			System.out.println("Paste the success awareness model");
			String model = readSuccessAwarenessModel(sc);
			testingObj.put("test",model);

			Map<String,Map<String,Object>> content = buildContent(sc);

			jsonObj.put("catalog", model);
			jsonObj.put("content", content);
		}catch(Exception e) {
			throw e;
		}

		System.out.println(jsonObj.toJSONString());
	}

	private static Map<String,String> buildAction(Scanner sc) {
		String actionId = getStringFromScanner(sc,"Action id: ");
		String points = getStringFromScanner(sc,"Points of the actions: ");		
		System.out.println("(Optional) Action name: ");
		String actionName = sc.nextLine();
		if(actionName.isEmpty())
			actionName = actionId + " name";

		System.out.println("(Optional) Content-type: ");
		String actionDescription = sc.nextLine();
		if(actionDescription.isEmpty())
			actionDescription = actionId + " description";

		Map<String,String> action = new HashMap<>();
		action.put("actionid", actionId);
		action.put("actionname", actionName);
		action.put("actiondesc", actionDescription);
		action.put("actionpointvalue", points);
		action.put("actionnotificationcheck", "true");
		action.put("actionnotificationmessage", actionId + " notification message");

		return action;		
	}

	private static String readSuccessAwarenessModel(Scanner sc) {
		return sc.nextLine();
	}

	private static Map<String, Map<String,Object>> buildContent(Scanner sc) {
		System.out.println("Now choose the name of the measure to gamify and its value to trigger the action. That is, when that value is reached the action will trigger on the "
				+ "gamification framework");

		Map<String, Map<String,Object>> content = new HashMap<>();
		while(true) {
			String measure = getStringFromScanner(sc,"Select measure name");

			double valueToTrigger = Double.parseDouble(getStringFromScanner(sc,"Select the value to trigger"));


			Map<String,Object> action = new HashMap<>();
			content.put(measure, action);
			action.put("service", "action");
			action.put("valueToTrigger", valueToTrigger + "");

			action.put("gamificationObject", buildAction(sc));

			System.out.println("Measure: " + measure + " is now being triggered at " + valueToTrigger + " with action id: " + ((Map<String,String>)action.get("gamificationObject"))
					.get("actionid"));

			String option = getStringFromScanner(sc,"Any key-Continue,0-leave");

			if(option.equals("0"))
				break;
		}
		return content;
	}

	private static String getStringFromScanner(Scanner sc,String textToDisplay) {
		String res = null;
		while(true) {
			System.out.println(textToDisplay);
			res= sc.nextLine();
			if(!res.isEmpty())
				break;
		}
		return res;
	}

}
