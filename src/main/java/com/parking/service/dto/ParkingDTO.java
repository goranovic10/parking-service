package com.parking.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingDTO {
  private String name;
  private String freeSpace;
  private String price;
  private boolean criticalCapacity;
  private String distance;

  public ParkingDTO(ParkingDTO parkingDTO) {
    this.name = parkingDTO.getName();
    this.freeSpace = parkingDTO.getFreeSpace();
    this.criticalCapacity = parkingDTO.isCriticalCapacity();
    this.price = parkingDTO.getPrice();
    this.distance = parkingDTO.getDistance();
  }
}
