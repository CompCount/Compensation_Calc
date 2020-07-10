package com.nationwide.hackathon.cr.pdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Component
public class CRPdfGenerator {

	@Value(value = "${customer.files.path}")
	private String path;
	
	@Value(value = "${price.calculation}")
	private String priceCalculation;
	
	private Map<String, Map<Integer, Integer>> priceMap = new HashMap<String, Map<Integer,Integer>>();
	
	private static final Logger logger = LoggerFactory.getLogger(CRPdfGenerator.class);
	
	public Map<String, String> createPDFFile(Map<String, Map<String, Integer>> finalCal){
		
		JSONObject priceJsonObject = null;
		try {
			priceJsonObject = new JSONObject(priceCalculation);
			preparePriceMap(priceJsonObject);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		logger.info("Preparing file json file {} and final pricemap {}",priceCalculation,priceMap);
		Map<String, String> fileDatils = new HashMap<String, String>();
		Set<String> dsKeys = finalCal.keySet();
		for(String dsKey : dsKeys) {
			 Map<String, Integer> details =finalCal.get(dsKey);
			 Document document = new Document();
				try {
					String filePath = path+"\\"+dsKey.replaceAll("\\s","")+".pdf";
					PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
					document.open();
					document.add(new Paragraph("Total sample pay for."+dsKey.replaceAll("\\s","")));
					PdfPTable table = new PdfPTable(4); // 3 columns.
					table.setWidthPercentage(70); // Width 100%
					table.setSpacingBefore(10f); // Space before table
					table.setSpacingAfter(10f); // Space after table
					// Set Column widths
					float[] columnWidths = { 1f, 1f, 1f };
					table.setWidths(columnWidths);
					int totalPrice=0;
					Set<String> productKeys = details.keySet();
					for(String productKey:productKeys) {
						Map<Integer, Integer> price = priceMap.get(productKey.replaceAll("\\s",""));
						Set<Integer> pKeys=price.keySet();
						Integer count=details.get(productKey);
						int c = pKeys.stream()
					            .min(Comparator.comparingInt(i -> Math.abs(i - count)))
					            .orElseThrow(() -> new NoSuchElementException("No value present"));
						pKeys.stream().min(Comparator.comparingInt(i -> Math.abs(i - count)));
						totalPrice=totalPrice+price.get(c);
						PdfPCell colCell1 = new PdfPCell(new Phrase(productKey));
						colCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
						colCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
						table.addCell(colCell1);
						PdfPCell colCell2 = new PdfPCell(new Phrase(count.toString()));
						colCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
						colCell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
						table.addCell(colCell2);
						PdfPCell colCell3 = new PdfPCell(new Paragraph(price.get(c).toString()));
						colCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
						colCell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
						table.addCell(colCell3);
					}
					PdfPCell colCell1 = new PdfPCell(new Phrase(""));
					colCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
					colCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
					table.addCell(colCell1);
					PdfPCell colCell2 = new PdfPCell(new Phrase(new Phrase("Total "+details.size() +" products - total ")));
					colCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
					colCell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
					table.addCell(colCell2);
					PdfPCell colCell3 = new PdfPCell(new Paragraph(String.valueOf(totalPrice)));
					colCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
					colCell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
					table.addCell(colCell3);
					document.add(table);
					document.close();
					writer.close();
					fileDatils.put(dsKey.replaceAll("\\s",""), filePath);
				} catch (FileNotFoundException | DocumentException e) {
					return null;
				}
		}
		return fileDatils;
	}
	
	private void preparePriceMap(JSONObject jsonObject) throws JSONException {
		System.out.println(jsonObject);
		Iterator<String> pItr=jsonObject.keys();
		while(pItr.hasNext()) {
			String key=pItr.next();
			logger.info("PKeys   "+key);
			JSONArray array = jsonObject.getJSONArray(key);
			Map<Integer, Integer> price = new HashMap<Integer, Integer>();
			for(int i=0;i<array.length();i++) {
				JSONObject cJson = array.getJSONObject(i);
				Iterator<String> cItr= cJson.keys();
				while(cItr.hasNext()) {
					String cKey=cItr.next();
					logger.info("cKeys   "+cKey);
					price.put(Integer.parseInt(cKey), cJson.getInt(cKey));
				}
			}
			priceMap.put(key, price);
		}
	}

}
