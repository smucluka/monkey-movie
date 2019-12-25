package hr.fer.dm.MyMovieApp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Rating {

	private String source;
	private String value;

	public String getSource() {
		return source;
	}

	@JsonProperty("Source")
	public void setSource(String source) {
		this.source = source;
	}

	public String getValue() {
		return value;
	}

	@JsonProperty("Value")
	public void setValue(String value) {
		this.value = value;
	}

}
