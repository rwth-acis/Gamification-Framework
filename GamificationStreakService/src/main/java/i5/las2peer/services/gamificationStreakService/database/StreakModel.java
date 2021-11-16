package i5.las2peer.services.gamificationStreakService.database;

import java.time.Period;
import java.util.Map;

@SuppressWarnings("unused")
public class StreakModel {

	public static enum StreakSatstus {
		ACTIVE, PAUSED, FAILED
	}

	private String streakId;
	private String name;
	private String description;
	private int streakLevel;
	private StreakSatstus status;
	private Map<Integer, String> achievements;
	private Map<Integer, String> badges;
	private String actionId;
	private int pointValue;
	private int pointThreshold;
	// in format yyyy/MM/dd hh:mm
	private String lockedDate;
	private String dueDate;
	private Period period;
	private boolean notificationCheck;
	private String notificationMessage;
}
