package io.kennethmartens.ckm.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import io.kennethmartens.ckm.data.entities.CameraInformation;
import io.kennethmartens.ckm.data.entities.CameraSettings;
import io.kennethmartens.ckm.data.entities.GeoLocation;
import io.kennethmartens.ckm.data.entities.Image;
import io.kennethmartens.ckm.data.repository.ImageRepository;
import io.kennethmartens.ckm.rest.v1.forms.ImageForm;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class ImageService {

    private final String FILE_BASE_PATH = "/Users/kennethmartens/images";

    private final ImageRepository repository;

    private final Vertx vertx;

    public ImageService(ImageRepository repository, Vertx vertx) {
        this.repository = repository;
        this.vertx = vertx;
    }

    public Uni<byte[]> getImageById(String id) {
        return repository.find("imageId", id).firstResult()
                .map(Image::getPath)
                .map(path -> vertx.fileSystem()
                        .readFileBlocking(path)
                        .getBytes()
                );
    }

    public Uni<Image> persist(ImageForm imageForm) {
        FileUpload upload = imageForm.getImage();
        UUID imageUploadUUID = UUID.randomUUID();
        String composedFilePath = String.format("%1$s/%2$s.jpg", FILE_BASE_PATH, imageUploadUUID);
//        File toBeMovedImagePath = new File(composedFilePath);

        return vertx.fileSystem()
                .move(upload.uploadedFile().toString(), composedFilePath)
                .map(x -> extractImageMetadata(new File(composedFilePath), imageUploadUUID))
                .flatMap(repository::persist);
    }

    private Image extractImageMetadata(File imageFile, UUID imageId) {
        try{
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

            log.debug("Extracting GPS Data");
            GpsDirectory gpsDirectory = imageMetadata.getFirstDirectoryOfType(GpsDirectory.class);
            if(gpsDirectory == null) {
                log.info("The uploaded image with filename {} has no GPS data", imageFile.getName());
            }

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
            log.error("Something went wrong whilst reading the file {}", imageFile);
        }

        throw new BadRequestException();
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

    public Uni<List<Image>> findAll() {
        return repository.findAll().list();
    }
}
