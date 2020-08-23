package com.parking.service.controller;

import com.parking.service.dto.ParkingDTO;
import com.parking.service.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ParkingController {

  private ParkingService parkingService;

  @Autowired
  public ParkingController(ParkingService parkingService) {
    this.parkingService = parkingService;
  }

  @GetMapping("/parking")
  public List<ParkingDTO> getParkingSpaces(
      @RequestParam("latitude") Double latitude, @RequestParam("longitude") Double longitude)
      throws Exception {
    return parkingService.getParkingSpaces(latitude, longitude);
  }

  @PutMapping("/parking/save")
  public void saveParkings() throws Exception {
    parkingService.saveParkings();
  }
}
