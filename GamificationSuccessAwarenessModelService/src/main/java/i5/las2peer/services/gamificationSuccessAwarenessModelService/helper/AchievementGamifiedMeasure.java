package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper;

import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Measure;

public class AchievementGamifiedMeasure extends GamifiedMeasure{

	public AchievementGamifiedMeasure(Measure measure, double value) {
		super(measure, value);
		this.serviceRootUrl = "createNewAchievement";
		this.service = "i5.las2peer.services.gamificationActionService.GamificationAchievementService";
		this.serviceClass = "i5.las2peer.services.gamificationAchievementService.GamificationAchievementService";
	}

}
