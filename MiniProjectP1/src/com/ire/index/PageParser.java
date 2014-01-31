package com.ire.index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import com.ire.sort.ExternalSort;

public class PageParser {
	
	public static int totalNumberOfDoc=0;
	private static Map<String,StringBuilder> allWords=new TreeMap<String,StringBuilder>();
	public static Set<String> stopWords;
	private Stemmer stemmer=new Stemmer();
	enum Fields{
		TITLE('t',25), BODY('b',1), INFOBOX('i',5), LINKS('l',5), CATAGORY('c',5);
		private char shortForm;
		private int weight;
		private Fields(char shortForm,int weight){
			this.shortForm=shortForm;
			this.weight=weight;
		}
		public char getShortForm() {
			return shortForm;
		}
		public int getWeight(){
			return weight;
		}
	}
	
	public void parse(WikiPage page) throws IOException{
		
		totalNumberOfDoc++;
		Map<String,Integer[]> wordCount=new HashMap<String, Integer[]>(256);
		String aux=page.getTitle().toString().toLowerCase();
		parseText(aux, wordCount,Fields.TITLE);
		
		aux=page.getText().toString().toLowerCase();
		parseText(aux,wordCount, Fields.BODY);
		
		aux=page.getInfoBox().toString().toLowerCase();
		parseText(aux,wordCount, Fields.INFOBOX);
		
		aux=page.getExternalLinks().toString().toLowerCase();
		parseText(aux,wordCount, Fields.LINKS);
		
		aux=page.getCategory().toString().toLowerCase();
		parseText(aux,wordCount, Fields.CATAGORY);
		
		insertToAllWords(page, wordCount);
	}
	
