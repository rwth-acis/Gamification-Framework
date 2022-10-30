package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper;

import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Measure;

public class ActionGamifiedMeasure extends GamifiedMeasure{

	public ActionGamifiedMeasure(Measure measure, double value) {
		super(measure, value);
		this.serviceRootUrl = "gamification/actions/";
		this.service = "i5.las2peer.services.gamificationActionService.GamificationActionService";
		this.serviceClass = "i5.las2peer.services.gamificationActionService.GamificationActionService";
	}

}
