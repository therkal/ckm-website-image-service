package io.kennethmartens.ckm.data.repository;

import io.kennethmartens.ckm.data.entities.Image;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;


import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ImageRepository implements ReactivePanacheMongoRepository<Image> {

}
