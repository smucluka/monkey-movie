package hr.fer.dm.MyMovieApp.model;

import org.springframework.data.annotation.Id;

public class Movie {

	@Id
	private Long id;
	private Long movieId;
	private String title;
	private String overview;
	private String poster_path;
	private String adult;
	private String genres;
	private String year;
	private String averageRating;
	private String recommendationValue;
	private String recommendationExplained;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getOverview() {
		return overview;
	}
	public void setOverview(String overview) {
		this.overview = overview;
	}
	public String getPoster_path() {
		return poster_path;
	}
	public void setPoster_path(String poster_path) {
		this.poster_path = poster_path;
	}
	public String getAdult() {
		return adult;
	}
	public void setAdult(String adult) {
		this.adult = adult;
	}
	public Long getMovieId() {
		return movieId;
	}
	public void setMovieId(Long movieId) {
		this.movieId = movieId;
	}
	public String getGenres() {
		return genres;
	}
	public void setGenres(String genres) {
		this.genres = genres;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getAverageRating() {
		return averageRating;
	}
	public void setAverageRating(String averageRating) {
		this.averageRating = averageRating;
	}
	public String getRecommendationValue() {
		return recommendationValue;
	}
	public void setRecommendationValue(String recommendationValue) {
		this.recommendationValue = recommendationValue;
	}
	public String getRecommendationExplained() {
		return recommendationExplained;
	}
	public void setRecommendationExplained(String recommendationExplained) {
		this.recommendationExplained = recommendationExplained;
	}
	
}
