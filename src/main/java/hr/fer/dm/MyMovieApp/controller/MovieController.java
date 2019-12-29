package hr.fer.dm.MyMovieApp.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import hr.fer.dm.MyMovieApp.helpers.SecurityHelper;
import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.MovieDetailed;
import hr.fer.dm.MyMovieApp.service.MovieService;
import hr.fer.dm.MyMovieApp.service.RecommendationService;

@Controller
public class MovieController {

	@Autowired 
    HttpSession session;
	
	@Autowired
	SecurityHelper securityHelper;
	@Autowired
	MovieService movieService;
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
				movies = movieService.getMovies(movieTitle);
				model.addAttribute("movies", movies);
				model.addAttribute("watchedMovies", movieService.getWatchedMoviesIds((String) session.getAttribute("userId")));
				model.addAttribute("watchList", movieService.getMovieWatchListIds((String) session.getAttribute("userId")));
				model.addAttribute("firsttime", "n");
			}
			return "movie_search";
		}

		return "redirect:/";
	}

	
	@RequestMapping(value = "/movies/details", method = RequestMethod.GET)
	public String getMovieDetails(@RequestParam("id") String id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		MovieDetailed movie;
		if (isAuthenticatedUser) {
			if (!id.isEmpty()) {
				movie = movieService.getMovieDetails(id);
				model.addAttribute("movie", movie);
				model.addAttribute("watched", movieService.getWatchedMoviesIds((String) session.getAttribute("userId")).contains(id));
				model.addAttribute("watchlisted", movieService.getMovieWatchListIds((String) session.getAttribute("userId")).contains(id));
			}
			return "movie_details";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watched", method = RequestMethod.GET)
	public String getWatchedMovies(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			model.addAttribute("watchedMovies", movieService.getWatchedMovies((String) session.getAttribute("userId")));
			return "movies_watched";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watchlist", method = RequestMethod.GET)
	public String getMovieWatchList(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			List<Movie> movies = movieService.getMovieWatchList((String) session.getAttribute("userId"));
			model.addAttribute("movieWatchList", movies);
			return "movie_watch_list";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/recommendation", method = RequestMethod.GET)
	public String getRecommendation(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			model.addAttribute("movies", recommendationService.getRecommendation((String) session.getAttribute("userId")));
			return "recommendation";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/recommendation/smart", method = RequestMethod.GET)
	public ResponseEntity<Movie> getSmartMovie(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			return new ResponseEntity<Movie>(recommendationService.getRecommendation((String) session.getAttribute("userId")).get(0), HttpStatus.OK);
		}
		return new ResponseEntity<Movie>(HttpStatus.FORBIDDEN);
	}
	
	//
	//REST
	//
	@RequestMapping(value = "/movies/watched/put", method = RequestMethod.GET)
	public String putMovieOnWatchedList(@RequestParam("id") String id, @RequestParam("rating") Integer rating, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		if (isAuthenticatedUser) {
			if (!id.isEmpty()) {
				movieService.removeMovieFromWatchList((String) session.getAttribute("userId"), id);
				movieService.addMovieToWatched((String) session.getAttribute("userId"), id, rating);
			}
			return "redirect:/movies/watched";
		}
		
		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watched/remove", method = RequestMethod.GET)
	public String removeMovieFromWatched(@RequestParam("id") String id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			movieService.removeMovieFromWatched((String) session.getAttribute("userId"), id);
			return "redirect:/movies/watched";
		}

		return "redirect:/";
	}
	
	@RequestMapping(value = "/movies/watchlist/put", method = RequestMethod.GET)
	public String putMovieOnWatchList(@RequestParam("id") String id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		if (isAuthenticatedUser) {
			if (!id.isEmpty()) {
				movieService.addMovieToWatchList((String) session.getAttribute("userId"), id);
			}
			return "redirect:/movies/watchlist";
		}
		
		return "redirect:/";
	}
	

	
	@RequestMapping(value = "/movies/watchlist/remove", method = RequestMethod.GET)
	public String removeMovieFromWatchList(@RequestParam("id") String id, Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);

		if (isAuthenticatedUser) {
			movieService.removeMovieFromWatchList((String) session.getAttribute("userId"), id);
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
