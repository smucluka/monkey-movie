package hr.fer.dm.MyMovieApp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import hr.fer.dm.MyMovieApp.model.MovieDetailed;

public interface MovieDetailedRepository extends MongoRepository<MovieDetailed, String>{

}

