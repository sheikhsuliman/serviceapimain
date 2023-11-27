package com.siryus.swisscon.api.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfiguration {

    @Value("${aws_access_key_id}")
    private String awsAccessKey;
    @Value("${aws_secret_access_key}")
    private String awsSecretAccessKey;
    @Value("${aws_region}")
    private String awsRegion;

    @Bean
    @ConditionalOnMissingBean(AmazonS3.class)
    public AmazonS3 amazonS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.awsAccessKey, this.awsSecretAccessKey);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(awsRegion))
                .build();
    }
    
    @Bean
    @ConditionalOnMissingBean(AmazonSNSClient.class)
    public AmazonSNS amazonSNSClient() {
        AWSCredentials credentials = new BasicAWSCredentials(this.awsAccessKey, this.awsSecretAccessKey);

        AmazonSNS result =  AmazonSNSClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(credentials))
                            .withRegion(Regions.fromName(awsRegion))
                            .build();

        return result;
    }        
}
