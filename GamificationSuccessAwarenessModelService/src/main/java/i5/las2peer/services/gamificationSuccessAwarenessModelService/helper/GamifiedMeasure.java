package i5.las2peer.services.gamificationSuccessAwarenessModelService.helper;

import java.util.Objects;

import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.model.Measure;


public abstract class GamifiedMeasure {

	
	protected String service;
	protected String serviceRootUrl;
	protected String serviceClass;
	private Measure measure;
	private double value;
	private String gamificationId;
	
	public GamifiedMeasure(Measure measure, double value) {
		this.measure = measure;
		this.value = value;
	}

	public String  getGamificationId() {
		return gamificationId;
	}

	public void setGamificationId(String gamificationId) {
		this.gamificationId = gamificationId;
	}

	public final Measure getMeasure() {
		return measure;
	}

	public final void setMeasure(Measure measure) {
		this.measure = measure;
	}

	public final double getValue() {
		return value;
	}

	public final void setValue(double value) {
		this.value = value;
	}
	
	public final String getService() {
		return service;
	}

	public final String getServiceRootUrl() {
		return serviceRootUrl;
	}
	
	public final String getServiceClass() {
		return serviceClass;
	}


	@Override
	public int hashCode() {
		return Objects.hash(measure.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GamifiedMeasure other = (GamifiedMeasure) obj;
		return measure.getName().equals(other.measure.getName());
	}	
}
