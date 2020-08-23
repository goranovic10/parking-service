package com.parking.service.repository;

import com.parking.service.model.ParkingDetails;
import org.springframework.data.repository.CrudRepository;

public interface ParkingRepository extends CrudRepository<ParkingDetails, String> {}