	public void insertToAllWords(WikiPage page, Map<String,Integer[]> wordCount) throws IOException{
		
		Iterator<Map.Entry<String,Integer[]> > entries=wordCount.entrySet().iterator();
		
		StringBuilder docList=null;
		while(entries.hasNext()){
			Map.Entry<String, Integer[]> entry=entries.next();
			 docList=allWords.get(entry.getKey());
			 if(docList == null){
				 docList=new StringBuilder();
				 docList.append(page.getId())
				 .append( ParsingConstants.DOC_COUNT_DELIMITER)
				 .append(getFieldsString(entry.getValue())
						 .append( ParsingConstants.DOC_DELIMITER));
				 allWords.put(entry.getKey(), docList);
			 }else{
				 docList.append(page.getId())
				 .append( ParsingConstants.DOC_COUNT_DELIMITER)
				 .append(getFieldsString(entry.getValue())
						 .append( ParsingConstants.DOC_DELIMITER));
			 }
		//System.out.println(entry.getKey()+ ":"+page.getId()+"-"+entry.getValue());
		}
		
		if( ParsingConstants.NUM_OF_PAGES_PER_CHUNK == ++ParsingConstants.NumOfPagesInMap ){
			dumpAllWords();
			ParsingConstants.NumOfPagesInMap=0;
			allWords = new TreeMap<String,StringBuilder>();
		}
	}
	
	
	public void dumpAllWords() throws IOException{
		 
		if(allWords.size() == 0)
			return;
		
		Writer writer=getWriterForDump();
		Iterator<Entry<String, StringBuilder>> entries = allWords.entrySet().iterator();
		StringBuffer blockOfData;
		Entry<String,StringBuilder> entry=null;
		
		while( entries.hasNext()){
			 blockOfData=new StringBuffer(2048);
			 entry = entries.next();
			 blockOfData.append(entry.getKey())
			 .append(ParsingConstants.WORD_DELIMITER)
			 .append(entry.getValue())
			 .append("\n");
			 //if( i % 100 == 0) {
			 //writeData(blockOfData.toString(), writer);
			 writer.write(blockOfData.toString());
			 //}
		}
		
		/*if(blockOfData.length() > 0)
			writeData(blockOfData.toString(), writer);*/
	
		try {
			if(writer != null)
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeData(String blockOfData, Writer writer ){
		try {
			writer.write(blockOfData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Writer getWriterForDump() {
		
		File dumpFile=new File(ParsingConstants.indexFileDir,""+ ParsingConstants.lastSubIndexFile++ );
		ParsingConstants.subIndexFiles.add(dumpFile.getAbsolutePath());
		try {
			return new BufferedWriter(new FileWriter(dumpFile,false));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public void parseText(String text,Map<String, Integer[]> wordCount, Fields type){
		//String []tokens = text.split("[0-9&|\\]\\[{}\\s=><\\-!();\'\"\\*#$\\,\\\\/]");
		String []tokens = text.split(ParsingConstants.DOC_PARSIGN_REGEX);
		Integer[] count;
		for(String token:tokens){
			
			if(token.isEmpty()){
				//System.out.println("empty");
				continue;
			}
			
			if( stopWords.contains(token))
				continue;
			
			//Stemming
			token = stemmer.stemWord(token);
					
			count=wordCount.get(token);
			
			if(count == null){
				count = new Integer[]{0,0,0,0,0};
				count[type.ordinal()]++;
				wordCount.put(token, count);
			}else{
				count[type.ordinal()]++;
				wordCount.put(token, count);
			}
		}
	}
	
	public void printPostingList(){
		Iterator< Map.Entry<String, StringBuilder> > entries=allWords.entrySet().iterator();
		PrintWriter writer=null;
		
		try {
			File indexFile =new File(ParsingConstants.indexFileDir, ParsingConstants.INDEX_FILE_NAME) ;
			
			
			writer=new PrintWriter(new FileOutputStream(indexFile));
			
		
		while(entries.hasNext()){
			Map.Entry<String,StringBuilder> entry=entries.next();
			/*if(entry.getValue().length() > 12000 * 6)
				continue;*/
			//System.out.println(entry.getKey()+"="+entry.getValue());
		
			writer.println(entry.getKey() + ParsingConstants.WORD_DELIMITER + entry.getValue());
			
			//writer.println(indexFile.length());
			
			//write.print()
			
		}
		ParsingConstants.absoluteIndexFilePath=indexFile.getAbsolutePath();
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(writer != null)
				writer.close();
		}
		ExternalSort.createOffsetsFile(ParsingConstants.absoluteIndexFilePath, ParsingConstants.OFFSETS_FILE);
	}
	
	public void mergeSubIndexFiles() throws IOException{

		File indexFile =new File(ParsingConstants.indexFileDir, ParsingConstants.INDEX_FILE_NAME) ;
		ParsingConstants.absoluteIndexFilePath=indexFile.getAbsolutePath();
		//System.out.println(ParsingConstants.indexFileDir);
		//System.out.println(ParsingConstants.absoluteIndexFilePath);
        
		if(ParsingConstants.subIndexFiles==null || ParsingConstants.subIndexFiles.size() == 0)
			return;
		
		else if(ParsingConstants.subIndexFiles.size() == 1){
				new File(ParsingConstants.subIndexFiles.get(0)).renameTo(indexFile);
			return;
		}
		
		List<BufferedReader> readers = getReaderOfSubIndexFiles();
		boolean reachedEOF[]=new boolean[readers.size()];
		
		PriorityQueue<MergeLine> pq=new PriorityQueue<MergeLine>(ParsingConstants.subIndexFiles.size());
		BufferedWriter indexFileWriter = new BufferedWriter( new FileWriter(indexFile,false));
		
		
		for(int i=0; i<readers.size(); i++){
			nextMergeLine(pq, readers, i,reachedEOF);
		}
		
		List<MergeLine> sameWords=new ArrayList<MergeLine>();;
		MergeLine mergeLine=null;
		String currentWord=null;
		
		// get least line from PQ and set current word
		mergeLine = pq.poll();
		nextMergeLine(pq, readers,mergeLine.getFileNum(),reachedEOF);
		currentWord=mergeLine.getWord();
		sameWords.add(mergeLine);
		
		while(!pq.isEmpty()){
			mergeLine = pq.poll();
			// if same word then we have to append
			if(currentWord.equals(mergeLine.getWord())){
				sameWords.add(mergeLine);
			}else{  // append all docIds of currentWord
				StringBuffer wholeLine=new StringBuffer();
				//Add word
				wholeLine.append(currentWord).append(ParsingConstants.WORD_DELIMITER);
				//append docIds
				for(MergeLine sameWord:sameWords){
					wholeLine.append(sameWord.getDocIds());
				}
				
				wholeLine.append("\n");
				
				writeData(wholeLine.toString(), indexFileWriter);
				sameWords=new ArrayList<MergeLine>();
				sameWords.add(mergeLine);
				currentWord=mergeLine.getWord();
		//		printLineToIndexFile(sameWords, currentWord, indexFileWriter, mergeLine);
		   }
			nextMergeLine(pq, readers, mergeLine.getFileNum(), reachedEOF);
		}
		StringBuffer wholeLine=new StringBuffer();
		//Add word
		wholeLine.append(currentWord).append(ParsingConstants.WORD_DELIMITER);
		//append docIds
		for(MergeLine sameWord:sameWords){
			wholeLine.append(sameWord.getDocIds());
		}
		
		wholeLine.append("\n");
		writeData(wholeLine.toString(), indexFileWriter);
		//printLineToIndexFile(sameWords, currentWord, indexFileWriter, mergeLine);
		
		
		
		for(BufferedReader reader:readers){
			if(reader != null)
				reader.close();
		}
		if(indexFileWriter != null)
			indexFileWriter.close();
		deleteFiles(ParsingConstants.subIndexFiles);
	}
	
	public void printLineToIndexFile(List<MergeLine> sameWords, String currentWord, 
			BufferedWriter indexFileWriter,MergeLine mergeLine)
	{
		StringBuffer wholeLine=new StringBuffer();
		//Add word
		wholeLine.append(currentWord).append(ParsingConstants.WORD_DELIMITER);
		//append docIds
		for(MergeLine sameWord:sameWords){
			wholeLine.append(sameWord.getDocIds());
		}
		
		wholeLine.append("\n");
		
		writeData(wholeLine.toString(), indexFileWriter);
		sameWords=new ArrayList<MergeLine>();
		sameWords.add(mergeLine);
		currentWord=mergeLine.getWord();
		
	}	
	
	public void deleteFiles(List<String> filePaths){
		for(String filePath:filePaths){
			new File(filePath).deleteOnExit();
		}
	}
	
	
	public void nextMergeLine(PriorityQueue<MergeLine> pq ,List<BufferedReader> readers,
			int readerNum, boolean[] reachedEOF) throws IOException{
		
		String line=null, word=null, docIds=null;
		int delimiterIndex=0;
		int numberOfFileReachedEnd=0;
		int numberOfReaders=readers.size();
		while( numberOfFileReachedEnd != numberOfReaders && reachedEOF[readerNum] ){
			readerNum = (readerNum+1) % numberOfReaders;
			numberOfFileReachedEnd++;
		}
		
		if( numberOfFileReachedEnd == numberOfReaders)
			return;
		
		if ( (line = readers.get(readerNum).readLine() ) != null){
			delimiterIndex = line.indexOf(ParsingConstants.WORD_DELIMITER);
			word=line.substring(0,delimiterIndex);
			docIds=line.substring(delimiterIndex+1);
			pq.add(new MergeLine(readerNum, word, docIds));
		}else{
			reachedEOF[readerNum]=true;
			nextMergeLine(pq, readers, (readerNum+1) % numberOfReaders, reachedEOF);
		}
	}
	
	public List<BufferedReader> getReaderOfSubIndexFiles(){
		List<BufferedReader> readers=new ArrayList<BufferedReader>(ParsingConstants.subIndexFiles.size());
		for(String file:ParsingConstants.subIndexFiles){
			try {
				readers.add(new BufferedReader(new FileReader(new File(file))));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return readers;	
	}
	private StringBuilder getFieldsString(Integer[] values){
		
		StringBuilder valueString=new StringBuilder();
		int weight=0;
		for(Fields field:Fields.values()){
			if(values[field.ordinal()] == 0)
				continue;
			weight = weight + (values[field.ordinal()].intValue() * field.getWeight());
			valueString.append(field.getShortForm()).append(values[field.ordinal()]);
		}
		valueString.append(ParsingConstants.WEIGHT_DELIMITER)
		.append( ParsingConstants.decimalFormat.format((termFrequence(weight))) );
		return valueString;
	}
	
	private Double termFrequence(int weight){
		double result=0;
		if(weight == 0)
			return new Double(0);
		else{
			result= 1 + Math.log10(weight);
		}
		return result;
	}
}
