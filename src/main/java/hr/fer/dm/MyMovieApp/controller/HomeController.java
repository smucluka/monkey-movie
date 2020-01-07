package hr.fer.dm.MyMovieApp.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import hr.fer.dm.MyMovieApp.helpers.SecurityHelper;
import hr.fer.dm.MyMovieApp.repository.MovieRepository;
import hr.fer.dm.MyMovieApp.repository.RatingsRepository;
import hr.fer.dm.MyMovieApp.repository.UserRepository;
import hr.fer.dm.MyMovieApp.service.UserService;

@Controller
public class HomeController {
	@Autowired
    UserRepository userRepository;
	@Autowired
    MovieRepository movieRepository;
	@Autowired
    RatingsRepository ratingsRepository;
	@Autowired
	SecurityHelper securityHelper;
	@Autowired
	UserService userService;
	
	//@RequestMapping(value = "/", method = RequestMethod.GET)
	@GetMapping("/")
	public String getHome(Principal principal, Model model) {
		
		model.addAttribute("userCount", userRepository.count());
		model.addAttribute("moviesCount", movieRepository.count());
		model.addAttribute("ratingsCount", ratingsRepository.count());
		return "index";
	}
	
	
	@GetMapping("/privacy_policy")
	public String getPP(Principal principal, Model model) {
		
		return "privacy_policy";
	}
	

}
