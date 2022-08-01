package io.kennethmartens.ckm.rest.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadDTO {

    private String id;
    private URI imageResource;

}
