package com.ire.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ire.index.ParsingConstants;
import com.ire.index.PageParser.Fields;

public class QueryWord {
	private String rawPostingList;
	private String word;
	private Fields field;
	private double idf;
	List<DocDetails> docDetails;
	
	public String getRawPostingList() {
		return rawPostingList;
	}
	public void setRawPostingList(String rawPostingList) {
		this.rawPostingList = rawPostingList;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public Fields getField() {
		return field;
	}
	public void setField(Fields field) {
		this.field = field;
	}
	public double getIdf() {
		return idf;
	}
	public void setIdf(double idf) {
		this.idf = idf;
	}
	
	
	public List<DocDetails> getDocDetails() {
		return docDetails;
	}
	public void setDocDetails(List<DocDetails> docDetails) {
		this.docDetails = docDetails;
	}


	public static CompareByIDF SORT_BY_IDF=new CompareByIDF();
	private static class CompareByIDF implements Comparator<QueryWord> {

		@Override
		public int compare(QueryWord o1, QueryWord o2) {
			// TODO Auto-generated method stub
			double diff=o1.getIdf() - o2.getIdf();
			 if(diff > 0)
				 return -1;
			 else if(diff < 0)
				 return 1;
			 return 0;
		}
		
	}
	public List<DocDetails> makeDocDetails(){
		docDetails=new ArrayList<>();
		int  len=rawPostingList.length(), endIndex,beginIndex=0;
		beginIndex=rawPostingList.indexOf(ParsingConstants.CHAR_WORD_DELIMITER);
		beginIndex++;
		while(true){
			endIndex=rawPostingList.indexOf(ParsingConstants.CHAR_DOC_DELIMITER,beginIndex);
			if(endIndex < 0)
				break;
			docDetails.add(new DocDetails(rawPostingList.substring(beginIndex,endIndex)));
			beginIndex=endIndex+1;
		}
		return docDetails;
	}
	
	public void sortDocDetailsByTf(){
		Collections.sort(docDetails, SORT_BY_TF);
	}
	
	public static CompareByTf SORT_BY_TF=new CompareByTf();
	private static class CompareByTf implements Comparator<DocDetails>{

		@Override
		public int compare(DocDetails o1, DocDetails o2) {
			// TODO Auto-generated method stub
			return Double.compare(o1.getTf(), o2.getTf());
		}
		
	}
}
