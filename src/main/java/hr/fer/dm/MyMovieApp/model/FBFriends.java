package hr.fer.dm.MyMovieApp.model;

import java.util.List;

public class FBFriends {
	private List<FBFriend> data;
	private Paging paging;
	
	public List<FBFriend> getData() {
		return data;
	}
	public void setData(List<FBFriend> data) {
		this.data = data;
	}
	public Paging getPaging() {
		return paging;
	}
	public void setPaging(Paging paging) {
		this.paging = paging;
	}
}
