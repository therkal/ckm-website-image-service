package io.kennethmartens.ckm.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.mongodb.MongoTimeoutException;
import io.kennethmartens.ckm.data.entities.CameraInformation;
import io.kennethmartens.ckm.data.entities.CameraSettings;
import io.kennethmartens.ckm.data.entities.GeoLocation;
import io.kennethmartens.ckm.data.entities.Image;
import io.kennethmartens.ckm.data.repository.ImageRepository;
import io.kennethmartens.ckm.rest.v1.forms.ImageForm;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class ImageService {

    @ConfigProperty(name = "ckm.image.filePath")
    String FILE_BASE_PATH;

    private final ImageRepository repository;
    private final Vertx vertx;

    public ImageService(ImageRepository repository, Vertx vertx) {
        this.repository = repository;
        this.vertx = vertx;
    }

    public Uni<Image> getImageMetadata(String id) {
        return repository.find("imageId", id)
                .singleResult()
                // If not found, throw not found exception
                .onFailure()
                .transform(throwable ->
                        new NotFoundException(String.format("Image Metadata for id %1$s not found", id))
                );
    }

    public Uni<byte[]> getImageById(String id) {
        return repository.find("imageId", id).firstResult()
                .map(Image::getPath)
                .map(path -> vertx.fileSystem()
                        .readFileBlocking(path)
                        .getBytes()
                );
    }

    public Uni<List<Image>> findAll() {
        return repository.findAll().list();
    }

    public Uni<Image> persist(ImageForm imageForm) {
        FileUpload upload = imageForm.getImage();
        UUID imageUploadUUID = UUID.randomUUID();
        String composedFilePath = String.format("%1$s/%2$s.jpg", FILE_BASE_PATH, imageUploadUUID);

        return vertx.fileSystem()
                // FileSystem move to new path
                .move(upload.uploadedFile().toString(), composedFilePath)
                // Extract the Image MetaData
                .map(x -> extractImageMetadata(new File(composedFilePath), imageUploadUUID))
                // Persist
                .flatMap(repository::persist)
                .onFailure()
                .transform( x -> {
                    log.error("Exception occurred while processing: {}", x.getMessage());
                    x.printStackTrace();

                    if (x instanceof MongoTimeoutException) {
                        // ToDo: Come up with Error Code.
                        return new InternalServerErrorException("Something went wrong on our side.");
                    }

                    if (x instanceof IOException || x instanceof ImageProcessingException) {
                        return new BadRequestException(x.getMessage());
                    }

                    return new InternalServerErrorException("Something went terribly wrong on our side.");
                })
                .log("Exception occurred during the processing of image. Cleaning up the file")
                .call(x -> vertx.fileSystem().delete(composedFilePath));
    }

    private Image extractImageMetadata(File imageFile, UUID imageId) {
        try {
            log.debug("Extracting MetaData for file {}", imageFile);
            Metadata imageMetadata = ImageMetadataReader.readMetadata(imageFile);

            // Exif IFD0
            log.debug("Extracting IFD0 Data");
            ExifIFD0Directory ifd0Directory = imageMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            String make = ifd0Directory.getString(ExifIFD0Directory.TAG_MAKE);
            String model = ifd0Directory.getString(ExifIFD0Directory.TAG_MODEL);
            log.debug("EXIF IFD0: make {}, model {}", make, model);

            log.debug("Extracting SubIFD Data");
            ExifSubIFDDirectory directory = imageMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date takenAt = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            String lens = directory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL);

            return Image.builder()
                    .imageId(imageId.toString())
                    .path(imageFile.getPath())
                    .takenAt(takenAt)
                    .uploadedAt(new Date())
                    .geoLocation(
                            this.extractGeolocation(
                                    imageMetadata.getFirstDirectoryOfType(GpsDirectory.class)
                            )
                    )
                    .cameraInformation(
                            CameraInformation.builder()
                                    .cameraMake(make)
                                    .cameraModel(model)
                                    .lens(lens)
                                    .build()
                    )
                    .cameraSettings(this.extractCameraInformation(directory))
                    .build();
        } catch (IOException | ImageProcessingException e) {
            e.printStackTrace();
            throw new BadRequestException(
                    String.format("The processing of image %1$s failed with message: %2$s", imageFile.getName(), e.getMessage())
            );
        }
    }

    private CameraSettings extractCameraInformation(ExifSubIFDDirectory directory) {
        // Exif SubIFD
        ExifSubIFDDescriptor descriptor = new ExifSubIFDDescriptor(directory);
        String shutterSpeed = descriptor.getExposureTimeDescription();
        String iso = descriptor.getIsoEquivalentDescription();
        String aperture = descriptor.getApertureValueDescription();
        String focalLength = descriptor.getFocalLengthDescription();
        log.debug("Camera Settings: Shutter speed {}, ISO {}, Aperture {}, Focal Length {}", shutterSpeed, iso, aperture, focalLength);

        return CameraSettings.builder()
                .shutterSpeed(shutterSpeed)
                .aperture(aperture)
                .iso(iso)
                .focalLength(focalLength)
                .build();
    }

    private GeoLocation extractGeolocation(GpsDirectory directory) {
        log.debug("Extracting GPS Data");
        if(directory == null) {
            log.debug("The uploaded image has no GPS data");
            return null;
        }

        com.drew.lang.GeoLocation geoLocation = directory.getGeoLocation();
        String altitude = directory.getString(GpsDirectory.TAG_ALTITUDE);
        String altRef = directory.getString(GpsDirectory.TAG_ALTITUDE_REF);

        log.debug("Extracting GPS Data: Lat {}, Lon {}, Alt {} {}", geoLocation.getLatitude(), geoLocation.getLongitude(), altitude, altRef);
        return GeoLocation.builder()
                .lat(geoLocation.getLatitude())
                .lon(geoLocation.getLongitude())
                .altitude(altitude)
                .build();
    }
}
