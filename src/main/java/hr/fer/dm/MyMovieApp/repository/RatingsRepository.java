package hr.fer.dm.MyMovieApp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import hr.fer.dm.MyMovieApp.model.Ratings;

public interface RatingsRepository extends MongoRepository<Ratings, String>{

	List<Ratings> findByUserId(String userId);
	List<Ratings> findByMovieIdAndRating(String movieId, String rating);
}

