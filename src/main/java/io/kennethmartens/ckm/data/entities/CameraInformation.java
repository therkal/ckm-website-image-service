package io.kennethmartens.ckm.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CameraInformation {
    private String cameraMake;
    private String cameraModel;
    private String lensSpecs;
}
