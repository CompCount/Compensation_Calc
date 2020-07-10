package com.nationwide.hackathon.cr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import com.nationwide.hackathon.cr.job.CRCoustomerRmittanceJob;

@SpringBootApplication(exclude = { HibernateJpaAutoConfiguration.class })
public class ComputeRemittanceApplication extends SpringBootServletInitializer {

	@Autowired
	CRCoustomerRmittanceJob coustomerRmittanceJob;
	
	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(ComputeRemittanceApplication.class, args);
		CRCoustomerRmittanceJob coustomerRmittanceJob = applicationContext.getBean(CRCoustomerRmittanceJob.class);
		coustomerRmittanceJob.extractAndUploadTransactionReport();
	}
	
}
