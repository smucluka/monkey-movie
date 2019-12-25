package hr.fer.dm.MyMovieApp.model;

public class FBFriend {
	private String id;
	private String name;
	private FBMovies movies;

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

	public FBMovies getMovies() {
		return movies;
	}

	public void setMovies(FBMovies movies) {
		this.movies = movies;
	}

}
