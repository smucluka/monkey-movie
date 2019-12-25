package hr.fer.dm.MyMovieApp.handler;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import hr.fer.dm.MyMovieApp.model.User;
import hr.fer.dm.MyMovieApp.service.UserService;

@Component
public class AuthSuccessHandlerImpl implements ApplicationListener<ApplicationEvent>{

    @Autowired 
    HttpSession session;
	@Autowired
	UserService userService;

	@Override
	public void onApplicationEvent(ApplicationEvent appEvent) {
	    if (appEvent instanceof AuthenticationSuccessEvent) {
	        AuthenticationSuccessEvent event = (AuthenticationSuccessEvent) appEvent;
	        OAuth2Authentication authUser = (OAuth2Authentication) event.getAuthentication();
			User user = userService.saveUser(authUser);
			session.setAttribute("userId", user.getId());
	    }
	}

}