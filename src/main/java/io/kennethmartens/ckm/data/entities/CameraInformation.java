package io.kennethmartens.ckm.data.entities;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CameraInformation {
    private String cameraMake;
    private String cameraModel;
    private String lens;
}
