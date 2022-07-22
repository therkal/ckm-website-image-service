package io.kennethmartens.ckm.rest.v1.resources;

import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/ping")
@Produces(MediaType.TEXT_HTML)
public class PingResource {

    @GET
    public Uni<String> ping() {
        return Uni.createFrom()
                .item("Application up and running");
    }
}
