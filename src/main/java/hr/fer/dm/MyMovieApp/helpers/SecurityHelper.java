package hr.fer.dm.MyMovieApp.helpers;

import java.security.Principal;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

@Component
public class SecurityHelper {
	
	public boolean isAuthenticatedUser(Principal principal){
		OAuth2Authentication currentUser = (OAuth2Authentication) principal;
		
		if(currentUser != null && currentUser.isAuthenticated()) {
			return true;
		}
		
		return false;
	}

}
