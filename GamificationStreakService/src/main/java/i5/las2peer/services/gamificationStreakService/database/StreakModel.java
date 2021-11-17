package i5.las2peer.services.gamificationStreakService.database;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;

@SuppressWarnings("unused")
public class StreakModel {

	public static enum StreakSatstus {
		ACTIVE, PAUSED, FAILED, UPDATED
	}

	private String streakId;
	private String name;
	private String description;
	private int streakLevel;
	private StreakSatstus status;
	//which streakLevel grants wwat achievement
	private Map<Integer, String> achievements;
	//how is a range of streakLevels represented
	private Map<Integer, String> badges;
	//the action of a streak
	private String actionId;
	//the value to pass in order to increase the streak
	private int pointThreshold;
	// in format yyyy/MM/dd hh:mm
	private LocalDateTime lockedDate;
	private LocalDateTime dueDate;
	private Period period;
	private boolean notificationCheck;
	private String notificationMessage;
	
	
	/**
	 * @return the streakId
	 */
	public String getStreakId() {
		return streakId;
	}
	/**
	 * @param streakId the streakId to set
	 */
	public void setStreakId(String streakId) {
		this.streakId = streakId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the streakLevel
	 */
	public int getStreakLevel() {
		return streakLevel;
	}
	/**
	 * @param streakLevel the streakLevel to set
	 */
	public void setStreakLevel(int streakLevel) {
		this.streakLevel = streakLevel;
	}
	/**
	 * @return the status
	 */
	public StreakSatstus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(StreakSatstus status) {
		this.status = status;
	}
	/**
	 * @return the achievements
	 */
	public Map<Integer, String> getAchievements() {
		return achievements;
	}
	/**
	 * @param achievements the achievements to set
	 */
	public void setAchievements(Map<Integer, String> achievements) {
		this.achievements = achievements;
	}
	/**
	 * @return the badges
	 */
	public Map<Integer, String> getBadges() {
		return badges;
	}
	/**
	 * @param badges the badges to set
	 */
	public void setBadges(Map<Integer, String> badges) {
		this.badges = badges;
	}
	/**
	 * @return the actionId
	 */
	public String getActionId() {
		return actionId;
	}
	/**
	 * @param actionId the actionId to set
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	/**
	 * @return the pointThreshold
	 */
	public int getPointThreshold() {
		return pointThreshold;
	}
	/**
	 * @param pointThreshold the pointThreshold to set
	 */
	public void setPointThreshold(int pointThreshold) {
		this.pointThreshold = pointThreshold;
	}
	
	/**
	 * @return the lockedDate
	 */
	public LocalDateTime getLockedDate() {
		return lockedDate;
	}
	/**
	 * @param lockedDate the lockedDate to set
	 */
	public void setLockedDate(LocalDateTime lockedDate) {
		this.lockedDate = lockedDate;
	}
	/**
	 * @return the dueDate
	 */
	public LocalDateTime getDueDate() {
		return dueDate;
	}
	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(LocalDateTime dueDate) {
		this.dueDate = dueDate;
	}
	/**
	 * @return the period
	 */
	public Period getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(Period period) {
		this.period = period;
	}
	/**
	 * @return the notificationCheck
	 */
	public boolean isNotificationCheck() {
		return notificationCheck;
	}
	/**
	 * @param notificationCheck the notificationCheck to set
	 */
	public void setNotificationCheck(boolean notificationCheck) {
		this.notificationCheck = notificationCheck;
	}
	/**
	 * @return the notificationMessage
	 */
	public String getNotificationMessage() {
		return notificationMessage;
	}
	/**
	 * @param notificationMessage the notificationMessage to set
	 */
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}
