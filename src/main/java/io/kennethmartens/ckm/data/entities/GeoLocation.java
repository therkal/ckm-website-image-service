package io.kennethmartens.ckm.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GeoLocation {
    private Double lat;
    private Double lon;
    private String altitude;
}
