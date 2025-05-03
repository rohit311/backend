package com.example.imageprocessing.entities;

import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity // This tells Hibernate to make a table out of this class
public class User {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer id;

  @Email
  private String email;

  @NotBlank
  private String password;
  private String fullName;
  private Date createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Date updatedAt;

  private String jwtToken;

  public Integer getId() {
    return id;
  }

  public String getEmail() {
      return email;
  }

  public String getPassword() {
    return password;
  }

  public String getFullName() {
      return fullName;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public String getJwtToken() {
    return jwtToken;
  }

  public User setId(Integer id) {
    this.id = id;
    return this;
  }

  public User setFullName(String fullName) {
    this.fullName = fullName;
    return this;
  }

  @Email
  public User setEmail(String email) {
    this.email = email;
    return this;
  }

  @NotBlank
  public User setPassword(String password) {
    this.password = password;
    return this;
  }

  public User setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public User setJwtToken(String jwtToken) {
    this.jwtToken = jwtToken;
    return this;
  }
}
