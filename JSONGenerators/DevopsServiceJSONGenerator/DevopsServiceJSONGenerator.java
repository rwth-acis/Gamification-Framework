package i5.las2peer.services.gamificationDevopsService;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import i5.las2peer.apiTestModel.BodyAssertion;
import i5.las2peer.apiTestModel.BodyAssertionOperator;
import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.ResponseBodyOperator;
import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;

public class DevopsServiceJSONGenerator {


	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception{		
		JSONArray allTests = new JSONArray();

		/*
		 tests->action->end
		 ^			|
		 |.........<-
		 */
		try(Scanner sc = new Scanner(System.in)){
			JSONObject testingObj = new JSONObject();

			System.out.println("Build test...");
			TestCase test = buildTest(sc);
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(byteArrayOut);
			os.writeObject(test);
			testingObj.put("test",Base64.getEncoder().encodeToString(byteArrayOut.toByteArray()));

			System.out.println("Build action...");
			Map<String,String> action = buildAction(sc);
			testingObj.put("gamificationObject", action);

			String stars = null;
			while(true) {
				System.out.println("Star rating (0-5): ");
				stars = sc.nextLine();
				if(stars.isEmpty())
					continue;
				float aux = Float.parseFloat(stars);
				if(aux <  0 || aux > 5) 
					continue;
				break;
			}
			String scope = null;
			String[] possibleScopes = new String[]{"code","build","test","scope","release","deploy","operator","monitor"};
			while(true) {
				System.out.println("Devops scope: (code,build,test,scope,release,deploy,operator,monitor)");
				scope = sc.nextLine();
				if(scope.isEmpty())
					continue;
				
				boolean leave = false;
				for(String s: possibleScopes) {
					if(s.equals(scope)) {
						leave = true;
						break;
					}
				}
				if(leave)
					break;
				System.out.println("Please select a valid scope for the test");
			}
			testingObj.put("starRating", Double.parseDouble(stars));
			testingObj.put("scope", scope);
			allTests.add(testingObj);
		}catch(Exception e) {
			throw e;
		}

		JSONObject toSend = new JSONObject();
		toSend.put("allTests", allTests.toJSONString());

		System.out.println(toSend.toString());
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

	private static TestCase buildTest(Scanner sc) {
		String testName = getStringFromScanner(sc,"Test name: ");

		String testRequestMethod = getStringFromScanner(sc,"Request method: ").toUpperCase();

		String testUrl = getStringFromScanner(sc,"Test url: ");

		System.out.println("(Optional) Boundary: ");
		String boundary = sc.nextLine();
		if(boundary.isEmpty())
			boundary = "--32532twtfaweafwsgfaegfawegf4";

		System.out.println("(Optional) Content-type: ");
		String contentType = sc.nextLine();
		if(contentType.isEmpty())
			contentType = "multipart/form-data";

		JSONObject pathParams = new JSONObject();
		pathParams.put("boundary", boundary);
		pathParams.put("contentType", contentType);

		List<RequestAssertion> assertions = new LinkedList<>();
		while(true) {
			String assertionType = getStringFromScanner(sc,"Define assertions\n1-Status Code, 2-Body, 0-Finish test");

			if(assertionType.equals("0"))
				break;

			RequestAssertion ass = null;
			if(assertionType.equals("1")) {
				String statusCode = getStringFromScanner(sc,"Status code: ");
				ass = new StatusCodeAssertion(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS, Integer.parseInt(statusCode));
			}else if(assertionType.equals("2")){
				/*
				 * HAS_TYPE: Whether the body (or field value) has a specific type (e.g., if it is a JSONObject, a Number, ...)
				 * HAS_FIELD: Whether it contains a field (e.g., if the JSONObject contains a field "id") 
				 * 
				 * HAS_LIST_ENTRY_THAT:
				 * Whether it is a list and contains an entry of a specific type
				 * Whether it is a list and contains an entry that contains a specific field
				 * 
				 * ALL_LIST_ENTRIES:
				 * Whether it is a list and all entries have a specific type
				 * Whether it is a list and all entries contain a specific field
				 */
				System.out.println("Type:\n1-HAS_TYPE: \n\t-Whether the body (or field value) has a specific type (e.g., if it is a JSONObject, a Number, ...)"
						+ "\n2-HAS_FIELD: \n\t-Whether it contains a field (e.g., if the JSONObject contains a field \"id\")"
						+ "\n3-HAS_LIST_ENTRY_THAT:"
						+ "\n\t-Whether it is a list and contains an entry of a specific type"
						+ "\n\t-Whether it is a list and contains an entry that contains a specific field"
						+ "\n4-ALL_LIST_ENTRIES:"
						+ "\n\t-Whether it is a list and all entries have a specific type"
						+ "\n\t-Whether it is a list and all entries contain a specific field"
						+ "\n0-Stop");
				System.out.println("Choose the type, input type and then the input value separated by spaces. Example: \n\t-4 2 test\n\t-4 _ test ");
				System.out.println("If either the input type or the the input value does not matter insert put a _");
				System.out.println("Available types: 2-JSONObject, 3-JSONArray, 4-String, 5-Boolean");

				BodyAssertionOperator operator = null;
				BodyAssertionOperator last = null;
				while(true) {
					System.out.println("Building the body assetion:");
					String[] aux = sc.nextLine().split(" ");
					if(aux.length == 0)
						continue;

					String type = aux[0];
					int inputType = aux[1].equals("_") ? -1 : Integer.parseInt(aux[1]);
					String inputValue = aux[2].equals("_") ? null : aux[2];

					if(type.equals("0"))
						break;

					int operatorId = -1;
					switch(type) {
					case "1":
						operatorId = ResponseBodyOperator.HAS_TYPE.getId();
						break;
					case "2":
						operatorId = ResponseBodyOperator.HAS_FIELD.getId();
						break;	
					case "3":
						operatorId = ResponseBodyOperator.HAS_LIST_ENTRY_THAT.getId();
						break;
					case "4":
						operatorId = ResponseBodyOperator.ALL_LIST_ENTRIES.getId();
						break;
					default:
						System.out.println("Type of the body assertion operator was not valid, please choose a number from 1 to 4");
						break;
					}
					if(operatorId != -1) {
						BodyAssertionOperator currentOperator = null;
						if(operator == null) { //1st iteration
							operator = new BodyAssertionOperator(-1, operatorId, -1, inputType,inputValue, null);
							currentOperator = operator;
						}else {
							currentOperator = new BodyAssertionOperator(-1, operatorId, -1, inputType,inputValue, null);
						}

						if(last != null) //1st iteration will be null
							last.setFollowedByOperator(currentOperator);
						last = currentOperator;
					}
					String option = getStringFromScanner(sc, "Any Key-Continue building body assertions,0-Leave");
					if(option.equals("0"))
						break;
				}
				ass = new BodyAssertion(-1,-1,-1,operator);
			}

			if(ass != null)
				assertions.add(ass);
		}
		TestRequest request = new TestRequest(testRequestMethod, testUrl,
				pathParams,-1,null,assertions);
		TestCase test = new TestCase(testName, Arrays.asList(request));
		return test;
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
