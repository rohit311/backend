package com.javatechie.handler;

import com.javatechie.event.PatientDischargeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceHandler {

    @Async
    @EventListener
    public void notifyPatients(PatientDischargeEvent event) {
        // Send discharge notification
        System.out.println("Notification Service: Sending discharge notification for patient "
                +event.getPatientName() +" : "+Thread.currentThread().getName());
    }
}
