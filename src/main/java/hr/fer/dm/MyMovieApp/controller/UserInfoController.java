package hr.fer.dm.MyMovieApp.controller;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import hr.fer.dm.MyMovieApp.helpers.SecurityHelper;
import hr.fer.dm.MyMovieApp.model.User;
import hr.fer.dm.MyMovieApp.service.UserService;

@Controller
public class UserInfoController {
	
	@Autowired 
    HttpSession session;
	@Autowired
	SecurityHelper securityHelper;
	@Autowired
	UserService userService;
	
	@GetMapping("/profile")
	public String getUserInfo(Principal principal, Model model) {
		boolean isAuthenticatedUser = securityHelper.isAuthenticatedUser(principal);
		
		if(isAuthenticatedUser) {
			String userId = (String) session.getAttribute("userId") ;
			User user = userService.getUserFromDB(userId);
			model.addAttribute("user", user);
			
			return "profile";
		}

		return "redirect:/";
	}
}
