package com.nationwide.hackathon.cr.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PartnerRemitanceDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${customer.select.sql}")
	private String customerSelectQuery;

	public List<Partners> getCustomerInfo(List<String> contract_ds_ar, List<String> product_ds_ar, List<String> distribution_ds_ar) {
		Map<String, List<String>> paramMap = new HashMap<String, List<String>>();
		paramMap.put("contract_ds", contract_ds_ar);
		paramMap.put("product_ds", product_ds_ar);
		paramMap.put("distribution_ds", distribution_ds_ar);
		NamedParameterJdbcTemplate template =  new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		final List<Partners> eEInputMaster = template.query(customerSelectQuery,paramMap,
				new ResultSetExtractor<List<Partners>>() {
					public List<Partners> extractData(ResultSet rs) throws SQLException, DataAccessException {
						List<Partners> mapping = new ArrayList<Partners>();
						while (rs.next()) {
							Partners customer = new Partners();
							customer.setDistributionPartner_Cd(rs.getString("DistributionPartner_Cd"));
							customer.setProduct_ds(rs.getString("product_ds"));
							customer.setContract_ds(rs.getString("contract_ds"));
							mapping.add(customer);
						}
						return mapping;
					}
				});
		return eEInputMaster;
	}

}
