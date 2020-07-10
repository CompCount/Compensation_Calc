package com.nationwide.hackathon.cr.dao;

public class Partners {

	private String DistributionPartner_Cd;
	private String product_ds;
	private String contract_ds;
	public String getDistributionPartner_Cd() {
		return DistributionPartner_Cd;
	}
	public void setDistributionPartner_Cd(String distributionPartner_Cd) {
		DistributionPartner_Cd = distributionPartner_Cd;
	}
	public String getProduct_ds() {
		return product_ds;
	}
	public void setProduct_ds(String product_ds) {
		this.product_ds = product_ds;
	}
	public String getContract_ds() {
		return contract_ds;
	}
	public void setContract_ds(String contract_ds) {
		this.contract_ds = contract_ds;
	}
}
