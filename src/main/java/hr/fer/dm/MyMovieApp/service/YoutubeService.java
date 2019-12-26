package hr.fer.dm.MyMovieApp.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import hr.fer.dm.MyMovieApp.model.YouTubeVideo;

@Service
public class YoutubeService {

    private static final long MAX_SEARCH_RESULTS = 5;
    
    public YouTubeVideo fetchVideosByQuery(String queryTerm) {
        List<YouTubeVideo> videos = new ArrayList<YouTubeVideo>();
    
        try {
            //instantiate youtube object
            YouTube youtube = getYouTube();
    
            //define what info we want to get
            YouTube.Search.List search = youtube.search().list("id,snippet");
            
            //set our credentials
            String apiKey = "AIzaSyDs-B-Ps0elwryPAvQbNZj7vNc3g5WixlI";
            search.setKey(apiKey);
            
            //set the search term
            search.setQ(queryTerm);
    
            //we only want video results
            search.setType("video");
    
            //set the fields that we're going to use
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            
            //set the max results
            search.setMaxResults(MAX_SEARCH_RESULTS);
            
            DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
            
            //perform the search and parse the results
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                for (SearchResult result : searchResultList) {
                    YouTubeVideo video = new YouTubeVideo();
                    video.setTitle(result.getSnippet().getTitle());
                    video.setUrl(buildVideoUrl(result.getId().getVideoId()));
                    video.setThumbnailUrl(result.getSnippet().getThumbnails().getDefault().getUrl());
//                    video.setDescription(result.getSnippet().getDescription());
//                    video.setChannelName(result.getSnippet().getChannelTitle());
            	   
                    //parse the date
//                    DateTime dateTime = result.getSnippet().getPublishedAt();
//                    Date date = new Date(dateTime.getValue());
//                    String dateString = df.format(date);
//                    video.setPublishDate(dateString);
                       
                    videos.add(video);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
        
        
        return videos.get(0);
    }
	
	private YouTube getYouTube() {
	    YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), 
	    (request) -> {}).setApplicationName("DruMre").build();
	  
	    return youtube;
	}
	
    private String buildVideoUrl(String videoId) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://www.youtube.com/embed/");
        builder.append(videoId);
    	
        return builder.toString();
    }
	
}
