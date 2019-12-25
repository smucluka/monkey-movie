package hr.fer.dm.MyMovieApp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import hr.fer.dm.MyMovieApp.model.User;

public interface UserRepository extends MongoRepository<User, String>{

}
