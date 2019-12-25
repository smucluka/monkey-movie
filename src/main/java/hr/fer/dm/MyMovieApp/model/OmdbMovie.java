package hr.fer.dm.MyMovieApp.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OmdbMovie {

	@Id
	private String imdbID;
	private String title;
	private String year;
	private String rated;
	private String released;
	private String runtime;
	private String genre;
	private String director;
	private String writer;
	private String actors;
	private String plot;
	private String language;
	private String country;
	private String awards;
	private String poster;
	private List<Rating> ratings;
	private String metascore;
	private String imdbRating;
	private String imdbVotes;
	private String type;
	private String DVD;
	private String boxOffice;
	private String production;
	private String website;
	private String response;

	public String getTitle() {
		return title;
	}
	@JsonProperty("Title")
	public void setTitle(String title) {
		this.title = title;
	}

	public String getYear() {
		return year;
	}

	@JsonProperty("Year")
	public void setYear(String year) {
		this.year = year;
	}

	public String getRated() {
		return rated;
	}

	@JsonProperty("Rated")
	public void setRated(String rated) {
		this.rated = rated;
	}

	public String getReleased() {
		return released;
	}

	@JsonProperty("Released")
	public void setReleased(String released) {
		this.released = released;
	}

	public String getRuntime() {
		return runtime;
	}

	@JsonProperty("Runtime")
	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}

	public String getGenre() {
		return genre;
	}

	@JsonProperty("Genre")
	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getDirector() {
		return director;
	}

	@JsonProperty("Director")
	public void setDirector(String director) {
		this.director = director;
	}

	public String getWriter() {
		return writer;
	}

	@JsonProperty("Writer")
	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getActors() {
		return actors;
	}

	@JsonProperty("Actors")
	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getPlot() {
		return plot;
	}

	@JsonProperty("Plot")
	public void setPlot(String plot) {
		this.plot = plot;
	}

	public String getLanguage() {
		return language;
	}

	@JsonProperty("Language")
	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCountry() {
		return country;
	}

	@JsonProperty("Country")
	public void setCountry(String country) {
		this.country = country;
	}

	public String getAwards() {
		return awards;
	}

	@JsonProperty("Awards")
	public void setAwards(String awards) {
		this.awards = awards;
	}

	public String getPoster() {
		return poster;
	}

	@JsonProperty("Poster")
	public void setPoster(String poster) {
		this.poster = poster;
	}

	public List<Rating> getRatings() {
		return ratings;
	}

	@JsonProperty("Ratings")
	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
	}

	public String getMetascore() {
		return metascore;
	}

	@JsonProperty("Metascore")
	public void setMetascore(String metascore) {
		this.metascore = metascore;
	}

	public String getImdbRating() {
		return imdbRating;
	}

	@JsonProperty("imdbRating")
	public void setImdbRating(String imdbRating) {
		this.imdbRating = imdbRating;
	}

	public String getImdbVotes() {
		return imdbVotes;
	}

	@JsonProperty("imdbVotes")
	public void setImdbVotes(String imdbVotes) {
		this.imdbVotes = imdbVotes;
	}

	public String getImdbID() {
		return imdbID;
	}

	@JsonProperty("imdbID")
	public void setImdbID(String imdbID) {
		this.imdbID = imdbID;
	}

	public String getType() {
		return type;
	}

	@JsonProperty("Type")
	public void setType(String type) {
		this.type = type;
	}

	public String getDVD() {
		return DVD;
	}

	@JsonProperty("DVD")
	public void setDVD(String dVD) {
		DVD = dVD;
	}

	public String getBoxOffice() {
		return boxOffice;
	}

	@JsonProperty("BoxOffice")
	public void setBoxOffice(String boxOffice) {
		this.boxOffice = boxOffice;
	}

	public String getProduction() {
		return production;
	}

	@JsonProperty("Production")
	public void setProduction(String production) {
		this.production = production;
	}

	public String getWebsite() {
		return website;
	}

	@JsonProperty("Website")
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getResponse() {
		return response;
	}
	@JsonProperty("Response")
	public void setResponse(String response) {
		this.response = response;
	}

}