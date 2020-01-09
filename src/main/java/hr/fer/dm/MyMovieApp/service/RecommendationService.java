package hr.fer.dm.MyMovieApp.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import hr.fer.dm.MyMovieApp.model.FBFriend;
import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.Ratings;
import hr.fer.dm.MyMovieApp.model.TmdbMovie;
import hr.fer.dm.MyMovieApp.model.WatchedMovie;
import hr.fer.dm.MyMovieApp.repository.MovieRepository;
import hr.fer.dm.MyMovieApp.repository.RatingsRepository;

@Service
public class RecommendationService {

	// Servisi djeca
	@Autowired
	UserService userService;
	@Autowired
	MovieService movieService;
	@Autowired
	OmdbService omdbService;
	@Autowired
	TmdbService tmdbService;
	@Autowired
	RatingsRepository ratingsRepository;
	@Autowired
	MovieRepository movieRepository;

	private final int NUM_RATINGS = 20;
	private final int NUM_NEIGHBOURHOODS = 10;
	private final int NUM_RECOMMENDATIONS = 9;
	private final int MIN_VALUE_RECOMMENDATION = 4;
	private Map<Long, Map<Long, Double>> ratings;
	private Map<Long, Double> averageRating;
	private Map<Long, List<Double>> friendsMap;
	public List<String> values;

	public List<Movie> getRecommendation(Long id, List<Long> friends) {

		values = new ArrayList<String>();
		List<WatchedMovie> myWatchedMovies = null;

		if (friends == null) {
			myWatchedMovies = new ArrayList<WatchedMovie>();
			for (WatchedMovie entry : movieService.getWatchedMovies(id)) {
				if (entry.getMovie().getMovieId() == null)
					continue;
				WatchedMovie wm = new WatchedMovie();
				wm.setId(entry.getMovie().getMovieId());
				wm.setMovie(entry.getMovie());
				wm.setRating(entry.getRating());
				myWatchedMovies.add(wm);
			}
		} else {
			friends.add(id);
			friendsMap = new HashMap<Long, List<Double>>();
			for (Long userId : friends) {
				List<WatchedMovie> friendMovies = movieService.getWatchedMovies(userId);
				if (friendMovies == null)
					continue;
				for (WatchedMovie friendMov : friendMovies) {
					if (friendMov.getMovie().getMovieId() == null)
						continue;
					if (friendsMap.containsKey(friendMov.getMovie().getMovieId())) {
						List<Double> rat = friendsMap.get(friendMov.getMovie().getMovieId());
						rat.add(friendMov.getRating());
						friendsMap.put(friendMov.getMovie().getMovieId(), rat);
					} else {
						List<Double> rat = new ArrayList<Double>();
						rat.add(friendMov.getRating());
						friendsMap.put(friendMov.getMovie().getMovieId(), rat);
					}
				}
			}

			myWatchedMovies = new ArrayList<WatchedMovie>();
			for (Map.Entry<Long, List<Double>> entry : friendsMap.entrySet()) {
				WatchedMovie wm = new WatchedMovie();
				wm.setId(entry.getKey());
				int i = 0;
				Double sum = 0.0;
				for (Double rating : entry.getValue()) {
					sum += rating;
					i++;
				}
				wm.setRating(sum / i);
				myWatchedMovies.add(wm);
			}
		}

		List<Long> added = new ArrayList<Long>();

		Random random = new Random();

		ratings = new HashMap<>();
		averageRating = new HashMap<Long, Double>();

		HashMap<Long, Double> myRatings = new HashMap<>();

		for (int i = 0; i < NUM_RATINGS; i++) {
			if (myWatchedMovies.size() == 0)
				break;
			// RATINGS
			int index = random.nextInt(myWatchedMovies.size());
			Long idMovie = myWatchedMovies.get(index).getId();
			myRatings.put(idMovie, myWatchedMovies.get(index).getRating());

			Double ratingg = myWatchedMovies.get(index).getRating();

			// USERS
			List<Ratings> ratin = ratingsRepository.findByMovieIdAndRating(myWatchedMovies.get(index).getId(), ratingg);

			for (Ratings ratt : ratin) {
				if (!added.contains(ratt.getUserId())) {
					added.add(ratt.getUserId());
				}
			}
			myWatchedMovies.remove(index);
		}

		Map<Long, List<Ratings>> usersMap = new HashMap<Long, List<Ratings>>();
		
		for (int i=0; i<ThreadLocalRandom.current().nextInt(added.size()/4, added.size()/3 + 1); i++) {
			if(added.size() == 0) break;
			int index = random.nextInt(added.size());
			usersMap.put(added.get(index), ratingsRepository.findByUserId(added.get(index)));
			added.remove(index);
		}
		List<Long> allMovieIds = new ArrayList<Long>();

		for (Map.Entry<Long, List<Ratings>> userMap : usersMap.entrySet()) {
			for (Ratings user : userMap.getValue()) {
				Long idUser = userMap.getKey();
				Long idMovie = user.getMovieId();
				allMovieIds.add(user.getMovieId());
				Double rating = Double.valueOf(user.getRating());

				if (ratings.containsKey(idUser)) {
					ratings.get(idUser).put(idMovie, rating);
					averageRating.put(idUser, averageRating.get(idUser) + rating);
				} else {
					Map<Long, Double> movieRating = new HashMap<>();
					movieRating.put(idMovie, rating);
					ratings.put(idUser, movieRating);
					averageRating.put(idUser, (double) rating);
				}
			}
		}
		Iterator<?> entries = averageRating.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			entry.setValue((double) entry.getValue() / (double) ratings.get(entry.getKey()).size());
		}

