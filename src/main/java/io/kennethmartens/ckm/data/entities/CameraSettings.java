package io.kennethmartens.ckm.data.entities;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CameraSettings {
    private String shutterSpeed;
    private String aperture;
    private String iso;
    private String focalLength;
}
