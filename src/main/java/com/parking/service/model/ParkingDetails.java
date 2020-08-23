package com.parking.service.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingDetails {

    @Id
    private String name;

    private String price;

    private String capacity;
}