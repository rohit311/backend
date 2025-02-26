package com.example.weatherapi.model;

import java.io.Serializable;

import lombok.*;

@NoArgsConstructor
@Setter
@Getter
@Data
public class Weather implements Serializable{
  public String dateTime;
  public String description;
  public String address;
  public double temperature;

  public Weather(String dateTime, String description, String address, double temperature) {
    this.dateTime = dateTime;
    this.description = description;
    this.address = address;
    this.temperature = temperature;
  }
}
