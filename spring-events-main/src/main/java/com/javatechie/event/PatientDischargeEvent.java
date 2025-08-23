package com.javatechie.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

public class PatientDischargeEvent extends ApplicationEvent {

    private String patientId;

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
    private String patientName;

    public PatientDischargeEvent(Object source,String patientId,String patientName) {
        super(source);
        this.patientId=patientId;
        this.patientName=patientName;
    }
}
