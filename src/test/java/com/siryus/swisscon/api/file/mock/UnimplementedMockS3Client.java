package com.siryus.swisscon.api.file.mock;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.S3ResponseMetadata;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketAccelerateConfiguration;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLoggingConfiguration;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.BucketReplicationConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult;
import com.amazonaws.services.s3.model.DeleteBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest;
import com.amazonaws.services.s3.model.DeleteBucketEncryptionResult;
import com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult;
import com.amazonaws.services.s3.model.DeleteBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult;
import com.amazonaws.services.s3.model.DeleteBucketPolicyRequest;
import com.amazonaws.services.s3.model.DeleteBucketReplicationConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectTaggingRequest;
import com.amazonaws.services.s3.model.DeleteObjectTaggingResult;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest;
import com.amazonaws.services.s3.model.DeletePublicAccessBlockResult;
import com.amazonaws.services.s3.model.DeleteVersionRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetBucketAccelerateConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketAclRequest;
import com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketEncryptionRequest;
import com.amazonaws.services.s3.model.GetBucketEncryptionResult;
import com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.GetBucketLoggingConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketNotificationConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketPolicyRequest;
import com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest;
import com.amazonaws.services.s3.model.GetBucketPolicyStatusResult;
import com.amazonaws.services.s3.model.GetBucketReplicationConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.GetObjectAclRequest;
import com.amazonaws.services.s3.model.GetObjectLegalHoldRequest;
import com.amazonaws.services.s3.model.GetObjectLegalHoldResult;
import com.amazonaws.services.s3.model.GetObjectLockConfigurationRequest;
import com.amazonaws.services.s3.model.GetObjectLockConfigurationResult;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRetentionRequest;
import com.amazonaws.services.s3.model.GetObjectRetentionResult;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.GetPublicAccessBlockRequest;
import com.amazonaws.services.s3.model.GetPublicAccessBlockResult;
import com.amazonaws.services.s3.model.GetS3AccountOwnerRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest;
import com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest;
import com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest;
import com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListNextBatchOfObjectsRequest;
import com.amazonaws.services.s3.model.ListNextBatchOfVersionsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ListPartsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.PartListing;
import com.amazonaws.services.s3.model.PresignedUrlDownloadRequest;
import com.amazonaws.services.s3.model.PresignedUrlDownloadResult;
import com.amazonaws.services.s3.model.PresignedUrlUploadRequest;
import com.amazonaws.services.s3.model.PresignedUrlUploadResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.RestoreObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SelectObjectContentRequest;
import com.amazonaws.services.s3.model.SelectObjectContentResult;
import com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketAclRequest;
import com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult;
import com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketEncryptionRequest;
import com.amazonaws.services.s3.model.SetBucketEncryptionResult;
import com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult;
import com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult;
import com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketPolicyRequest;
import com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.SetObjectAclRequest;
import com.amazonaws.services.s3.model.SetObjectLegalHoldRequest;
import com.amazonaws.services.s3.model.SetObjectLegalHoldResult;
import com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest;
import com.amazonaws.services.s3.model.SetObjectLockConfigurationResult;
import com.amazonaws.services.s3.model.SetObjectRetentionRequest;
import com.amazonaws.services.s3.model.SetObjectRetentionResult;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingResult;
import com.amazonaws.services.s3.model.SetPublicAccessBlockRequest;
import com.amazonaws.services.s3.model.SetPublicAccessBlockResult;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration;
import com.amazonaws.services.s3.model.inventory.InventoryConfiguration;
import com.amazonaws.services.s3.model.metrics.MetricsConfiguration;
import com.amazonaws.services.s3.waiters.AmazonS3Waiters;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class UnimplementedMockS3Client implements AmazonS3 {

    @Override
    public void setEndpoint(String s) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setRegion(Region region) throws IllegalArgumentException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setS3ClientOptions(S3ClientOptions s3ClientOptions) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void changeObjectStorageClass(String s, String s1, StorageClass storageClass) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setObjectRedirectLocation(String s, String s1, String s2) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectListing listObjects(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectListing listObjects(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest listObjectsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ListObjectsV2Result listObjectsV2(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ListObjectsV2Result listObjectsV2(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ListObjectsV2Result listObjectsV2(ListObjectsV2Request listObjectsV2Request) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing objectListing) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ListNextBatchOfObjectsRequest listNextBatchOfObjectsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public VersionListing listVersions(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public VersionListing listNextBatchOfVersions(VersionListing versionListing) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public VersionListing listNextBatchOfVersions(ListNextBatchOfVersionsRequest listNextBatchOfVersionsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public VersionListing listVersions(String s, String s1, String s2, String s3, String s4, Integer integer) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public VersionListing listVersions(ListVersionsRequest listVersionsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public Owner getS3AccountOwner() throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public Owner getS3AccountOwner(GetS3AccountOwnerRequest getS3AccountOwnerRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public boolean doesBucketExist(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public boolean doesBucketExistV2(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public HeadBucketResult headBucket(HeadBucketRequest headBucketRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public List<Bucket> listBuckets() throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public String getBucketLocation(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public String getBucketLocation(GetBucketLocationRequest getBucketLocationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public Bucket createBucket(CreateBucketRequest createBucketRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public Bucket createBucket(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public Bucket createBucket(String s, com.amazonaws.services.s3.model.Region region) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public Bucket createBucket(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public AccessControlList getObjectAcl(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public AccessControlList getObjectAcl(String s, String s1, String s2) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public AccessControlList getObjectAcl(GetObjectAclRequest getObjectAclRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setObjectAcl(String s, String s1, AccessControlList accessControlList) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setObjectAcl(String s, String s1, CannedAccessControlList cannedAccessControlList) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setObjectAcl(String s, String s1, String s2, AccessControlList accessControlList) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setObjectAcl(String s, String s1, String s2, CannedAccessControlList cannedAccessControlList) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setObjectAcl(SetObjectAclRequest setObjectAclRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public AccessControlList getBucketAcl(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketAcl(SetBucketAclRequest setBucketAclRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public AccessControlList getBucketAcl(GetBucketAclRequest getBucketAclRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketAcl(String s, AccessControlList accessControlList) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketAcl(String s, CannedAccessControlList cannedAccessControlList) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectMetadata getObjectMetadata(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest getObjectMetadataRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public S3Object getObject(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public S3Object getObject(GetObjectRequest getObjectRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest, File file) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public String getObjectAsString(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetObjectTaggingResult getObjectTagging(GetObjectTaggingRequest getObjectTaggingRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetObjectTaggingResult setObjectTagging(SetObjectTaggingRequest setObjectTaggingRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteObjectTaggingResult deleteObjectTagging(DeleteObjectTaggingRequest deleteObjectTaggingRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucket(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest putObjectRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public PutObjectResult putObject(String s, String s1, File file) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public PutObjectResult putObject(String s, String s1, InputStream inputStream, ObjectMetadata objectMetadata) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public PutObjectResult putObject(String s, String s1, String s2) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public CopyObjectResult copyObject(String s, String s1, String s2, String s3) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public CopyObjectResult copyObject(CopyObjectRequest copyObjectRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public CopyPartResult copyPart(CopyPartRequest copyPartRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteObject(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteObject(DeleteObjectRequest deleteObjectRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteObjectsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteVersion(String s, String s1, String s2) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteVersion(DeleteVersionRequest deleteVersionRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(GetBucketLoggingConfigurationRequest getBucketLoggingConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketLoggingConfiguration(SetBucketLoggingConfigurationRequest setBucketLoggingConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(GetBucketVersioningConfigurationRequest getBucketVersioningConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketVersioningConfiguration(SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(String s) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(GetBucketLifecycleConfigurationRequest getBucketLifecycleConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketLifecycleConfiguration(String s, BucketLifecycleConfiguration bucketLifecycleConfiguration) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketLifecycleConfiguration(SetBucketLifecycleConfigurationRequest setBucketLifecycleConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketLifecycleConfiguration(String s) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketLifecycleConfiguration(DeleteBucketLifecycleConfigurationRequest deleteBucketLifecycleConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(String s) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(GetBucketCrossOriginConfigurationRequest getBucketCrossOriginConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketCrossOriginConfiguration(String s, BucketCrossOriginConfiguration bucketCrossOriginConfiguration) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketCrossOriginConfiguration(SetBucketCrossOriginConfigurationRequest setBucketCrossOriginConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(String s) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(DeleteBucketCrossOriginConfigurationRequest deleteBucketCrossOriginConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(String s) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(GetBucketTaggingConfigurationRequest getBucketTaggingConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketTaggingConfiguration(String s, BucketTaggingConfiguration bucketTaggingConfiguration) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketTaggingConfiguration(SetBucketTaggingConfigurationRequest setBucketTaggingConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketTaggingConfiguration(String s) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketTaggingConfiguration(DeleteBucketTaggingConfigurationRequest deleteBucketTaggingConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(GetBucketNotificationConfigurationRequest getBucketNotificationConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketNotificationConfiguration(SetBucketNotificationConfigurationRequest setBucketNotificationConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketNotificationConfiguration(String s, BucketNotificationConfiguration bucketNotificationConfiguration) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(GetBucketWebsiteConfigurationRequest getBucketWebsiteConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketWebsiteConfiguration(String s, BucketWebsiteConfiguration bucketWebsiteConfiguration) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketWebsiteConfiguration(SetBucketWebsiteConfigurationRequest setBucketWebsiteConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketWebsiteConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketWebsiteConfiguration(DeleteBucketWebsiteConfigurationRequest deleteBucketWebsiteConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketPolicy getBucketPolicy(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketPolicy getBucketPolicy(GetBucketPolicyRequest getBucketPolicyRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketPolicy(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketPolicy(SetBucketPolicyRequest setBucketPolicyRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketPolicy(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketPolicy(DeleteBucketPolicyRequest deleteBucketPolicyRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public URL generatePresignedUrl(String s, String s1, Date date) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public URL generatePresignedUrl(String s, String s1, Date date, HttpMethod httpMethod) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public URL generatePresignedUrl(GeneratePresignedUrlRequest generatePresignedUrlRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadRequest initiateMultipartUploadRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest uploadPartRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public PartListing listParts(ListPartsRequest listPartsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest abortMultipartUploadRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest completeMultipartUploadRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public MultipartUploadListing listMultipartUploads(ListMultipartUploadsRequest listMultipartUploadsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public S3ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest amazonWebServiceRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void restoreObject(RestoreObjectRequest restoreObjectRequest) throws AmazonServiceException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public RestoreObjectResult restoreObjectV2(RestoreObjectRequest restoreObjectRequest) throws AmazonServiceException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void restoreObject(String s, String s1, int i) throws AmazonServiceException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void enableRequesterPays(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void disableRequesterPays(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public boolean isRequesterPaysEnabled(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketReplicationConfiguration(String s, BucketReplicationConfiguration bucketReplicationConfiguration) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketReplicationConfiguration(SetBucketReplicationConfigurationRequest setBucketReplicationConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketReplicationConfiguration getBucketReplicationConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketReplicationConfiguration getBucketReplicationConfiguration(GetBucketReplicationConfigurationRequest getBucketReplicationConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketReplicationConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void deleteBucketReplicationConfiguration(DeleteBucketReplicationConfigurationRequest deleteBucketReplicationConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public boolean doesObjectExist(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketAccelerateConfiguration getBucketAccelerateConfiguration(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public BucketAccelerateConfiguration getBucketAccelerateConfiguration(GetBucketAccelerateConfigurationRequest getBucketAccelerateConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketAccelerateConfiguration(String s, BucketAccelerateConfiguration bucketAccelerateConfiguration) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void setBucketAccelerateConfiguration(SetBucketAccelerateConfigurationRequest setBucketAccelerateConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(DeleteBucketMetricsConfigurationRequest deleteBucketMetricsConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(GetBucketMetricsConfigurationRequest getBucketMetricsConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(String s, MetricsConfiguration metricsConfiguration) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(SetBucketMetricsConfigurationRequest setBucketMetricsConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ListBucketMetricsConfigurationsResult listBucketMetricsConfigurations(ListBucketMetricsConfigurationsRequest listBucketMetricsConfigurationsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(DeleteBucketAnalyticsConfigurationRequest deleteBucketAnalyticsConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(GetBucketAnalyticsConfigurationRequest getBucketAnalyticsConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(String s, AnalyticsConfiguration analyticsConfiguration) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(SetBucketAnalyticsConfigurationRequest setBucketAnalyticsConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ListBucketAnalyticsConfigurationsResult listBucketAnalyticsConfigurations(ListBucketAnalyticsConfigurationsRequest listBucketAnalyticsConfigurationsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(DeleteBucketInventoryConfigurationRequest deleteBucketInventoryConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(String s, String s1) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(GetBucketInventoryConfigurationRequest getBucketInventoryConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(String s, InventoryConfiguration inventoryConfiguration) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(SetBucketInventoryConfigurationRequest setBucketInventoryConfigurationRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public ListBucketInventoryConfigurationsResult listBucketInventoryConfigurations(ListBucketInventoryConfigurationsRequest listBucketInventoryConfigurationsRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketEncryptionResult deleteBucketEncryption(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeleteBucketEncryptionResult deleteBucketEncryption(DeleteBucketEncryptionRequest deleteBucketEncryptionRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketEncryptionResult getBucketEncryption(String s) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketEncryptionResult getBucketEncryption(GetBucketEncryptionRequest getBucketEncryptionRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetBucketEncryptionResult setBucketEncryption(SetBucketEncryptionRequest setBucketEncryptionRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetPublicAccessBlockResult setPublicAccessBlock(SetPublicAccessBlockRequest setPublicAccessBlockRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetPublicAccessBlockResult getPublicAccessBlock(GetPublicAccessBlockRequest getPublicAccessBlockRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public DeletePublicAccessBlockResult deletePublicAccessBlock(DeletePublicAccessBlockRequest deletePublicAccessBlockRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetBucketPolicyStatusResult getBucketPolicyStatus(GetBucketPolicyStatusRequest getBucketPolicyStatusRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SelectObjectContentResult selectObjectContent(SelectObjectContentRequest selectObjectContentRequest) throws SdkClientException {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetObjectLegalHoldResult setObjectLegalHold(SetObjectLegalHoldRequest setObjectLegalHoldRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetObjectLegalHoldResult getObjectLegalHold(GetObjectLegalHoldRequest getObjectLegalHoldRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetObjectLockConfigurationResult setObjectLockConfiguration(SetObjectLockConfigurationRequest setObjectLockConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetObjectLockConfigurationResult getObjectLockConfiguration(GetObjectLockConfigurationRequest getObjectLockConfigurationRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public SetObjectRetentionResult setObjectRetention(SetObjectRetentionRequest setObjectRetentionRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public GetObjectRetentionResult getObjectRetention(GetObjectRetentionRequest getObjectRetentionRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public PresignedUrlDownloadResult download(PresignedUrlDownloadRequest presignedUrlDownloadRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void download(PresignedUrlDownloadRequest presignedUrlDownloadRequest, File file) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public PresignedUrlUploadResult upload(PresignedUrlUploadRequest presignedUrlUploadRequest) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public void shutdown() {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public com.amazonaws.services.s3.model.Region getRegion() {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public String getRegionName() {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public URL getUrl(String s, String s1) {
        throw new MockS3ClientMethodNotImplementedException();
    }

    @Override
    public AmazonS3Waiters waiters() {
        throw new MockS3ClientMethodNotImplementedException();
    }

    private class MockS3ClientMethodNotImplementedException extends RuntimeException {

        private MockS3ClientMethodNotImplementedException() {
            super(MockS3Client.class.getName() + ": Mock method not implemented");
        }
    }

}
