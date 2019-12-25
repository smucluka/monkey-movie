package hr.fer.dm.MyMovieApp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import hr.fer.dm.MyMovieApp.model.Movie;

public interface MovieRepository extends MongoRepository<Movie, String>{

	List<Movie> findByTitleIgnoreCase(String title);
	List<Movie> findByIdIn(List<String> ids);
	
}
