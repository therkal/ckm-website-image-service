package io.kennethmartens.ckm.rest.v1.forms;

import lombok.Data;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Data
public class ImageForm {
    @RestForm("image")
    public FileUpload image;
}
