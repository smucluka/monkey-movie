package hr.fer.dm.MyMovieApp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import hr.fer.dm.MyMovieApp.model.MovieDetailed;
import hr.fer.dm.MyMovieApp.model.TmdbMovie;

public interface MovieDetailedRepository extends MongoRepository<MovieDetailed, String>{

}

