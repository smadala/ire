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

import com.ire.sort.ExternalSort;

public class PageParser {
	
	private static Map<String,StringBuffer> allWords=new TreeMap<String,StringBuffer>();
	public static Set<String> stopWords;
	private Stemmer stemmer=new Stemmer();
	
	public void parse(WikiPage page) throws IOException{
		Map<String,Integer> wordCount=new HashMap<String, Integer>(256);
		String title=page.getTitle().toString().toLowerCase();
		parseText(title, wordCount);
		String text=page.getText().toString().toLowerCase();
		parseText(text,wordCount);
		
		insertToAllWords(page, wordCount);
	}
	
	public void insertToAllWords(WikiPage page, Map<String,Integer> wordCount) throws IOException{
		
		Iterator<Map.Entry<String,Integer> > entries=wordCount.entrySet().iterator();
		
		StringBuffer docList=null;
		while(entries.hasNext()){
			Map.Entry<String, Integer> entry=entries.next();
			 docList=allWords.get(entry.getKey());
			 if(docList == null){
				 docList=new StringBuffer();
				 docList.append(page.getId() /*+  ParsingConstants.DOC_COUNT_DELIMITER + entry.getValue()*/+ ParsingConstants.DOC_DELIMITER);
				 allWords.put(entry.getKey(), docList);
			 }else{
				 docList.append(page.getId()/*+ ParsingConstants.DOC_COUNT_DELIMITER +entry.getValue()*/+ ParsingConstants.DOC_DELIMITER);
			 }
		//System.out.println(entry.getKey()+ ":"+page.getId()+"-"+entry.getValue());
		}
		
		if( ParsingConstants.NUM_OF_PAGES_PER_CHUNK == ++ParsingConstants.NumOfPagesInMap ){
			dumpAllWords();
			ParsingConstants.NumOfPagesInMap=0;
			allWords = new TreeMap<String,StringBuffer>();
		}
	}
	
	
	public void dumpAllWords() throws IOException{
		 
		if(allWords.size() == 0)
			return;
		
		Writer writer=getWriterForDump();
		Iterator<Entry<String, StringBuffer>> entries = allWords.entrySet().iterator();
		StringBuffer blockOfData;
		Entry<String,StringBuffer> entry=null;
		
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
	
	
	
	public void parseText(String text,Map<String, Integer> wordCount){
		//String []tokens = text.split("[0-9&|\\]\\[{}\\s=><\\-!();\'\"\\*#$\\,\\\\/]");
		String []tokens = text.split(ParsingConstants.DOC_PARSIGN_REGEX);
		Integer count;
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
				wordCount.put(token, 1);
			}/*else{
				wordCount.put(token, count+1);
			}*/
		}
	}
	
	public void printPostingList(){
		Iterator< Map.Entry<String, StringBuffer> > entries=allWords.entrySet().iterator();
		PrintWriter writer=null;
		
		try {
			File indexFile =new File(ParsingConstants.indexFileDir, ParsingConstants.INDEX_FILE_NAME) ;
			
			
			writer=new PrintWriter(new FileOutputStream(indexFile));
			
		
		while(entries.hasNext()){
			Map.Entry<String,StringBuffer> entry=entries.next();
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
}
