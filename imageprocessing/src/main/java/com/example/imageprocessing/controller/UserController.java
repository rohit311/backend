package com.example.imageprocessing.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.imageprocessing.entities.User;
import com.example.imageprocessing.security.JwtUtil;
import com.example.imageprocessing.services.UserService;

import jakarta.validation.Valid;



@RestController
public class UserController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserService userServiceImpl;

  @Autowired
  JwtUtil jwtUtils;

  @PostMapping("/register")
  public String createUser(@RequestBody @Valid User payload) {
    System.out.println(payload);

    if (userServiceImpl.existsByEmail(payload.getEmail())) {
      return "Error: Username is already taken!";
    }

    userServiceImpl.registerUser(payload);

    return "User registered successfully!";
  }

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@RequestBody @Valid User payload) {
    System.out.println("post payload "+ payload.getEmail());

    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(payload.getEmail(), payload.getPassword())
        );

        // final UserDetails userDetails = userServiceImpl.loadUserByUsername(payload.getEmail());
        final String jwt = jwtUtils.generateToken(payload.getEmail());

        System.out.println("jwt "+ jwt);
        return ResponseEntity.ok(new User()
                                  .setEmail(payload.getEmail())
                                  .setPassword(payload.getPassword())
                                  .setJwtToken(jwt)
              );
  }
}
