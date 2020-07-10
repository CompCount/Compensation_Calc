package com.nationwide.hackathon.cr.s3;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Component
public class CrS3FileUploader {

	private static final Logger logger = LoggerFactory.getLogger(CrS3FileUploader.class);
	
	@Value("${cr.reports.s3.region}")
	private String s3RegionName;

	private S3Client s3Client;

	public S3Client getS3Client() {
		return s3Client;
	}

	public void setS3Client(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	public boolean uploadFiletoS3Bucket(String filePath, String s3BucketName, String s3KeyName) {
		
		try {
			
		logger.info("Starting upload of file :: "+s3KeyName);
		
		if(s3Client==null) {
			s3Client=S3Client.builder().region(Region.of(s3RegionName)).build();
		}

		PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(s3BucketName)
				.key(s3KeyName).build();

		File file = new File(filePath);

		PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

		if(response.sdkHttpResponse().isSuccessful()) {
			logger.info("Upload of file :: "+s3KeyName+" succeeded");
			return true;
		} else {
			logger.error("Upload of file :: "+s3KeyName+" failed");
			logger.error("Received response code :: "+response.sdkHttpResponse().statusCode());
			return false;
		}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error occured during file upload to S3 Bucket :: ",e);
			return false;
		}

	}

}
