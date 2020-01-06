package hr.fer.dm.MyMovieApp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.MovieDetailed;
import hr.fer.dm.MyMovieApp.model.NyTimesReview;
import hr.fer.dm.MyMovieApp.model.OmdbMovie;
import hr.fer.dm.MyMovieApp.model.Soundtrack;
import hr.fer.dm.MyMovieApp.model.TmdbMovie;
import hr.fer.dm.MyMovieApp.model.User;
import hr.fer.dm.MyMovieApp.model.WatchedMovie;
import hr.fer.dm.MyMovieApp.model.YouTubeVideo;
import hr.fer.dm.MyMovieApp.repository.MovieDetailedRepository;
import hr.fer.dm.MyMovieApp.repository.MovieRepository;
import hr.fer.dm.MyMovieApp.repository.UserRepository;

//Glavni servis
@Service
public class MovieService {

	// Servisi djeca
	@Autowired
	UserService userService;
	@Autowired
	OmdbService omdbService;
	@Autowired
	TmdbService tmdbService;
	@Autowired
	YoutubeService youtubeService;
	@Autowired
	NyTimesService nyTimesService;
	@Autowired
	SoundrtackService soundtrackService;

	// Repository
	@Autowired
	MovieRepository movieRepository;
	@Autowired
	MovieDetailedRepository movieDetailedRepository;
	@Autowired
	UserRepository userRepository;

	// TODO implementirati prvo provjeru iz baze podataka
	public List<Movie> getMovies(String movieTitle) {

		List<Movie> movies = tmdbService.getMoviesByTitle(movieTitle, false);

		if (movies.size() > 0)
			saveMovies(movies);
		return movies;

	}

	public MovieDetailed getMovieDetails(String id) {
		MovieDetailed movie = getDetailedMovie(id);

		if (movie == null) {
			movie = new MovieDetailed();

			// Dohvat svih detaljnih informacija o filmu
			TmdbMovie tmdbMovie = tmdbService.getMovieByTmdbId(id);
			OmdbMovie omdbMovie = omdbService.getMovieByImdbId(tmdbMovie.getImdb_id());
			YouTubeVideo youtubeTrailer = youtubeService
					.fetchVideosByQuery(omdbMovie.getTitle() + " " + omdbMovie.getYear() + " trailer");
			NyTimesReview nyTimesReview = nyTimesService.getReviewByQuery(tmdbMovie.getTitle());
			List<Soundtrack> soundtrackList = soundtrackService.getSoundtracksByMovieTitle(tmdbMovie.getTitle());

			movie.setTmdbMovie(tmdbMovie);
			movie.setOmdbMovie(omdbMovie);
			movie.setYoutubeTrailer(youtubeTrailer);
			movie.setNyTimesReview(nyTimesReview);
			movie.setSoundtracks(soundtrackList);

			saveDetailedMovie(movie);
		}

		return movie;
	}
	
	public String getRating(String id) {
		Movie movie = getMovieFromDB(id);

		if (movie == null || movie.getRated() == null) {
			
			TmdbMovie tmdbMovie = tmdbService.getMovieByTmdbId(id);
			OmdbMovie omdbMovie = omdbService.getMovieByImdbId(tmdbMovie.getImdb_id());
			movie.setRated(omdbMovie.getRated());
			saveMovie(movie);
			return omdbMovie.getRated();
		}else {
			return movie.getRated();
		}
	}

//	public List<Movie> getFacebookMovies(FBMovies fbMovies) {
//
//		List<Movie> likedMovies = new ArrayList<Movie>();
//		for (FBMovie fbMovie : fbMovies.getData()) {
//
//			List<Movie> movieList = movieRepository.findByTitleIgnoreCase(fbMovie.getName().trim());
//
//			if (movieList.isEmpty()) {
//				List<Movie> movies = tmdbService.getMoviesByTitle(fbMovie.getName(), true);
//				if (!movies.isEmpty() && movies.get(0).getTitle().toLowerCase().trim()
//						.equals(fbMovie.getName().toLowerCase().trim())) {
//					saveMovie(movies.get(0));
//					likedMovies.add(movies.get(0));
//				}
//			} else {
//				likedMovies.addAll(movieList);
//			}
//
//		}
//		return likedMovies;
//	}

	public List<WatchedMovie> getWatchedMovies(String userId) {
		User user = userService.getUserFromDB(userId);
		List<WatchedMovie> movieWatchListids = null;
		try {
			movieWatchListids = user.getWatched_movie_ids();

			for (WatchedMovie watchedMov : movieWatchListids) {
				watchedMov.setMovie(getMovieFromDB(watchedMov.getId()));
			}
		}catch(Exception e) {
			System.err.println(e);
		}
		

		return movieWatchListids;
	}

