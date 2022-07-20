package io.kennethmartens.ckm.data.entities;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class Image extends ReactivePanacheMongoEntity {

    private Date takenAt;
    private Date uploadedAt;
    private GeoLocation geoLocation;
    private CameraInformation cameraInformation;
    private CameraSettings cameraSettings;

}
