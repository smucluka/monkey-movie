package hr.fer.dm.MyMovieApp.model;

import org.springframework.data.annotation.Id;

public class WatchedMovie {

	private String id;
	private Integer rating;
	private Movie movie;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public Movie getMovie() {
		return movie;
	}
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	
}
