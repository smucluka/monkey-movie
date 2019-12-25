package hr.fer.dm.MyMovieApp.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

public class MovieDetailed {

	@Id
	private String id;
	private TmdbMovie tmdbMovie;
	private OmdbMovie omdbMovie;
	private YouTubeVideo youtubeTrailer;
	private NyTimesReview nyTimesReview;
	private List<Soundtrack> soundtracks;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public TmdbMovie getTmdbMovie() {
		return tmdbMovie;
	}
	public void setTmdbMovie(TmdbMovie tmdbMovie) {
		this.tmdbMovie = tmdbMovie;
	}
	public OmdbMovie getOmdbMovie() {
		return omdbMovie;
	}
	public void setOmdbMovie(OmdbMovie omdbMovie) {
		this.omdbMovie = omdbMovie;
	}
	public YouTubeVideo getYoutubeTrailer() {
		return youtubeTrailer;
	}
	public void setYoutubeTrailer(YouTubeVideo youtubeTrailer) {
		this.youtubeTrailer = youtubeTrailer;
	}
	public NyTimesReview getNyTimesReview() {
		return nyTimesReview;
	}
	public void setNyTimesReview(NyTimesReview nyTimesReview) {
		this.nyTimesReview = nyTimesReview;
	}
	public List<Soundtrack> getSoundtracks() {
		if(this.soundtracks == null) {
			this.soundtracks = new ArrayList<Soundtrack>();
		}
		return soundtracks;
	}
	public void setSoundtracks(List<Soundtrack> soundtracks) {
		this.soundtracks = soundtracks;
	}
	
}
