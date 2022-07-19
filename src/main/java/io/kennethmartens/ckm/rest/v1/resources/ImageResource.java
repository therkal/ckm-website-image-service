package io.kennethmartens.ckm.rest.v1.resources;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Path(ImageResource.API_IMAGES)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImageResource {
    public static final String API_IMAGES = "/images";

    @GET
    public String hello() {
        return "Running";
    }

}
