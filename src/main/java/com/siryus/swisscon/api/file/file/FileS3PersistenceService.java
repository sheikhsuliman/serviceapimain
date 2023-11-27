package com.siryus.swisscon.api.file.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.siryus.swisscon.api.file.FileExceptions;
import com.siryus.swisscon.api.file.image.ImageConverterRegistry;
import com.siryus.swisscon.api.file.image.ImageUtil;
import com.siryus.swisscon.api.file.image.ToImageConverter;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.siryus.swisscon.api.file.image.ImageThumbUtil.MAX_HEIGHT;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.MAX_WIDTH;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.MEDIUM_MAX_HEIGHT;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.MEDIUM_MAX_WIDTH;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.SMALL_MAX_HEIGHT;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.SMALL_MAX_WIDTH;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.getMediumPath;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.getSmallPath;

@Service
public class FileS3PersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileS3PersistenceService.class);

    @Value("${aws_namecard_bucket}")
    private String nameCardBucket;

    private AmazonS3 s3Client;

    @Autowired
    public void setS3Client(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public FileData saveFile(MultipartFile multipartFile, String filename) {
        FileData file = null;
        try {
            // Convert to file
            File tmpFile = streamToTmpFile(multipartFile.getInputStream());
            // Build DTO
            file = FileData.builder()
                    .contentLength(multipartFile.getSize())
                    .contentType(multipartFile.getContentType())
                    .in(tmpFile)
                    .path(filename).build();

            file = ImageUtil.convertToPngIfGif(file);

            return this.saveFile(file);

        } catch (IOException e) {
            throw FileExceptions.failedToPersistFile(filename);
        } finally {
            closeFileDto(file);
        }
    }

    private static File streamToTmpFile(InputStream in) {
        FileOutputStream out = null;
        try {
            final File tempFile = File.createTempFile("upload", ".tmp");
            tempFile.deleteOnExit();
            out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
            return tempFile;
        } catch (Exception e) {
            throw FileExceptions.failedToPersistFile("upload.tmp");
        } finally {
            if (out != null) {
                IOUtils.closeQuietly(in);
            }
        }
    }

    private void closeFileDto(FileData file) {
        if (file != null && file.getTmpFile() != null) {
            file.getTmpFile().delete();
        }
    }

    /**
     * The method saves the given multipart file to the pathFragment specified, ignoring the original file name.
     */
    private FileData saveFile(FileData file) {
        String url;
        String urlSmall = null;
        String urlMedium = null;
        FileInputStream in = null;

        try {

            // Base for previews
            BufferedImage img = null;
            if (ImageUtil.isImage(file.getContentType())) {
                in = new FileInputStream(file.getIn());
                img = ImageIO.read(in);
                IOUtils.closeQuietly(in);

                url = saveImageFile(img, file.getContentType(), file.getPath(), MAX_WIDTH, MAX_HEIGHT);
            }
            // Other file types
            else {
                // Converter?
                ToImageConverter converter = ImageConverterRegistry.converters.get(file.getContentType());
                if (converter != null) {
                    try {
                        img = converter.toImageFile(file);
                    } catch (Exception e) {
                        LOGGER.error("Failed converting file to image", e);
                    }
                }
                // Save actual file
                url = saveFile(file.getIn(), file.getContentLength(), file.getContentType(), file.getPath());
            }

            if (img != null) {
                urlMedium = saveImageFile(img, "image/png", getMediumPath(file.getPath()), MEDIUM_MAX_WIDTH, MEDIUM_MAX_HEIGHT);
                urlSmall = saveImageFile(img, "image/png", getSmallPath(file.getPath()), SMALL_MAX_WIDTH, SMALL_MAX_HEIGHT);
            }

        } catch (IOException e) {
            throw FileExceptions.failedToPersistFile(file.getFilename());
        } finally {
            IOUtils.closeQuietly(in);
            this.closeFileDto(file);
        }

        file.setUrl(url);
        file.setUrlMedium(urlMedium);
        file.setUrlSmall(urlSmall);

        return file;
    }

    private String saveFile(File file, long contentLength, String contentType, String path) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(contentLength);
        meta.setContentType(contentType);
        FileInputStream in = null;

        try {
            in = new FileInputStream(file);
            this.s3Client.putObject((new PutObjectRequest(this.nameCardBucket, path, in, meta)).withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (Exception var13) {
            throw FileExceptions.failedToPersistFile(file.getName());
        } finally {
            if (Objects.nonNull(in)) {
                IOUtils.closeQuietly(in);
            }

        }

        String url = this.s3Client.getUrl(this.nameCardBucket, path).toString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("File saved, url: {}, size: {}, contentType: {}", url, contentLength, contentType);
        }

        return url;
    }


    public void deleteByPath(String... paths) {
        this.s3Client.deleteObjects((new DeleteObjectsRequest(this.nameCardBucket)).withKeys(paths));
    }

    public void deleteByPrefixes(String... prefixes) {
        Arrays.asList(prefixes).forEach(this::deleteByPrefix);
    }

    public void deleteByPrefix(String prefix) {
        String[] keys = s3Client.listObjects(nameCardBucket, prefix)
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .toArray(String[]::new);

        if (keys.length > 0) {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(nameCardBucket).withKeys(keys);
            s3Client.deleteObjects(deleteObjectsRequest);
        }
    }

    public void deleteFilesWhichContains(String prefix, String filename) {

        // prepare request
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
        listObjectsV2Request.setBucketName(nameCardBucket);
        listObjectsV2Request.setPrefix(prefix);
        listObjectsV2Request.setMaxKeys(500);
        ListObjectsV2Result result;

        List<String> keys = new LinkedList<>();

        do {
            result = s3Client.listObjectsV2(listObjectsV2Request);

            List<String> keysPaginated = result.getObjectSummaries().stream()
                    .map(S3ObjectSummary::getKey)
                    .filter(key -> key.contains(filename))
                    .collect(Collectors.toList());
            listObjectsV2Request.setContinuationToken(result.getNextContinuationToken());

            keys.addAll(keysPaginated);
        } while (result.isTruncated());

        if (keys.size() > 0) {
            s3Client.deleteObjects(new DeleteObjectsRequest(nameCardBucket).withKeys(keys.toArray(new String[0])));
        }
    }

    private boolean needScaling(BufferedImage img, int maxWidth, int maxHeight) {
        return img.getWidth() > maxWidth || img.getHeight() > maxHeight;
    }

    public FileData createScaledTempFile(BufferedImage img, String contentType, int maxWidth, int maxHeight) throws IOException {
        FileData scaledFile;
        FileOutputStream os = null;

        File newFile = File.createTempFile("upload", "tmp");
        try {
            BufferedImage scaled = Scalr.resize(img,
                    Scalr.Method.QUALITY,
                    Scalr.Mode.FIT_TO_WIDTH,
                    maxWidth,
                    maxHeight,
                    Scalr.OP_ANTIALIAS);
            os = new FileOutputStream(newFile);
            ImageIO.write(scaled, ImageUtil.getImageIoFormat(contentType), os);
            scaledFile = FileData.builder()
                    .contentLength(newFile.length())
                    .contentType(contentType)
                    .in(newFile)
                    .build();
        } finally {
            IOUtils.closeQuietly(os);
        }
        return scaledFile;
    }

    private String saveImageFile(BufferedImage img, String contentType, String path, int maxWidth, int maxHeight) throws IOException {
        FileData tempFile = null;
        try {
            tempFile = needScaling(img, maxWidth, maxHeight) ?
                    createScaledTempFile(img, contentType, maxWidth, maxHeight) :
                    createTempFile(img, contentType);

            return saveFile(tempFile.getIn(), tempFile.getContentLength(), tempFile.getContentType(), path);
        } finally {
            this.closeFileDto(tempFile);
        }
    }

    private FileData createTempFile(BufferedImage img, String contentType) throws IOException {
        OutputStream os = null;
        try {
            File tmpFile = File.createTempFile("image", "tmp");
            os = new FileOutputStream(tmpFile);
            ImageIO.write(img, ImageUtil.getImageIoFormat(contentType), os);
            return FileData.builder()
                    .contentLength(tmpFile.length())
                    .contentType(contentType)
                    .in(tmpFile)
                    .build();
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    public boolean fileWithPrefixExist(String prefix) {
        return s3Client.doesObjectExist(nameCardBucket, prefix);
    }

}
