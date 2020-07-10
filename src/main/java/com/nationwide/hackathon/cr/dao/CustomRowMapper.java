package com.nationwide.hackathon.cr.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

@Component
public class CustomRowMapper {

    public Partners mapRow(ResultSet rs, int rowNum) throws SQLException {
        Partners customer = new Partners();
        customer.setDistributionPartner_Cd(rs.getString("DistributionPartner_Cd"));
        customer.setProduct_ds(rs.getString("product_ds"));
        customer.setContract_ds(rs.getString("contract_ds"));
        return customer;

    }
}
