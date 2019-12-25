package hr.fer.dm.MyMovieApp.service;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import hr.fer.dm.MyMovieApp.model.OmdbMovie;


@Service
public class OmdbService {

	RestTemplate restTemplate = new RestTemplate();
	private final String api_key_omdb = "b78d05ae";
	
	
	public OmdbMovie getMovieByImdbId(String imdbID) {
		
		OmdbMovie movie = new OmdbMovie();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://www.omdbapi.com")
				.queryParam("apikey", api_key_omdb).queryParam("i", imdbID);
		movie = restTemplate.getForObject(uriBuilder.toUriString(), OmdbMovie.class);

		return movie;
	}
	
}
