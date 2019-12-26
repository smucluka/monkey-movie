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

@Service
public class RecommendationService {

	// Servisi djeca
	@Autowired
	UserService userService;
	@Autowired
	MovieService movieService;

	public List<Movie> getRecommendation(String id) {

		List<Movie> allFriendsMovies = new ArrayList<Movie>();
		List<String> existingImdbIds = new ArrayList<String>();

		List<FBFriend> fbFriends = userService.getUserFromDB(id).getFriends().getData();

		for (FBFriend friend : fbFriends) {
			
			for(Movie mov : movieService.getFacebookMovies(friend.getMovies())) {
				if(!existingImdbIds.contains(mov.getId())) {
					allFriendsMovies.add(mov);
					existingImdbIds.add(mov.getId());
				}
			}
			
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
	}

}
