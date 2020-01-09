package hr.fer.dm.MyMovieApp.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import hr.fer.dm.MyMovieApp.helpers.SecurityHelper;
import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.MovieDetailed;
import hr.fer.dm.MyMovieApp.model.User;
import hr.fer.dm.MyMovieApp.service.MovieService;
import hr.fer.dm.MyMovieApp.service.RecommendationService;
import hr.fer.dm.MyMovieApp.service.TmdbService;
import hr.fer.dm.MyMovieApp.service.UserService;

@Controller
public class MovieController {

	@Autowired 
    HttpSession session;
	@Autowired
	SecurityHelper securityHelper;
	@Autowired
	MovieService movieService;
	@Autowired
	TmdbService tmdbService;
	@Autowired
	UserService userService;
	@Autowired
	RecommendationService recommendationService;

	@RequestMapping(value = "/movies/search", method = RequestMethod.GET)
	public String getMovies(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			model.addAttribute("firsttime", "y");
			return "movie_search";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/searched", method = RequestMethod.GET)
	public String searchMovies(@RequestParam("movieTitle") String movieTitle, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		List<Movie> movies;
		if (isAuthenticatedUser) {
			if (!movieTitle.isEmpty()) {
				movies = movieService.getMovies(movieTitle, false, false);
				model.addAttribute("movies", movies);
				model.addAttribute("watchedMovies", movieService.getWatchedMoviesIds((Long) session.getAttribute("userId")));
				model.addAttribute("watchList", movieService.getMovieWatchListIds((Long) session.getAttribute("userId")));
				model.addAttribute("firsttime", "n");
			}
			return "movie_search";
		}

		return "redirect:/";
	}

	
	@RequestMapping(value = "/movies/details", method = RequestMethod.GET)
	public String getMovieDetails(@RequestParam("id") Long id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		MovieDetailed movie;
		if (isAuthenticatedUser) {
			if (id != null) {
				movie = movieService.getMovieDetails(id);
				model.addAttribute("movie", movie);
				model.addAttribute("watched", movieService.getWatchedMoviesIds(((Long) session.getAttribute("userId"))).contains(id));
				model.addAttribute("watchlisted", movieService.getMovieWatchListIds(((Long) session.getAttribute("userId"))).contains(id));
			}
			return "movie_details";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watched", method = RequestMethod.GET)
	public String getWatchedMovies(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			model.addAttribute("watchedMovies", movieService.getWatchedMovies(((Long) session.getAttribute("userId"))));
			return "movies_watched";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watchlist", method = RequestMethod.GET)
	public String getMovieWatchList(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			List<Movie> movies = movieService.getMovieWatchList(((Long) session.getAttribute("userId")));
			model.addAttribute("movieWatchList", movies);
			return "movie_watch_list";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/recommendation", method = RequestMethod.GET)
	public String getRecommendation(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			Long userId = ((Long) session.getAttribute("userId"));
			User user = userService.getUserFromDB(userId);
			model.addAttribute("user", user);
			model.addAttribute("friends", userService.getFBFriends(userId));
			return "recommendation";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/popular", method = RequestMethod.GET)
	public String getPopular(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			model.addAttribute("popularMovies", movieService.getPopularMovies());
			model.addAttribute("watchedMovies", movieService.getWatchedMoviesIds(((Long) session.getAttribute("userId"))));
			model.addAttribute("watchList", movieService.getMovieWatchListIds(((Long) session.getAttribute("userId"))));
			return "popular";
		}

		return "redirect:/";
	}

	//
	//REST
	//
	@RequestMapping(value = "/movies/recommendation/solo", method = RequestMethod.GET)
	public ResponseEntity<List<Movie>> getSoloRecommendation(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			return new ResponseEntity<List<Movie>>(recommendationService.getRecommendation(((Long) session.getAttribute("userId")), null), HttpStatus.OK);
		}
		return new ResponseEntity<List<Movie>>(HttpStatus.FORBIDDEN);
	}

	@RequestMapping(value = "/movies/recommendation/party", method = RequestMethod.GET)
	public ResponseEntity<List<Movie>> getPartyRecommendation(@RequestParam(value="userIds[]") List<Long> userIds, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			return new ResponseEntity<List<Movie>>(recommendationService.getRecommendation(((Long) session.getAttribute("userId")), userIds), HttpStatus.OK);
		}
		return new ResponseEntity<List<Movie>>(HttpStatus.FORBIDDEN);
	}
	@RequestMapping(value = "/movies/watched/put", method = RequestMethod.GET)
	public String putMovieOnWatchedList(@RequestParam("id") Long id, @RequestParam("rating") Double rating, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		if (isAuthenticatedUser) {
			if (id != null) {
				movieService.removeMovieFromWatchList(((Long) session.getAttribute("userId")), id);
				movieService.addMovieToWatched(((Long) session.getAttribute("userId")), id, rating);
			}
			model.addAttribute("watchedMovies", movieService.getWatchedMovies(((Long) session.getAttribute("userId"))));
			return "redirect:/movies/watched";
		}
		
		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watched/remove", method = RequestMethod.GET)
	public String removeMovieFromWatched(@RequestParam("id") Long id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			movieService.removeMovieFromWatched(((Long) session.getAttribute("userId")), id);
			model.addAttribute("watchedMovies", movieService.getWatchedMovies(((Long) session.getAttribute("userId"))));
			return "redirect:/movies/watched";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watchlist/put", method = RequestMethod.GET)
	public String putMovieOnWatchList(@RequestParam("id") Long id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		if (isAuthenticatedUser) {
			if (id != null) {
				movieService.addMovieToWatchList(((Long) session.getAttribute("userId")), id);
			}
			return "redirect:/movies/watchlist";
		}
		
		return "redirect:/";
	}
	

	
	@RequestMapping(value = "/movies/watchlist/remove", method = RequestMethod.GET)
	public String removeMovieFromWatchList(@RequestParam("id") Long id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			movieService.removeMovieFromWatchList(((Long) session.getAttribute("userId")), id);
			return "redirect:/movies/watchlist";
		}

		return "redirect:/";
	}
	
	
//	@RequestMapping(value = "/movies/liked", method = RequestMethod.GET)
//	public String getLikedMovies(Principal principal, Model model) {
//		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
//
//		if (isAuthenticatedUser) {
//			model.addAttribute("likedMovies", movieService.getLikedMovies((OAuth2Authentication) principal));
//			return "liked_movies";
//		}
//
//		return "redirect:/";
//	}
	
	/*
	 * Details
	 */
}
