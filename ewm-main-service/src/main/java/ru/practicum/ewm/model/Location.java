package ru.practicum.ewm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Location {

    @Column(name = "location_lat", nullable = false)
    private Double lat;

    @Column(name = "location_lon", nullable = false)
    private Double lon;
}

