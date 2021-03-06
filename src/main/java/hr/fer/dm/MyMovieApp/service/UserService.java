package hr.fer.dm.MyMovieApp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import hr.fer.dm.MyMovieApp.model.FBFriend;
import hr.fer.dm.MyMovieApp.model.FBFriends;
import hr.fer.dm.MyMovieApp.model.User;
import hr.fer.dm.MyMovieApp.model.WatchedMovie;
import hr.fer.dm.MyMovieApp.repository.RatingsRepository;
import hr.fer.dm.MyMovieApp.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;
	@Autowired
	RatingsRepository ratingsRepository;

	RestTemplate restTemplate = new RestTemplate();

	public User saveUser(OAuth2Authentication authUser) {
		User user = getUserInfo(authUser);
		try {
			userRepository.save(user);
		} catch (Error err) {
			System.err.println(err);
		}
		return user;
	}

	public User getUserInfo(OAuth2Authentication authUser) {
		User user = null;
		OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authUser.getDetails();
		try {
			user = apiUserInfo(details.getTokenValue());

			Optional<User> userTemp = userRepository.findById(user.getId());

			if (userTemp.isPresent()) {
				user.setWatched_movie_ids(userTemp.get().getWatched_movie_ids());
				user.setWatch_list_movie_ids(userTemp.get().getWatch_list_movie_ids());
			} else {
				user.setWatch_list_movie_ids(new ArrayList<Long>());
				user.setWatched_movie_ids(new ArrayList<WatchedMovie>());
			}

			// user.setMovies(fetchLikedMovies(details.getTokenValue(), user.getId()));
			user.setFriends(fetchFBFriends(details.getTokenValue(), user.getId()));

		} catch (Error err) {
			System.err.println(err);
		}

		return user;
	}

	private User apiUserInfo(String token) {
		String fields = "id,name,email,age_range";
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://graph.facebook.com/me")
				.queryParam("access_token", token).queryParam("fields", fields);
		User user = restTemplate.getForObject(uriBuilder.toUriString(), User.class);

		// profile picture
		fields = "picture.height(961)";
		uriBuilder = UriComponentsBuilder.fromUriString("https://graph.facebook.com/me")
				.queryParam("access_token", token).queryParam("fields", fields);

		JsonNode response = restTemplate.getForObject(uriBuilder.toUriString(), JsonNode.class);
		user.setProfilePicture(response.get("picture").get("data").get("url").textValue());
		return user;
	}

	public FBFriends fetchFBFriends(final String accessToken, final Long userId) {
		final String fields = "friends";
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://graph.facebook.com/me")
				.queryParam("access_token", accessToken).queryParam("user_id", userId).queryParam("fields", fields);
		User user = restTemplate.getForObject(uriBuilder.toUriString(), User.class);

		List<FBFriend> friendList = new ArrayList<>();

		if (user.getFriends() != null) {
			int maxFriends = 10;
			for (FBFriend friend : user.getFriends().getData()) {
				if (maxFriends <= 0)
					break;
				// friend.setMovies(fetchLikedMovies(accessToken, friend.getId()));
				friendList.add(friend);
			}

			if (user.getFriends().getPaging() != null && user.getFriends().getPaging().getNext() != null) {
				while (user.getFriends().getPaging().getNext() != null) {
					FBFriends pagedFriends = restTemplate.getForObject(user.getFriends().getPaging().getNext(),
							FBFriends.class);
					user.setFriends(pagedFriends);
					for (FBFriend friend : user.getFriends().getData()) {
						if (maxFriends <= 0)
							break;
						// friend.setMovies(fetchLikedMovies(accessToken, friend.getId()));
						friendList.add(friend);
					}
				}
			}
		}

		FBFriends friends = new FBFriends();
		friends.setData(friendList);

		return friends;
	}
	
	public List<FBFriend> getFBFriends(Long userId){
		List<FBFriend> fbFriends = new ArrayList<FBFriend>();
		User user = getUserFromDB(userId);
		for (FBFriend fbFriend : user.getFriends().getData()) {
			User friend = getUserFromDB(fbFriend.getId());
			if(friend != null) {
				fbFriends.add(fbFriend);
			}
		}
		return fbFriends;
	}

//	public FBMovies fetchLikedMovies(final String accessToken, final String userId) {
//		final String fields = "movies";
//		HttpHeaders headers = new HttpHeaders();
//		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://graph.facebook.com/me")
//				.queryParam("access_token", accessToken).queryParam("user_id", userId).queryParam("fields", fields);
//		User user = restTemplate.getForObject(uriBuilder.toUriString(), User.class);
//
//		List<FBMovie> movieList = new ArrayList<>();
//
//		if (user.getMovies() != null) {
//
//			for (FBMovie mov : user.getMovies().getData()) {
//				movieList.add(mov);
//			}
//
//			while (user.getMovies().getPaging().getNext() != null) {
//				FBMovies pagedMovies = restTemplate.getForObject(user.getMovies().getPaging().getNext(), FBMovies.class);
//				user.setMovies(pagedMovies);
//				for (FBMovie mov : user.getMovies().getData()) {
//					movieList.add(mov);
//				}
//			}
//		}
//
//		FBMovies movies = new FBMovies();
//		movies.setData(movieList);
//		return movies;
//	}

//	public FBMovies fetchLikedMovies(final String accessToken, final String userId) {
//		final String fields = "movies";
//		HttpHeaders headers = new HttpHeaders();
//		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://graph.facebook.com/" + userId)
//				.queryParam("access_token", accessToken).queryParam("fields", fields);
//		User user = restTemplate.getForObject(uriBuilder.toUriString(), User.class);
//
//		List<FBMovie> movieList = new ArrayList<>();
//
//		if (user.getMovies() != null) {
//
//			for (FBMovie mov : user.getMovies().getData()) {
//				movieList.add(mov);
//			}
//
//			while (user.getMovies().getPaging().getNext() != null) {
//				FBMovies pagedMovies = restTemplate.getForObject(user.getMovies().getPaging().getNext(), FBMovies.class);
//				user.setMovies(pagedMovies);
//				for (FBMovie mov : user.getMovies().getData()) {
//					movieList.add(mov);
//				}
//			}
//		}
//
//		FBMovies movies = new FBMovies();
//		movies.setData(movieList);
//		return movies;
//	}

//	public FBMovies fetchFriendsMovies(final String accessToken, final String friendId) {
//		final String fields = "movies";
//		HttpHeaders headers = new HttpHeaders();
//		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://graph.facebook.com/" + friendId)
//				.queryParam("access_token", accessToken).queryParam("fields", fields);
//		User user = restTemplate.getForObject(uriBuilder.toUriString(), User.class);
//
//		List<FBMovie> movieList = new ArrayList<>();
//
//		if (user.getMovies() != null) {
//
//			for (FBMovie mov : user.getMovies().getData()) {
//				movieList.add(mov);
//			}
//
//			while (user.getMovies().getPaging().getNext() != null) {
//				FBMovies pagedMovies = restTemplate.getForObject(user.getMovies().getPaging().getNext(), FBMovies.class);
//				user.setMovies(pagedMovies);
//				for (FBMovie mov : user.getMovies().getData()) {
//					movieList.add(mov);
//				}
//			}
//		}
//		
//		FBMovies movies = new FBMovies();
//		movies.setData(movieList);
//		return movies;
//	}

	public User getUserFromDB(Long id) {
		User user = null;
		try {
			Optional<User> userTmp = userRepository.findById(id);
			if(userTmp.isPresent()) {
				user = userTmp.get();
			}
		} catch (Error err) {
			System.err.println(err);
		}

		return user;
	}

}
