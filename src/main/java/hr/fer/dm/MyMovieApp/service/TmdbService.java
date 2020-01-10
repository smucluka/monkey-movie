package hr.fer.dm.MyMovieApp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import hr.fer.dm.MyMovieApp.model.Genre;
import hr.fer.dm.MyMovieApp.model.Movie;
import hr.fer.dm.MyMovieApp.model.TmdbMovie;
import hr.fer.dm.MyMovieApp.repository.MovieRepository;

@Service
public class TmdbService {
	
	@Autowired
	MovieRepository movieRepository;
	
	private final String api_key_tmdb = "637554c41022b56ae69b09f7b828bfb2";
	RestTemplate restTemplate = new RestTemplate();

	public List<Movie> getMoviesByTitle(String movieTitle, boolean returnOne, boolean returnOnePage) {
		
		movieTitle = prepareTitle(movieTitle);
		
		List<Movie> movies = new ArrayList<>();

		movieTitle = prepareTitle(movieTitle);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString("https://api.themoviedb.org/3/search/movie").queryParam("api_key", api_key_tmdb)
				.queryParam("query", movieTitle.trim());
		String uri = uriBuilder.toUriString();
		uri += "&query='" + movieTitle + "'";
		String json = restTemplate.getForObject(uri, String.class);

		JSONObject obj1 = new JSONObject(json);

		int total_pages = obj1.getInt("total_pages");

		if (total_pages >= 1) {
			JSONArray arr = obj1.getJSONArray("results");
			for (int i = 0; i < arr.length(); i++) {
				Long id = Long.valueOf(arr.getJSONObject(i).getInt("id"));

				Object title = arr.getJSONObject(i).get("title");
				Object overview = arr.getJSONObject(i).get("overview");
				Object poster_path = arr.getJSONObject(i).get("poster_path");

				
				Optional<Movie> mov = movieRepository.findById(id);
				Movie newMovie = null;
				if(mov.isPresent()) {
					newMovie = mov.get();
				}else {
					newMovie = new Movie();
				}
				
				newMovie.setId(Long.valueOf(id));
				newMovie.setTitle(title.toString());
				newMovie.setOverview(overview.toString());
				newMovie.setPoster_path("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + poster_path.toString());

				movies.add(newMovie);
				
				if(returnOne) {
					return movies;
				}
			}
			if(returnOnePage) {
				return movies;
			}
			for (int index = 2; index <= total_pages; index++) {
				UriComponentsBuilder uriBuilder2 = UriComponentsBuilder
						.fromUriString("https://api.themoviedb.org/3/search/movie").queryParam("api_key", api_key_tmdb);
				String uriNew = uriBuilder2.toUriString();
				uriNew += "&query='" + movieTitle + "'&page="+index ;
				String json2 = restTemplate.getForObject(uriNew, String.class);

				JSONObject obj2 = new JSONObject(json2);

				arr = obj2.getJSONArray("results");
				for (int i = 0; i < arr.length(); i++) {
					Long id = Long.valueOf(arr.getJSONObject(i).getInt("id"));

					Object title = arr.getJSONObject(i).get("title");
					Object overview = arr.getJSONObject(i).get("overview");
					Object poster_path = arr.getJSONObject(i).get("poster_path");


					Optional<Movie> mov = movieRepository.findById(id);
					Movie newMovie = null;
					if(mov.isPresent()) {
						newMovie = mov.get();
					}else {
						newMovie = new Movie();
					}
					
					newMovie.setId(Long.valueOf(id));
					newMovie.setTitle(title.toString());
					newMovie.setOverview(overview.toString());
					newMovie.setPoster_path("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + poster_path.toString());

					movies.add(newMovie);
				}

			}
		}

		return movies;
	}
		
	public List<Movie> getPopularMovies() {
		
		List<Movie> movies = new ArrayList<>();

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString("https://api.themoviedb.org/3/movie/popular").queryParam("api_key", api_key_tmdb);
		String json = restTemplate.getForObject(uriBuilder.toUriString(), String.class);

		JSONObject obj1 = new JSONObject(json);

		int total_pages = obj1.getInt("total_pages");
		int cnt=0;
		if (total_pages >= 1) {
			JSONArray arr = obj1.getJSONArray("results");
			for (int i = 0; i < arr.length(); i++) {
				Long id = Long.valueOf(arr.getJSONObject(i).getInt("id"));

				Object title = arr.getJSONObject(i).get("title");
				Object overview = arr.getJSONObject(i).get("overview");
				Object poster_path = arr.getJSONObject(i).get("poster_path");

				Optional<Movie> mov = movieRepository.findById(id);
				Movie newMovie = null;
				if(mov.isPresent()) {
					newMovie = mov.get();
				}else {
					newMovie = new Movie();
				}
				
				newMovie.setId(Long.valueOf(id));
				newMovie.setTitle(title.toString());
				newMovie.setOverview(overview.toString());
				newMovie.setPoster_path("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + poster_path.toString());
				
				movies.add(newMovie);
				cnt++;
				if(cnt>=18) break;
			}
		}

		return movies;
	}
	
	public TmdbMovie getMovieByTmdbId(Long id) {
		TmdbMovie movieDetailed = new TmdbMovie();

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://api.themoviedb.org/3/movie/" + id)
				.queryParam("api_key", api_key_tmdb).queryParam("language", "en-US");
		String uri = uriBuilder.toUriString();
		try {
			String json = restTemplate.getForObject(uri, String.class);
			JSONObject obj = new JSONObject(json);

			Object imdb_id = obj.get("imdb_id");
			Object title = obj.get("title");
			Object overview = obj.get("overview");
			Object tagline = obj.get("tagline");
			Object budget = obj.get("budget");
			Object original_language = obj.get("original_language");
			Object vote_average = obj.get("vote_average");
			Object release_date = obj.get("release_date");
			Object poster_path = obj.get("poster_path");

			List<Genre> genresList = new ArrayList<>();
			JSONArray genres = obj.getJSONArray("genres");
			for (int i = 0; i < genres.length(); i++) {
				String genreId = Integer.toString(genres.getJSONObject(i).getInt("id"));
				String genreName = genres.getJSONObject(i).getString("name");
				genresList.add(new Genre(genreId, genreName));
			}

			movieDetailed.setId(id);
			movieDetailed.setImdb_id(imdb_id.toString());
			movieDetailed.setTitle(title.toString());
			movieDetailed.setOverview(overview.toString());
			movieDetailed.setTagline(tagline.toString());
			movieDetailed.setBudget(budget.toString());
			movieDetailed.setOriginal_language(original_language.toString());
			movieDetailed.setVote_average(vote_average.toString());
			movieDetailed.setRelease_date(release_date.toString());
			movieDetailed.setPoster_path("https://image.tmdb.org/t/p/w300_and_h450_bestv2" + poster_path.toString());
			movieDetailed.setGenres(genresList);

		} catch (Exception err) {

		}

		return movieDetailed;
	}

	private String prepareTitle(String movieTitle) {
		String s1 = movieTitle.substring(0, 1).toUpperCase();
		String nameCapitalized = s1 + movieTitle.substring(1);
		return nameCapitalized.trim();
	}

}
