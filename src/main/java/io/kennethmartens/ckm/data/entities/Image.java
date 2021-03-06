package io.kennethmartens.ckm.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image extends ReactivePanacheMongoEntity {

    private String imageId;
    private Date takenAt;
    private Date uploadedAt;
    private GeoLocation geoLocation;
    private CameraInformation cameraInformation;
    private CameraSettings cameraSettings;

    @JsonIgnore
    private String path;

}
