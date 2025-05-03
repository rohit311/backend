package com.example.imageprocessing.repositories;

import org.springframework.data.repository.CrudRepository;

import com.example.imageprocessing.entities.User;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, Integer> {
  User findByEmail(String email);
  boolean existsByEmail(String username);
}