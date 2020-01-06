package hr.fer.dm.MyMovieApp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.Ratings;
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
	RatingsRepository ratingsRepository;
	@Autowired
	MovieRepository movieRepository;


	final int NUM_RATINGS = 20;
	final int NUM_NEIGHBOURHOODS = 10;
	final int NUM_RECOMMENDATIONS = 6;
	final int MIN_VALUE_RECOMMENDATION = 3;
	private Map<Integer, Map<Integer, Double>> ratings;
	private Map<Integer, Double> averageRating;
	Map<String, List<Double>> friendsMap;

	public List<Movie> getRecommendation(String id, List<String> friends) {
		
		List<WatchedMovie> myWatchedMovies = null;
		
		if(friends==null) {
			myWatchedMovies = new ArrayList<WatchedMovie>();
			for (WatchedMovie entry : movieService.getWatchedMovies(id)) {
				if(entry.getMovie().getMovieId() == null) continue;
	            WatchedMovie wm = new WatchedMovie();
	            wm.setId(entry.getMovie().getMovieId());
	            wm.setRating(entry.getRating());
	            myWatchedMovies.add(wm);
			} 	
		}else {
			friends.add(id);
			friendsMap = new HashMap<String, List<Double>>();
			for(String userId : friends) {
				List<WatchedMovie> friendMovies = movieService.getWatchedMovies(userId);
				if(friendMovies == null) continue;
				for(WatchedMovie friendMov : friendMovies) {
					if(friendMov.getMovie().getMovieId() == null) continue;
					if(friendsMap.containsKey(friendMov.getMovie().getMovieId())) {
						List<Double> rat = friendsMap.get(friendMov.getMovie().getMovieId());
						rat.add(friendMov.getRating());
						friendsMap.put(friendMov.getMovie().getMovieId(), rat);
					}else {
						List<Double> rat = new ArrayList<Double>();
						rat.add(friendMov.getRating());
						friendsMap.put(friendMov.getMovie().getMovieId(), rat);
					}
				}
			} 

			myWatchedMovies = new ArrayList<WatchedMovie>();
			for (Map.Entry<String, List<Double>> entry : friendsMap.entrySet()) { 
	            WatchedMovie wm = new WatchedMovie();
	            wm.setId(entry.getKey());
	            int i=0;
	            Double sum = 0.0;
	            for(Double rating : entry.getValue()) {
	            	sum += rating;
	            	i++;
	            }
	            wm.setRating(sum/i);
	            myWatchedMovies.add(wm);
			} 
		}
		
		List<Integer> added = new ArrayList<Integer>();
		List<Ratings> users = new ArrayList<Ratings>();

		Random random = new Random();

		ratings = new HashMap<>();
		averageRating = new HashMap<Integer, Double>();

		HashMap<Integer, Double> myRatings = new HashMap<>();

		for (int i = 0; i < NUM_RATINGS; i++) {
			if (myWatchedMovies.size() == 0)
				break;
			//RATINGS
			int index = random.nextInt(myWatchedMovies.size());
			int idMovie = Integer.valueOf(myWatchedMovies.get(index).getId());
			myRatings.put(idMovie, myWatchedMovies.get(index).getRating());
			
			String ratingg = String.valueOf(myWatchedMovies.get(index).getRating());
			ratingg = ratingg.replace(".0", "");
			//USERS
			List<Ratings> rat = ratingsRepository.findByMovieIdAndRating(Integer.valueOf(myWatchedMovies.get(index).getId()), Double.valueOf(ratingg));
			
			if(rat.isEmpty()) {
				myWatchedMovies.remove(index);
				continue;
			}
			
			int stop = random.nextInt(15 - 3 + 1) + 3;
			int j=0;
			while(j < stop) {
				if(rat.isEmpty()) break;
				Ratings ratt = rat.get(random.nextInt(rat.size()));
				rat.remove(ratt);
				if(added.contains(ratt.getUserId())) continue;
				users.addAll(ratingsRepository.findByUserId(ratt.getUserId()));
				added.add(ratt.getUserId());
				j++;
			}
			myWatchedMovies.remove(index);
		}

		List<String> allMovieIds = new ArrayList<String>();

		for (Ratings user : users) {
			int idUser = Integer.valueOf(user.getUserId());
			int idMovie = Integer.valueOf(user.getMovieId());
			allMovieIds.add(""+user.getMovieId());
			Double rating = Double.valueOf(user.getRating());

			if (ratings.containsKey(idUser)) {
				ratings.get(idUser).put(idMovie, rating);
				averageRating.put(idUser, averageRating.get(idUser) + rating);
			} else {
				Map<Integer, Double> movieRating = new HashMap<>();
				movieRating.put(idMovie, rating);
				ratings.put(idUser, movieRating);
				averageRating.put(idUser, (double) rating);
			}
		}
		Iterator entries = averageRating.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			entry.setValue((double) entry.getValue() / (double) ratings.get(entry.getKey()).size());
		}

		Map<Integer, Double> neighbourhoods = getNeighbourhoods(myRatings, NUM_NEIGHBOURHOODS);
		Map<Integer, Double> recommendations = getRecommendations(myRatings, neighbourhoods,
				movieRepository.findByMovieIdIn(allMovieIds));

		ValueComparator valueComparator = new ValueComparator(recommendations);
		Map<Integer, Double> sortedRecommendations = new TreeMap<>(valueComparator);
		sortedRecommendations.putAll(recommendations);

		entries = sortedRecommendations.entrySet().iterator();
		int i = 0;
		List<Movie> recommended = new ArrayList<Movie>();
		while (entries.hasNext() && i < NUM_RECOMMENDATIONS) {
			Map.Entry entry = (Map.Entry) entries.next();
			if ((double) entry.getValue() >= MIN_VALUE_RECOMMENDATION) {
				recommended.addAll(movieRepository.findByMovieId("" + entry.getKey()));
				i++;
			}
		}

		List<Movie> returnList = new ArrayList<Movie>();
		for(Movie mov : recommended) {
			if(mov.getOverview() == null || mov.getPoster_path() == null) {
				String title = mov.getTitle().replaceAll("\\([^\\(]*\\)", "");
				title = title.replace(", The", "");
				movieService.getMovies(title);
				returnList.add(movieRepository.findOne(mov.getId()));
			}else {
				returnList.add(movieRepository.findOne(mov.getId()));	
			}
		}
		return returnList;
	}

	/**
	 * Get predictions of each movie by a user giving some ratings and its
	 * neighbourhood: r(u,i) = r(u) + sum(sim(u,v) * (r(v,i) - r(v))) /
	 * sum(abs(sim(u,v))) sim(u,v): similarity between u and v users r(u,i): rating
	 * of the movie i by the user u r(u): average rating of the user u
	 * 
	 * @param userRatings    ratings of the user
	 * @param neighbourhoods nearest neighbourhoods
	 * @param movies         movies in the database
	 * @return predictions for each movie
	 */
	public Map<Integer, Double> getRecommendations(Map<Integer, Double> userRatings,
			Map<Integer, Double> neighbourhoods, List<Movie> movies) {
		Map<Integer, Double> predictedRatings = new HashMap<>();

		double userAverage = getAverage(userRatings);

		for (Movie mov : movies) {
			int movie = Integer.valueOf(mov.getMovieId());
			if (!userRatings.containsKey(movie)) {
				double numerator = 0, denominator = 0;
				for (int neighbourhood : neighbourhoods.keySet()) {
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
					if (predictedRating > 5) {
						predictedRating = 5;
					}
				}
				predictedRatings.put(movie, predictedRating);
			}
		}

		return predictedRatings;
	}

	/**
	 * Get the k-nearest neighbourhoods using Pearson: sim(i,j) = numerator /
	 * (sqrt(userDenominator^2) * sqrt(otherUserDenominator^2)) numerator =
	 * sum((r(u,i) - r(u)) * (r(v,i) - r(v))) userDenominator = sum(r(u,i) - r(i))
	 * otherUserDenominator = sum(r(v,i) - r(v)) r(u,i): rating of the movie i by
	 * the user u r(u): average rating of the user u
	 * 
	 * @param userRatings ratings of the user
	 * @param k           number of output neighbourhoods
	 * @return nearest neighbourhoods
	 */
	public Map<Integer, Double> getNeighbourhoods(Map<Integer, Double> userRatings, int k) {
		Map<Integer, Double> neighbourhoods = new HashMap<>();
		ValueComparator valueComparator = new ValueComparator(neighbourhoods);
		Map<Integer, Double> sortedNeighbourhoods = new TreeMap<>(valueComparator);

		double userAverage = getAverage(userRatings);

		for (int user : ratings.keySet()) {
			ArrayList<Integer> matches = new ArrayList<>();
			for (int movie : userRatings.keySet()) {
				if (ratings.get(user).containsKey(movie)) {
					matches.add(movie);
				}
			}
			double matchRate;
			if (matches.size() > 0) {
				double numerator = 0, userDenominator = 0, otherUserDenominator = 0;
				for (int movie : matches) {
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

		Map<Integer, Double> output = new TreeMap<>();

		Iterator entries = sortedNeighbourhoods.entrySet().iterator();
		int i = 0;
		while (entries.hasNext() && i < k) {
			Map.Entry entry = (Map.Entry) entries.next();
			if ((double) entry.getValue() > 0) {
				output.put((int) entry.getKey(), (double) entry.getValue());
				i++;
			}
		}

		return output;
	}

	/**
	 * Get average of the ratings of a user
	 * 
	 * @param userRatings ratings of a user
	 * @return average or the ratings of a user
	 */
	private double getAverage(Map<Integer, Double> userRatings) {
		Double userAverage = 0.0;
		Iterator userEntries = userRatings.entrySet().iterator();
		while (userEntries.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) userEntries.next();
			userAverage = userAverage + Double.valueOf(entry.getValue());
		}
		return userAverage / userRatings.size();
	}

	public boolean suitableMovie(String movieId, String min, String max) {

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

	// Plain friends
	/*
	 * public List<Movie> getRecommendation(String id) {
	 * 
	 * List<Movie> possibleMovies = new ArrayList<Movie>(); List<String>
	 * existingImdbIds = new ArrayList<String>();
	 * 
	 * User user = userService.getUserFromDB(id);
	 * 
	 * List<FBFriend> fbFriends = user.getFriends().getData();
	 * 
	 * List<String> myWatchedMovies = movieService.getWatchedMoviesIds(id);
	 * List<String> myWatchList = movieService.getMovieWatchListIds(id);
	 * 
	 * for (FBFriend friend : fbFriends) {
	 * 
	 * List<WatchedMovie> watchedMovies =
	 * movieService.getWatchedMovies(friend.getId()); List<Movie> watchList =
	 * movieService.getMovieWatchList(friend.getId());
	 * 
	 * if(watchedMovies == null) continue; if(watchList == null) continue;
	 * 
	 * for(WatchedMovie watchedMovie : watchedMovies) {
	 * 
	 * if(!suitableMovie(watchedMovie.getMovie().getId(),
	 * user.getAge_range().getMin(), user.getAge_range().getMax())) { continue; }
	 * 
	 * if(!existingImdbIds.contains(watchedMovie.getMovie().getId()) &&
	 * !myWatchedMovies.contains(watchedMovie.getMovie().getId()) &&
	 * !myWatchList.contains(watchedMovie.getMovie().getId())) {
	 * possibleMovies.add(watchedMovie.getMovie());
	 * existingImdbIds.add(watchedMovie.getMovie().getId()); } }
	 * 
	 * for(Movie watchListMovie : watchList) {
	 * 
	 * if(!suitableMovie(watchListMovie.getId(), user.getAge_range().getMin(),
	 * user.getAge_range().getMax())) { continue; }
	 * 
	 * if(!existingImdbIds.contains(watchListMovie.getId()) &&
	 * !myWatchedMovies.contains(watchListMovie.getId()) &&
	 * !myWatchList.contains(watchListMovie.getId())) {
	 * possibleMovies.add(watchListMovie);
	 * existingImdbIds.add(watchListMovie.getId()); } }
	 * 
	 * }
	 * 
	 * 
	 * List<Movie> randomList = new ArrayList<Movie>(); Random rand = new Random();
	 * int numberOfElements = 6; for (int i = 0; i < numberOfElements; i++) { if
	 * (possibleMovies.size() == 0) break;
	 * 
	 * int randomIndex = rand.nextInt(possibleMovies.size()); Movie randomElement =
	 * possibleMovies.get(randomIndex);
	 * 
	 * randomList.add(randomElement); possibleMovies.remove(randomIndex);
	 * 
	 * }
	 * 
	 * 
	 * return randomList; }
	 */

	// verzija s fb filmovima
	/*
	 * public List<Movie> getRecommendation(String id) {
	 * 
	 * List<Movie> allFriendsMovies = new ArrayList<Movie>(); List<String>
	 * existingImdbIds = new ArrayList<String>();
	 * 
	 * List<FBFriend> fbFriends =
	 * userService.getUserFromDB(id).getFriends().getData();
	 * 
	 * List<String> watchedMovies = movieService.getWatchedMoviesIds(id);
	 * List<String> watchList = movieService.getMovieWatchListIds(id);
	 * 
	 * for (FBFriend friend : fbFriends) {
	 * 
	 * for(Movie mov : movieService.getFacebookMovies(friend.getMovies())) {
	 * if(!existingImdbIds.contains(mov.getId()) &&
	 * !watchedMovies.contains(mov.getId()) && !watchList.contains(mov.getId())) {
	 * allFriendsMovies.add(mov); existingImdbIds.add(mov.getId()); } }
	 * 
	 * }
	 * 
	 * 
	 * //ako frendovi nemaju lajkane filmove if(allFriendsMovies.isEmpty()) {
	 * allFriendsMovies = movieService.getAllMovies(); }
	 * 
	 * List<Movie> randomList = new ArrayList<Movie>(); Random rand = new Random();
	 * int numberOfElements = 6; for (int i = 0; i < numberOfElements; i++) { if
	 * (allFriendsMovies.size() == 0) break;
	 * 
	 * int randomIndex = rand.nextInt(allFriendsMovies.size()); Movie randomElement
	 * = allFriendsMovies.get(randomIndex);
	 * 
	 * randomList.add(randomElement); allFriendsMovies.remove(randomIndex);
	 * 
	 * }
	 * 
	 * 
	 * 
	 * return randomList; }
	 */

}

class ValueComparator implements Comparator<Integer> {
	private Map<Integer, Double> base;

	public ValueComparator(Map<Integer, Double> base) {
		this.base = base;
	}

	public int compare(Integer a, Integer b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}
}