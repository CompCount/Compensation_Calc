package com.nationwide.hackathon.cr.job;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nationwide.hackathon.cr.dao.PartnerRemitanceDao;
import com.nationwide.hackathon.cr.dao.Partners;
import com.nationwide.hackathon.cr.email.NotificationPlugin;
import com.nationwide.hackathon.cr.pdf.CRPdfGenerator;
import com.nationwide.hackathon.cr.s3.CrS3FileUploader;;

@Component
public class CRCoustomerRmittanceJob {


	@Autowired
	CrS3FileUploader s3Uploader;
	
	@Autowired
	PartnerRemitanceDao partnerRemitanceDao;
	
	@Value(value = "${customer.ignore.contract.ds}")
	private String ignore_contract_ds;

	@Value(value = "${customer.product.ds}")
	private String product_ds;
	
	@Value(value = "${customer.distribution.ds}")
	private String distribution_ds;
	
	@Value(value = "${cr.reports.s3.bucket}")
	private String bucketName;
	
	@Autowired
	private CRPdfGenerator crPdfGenerator;
	
	@Value(value = "${to.email}")
	private String toEmail;
	
	@Value(value = "${email.message}")
	private String emailMsg;
	
	@Autowired
	private NotificationPlugin notificationPlugin;
	
	private static final Logger logger = LoggerFactory.getLogger(CRCoustomerRmittanceJob.class);

	public void extractAndUploadTransactionReport() {

		logger.info("==================================================================================");
		logger.info("======================CUSTOMER REMITTANCE GENERATOR - START===========================");
		logger.info("==================================================================================");
		Long authExportStart = System.currentTimeMillis();
		List<String> contract_ds_ar=Arrays.asList(ignore_contract_ds.split(","));
		List<String> product_ds_ar=Arrays.asList(product_ds.split(","));
		List<String> distribution_ds_ar=Arrays.asList(distribution_ds.split(","));
		logger.info("Deatils of contract {}, porduct ds {} and distribution ds {}",contract_ds_ar,product_ds_ar,distribution_ds_ar);
		List<Partners> partnersList =partnerRemitanceDao.getCustomerInfo(contract_ds_ar,product_ds_ar,distribution_ds_ar);
		Map<String, Map<String, Integer>> finalCal = new HashMap<String, Map<String,Integer>>();
		Map<String, String> fileDatils = null;
		if(partnersList!=null) {
			logger.info("Size of the list after all filters {}",partnersList.size());
			for(Partners partners:partnersList) {
				if(finalCal.containsKey(partners.getDistributionPartner_Cd())) {
					Map<String, Integer> details=finalCal.get(partners.getDistributionPartner_Cd());
					if(details.containsKey(partners.getProduct_ds())) {
						int count=details.get(partners.getProduct_ds());
						details.put(partners.getProduct_ds(), ++count);
					}else {
						details.put(partners.getProduct_ds(), 1);
					}
				}else {
					Map<String, Integer> details = new HashMap<String, Integer>();
					details.put(partners.getProduct_ds(), 1);
					finalCal.put(partners.getDistributionPartner_Cd(), details);
				}
			}
			logger.info("Final Details for calculation {}",finalCal);
			fileDatils=crPdfGenerator.createPDFFile(finalCal);
		}
		
		int authRecordCount = null!=fileDatils?fileDatils.size():0;
		Long authExportEnd = System.currentTimeMillis();
		boolean s3AuthUploadStatus = false;
		boolean sendEmail = false;
		Long authUploadStart = 0L;
		Long authUploadEnd = 0L;
		if (authRecordCount > 0) {
			authUploadStart = System.currentTimeMillis();
			Set<String> filesKeys = fileDatils.keySet();
			for(String filesKey:filesKeys) {
				s3AuthUploadStatus = s3Uploader.uploadFiletoS3Bucket(fileDatils.get(filesKey),
						bucketName, filesKey);
				emailMsg=" File name "+filesKey+".pdf"+" Path"+fileDatils.get(filesKey)+System.getProperty("line.separator");
			}
			authUploadEnd = System.currentTimeMillis();
			sendEmail=notificationPlugin.sendEmail(emailMsg, toEmail);
		}
		logger.info("==================================================================================");
		logger.info("=======================CUSTOMER REMITTANCE GENERATOR - END============================");
		logger.info("==================================================================================");
		logger.info("Customer Report Run Status :: " + ((partnersList.size() != -1) ? "Success" : "Failed"));
		logger.info("Customer Report Row Count  :: " + ((partnersList.size() != -1) ? partnersList.size() : "Run Failed"));
		logger.info("Customer Report Export Time taken :: " + (authExportEnd - authExportStart) + " ms");
		
		if(authRecordCount > 0) {
			logger.info("Customer Report Upload Status :: " + (s3AuthUploadStatus ? "Success" : "Failed"));
			logger.info("Customer Report Email Status :: " + (sendEmail ? "Success" : "Failed"));
			logger.info("Customer Report Upload Time taken :: " + (authUploadEnd - authUploadStart) + " ms");
		} else {
			logger.info("Customer Report Upload Status :: "+"Nothing to upload!");
		}
		logger.info("==================================================================================");

	}

}
