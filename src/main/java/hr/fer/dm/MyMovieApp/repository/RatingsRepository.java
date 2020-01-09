package hr.fer.dm.MyMovieApp.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import hr.fer.dm.MyMovieApp.model.Ratings;

public interface RatingsRepository extends MongoRepository<Ratings, String>{
	
	@Query(fields="{'movieId' : 1, 'rating' : 1}")
	List<Ratings> findByUserId(Long userId);
	@Query(fields="{'movieId' : 1, 'rating' : 1}")
	List<Ratings> findByUserIdIn(List<Long> userIds);
	@Query(fields="{'userId' : 1}")
	List<Ratings> findByMovieIdAndRating(Long movieId, Double rating);
	@Query(fields="{}")
	List<Ratings> findByUserIdAndMovieId(Long userId, Long movieId);
}

