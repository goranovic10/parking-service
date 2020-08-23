package com.parking.service.jobs;

import com.parking.service.service.ParkingService;
import com.parking.service.utils.ParkingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingScheduler {

  private final ParkingService parkingService;

  @Scheduled(fixedRate = 86400000)
  public void saveParkings() throws Exception {
    parkingService.saveParkings();
  }
}
