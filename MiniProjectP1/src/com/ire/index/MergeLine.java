package src.com.ire.index;

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
		if(o == null)
			return 1;
		return word.compareTo(o.getWord());
	}
}
