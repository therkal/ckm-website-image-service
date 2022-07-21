package io.kennethmartens.ckm.rest.v1.resources;

import io.kennethmartens.ckm.data.entities.Image;
import io.kennethmartens.ckm.rest.v1.forms.ImageForm;
import io.kennethmartens.ckm.services.ImageService;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.MultipartForm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Slf4j
@Path(ImageResource.API_IMAGES)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImageResource {
    public static final String API_IMAGES = "/images";

    private final ImageService service;

    public ImageResource(ImageService service) {
        this.service = service;
    }

    @GET
    @Path("/{id}")
    @Produces("image/jpg")
    public Uni<byte[]> getImage(String id) {
        return service.getImageById(id);
    }

    @GET
    public Uni<List<Image>> getAll() {
        return service.findAll();
    }

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public Uni<Image> storeImage(@MultipartForm ImageForm imageForm) {
        log.info("POST request to {} with {}", API_IMAGES, imageForm);
        return service.persist(imageForm);
    }
}
