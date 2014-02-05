package com.ire.search;

import java.util.ArrayList;
import java.util.List;

import com.ire.index.PageParser.Fields;
import com.ire.index.ParsingConstants;

public class DocDetails {
	private String docId;
	private int fieldType;
	private double tf;
	private List<DocDetails> resultDocs;
	
	public DocDetails(String details){
		//word#idf=docid-freq:weight;
		 int startIndex,endIndex;
		 startIndex= details.indexOf(ParsingConstants.CHAR_DOC_COUNT_DELIMITER);
		 docId=details.substring(0, startIndex);
		 
		 endIndex=details.indexOf(ParsingConstants.CHAR_WEIGHT_DELIMITER, startIndex);
		 fieldType= Integer.parseInt(details.substring(startIndex+1, endIndex));
		 
		 //startIndex = details.indexOf(ParsingConstants.CHAR_DOC_DELIMITER); // save asgn startIndex=endIndex :)
		 tf=Double.parseDouble(details.substring(endIndex+1));
		 resultDocs=new ArrayList<>();
		 resultDocs.add(this);
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	
	public int getFieldType() {
		return fieldType;
	}
	public void setFieldType(int fieldType) {
		this.fieldType = fieldType;
	}
	public double getTf() {
		return tf;
	}
	public void setTf(double tf) {
		this.tf = tf;
	}
	
	public List<DocDetails> getResultDocs() {
		return resultDocs;
	}
	public void setResultDocs(List<DocDetails> resultDocs) {
		this.resultDocs = resultDocs;
	}
	public static List<DocDetails> intersection(List<DocDetails> l1,List<DocDetails> l2,Fields field){
		List<DocDetails> common=new ArrayList<>();
		
		if(l2 == null){
			if(field == null) //no field type
				common.addAll(l1);
			else{
				for(DocDetails docDetail:l1){ //check field type
					if( (field.getSetbit() & docDetail.getFieldType()) == field.getSetbit() ){
						common.add(docDetail);
					}
				}
			}
			return common;
		}
		
		int len1=l1.size(),len2=l2.size(),diff;
		
		for(int i=0,j=0;i<len1 && j<len2; ){
			diff=l1.get(i).getDocId().compareTo(l2.get(j).getDocId());
			if(diff > 0)
				j++;
			else if(diff < 0)
				i++;
			else{
				if( field == null){
					l1.get(i).getResultDocs().add(l2.get(i));
					common.add(l1.get(i));
				}
				//field match
				else if( (field.getSetbit() & l2.get(i).getFieldType()) == field.getSetbit() ){
					l1.get(i).getResultDocs().add(l2.get(i));
					common.add(l1.get(i));
				}
				i++;
				j++;
			}
		}
		return common;
	}
}