		Map<Long, Double> neighbourhoods = getNeighbourhoods(myRatings, NUM_NEIGHBOURHOODS);
		Map<Long, Double> recommendations = getRecommendations(myRatings, neighbourhoods,
				movieRepository.findByMovieIdIn(allMovieIds));

		ValueComparator valueComparator = new ValueComparator(recommendations);
		Map<Long, Double> sortedRecommendations = new TreeMap<>(valueComparator);
		sortedRecommendations.putAll(recommendations);

		entries = sortedRecommendations.entrySet().iterator();
		int i = 0;
		List<Movie> recommended = new ArrayList<Movie>();
		DecimalFormat df = new DecimalFormat("#.##");
		while (entries.hasNext() && i < NUM_RECOMMENDATIONS) {
			Map.Entry entry = (Map.Entry) entries.next();
			if ((double) entry.getValue() >= MIN_VALUE_RECOMMENDATION) {
				recommended.addAll(movieRepository.findByMovieId(Long.valueOf("" + entry.getKey())));
				String str = df.format(entry.getValue());
				if (!str.contains(".")) {
					str += ".0";
				}
				values.add(str);
				i++;
			}
		}

		int j = 0;
		List<Movie> returnList = new ArrayList<Movie>();
		for (Movie mov : recommended) {
			if (mov.getOverview() == null || mov.getPoster_path() == null) {
				TmdbMovie movie = tmdbService.getMovieByTmdbId(mov.getId());
				mov.setTitle(movie.getTitle());
				mov.setPoster_path(movie.getPoster_path());
				mov.setOverview(movie.getOverview());
				movieService.saveMovie(mov);
				// Must be below this
				mov.setRecommendationValue(values.get(j));
				returnList.add(mov);
			} else {
				mov.setRecommendationValue(values.get(j));
				returnList.add(mov);
			}
			j++;
		}
		return recommended;
	}

	public Map<Long, Double> getRecommendations(Map<Long, Double> userRatings, Map<Long, Double> neighbourhoods,
			List<Movie> movies) {
		Map<Long, Double> predictedRatings = new HashMap<>();

		double userAverage = getAverage(userRatings);

		for (Movie mov : movies) {
			Long movie = mov.getMovieId();
			if (!userRatings.containsKey(movie)) {
				double numerator = 0, denominator = 0;
				for (Long neighbourhood : neighbourhoods.keySet()) {
					if (ratings.get(neighbourhood).containsKey(movie)) {
						double matchRate = neighbourhoods.get(neighbourhood);
						numerator += matchRate
								* (ratings.get(neighbourhood).get(movie) - averageRating.get(neighbourhood));
						denominator += Math.abs(matchRate);
					}
				}
				double predictedRating = 0;
				if (denominator > 0) {
					predictedRating = userAverage + numerator / denominator;					
				}
				predictedRatings.put(movie, predictedRating);
			}
		}

		return predictedRatings;
	}

	public Map<Long, Double> getNeighbourhoods(Map<Long, Double> userRatings, int k) {
		Map<Long, Double> neighbourhoods = new HashMap<>();
		ValueComparator valueComparator = new ValueComparator(neighbourhoods);
		Map<Long, Double> sortedNeighbourhoods = new TreeMap<>(valueComparator);

		double userAverage = getAverage(userRatings);

		for (Long user : ratings.keySet()) {
			ArrayList<Long> matches = new ArrayList<>();
			for (Long movie : userRatings.keySet()) {
				if (ratings.get(user).containsKey(movie)) {
					matches.add(movie);
				}
			}
			double matchRate;
			if (matches.size() > 0) {
				double numerator = 0, userDenominator = 0, otherUserDenominator = 0;
				for (Long movie : matches) {
					double u = userRatings.get(movie) - userAverage;
					double v = ratings.get(user).get(movie) - averageRating.get(user);

					numerator += u * v;
					userDenominator += u * u;
					otherUserDenominator += v * v;
				}
				if (userDenominator == 0 || otherUserDenominator == 0) {
					matchRate = 0;
				} else {
					matchRate = numerator / (Math.sqrt(userDenominator) * Math.sqrt(otherUserDenominator));
				}
			} else {
				matchRate = 0;
			}

			neighbourhoods.put(user, matchRate);
		}
		sortedNeighbourhoods.putAll(neighbourhoods);

		Map<Long, Double> output = new TreeMap<>();

		Iterator entries = sortedNeighbourhoods.entrySet().iterator();
		int i = 0;
		while (entries.hasNext() && i < k) {
			Map.Entry entry = (Map.Entry) entries.next();
			if ((double) entry.getValue() > 0) {
				output.put(Long.valueOf(String.valueOf(entry.getKey())), (double) entry.getValue());
				i++;
			}
		}

		return output;
	}

	private double getAverage(Map<Long, Double> userRatings) {
		Double userAverage = 0.0;
		Iterator userEntries = userRatings.entrySet().iterator();
		while (userEntries.hasNext()) {
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) userEntries.next();
			userAverage = userAverage + Double.valueOf(entry.getValue());
		}
		return userAverage / userRatings.size();
	}

	public boolean suitableMovie(Long movieId, String min, String max) {

		String rated = movieService.getRating(movieId);

		if (rated == null || rated.equals(""))
			return true;

		if (rated.equals("G")) {
			return true;
		} else if (rated.equals("PG")) {
			return true;
		} else if (rated.equals("PG-13")) {
			return true;
		} else if (rated.equals("R")) {
			if (min.equals("18") || min.equals("21")) {
				return true;
			} else {
				return false;
			}
		} else if (rated.equals("NC-17")) {
			if (min.equals("18") || min.equals("21")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

}

class ValueComparator implements Comparator<Long> {
	private Map<Long, Double> base;

	public ValueComparator(Map<Long, Double> base) {
		this.base = base;
	}

	public int compare(Long a, Long b) {
		if (base.get(a) > base.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}
}