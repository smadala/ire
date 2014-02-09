package com.ire.index;


public class MergeLine implements Comparable<MergeLine>{
	private String word;
	private  int fileNum;
	private String docIds;
	public MergeLine(int fileNum, String word,String docIds){
		this.word=word;
		this.fileNum=fileNum;
		this.docIds=docIds;
	}
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getFileNum() {
		return fileNum;
	}

	public void setFileNum(int fileNum) {
		this.fileNum = fileNum;
	}

	public String getDocIds() {
		return docIds;
	}

	public void setDocIds(String docIds) {
		this.docIds = docIds;
	}

	@Override
	public int compareTo(MergeLine o) {
		int diff=0;
		if(o == null)
			return 1;
	//	return word.compareTo(o.getWord());
		diff = word.compareTo(o.getWord());
		if( diff == 0){
			int end1,end2, docId1, docId2;
			end1=docIds.indexOf(ParsingConstants.CHAR_DOC_COUNT_DELIMITER);
			docId1=Integer.parseInt(docIds.substring(0, end1));
			end2=o.getDocIds().indexOf(ParsingConstants.CHAR_DOC_COUNT_DELIMITER);
			docId2=Integer.parseInt(o.getDocIds().substring(0, end2));
			return docId1>docId2?1:-1;
		}
		return diff;
	}
	/*public class DocDetails implements Comparator<DocDetails>{
		private int docId;
		private String otherValues;

		@Override
		public int compare(DocDetails o1, DocDetails o2) {
			// TODO Auto-generated method stub
			if( docId,otherValues )
		}

		public int getDocId() {
			return docId;
		}

		public void setDocId(int docId) {
			this.docId = docId;
		}

		public String getOtherValues() {
			return otherValues;
		}

		public void setOtherValues(String otherValues) {
			this.otherValues = otherValues;
		}
		
	}
	public String sortDocIds(){
		
	}*/
}
