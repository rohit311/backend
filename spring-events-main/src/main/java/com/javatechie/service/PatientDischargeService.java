package com.javatechie.service;

import com.javatechie.event.PatientDischargeEvent;
import com.javatechie.handler.BillingServiceHandler;
import com.javatechie.handler.HousekeepingServiceHandler;
import com.javatechie.handler.MedicalRecordsServiceHandler;
import com.javatechie.handler.NotificationServiceHandler;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PatientDischargeService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final Logger LOGGER = LoggerFactory.getLogger(PatientDischargeService.class);

    public String dischargePatient(String patientId, String patientName) {

        LOGGER.info("patient discharge process initiated {} ", patientName);
//        billingService.processBill();
//        medicalRecordsService.updatePatientHistory();
//        housekeepingService.cleanAndAssign();
//        notificationService.notifyPatients();
        //publish an event
        eventPublisher.publishEvent(new PatientDischargeEvent(this, patientId, patientName));

        LOGGER.info("patient discharge process completed {} ", patientName);
        return "Patient " + patientName + " with ID " + patientId + " discharged successfully!";
    }
}
