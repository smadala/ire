package com.ire.search;

import java.util.Comparator;

public class QueryWord {
	
	private String word;
	private double idf;
	private String docIds;
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public double getIdf() {
		return idf;
	}
	public void setIdf(double idf) {
		this.idf = idf;
	}
	public String getDocIds() {
		return docIds;
	}
	public void setDocIds(String docIds) {
		this.docIds = docIds;
	}
	
	public static CompareByIDF SORT_BY_IDF=new CompareByIDF();
	private static class CompareByIDF implements Comparator<QueryWord> {

		@Override
		public int compare(QueryWord o1, QueryWord o2) {
			// TODO Auto-generated method stub
			double diff=o1.getIdf() - o1.getIdf();
			 if(diff > 0)
				 return 1;
			 else if(diff < 0)
				 return -1;
			 return 0;
		}
		
	}
}
