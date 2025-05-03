package com.example.imageprocessing.repositories;

import org.springframework.data.repository.CrudRepository;

import com.example.imageprocessing.entities.Image;

public interface ImageRepository extends CrudRepository<Image, Integer>{
  Image findByName(String name);

}
