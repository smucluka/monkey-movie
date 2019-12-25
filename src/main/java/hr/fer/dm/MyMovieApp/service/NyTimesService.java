package hr.fer.dm.MyMovieApp.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import hr.fer.dm.MyMovieApp.model.NyTimesLink;
import hr.fer.dm.MyMovieApp.model.NyTimesReview;

@Service
public class NyTimesService {
	
	private final String apiKey = "l4M9nPoPns90WOvRf4E9e0ei2EP93qvP";
	RestTemplate restTemplate = new RestTemplate();
	
	public NyTimesReview getReviewByQuery(String query) {
		
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString("https://api.nytimes.com/svc/movies/v2/reviews/search.json").queryParam("api-key", apiKey)
				.queryParam("query", query);
		
		String uri = uriBuilder.toUriString();
		String json = restTemplate.getForObject(uri, String.class);
		JSONObject obj1 = new JSONObject(json);
		JSONArray arr = obj1.getJSONArray("results");
		
		for (int i = 0; i < arr.length(); i++) {
			String displayTitle = arr.getJSONObject(i).getString("display_title");
			
			if (displayTitle.equalsIgnoreCase(query)) {
				
				
				NyTimesReview review = new NyTimesReview();
				
				try {

					review.setDisplay_title(displayTitle);
					review.setMpaa_rating(arr.getJSONObject(i).getString("mpaa_rating"));
					review.setCritics_pick(Integer.toString(arr.getJSONObject(i).getInt("critics_pick")));
					review.setByline(arr.getJSONObject(i).getString("byline"));
					review.setHeadline(arr.getJSONObject(i).getString("headline"));
					review.setSummary_short(arr.getJSONObject(i).getString("summary_short"));
					review.setPublication_date(arr.getJSONObject(i).getString("publication_date"));
					review.setOpening_date(arr.getJSONObject(i).getString("opening_date"));
					review.setDate_updated(arr.getJSONObject(i).getString("date_updated"));
					NyTimesLink link = new NyTimesLink();
					JSONObject temp = arr.getJSONObject(i).getJSONObject("link");
					link.setType(temp.getString("type"));
					link.setUrl(temp.getString("url"));
					link.setSuggested_link_text(temp.getString("suggested_link_text"));
					review.setLink(link);
					//review.setMultimedia(arr.getJSONObject(i).getString("multimedia"));
				
					return review;
				}
				catch (Exception e) {
					return null;
				}
				
			}

		}
		
		return null;
	}

}
