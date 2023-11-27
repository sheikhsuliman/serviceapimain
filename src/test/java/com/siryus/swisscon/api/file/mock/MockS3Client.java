package com.siryus.swisscon.api.file.mock;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class MockS3Client extends UnimplementedMockS3Client {

    public static final String TEMP_FOLDER = "temp";
    private static final String SRC_TEST_RESOURCES = "src/test/resources/";

    private List<S3ObjectSummary> s3ObjectSummaries = new LinkedList<>();

    @Override
    public ListObjectsV2Result listObjectsV2(ListObjectsV2Request listObjectsV2Request) throws SdkClientException {
        ListObjectsV2Result listObjectsV2Result = new ListObjectsV2Result();
        listObjectsV2Result.setBucketName(listObjectsV2Request.getBucketName());
        listObjectsV2Result.setTruncated(false);

        List<S3ObjectSummary> matchingObjects = s3ObjectSummaries
                .stream()
                .filter(o -> o.getKey().startsWith(listObjectsV2Request.getPrefix()))
                .collect(Collectors.toList());

        listObjectsV2Result.getObjectSummaries().addAll(matchingObjects);

        return listObjectsV2Result;
    }

    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteObjectsRequest) throws SdkClientException {

        // remove deleted objects from list
        Predicate<S3ObjectSummary> requestKeyInS3ObjectKeys = ( o) -> deleteObjectsRequest
                .getKeys()
                .stream()
                .anyMatch(rq->o.getKey().startsWith(rq.getKey()));
        s3ObjectSummaries.removeIf(requestKeyInS3ObjectKeys);

        // delete files on local filesystem
        deleteObjectsRequest.getKeys().forEach(k->deleteFile(k.getKey()));

        // create response
        List<DeleteObjectsResult.DeletedObject> deletedObjects = deleteObjectsRequest.getKeys().stream().map(k -> {
            DeleteObjectsResult.DeletedObject deletedObject = new DeleteObjectsResult.DeletedObject();
            deletedObject.setKey(k.getKey());
            deletedObject.setDeleteMarker(true);
            return deletedObject;
        }).collect(Collectors.toList());

        DeleteObjectsResult deleteObjectsResult = new DeleteObjectsResult(deletedObjects);
        deleteObjectsResult.setRequesterCharged(true);

        return deleteObjectsResult;
    }

    @Override
    public boolean doesObjectExist(String bucket, String key) throws SdkClientException {
        return s3ObjectSummaries.stream().anyMatch(o -> {
            boolean bucketMatches = o.getBucketName().equals(bucket);
            boolean keyMatches = o.getKey().equals(key);
            return bucketMatches && keyMatches;
        });
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest putObjectRequest) throws SdkClientException {
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setBucketName(putObjectRequest.getBucketName());
        s3ObjectSummary.setLastModified(Date.from(Instant.now()));
        s3ObjectSummary.setKey(putObjectRequest.getKey());

        s3ObjectSummaries.add(s3ObjectSummary);

        try {
            persistFile(putObjectRequest);
        } catch (IOException e) {
            throw new MockS3ClientIOException(e);
        }

        return new PutObjectResult();
    }

    private void persistFile(PutObjectRequest putObjectRequest) throws IOException {
        File targetFile = new File(SRC_TEST_RESOURCES + TEMP_FOLDER + "/" + putObjectRequest.getKey());
        InputStream inputStream = putObjectRequest.getInputStream();
        FileUtils.copyInputStreamToFile(inputStream, targetFile);
    }

    private void deleteFile(String key) {
        File fileToDelete = new File(SRC_TEST_RESOURCES + TEMP_FOLDER + "/" + key);
        try {
            if(fileToDelete.exists()) {
                FileUtils.forceDelete(fileToDelete);
            }
        } catch (IOException e) {
            throw new MockS3ClientIOException(e);
        }
    }

    @Override
    public URL getUrl(String bucket, String key) {
        try {
            return FileUtils.getFile(SRC_TEST_RESOURCES + TEMP_FOLDER + "/" + key).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MockS3ClientIOException(e);
        }
    }

    @Override
    public ObjectListing listObjects(String bucket, String key) throws SdkClientException {

        ObjectListing objectListing = new ObjectListing();

        objectListing.setBucketName(bucket);
        objectListing.getObjectSummaries().addAll(s3ObjectSummaries);

        return objectListing;
    }

    public void cleanMockS3FileFolder() {
        File fileToDelete = new File(SRC_TEST_RESOURCES + TEMP_FOLDER);
        try {
            if(fileToDelete.exists()) {
                FileUtils.forceDelete(fileToDelete);
            }
        } catch (IOException e) {
            throw new MockS3ClientIOException(e);
        }
    }

    private class MockS3ClientIOException extends RuntimeException {

        private MockS3ClientIOException(Throwable e) {
            super(MockS3Client.class.getName() + ": Error during File Operation", e);
        }
    }
}
