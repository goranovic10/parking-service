package com.parking.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParkingController {

    @GetMapping("/parking")
    public String hello() {
        return "Hello Parking Service!";
    }
}
