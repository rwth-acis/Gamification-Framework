package SuccessAwarenessGUI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionsPanelManager {

	private static final ActionsPanelManager INSTANCE = new ActionsPanelManager();
	
	public static ActionsPanelManager getInstance(){
		return INSTANCE;
	}
	
	private List<CreateActionPanel> panels;
	private ActionsPanelManager(){
		panels = new ArrayList<>(); 
	}
	
	
	
	public void add(CreateActionPanel newPanel){
		panels.add(newPanel);
	}
	
	public Map<String,Map<String,Object>> getAllGamifiedMeasures(){
		Map<String, Map<String,Object>> content = new HashMap<>();
		for(CreateActionPanel panel: panels){
			Map<String,Object> gamifiedMeasure = panel.getGamifiedMeasure();
			if(gamifiedMeasure != null && panel.getMeasureName() != null){
				content.put(panel.getMeasureName(), gamifiedMeasure);
			}
		}
		return content;
	}
	
}
