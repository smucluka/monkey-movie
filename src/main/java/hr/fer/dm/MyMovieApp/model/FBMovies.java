package hr.fer.dm.MyMovieApp.model;

import java.util.List;

public class FBMovies {

	private List<FBMovie> data;
	private Paging paging;
	
	public List<FBMovie> getData() {
		return data;
	}
	public void setData(List<FBMovie> data) {
		this.data = data;
	}
	public Paging getPaging() {
		return paging;
	}
	public void setPaging(Paging paging) {
		this.paging = paging;
	}
	
}
