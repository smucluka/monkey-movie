package hr.fer.dm.MyMovieApp.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import hr.fer.dm.MyMovieApp.model.Genre;
import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.Ratings;
import hr.fer.dm.MyMovieApp.model.TmdbMovie;
import hr.fer.dm.MyMovieApp.model.User;
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

	public List<Movie> getRecommendation(Long id, List<Long> friends) {
		
	
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
			List<WatchedMovie> allFriendsMovies = new ArrayList<WatchedMovie>();
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
						allFriendsMovies.add(friendMov);
					}
				}
			}

			myWatchedMovies = new ArrayList<WatchedMovie>();
			for (WatchedMovie friendMov : allFriendsMovies) {
				WatchedMovie wm = new WatchedMovie();
				wm.setId(friendMov.getMovie().getMovieId());
				int i = 0;
				Double sum = 0.0;
				for (Double rating : friendsMap.get(friendMov.getMovie().getMovieId())) {
					sum += rating;
					i++;
				}
				wm.setRating(sum / i);
				wm.setMovie(friendMov.getMovie());
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
		}

		Map<Long, List<Ratings>> usersMap = new HashMap<Long, List<Ratings>>();
		
		for (int i=0; i<NUM_NEIGHBOURHOODS*15; i++) {
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
		
		User user = userService.getUserFromDB(id);
		HashMap<String, Double> genreBonusMap = getGenreBonusMap(myWatchedMovies);
		List<Movie> finalRecommendations = new ArrayList<Movie>();
		int i = 0;
		DecimalFormat df = new DecimalFormat("#.##");
		while (entries.hasNext() && i < NUM_RECOMMENDATIONS + 15) {
			Map.Entry entry = (Map.Entry) entries.next();
			if ((double) entry.getValue() >= MIN_VALUE_RECOMMENDATION) {
				List<Movie> moviesList = movieRepository.findByMovieId(Long.valueOf("" + entry.getKey()));

				
				Movie mov = moviesList.get(0);
				if (mov.getOverview() == null || mov.getPoster_path() == null || mov.getGenres() == null) {
					TmdbMovie movie = tmdbService.getMovieByTmdbId(mov.getId());
					mov.setTitle(movie.getTitle());
					mov.setPoster_path(movie.getPoster_path());
					mov.setOverview(movie.getOverview());
					if(movie.getGenres() != null) {
						String genre = "";
						for(Genre gen : movie.getGenres()) {
							genre += gen.getName() + "|";
						}
						if (genre != null && genre.length() > 0 && genre.charAt(genre.length() - 1) == '|') {
							genre = genre.substring(0, genre.length() - 1);
					    }
						mov.setGenres(genre);	
					}
					movieService.saveMovie(mov);
					if(mov.getTitle() == null || mov.getTitle() == "") continue;
				}
				
				double value = ((double) entry.getValue()*16) + (double) calculateBonus(genreBonusMap, mov.getGenres());

				//OUTLIER GENERS!!!
				if(mov.getGenres().contains("Animation")) {
					if(genreBonusMap.containsKey("Animation")) {
						Double total = genreBonusMap.get("Animation");
						if(total == null) total = 0.0;
						if(user.getAge_range().getMax() == null && total < 3) {
							value /= 1.7;
						}else if(user.getAge_range().getMax() != null && user.getAge_range().getMax() == "21" && total < 3) {
							value /= 1.4;
						}else if(total < 3){
							value /= 1.2;
						}
					}
				}else if(mov.getGenres().contains("Musical")) {
					if(genreBonusMap.containsKey("Musical")) {
						Double total = genreBonusMap.get("Musical");
						if(total == null) total = 0.0;
						if(user.getAge_range().getMax() == null && total < 3) {
							value /= 1.5;
						}else if(user.getAge_range().getMax() != null && user.getAge_range().getMax() == "21" && total < 3) {
							value /= 1.4;
						}else if(total < 3){
							value /= 1.2;
						}
					}
				}else if(mov.getGenres().contains("Romance")) {
					if(genreBonusMap.containsKey("Romance")) {
						Double total = genreBonusMap.get("Romance");
						if(total == null) total = 0.0;
						if(user.getAge_range().getMax() == null && total < 3) {
							value /= 1.5;
						}else if(user.getAge_range().getMax() != null && user.getAge_range().getMax() == "21" && total < 3) {
							value /= 1.4;
						}else if(total < 3){
							value /= 1.3;
						}
					}
				}else if(mov.getGenres().contains("Horror")) {
					if(genreBonusMap.containsKey("Horror")) {
						Double total = genreBonusMap.get("Horror");
						if(total == null) total = 0.0;
						if(user.getAge_range().getMax() != null && user.getAge_range().getMax() == "18" && total < 5) {
							value /= 1.5;
						}
					}
				}
				
				if(value > 100){
                    value = 100;
                }
				String str = df.format(value);
				if (!str.contains(".")) {
					str += ".0";
				}
				mov.setRecommendationValue(str);

				i++;
				finalRecommendations.add(mov);
			}
		}
		
		Collections.sort(finalRecommendations, new Comparator<Movie>(){
		     public int compare(Movie o1, Movie o2){
		         if(Double.valueOf(o1.getRecommendationValue()) == Double.valueOf(o2.getRecommendationValue()))
		             return 0;
		         return Double.valueOf(o1.getRecommendationValue()) > Double.valueOf(o2.getRecommendationValue()) ? -1 : 1;
		     }
		});
		
		return finalRecommendations.subList(0, NUM_RECOMMENDATIONS);
	}
	
	public HashMap<String, Double> getGenreBonusMap(List<WatchedMovie> watchedMovies){
		
		int cnt = 0;
		HashMap<String, Double> bonusMap = new HashMap<>();
		for(WatchedMovie wm : watchedMovies) {
			if(wm.getMovie().getGenres() == null) continue;
			for(String genre : wm.getMovie().getGenres().split("\\|")) {
				if(!bonusMap.containsKey(genre)) {
					bonusMap.put(genre, 1.0);
				}else {
					bonusMap.put(genre, bonusMap.get(genre) + 1);
				}
				cnt++;
			}
		}
		
		for(Map.Entry<String, Double> entry : bonusMap.entrySet()) {
			entry.setValue(entry.getValue()/cnt);
		}
		return bonusMap;
	}
	
	public Double calculateBonus(HashMap<String, Double> bonusMap, String genres){
		double valuePerGenre = 20.0/genres.split("\\|").length;
		double bonusSum = 0;
		for(String gen : genres.split("\\|")) {
			if(bonusMap.containsKey(gen)) {
				Double percentage = bonusMap.get(gen);
				bonusSum += percentage * valuePerGenre;
			}
		}
		return bonusSum;
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