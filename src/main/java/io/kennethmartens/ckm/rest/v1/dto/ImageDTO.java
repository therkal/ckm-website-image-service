package io.kennethmartens.ckm.rest.v1.dto;

import io.kennethmartens.ckm.data.entities.CameraInformation;
import io.kennethmartens.ckm.data.entities.CameraSettings;
import io.kennethmartens.ckm.data.entities.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDTO  {

    // ToDo: Reusable props
    private String title;
    private String imageId;
    private Date takenAt;
    private Date uploadedAt;
    private GeoLocation geoLocation;
    private CameraInformation cameraInformation;
    private CameraSettings cameraSettings;

    private URI imageResource;

}
