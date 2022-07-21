package io.kennethmartens.ckm.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CameraSettings {
    private String shutterSpeed;
    private String aperture;
    private String iso;
    private String focalLength;
}
