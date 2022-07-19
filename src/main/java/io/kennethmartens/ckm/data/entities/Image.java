package io.kennethmartens.ckm.entities;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.Data;

@Data
public class Image extends ReactivePanacheMongoEntity {

    private String image;
    private GeoLocation geoLocation;

}
