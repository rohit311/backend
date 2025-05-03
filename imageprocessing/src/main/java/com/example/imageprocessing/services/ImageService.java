package com.example.imageprocessing.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.imageprocessing.entities.Image;
import com.example.imageprocessing.repositories.ImageRepository;
import com.example.imageprocessing.utils.ImageUtility;

@Component
public class ImageService {
  @Autowired
  private ImageRepository imageRepository;

  public String uploadImage(MultipartFile imageFile) throws IOException {
        var imageToSave = Image.builder()
                .name(imageFile.getOriginalFilename())
                .type(imageFile.getContentType())
                .picByte(ImageUtility.compressImage(imageFile.getBytes()))
                .build();
        imageRepository.save(imageToSave);
        return "file uploaded successfully : " + imageFile.getOriginalFilename();
    }

  public Image loadImageByName(String imageName) {
    Image imageRec = imageRepository.findByName(imageName);

    if (imageRec == null) {
      throw new UsernameNotFoundException("Image Not Found with name: " + imageName);
    }

    return imageRec;
  }
}
