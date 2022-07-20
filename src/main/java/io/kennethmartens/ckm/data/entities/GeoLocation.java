package io.kennethmartens.ckm.data.entities;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GeoLocation {
    private Double lat;
    private Double lon;
    private String altitude;
}
