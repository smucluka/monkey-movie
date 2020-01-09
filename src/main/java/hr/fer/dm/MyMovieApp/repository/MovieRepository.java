package hr.fer.dm.MyMovieApp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import hr.fer.dm.MyMovieApp.model.Movie;

public interface MovieRepository extends MongoRepository<Movie, Long>{

	List<Movie> findByTitleIgnoreCase(String title);
	List<Movie> findByIdIn(List<Long> ids);
	List<Movie> findByMovieIdIn(List<Long> movieIds);
	List<Movie> findByMovieId(Long movieId);
	
}
