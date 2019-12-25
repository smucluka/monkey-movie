package hr.fer.dm.MyMovieApp.model;

import org.springframework.data.annotation.Id;

public class FBMovie {

	@Id
	private String id;
	private String name;
	private String createdTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	
}
