package hr.fer.dm.MyMovieApp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import hr.fer.dm.MyMovieApp.model.Soundtrack;

@Service
public class SoundrtackService {
	
	RestTemplate restTemplate = new RestTemplate();
	
	public List<Soundtrack> getSoundtracksByMovieTitle(String movieTitle){
		
		String yourUrl = "https://itunes.apple.com/search?term=" + movieTitle + "&media=movie&entity=musicTrack&limit=200";
		String json = restTemplate.getForObject(yourUrl, String.class);
		JSONObject obj1 = new JSONObject(json);
		JSONArray arr = obj1.getJSONArray("results");
		
		List<Soundtrack> soundtrackList = new ArrayList<>();
		
		for (int i = 0; i < arr.length(); i++) {
			String wrapperType = arr.getJSONObject(i).getString("wrapperType");
			if (!wrapperType.equals("track"))
            {
                continue;
            }
			
			try {
				Soundtrack track = new Soundtrack();

				track.setTrackName(arr.getJSONObject(i).getString("trackName"));
				track.setArtistName(arr.getJSONObject(i).getString("artistName"));
				track.setCollectionName(arr.getJSONObject(i).getString("collectionName"));
				track.setTrackViewUrl(arr.getJSONObject(i).getString("trackViewUrl"));
				track.setGenreName(arr.getJSONObject(i).getString("primaryGenreName"));
				track.setPreviewUrl(arr.getJSONObject(i).getString("previewUrl"));

				//search nam je vratio ime izvođača isto kao ime filma, sto vjerojatno znači da smo dobili krivi rezultat
				if (track.getArtistName() != null && track.getArtistName().toLowerCase().contains(movieTitle.toLowerCase()))
				{
				    continue;
				}
				if (track.getCollectionName() != null && (!track.getCollectionName().toLowerCase().contains(movieTitle.toLowerCase()) || track.getCollectionName().toLowerCase().equals(movieTitle.toLowerCase()) ) )
				{
				    continue;
				}
				
				

				//ako se naziv te pjesme dosad ne pojavljuje, eliminramo da se dva puta pojavi ista pjesma
//				if (soundtrackList.stream().filter(x -> x.getTrackName().equals(track.getTrackName()))
//				        .findAny().orElse(null) == null)
//				{
//				    soundtrackList.add(track);
//				}
				
				soundtrackList.add(track);
			}
			catch (Exception e) {
				continue;
			}
			
		}
		
		//dodatni filter za filmove koji imaju nastavke(u svom naslovu sadrže ime našeg filma koji searchamo)
		soundtrackList = filterSoundtracks(soundtrackList, movieTitle);
		
		return soundtrackList;
	}
	
	//metoda dobivena proučavanjem rezultata dobivenih sa iTunes apija i koji inače odskaču (false positivi)
	private List<Soundtrack> filterSoundtracks(List<Soundtrack> soundtracksToFilter, String movieTitle){
		if(soundtracksToFilter.size() < 20) {
			return soundtracksToFilter;
		}
		else {
			int i = soundtracksToFilter.size();
			//int previousI = i + 1;
			
			while( i > 1/*soundtracksToFilter.size() >= 20 && previousI != i*/) {
				String collectionName = soundtracksToFilter.get(i -1).getCollectionName();
				if(!(  collectionName.contains(movieTitle + " (") || collectionName.contains(movieTitle + " [")
						|| collectionName.contains(movieTitle + " -") || collectionName.contains("(From \"" + movieTitle) ) ) {
					soundtracksToFilter.remove(i - 1);
				}
				else {
					String[] strThatIsUsualInCorrectResult = new String[] {"Collectors", "Original", "Motion Picture", "Soundtrack", "- The Album", "(From \""};
					if(Arrays.stream(strThatIsUsualInCorrectResult).anyMatch(search -> collectionName.contains(search)) ) {
						//previousI = i - 1;
					}
					else {
						soundtracksToFilter.remove(i - 1);
					}
				}
				
				i--;
			}
		}
		
		return soundtracksToFilter;
	}
	
	

}
