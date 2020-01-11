package hr.fer.dm.MyMovieApp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.MovieDetailed;
import hr.fer.dm.MyMovieApp.model.NyTimesReview;
import hr.fer.dm.MyMovieApp.model.OmdbMovie;
import hr.fer.dm.MyMovieApp.model.Ratings;
import hr.fer.dm.MyMovieApp.model.Soundtrack;
import hr.fer.dm.MyMovieApp.model.TmdbMovie;
import hr.fer.dm.MyMovieApp.model.User;
import hr.fer.dm.MyMovieApp.model.WatchedMovie;
import hr.fer.dm.MyMovieApp.model.YouTubeVideo;
import hr.fer.dm.MyMovieApp.repository.MovieDetailedRepository;
import hr.fer.dm.MyMovieApp.repository.MovieRepository;
import hr.fer.dm.MyMovieApp.repository.RatingsRepository;
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
	@Autowired
	RatingsRepository ratingsRepository;

	// TODO implementirati prvo provjeru iz baze podataka
	public List<Movie> getMovies(String movieTitle, boolean returnOne, boolean returnOnePage) {

		List<Movie> movies = tmdbService.getMoviesByTitle(movieTitle, returnOne, returnOnePage);

		if (movies.size() > 0)
			saveMovies(movies);
		return movies;

	}

	public List<Movie> getPopularMovies() {

		List<Movie> movies = tmdbService.getPopularMovies();

		if (movies.size() > 0)
			saveMovies(movies);
		return movies;

	}

	// TODO implementirati prvo provjeru iz baze podataka
	public MovieDetailed getMovieDetails(Long id) {
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

			movie.setId(id);
			movie.setImdbId(omdbMovie.getImdbID());
			movie.setRuntime(omdbMovie.getRuntime());
			movie.setProduction(omdbMovie.getProduction());
			movie.setCountry(omdbMovie.getCountry());
			movie.setLanguage(omdbMovie.getLanguage());
			movie.setDirector(omdbMovie.getDirector());
			movie.setWriter(omdbMovie.getWriter());
			movie.setActors(omdbMovie.getActors());
			movie.setAwards(omdbMovie.getAwards());
			movie.setBoxOffice(omdbMovie.getBoxOffice());
			movie.setRatings(omdbMovie.getRatings());
			movie.setYear(omdbMovie.getYear());
			movie.setYoutubeTrailer(youtubeTrailer.getUrl());
			movie.setNyTimesReview(nyTimesReview);
			movie.setSoundtracks(soundtrackList);

			saveDetailedMovie(movie);
		}

		return movie;
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

	public List<WatchedMovie> getWatchedMovies(Long userId) {
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

	public List<Long> getWatchedMoviesIds(Long userId) {
		User user = userService.getUserFromDB(userId);

		List<WatchedMovie> movieWatchListids = user.getWatched_movie_ids();

		List<Long> ids = new ArrayList<Long>();
		for (WatchedMovie watchedMov : movieWatchListids) {
			ids.add(watchedMov.getId());
		}

		return ids;
	}

	public void addMovieToWatched(Long user_id, Long id, Double rating) {
		User user = null;
		try {
			user = userRepository.findById(user_id).get();
			List<WatchedMovie> watchedMovies = user.getWatched_movie_ids();

			if (watchedMovies == null)
				watchedMovies = new ArrayList<>();

			List<Long> watchedMovieIds = new ArrayList<Long>();
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
			
			Movie movie = getMovieFromDB(id);
			if(movie.getMovieId() == null) {
				movie.setMovieId(Long.valueOf(1000000 + movie.getId()));
				saveMovie(movie);
			}
			
			Ratings ratings = new Ratings();
			ratings.setUserId(user_id);
			ratings.setMovieId(movie.getMovieId());
			ratings.setRating(rating);
			ratings.setTimestamp(String.valueOf(System.currentTimeMillis()));
			ratingsRepository.save(ratings);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void removeMovieFromWatched(Long user_id, Long id) {
		User user = null;
		try {
			user = userRepository.findById(user_id).get();

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
			
			Movie mov = getMovieFromDB(id);
			if(mov.getMovieId() != null) {
				List<Ratings> rating = ratingsRepository.findByUserIdAndMovieId(user_id, mov.getMovieId());
				ratingsRepository.deleteAll(rating);
			}
			
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public List<Movie> getMovieWatchList(Long userId) {
		User user = userService.getUserFromDB(userId);

		List<Long> movieWatchListids = null;
		List<Movie> movieWatchList = null;
		try {
			movieWatchListids = user.getWatch_list_movie_ids();
			movieWatchList = getMoviesByIds(movieWatchListids);	
		}catch(Exception e) {
			System.err.println(e);
		}

		return movieWatchList;
	}

	public List<Long> getMovieWatchListIds(Long userId) {
		User user = userService.getUserFromDB(userId);

		List<Long> movieWatchListids = user.getWatch_list_movie_ids();

		return movieWatchListids;
	}

	public void addMovieToWatchList(Long user_id, Long id) {
		User user = null;
		try {
			user = userRepository.findById(user_id).get();
			List<Long> ids = user.getWatch_list_movie_ids();
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

	public void removeMovieFromWatchList(Long user_id, Long id) {
		User user = null;
		try {
			user = userRepository.findById(user_id).get();
			List<Long> ids = user.getWatch_list_movie_ids();
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
			movieRepository.saveAll(movies);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void saveMovie(Movie movie) {
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

	private MovieDetailed getDetailedMovie(Long id) {
		MovieDetailed movie = null;
		try {
			Optional<MovieDetailed> mov = movieDetailedRepository.findById(String.valueOf(id));
			if(mov.isPresent()) {
				movie = mov.get();
				return movie;
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}

	public Movie getMovieFromDB(Long movieId) {
		Movie movie = null;
		try {
			Optional<Movie> mov = movieRepository.findById(movieId);
			if(mov.isPresent()) {
				movie = mov.get();
				return movie;
			}
		} catch (Exception e) {
			System.err.println(e);
		}

		return movie;
	}

	private List<Movie> getMoviesByIds(List<Long> ids) {
		List<Movie> movies = null;
		try {
			movies = movieRepository.findByIdIn(ids);
		} catch (Exception e) {
			System.err.println(e);
		}

		return movies;
	}
}