	public List<String> getWatchedMoviesIds(String userId) {
		User user = userService.getUserFromDB(userId);

		List<WatchedMovie> movieWatchListids = user.getWatched_movie_ids();

		List<String> ids = new ArrayList<String>();
		for (WatchedMovie watchedMov : movieWatchListids) {
			ids.add(watchedMov.getId());
		}

		return ids;
	}

	public void addMovieToWatched(String user_id, String id, Double rating) {
		User user = null;
		try {
			user = userRepository.findOne(user_id);
			List<WatchedMovie> watchedMovies = user.getWatched_movie_ids();

			if (watchedMovies == null)
				watchedMovies = new ArrayList<>();

			List<String> watchedMovieIds = new ArrayList<String>();
			for (WatchedMovie watchedMov : watchedMovies) {
				watchedMovieIds.add(watchedMov.getId());
			}

			if (!watchedMovieIds.contains(id)) {
				WatchedMovie watchedMovie = new WatchedMovie();
				watchedMovie.setId(id);
				watchedMovie.setRating(rating);
				watchedMovies.add(watchedMovie);
			}

			user.setWatched_movie_ids(watchedMovies);

			userRepository.save(user);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void removeMovieFromWatched(String user_id, String id) {
		User user = null;
		try {
			user = userRepository.findOne(user_id);

			List<WatchedMovie> watchedMovies = user.getWatched_movie_ids();

			if(watchedMovies == null) return;

			List<WatchedMovie> found = new ArrayList<WatchedMovie>();
			
			for (WatchedMovie watchedMov : watchedMovies) {
				if(watchedMov.getId().equals(id)) {
			        found.add(watchedMov);
				}			
			}
			watchedMovies.removeAll(found);
			
			user.setWatched_movie_ids(watchedMovies);
			userRepository.save(user);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public List<Movie> getMovieWatchList(String userId) {
		User user = userService.getUserFromDB(userId);

		List<String> movieWatchListids = null;
		List<Movie> movieWatchList = null;
		try {
			movieWatchListids = user.getWatch_list_movie_ids();
			movieWatchList = getMoviesByIds(movieWatchListids);	
		}catch(Exception e) {
			System.err.println(e);
		}

		return movieWatchList;
	}

	public List<String> getMovieWatchListIds(String userId) {
		User user = userService.getUserFromDB(userId);

		List<String> movieWatchListids = user.getWatch_list_movie_ids();

		return movieWatchListids;
	}

	public void addMovieToWatchList(String user_id, String id) {
		User user = null;
		try {
			user = userRepository.findOne(user_id);
			List<String> ids = user.getWatch_list_movie_ids();
			if (ids == null)
				ids = new ArrayList<>();
			if (!ids.contains(id))
				ids.add(id);
			user.setWatch_list_movie_ids(ids);

			userRepository.save(user);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void removeMovieFromWatchList(String user_id, String id) {
		User user = null;
		try {
			user = userRepository.findOne(user_id);
			List<String> ids = user.getWatch_list_movie_ids();
			if (ids == null)
				return;
			if (ids.contains(id))
				ids.remove(id);
			user.setWatch_list_movie_ids(ids);

			userRepository.save(user);
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	public List<Movie> getAllMovies(){
		try {
			return movieRepository.findAll();	
		}catch(Exception e) {
			System.err.println(e);
		}
		return null;
	}

	private void saveMovies(List<Movie> movies) {
		try {
			movieRepository.save(movies);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private void saveMovie(Movie movie) {
		try {
			movieRepository.save(movie);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private void saveDetailedMovie(MovieDetailed movie) {
		try {
			//TODO ovo je potrebno kako bi provjerio u bazi da li postoji objekt prije poziva apija
			//movie.setId(movie.getTmdbMovie().getId());
			movieDetailedRepository.save(movie);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private MovieDetailed getDetailedMovie(String id) {
		MovieDetailed movie = null;
		try {
			movie = movieDetailedRepository.findOne(id);
			return movie;
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}

	private Movie getMovieFromDB(String movieId) {
		Movie movie = null;
		try {
			movie = movieRepository.findOne(movieId);
		} catch (Exception e) {
			System.err.println(e);
		}

		return movie;
	}

	private List<Movie> getMoviesByIds(List<String> ids) {
		List<Movie> movies = null;
		try {
			movies = movieRepository.findByIdIn(ids);
		} catch (Exception e) {
			System.err.println(e);
		}

		return movies;
	}
}
