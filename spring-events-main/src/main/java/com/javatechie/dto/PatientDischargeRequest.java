package com.javatechie.dto;

import lombok.Data;

@Data
public class PatientDischargeRequest {

    private String patientId;
    private String patientName;


  public String getPatientId() {
    return this.patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getPatientName() {
    return this.patientName;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }


}
