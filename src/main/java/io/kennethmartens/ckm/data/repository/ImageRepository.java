package io.kennethmartens.ckm.repository;

import io.kennethmartens.ckm.entities.Image;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ImageRepository implements ReactivePanacheMongoRepository<Image> {
}
