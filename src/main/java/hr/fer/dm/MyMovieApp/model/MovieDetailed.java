package hr.fer.dm.MyMovieApp.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

public class MovieDetailed {

	@Id
	private Long id;
	private String imdbId;
	private String rated;
	private String runtime;
	private String genre;
	private String director;
	private String writer;
	private String actors;
	private String plot;
	private String language;
	private String country;
	private String awards;
	private List<Rating> ratings;
	private String boxOffice;
	private String production;
	private String website;
	private String year;
	private String youtubeTrailer;
	private NyTimesReview nyTimesReview;
	private List<Soundtrack> soundtracks;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getImdbId() {
		return imdbId;
	}
	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}
	public String getRated() {
		return rated;
	}
	public void setRated(String rated) {
		this.rated = rated;
	}
	public String getRuntime() {
		return runtime;
	}
	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getWriter() {
		return writer;
	}
	public void setWriter(String writer) {
		this.writer = writer;
	}
	public String getActors() {
		return actors;
	}
	public void setActors(String actors) {
		this.actors = actors;
	}
	public String getPlot() {
		return plot;
	}
	public void setPlot(String plot) {
		this.plot = plot;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getAwards() {
		return awards;
	}
	public void setAwards(String awards) {
		this.awards = awards;
	}
	public List<Rating> getRatings() {
		return ratings;
	}
	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
	}
	public String getBoxOffice() {
		return boxOffice;
	}
	public void setBoxOffice(String boxOffice) {
		this.boxOffice = boxOffice;
	}
	public String getProduction() {
		return production;
	}
	public void setProduction(String production) {
		this.production = production;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getYoutubeTrailer() {
		return youtubeTrailer;
	}
	public void setYoutubeTrailer(String youtubeTrailer) {
		this.youtubeTrailer = youtubeTrailer;
	}
	public NyTimesReview getNyTimesReview() {
		return nyTimesReview;
	}
	public void setNyTimesReview(NyTimesReview nyTimesReview) {
		this.nyTimesReview = nyTimesReview;
	}
	public List<Soundtrack> getSoundtracks() {
		return soundtracks;
	}
	public void setSoundtracks(List<Soundtrack> soundtracks) {
		this.soundtracks = soundtracks;
	}
	
}
