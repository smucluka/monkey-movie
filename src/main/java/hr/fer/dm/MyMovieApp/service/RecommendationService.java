package hr.fer.dm.MyMovieApp.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hr.fer.dm.MyMovieApp.model.FBFriend;
import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.WatchedMovie;

@Service
public class RecommendationService {

	// Servisi djeca
	@Autowired
	UserService userService;
	@Autowired
	MovieService movieService;

	public List<Movie> getRecommendation(String id) {
		
		List<Movie> possibleMovies = new ArrayList<Movie>();
		List<String> existingImdbIds = new ArrayList<String>();

		List<FBFriend> fbFriends = userService.getUserFromDB(id).getFriends().getData();

		List<String> myWatchedMovies = movieService.getWatchedMoviesIds(id);
		List<String> myWatchList = movieService.getMovieWatchListIds(id);
		
		for (FBFriend friend : fbFriends) {
			
			List<WatchedMovie> watchedMovies = movieService.getWatchedMovies(friend.getId());
			List<Movie> watchList = movieService.getMovieWatchList(friend.getId());
			
			if(watchedMovies == null) continue;
			if(watchList == null) continue;
			
			for(WatchedMovie watchedMovie : watchedMovies) {
				if(!existingImdbIds.contains(watchedMovie.getMovie().getId()) 
						&& !myWatchedMovies.contains(watchedMovie.getMovie().getId()) && !myWatchList.contains(watchedMovie.getMovie().getId())) {
					possibleMovies.add(watchedMovie.getMovie());
					existingImdbIds.add(watchedMovie.getMovie().getId());
				}
			}
			
			for(Movie watchListMovie : watchList) {
				if(!existingImdbIds.contains(watchListMovie.getId()) 
						&& !myWatchedMovies.contains(watchListMovie.getId()) && !myWatchList.contains(watchListMovie.getId())) {
					possibleMovies.add(watchListMovie);
					existingImdbIds.add(watchListMovie.getId());
				}
			}
			
		}
		
		
		List<Movie> randomList = new ArrayList<Movie>();
		Random rand = new Random();
		int numberOfElements = 6;
		for (int i = 0; i < numberOfElements; i++) {
			if (possibleMovies.size() == 0)
				break;

			int randomIndex = rand.nextInt(possibleMovies.size());
			Movie randomElement = possibleMovies.get(randomIndex);

			randomList.add(randomElement);
			possibleMovies.remove(randomIndex);

		}
		
		
		return randomList;
	}
	
	//verzija s fb filmovima
	/*public List<Movie> getRecommendation(String id) {

		List<Movie> allFriendsMovies = new ArrayList<Movie>();
		List<String> existingImdbIds = new ArrayList<String>();

		List<FBFriend> fbFriends = userService.getUserFromDB(id).getFriends().getData();

		List<String> watchedMovies = movieService.getWatchedMoviesIds(id);
		List<String> watchList = movieService.getMovieWatchListIds(id);
		
		for (FBFriend friend : fbFriends) {
			
			for(Movie mov : movieService.getFacebookMovies(friend.getMovies())) {
				if(!existingImdbIds.contains(mov.getId()) && !watchedMovies.contains(mov.getId()) && !watchList.contains(mov.getId())) {
					allFriendsMovies.add(mov);
					existingImdbIds.add(mov.getId());
				}
			}
			
		}
		
		
		//ako frendovi nemaju lajkane filmove
		if(allFriendsMovies.isEmpty()) {
			allFriendsMovies = movieService.getAllMovies();
		}
		
		List<Movie> randomList = new ArrayList<Movie>();
		Random rand = new Random();
		int numberOfElements = 6;
		for (int i = 0; i < numberOfElements; i++) {
			if (allFriendsMovies.size() == 0)
				break;

			int randomIndex = rand.nextInt(allFriendsMovies.size());
			Movie randomElement = allFriendsMovies.get(randomIndex);

			randomList.add(randomElement);
			allFriendsMovies.remove(randomIndex);

		}
		
		

		return randomList;
	}*/

	
}
