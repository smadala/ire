package com.ire.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ire.index.PageParser.Fields;
import com.ire.index.ParsingConstants;
import com.ire.index.Stemmer;
import com.ire.index.WikiParser;

public class Search {

	
	//private static Map<String,Long> offsets;
	private static Stemmer stemmer=new Stemmer();
	private static List<RandomAccessFile> primayIndexeFiles;
	private static List<RandomAccessFile> indexFiles;
	private static List<TreeMap<String,Long>> secIndexes;
	
	private static RandomAccessFile titlePrimIndex;
	private static RandomAccessFile titleIndex;
	private static TreeMap<Integer, Long> titleSec;
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void loadSecondryIndexes(String indexFolder) throws Exception{
		String tokens[];
		TreeMap<String,Long> secIndex;
		secIndexes=new ArrayList<TreeMap<String,Long>>();
		primayIndexeFiles=new ArrayList<RandomAccessFile>();
		indexFiles=new ArrayList<RandomAccessFile>();
		File auxFile=null;
		for(int i=0;i<ParsingConstants.NUM_OF_INDEXFILES;i++){
			
			BufferedReader reader=new BufferedReader(new FileReader(new File(indexFolder,i+ParsingConstants.SECONDRY_SUFFIX)));
			secIndex=new TreeMap<String,Long>();
			for(String line; (line = reader.readLine())!= null; ){
				tokens=line.split(ParsingConstants.wordDelimiter);
				if(tokens.length != 2)
					continue;
				secIndex.put(tokens[0], Long.parseLong(tokens[1]));
			}
			secIndexes.add(secIndex);
			
			auxFile=new File(indexFolder,i+ParsingConstants.OFFSET_SUFFIX);
			primayIndexeFiles.add( new RandomAccessFile(auxFile.getAbsoluteFile(), "r"));
			
			auxFile=new File(indexFolder,i+ParsingConstants.INDEX_SUFFIX);
			indexFiles.add( new RandomAccessFile(auxFile.getAbsoluteFile(), "r"));
		}
		//Title indexes
		BufferedReader reader=new BufferedReader(new FileReader(new File(indexFolder,ParsingConstants.TITLES_FILE_PREFIX+ParsingConstants.SECONDRY_SUFFIX)));
		titleSec=new TreeMap<Integer,Long>();
		for(String line; (line = reader.readLine())!= null; ){
			tokens=line.split(ParsingConstants.wordDelimiter);
			if(tokens.length != 2)
				continue;
			titleSec.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
		}
		
		
		auxFile=new File(indexFolder,ParsingConstants.TITLES_FILE_PREFIX+ParsingConstants.OFFSET_SUFFIX);
		titlePrimIndex = new RandomAccessFile(auxFile.getAbsoluteFile(), "r");
		
		auxFile=new File(indexFolder,ParsingConstants.TITLES_FILE_PREFIX+ParsingConstants.INDEX_SUFFIX);
		titleIndex = new RandomAccessFile(auxFile.getAbsoluteFile(), "r");
	}
	
	public static void main(String[] args) throws Exception  {
		// TODO Auto-generated method stub
		
		
		long start = System.currentTimeMillis();
		final String indexFolder=args[0];
		loadSecondryIndexes(indexFolder);
		WikiParser.loadStopWords();
		final String queryFile=args[1];
		String line=null;
		
		
		String tokens[]=null;
		
		/*BufferedReader reader=new BufferedReader(new FileReader(new File(queryFile)));
		int numOfQueries=Integer.parseInt(reader.nextLine());*/
		Scanner reader=new Scanner(System.in);
		int numOfQueries=Integer.parseInt(reader.nextLine());
	//	String[] queryWords= new String[numOfQueries];
		

		for(int i=0;  i < numOfQueries;i++){
			List<QueryWord> queryWords=new ArrayList<>();
			QueryWord queryWord;
		//	line = reader.readLine();
			line = reader.nextLine();
			
			if(line != null){
				tokens =line.toLowerCase().split("\\s+");
				for(String word:tokens){
					queryWord=new QueryWord();
					if( word.length()>2 && word.charAt(1) == ':'){
						queryWord.setField(Fields.getField(word.charAt(0)));
						queryWord.setWord(stemmer.stemWord(word.substring(2)));
					}else{
						queryWord.setWord(stemmer.stemWord(word));
					}
					queryWords.add(queryWord);
				}
			}
			
			getPostingList(queryWords);
			for(QueryWord qWord:queryWords){
				qWord.makeDocDetails();
			}
			if(queryWords.size() == 1){
				List<String> docIds=new ArrayList<>();
				queryWord=queryWords.get(0);
				if(queryWord.getField() == null || true){
					queryWord.sortDocDetailsByTf();
				}
				for(int index=0;index<ParsingConstants.MIN_RESULTSET && index < queryWord.getDocDetails().size() ;index++){
					docIds.add(queryWord.getDocDetails().get(i).getDocId());
				}
				displayTitles(docIds);
			}
			Collections.sort(queryWords, QueryWord.SORT_BY_IDF);
			
			List<List<DocDetails>> andDocDetails;
			
			andDocDetails=findAndOfDocDetails(queryWords);
			
			int resultIndex=0; boolean canDesplay=false;
			for(int index=andDocDetails.size()-1;index>=0;index--){
				if(andDocDetails.get(index).size() >= ParsingConstants.MIN_RESULTSET ){
					resultIndex=index;
					canDesplay=true;
				}
			}
			if(canDesplay){
				displayResults(queryWords,andDocDetails.get(resultIndex),resultIndex);
				
			}else{
				
			}
		}
		
		//System.out.print( System.currentTimeMillis() - start  +" Exit");
	}
	
	private static void displayResults(List<QueryWord> queryWords,
			List<DocDetails> relevantDocs, int depthIndex) throws IOException{
		List<Double> idfs=new ArrayList<>();
		List<Double> tfs=new ArrayList<>();
		Map<Double,List<String>> cosAngle=new TreeMap<>(Collections.reverseOrder()); 
		for(int i=0;i<depthIndex;i++){
			tfs.add(1d);
			idfs.add(queryWords.get(i).getIdf());
		}
		List<DocDetails> matchedDocs;
		List<Double> queryUnitVector=getUnitVector(idfs, tfs);
		List<Double> docUnitVector;
		List<String> docIds;
		double distance=0d;
		for(DocDetails docDetail:relevantDocs){
			tfs=new ArrayList<>();
			distance=0d;
			matchedDocs=docDetail.getResultDocs();
			for( int i=0;i<depthIndex;i++){
				tfs.add(matchedDocs.get(i).getTf());
			}
			docUnitVector=getUnitVector(idfs, tfs);
			
			for(int i=0;i<depthIndex;i++){
				distance+=docUnitVector.get(i)*queryUnitVector.get(i);
			}
			docIds = cosAngle.get(distance);
			if(docIds == null){
				docIds=new ArrayList<>();
			}
			docIds.add(docDetail.getDocId());
			cosAngle.put(distance, docIds);
		}
		
		int numOfResults=0,beginIndex,endIndex;
		String title;
		
		for(Double dist:cosAngle.keySet()){
			for(String docId:cosAngle.get(dist)){
				title=getTitle(docId);
				if(title == null)
					continue;
				beginIndex=title.indexOf(ParsingConstants.wordDelimiter);
				System.out.println(title.substring(beginIndex+1));
				if(numOfResults++ == ParsingConstants.MIN_RESULTSET)
					break;
			}
		}
	}
	private static void displayTitles(List<String> docIds) throws IOException{
		String title;
		int beginIndex;
		for(String docId:docIds){
			title=getTitle(docId);
			if(title == null)
				continue;
			beginIndex=title.indexOf(ParsingConstants.wordDelimiter);
			System.out.println(title.substring(beginIndex+1));
		}
	}
	private static List<Double> getUnitVector(List<Double> idfs,List<Double> tfs){
		List<Double> unitVector=new ArrayList<>(idfs.size());
		List<Double> temp =new ArrayList<>();
		double length=0;
		for(int i=0;i<idfs.size(); i++){
			temp.add(idfs.get(i)*tfs.get(i));
			length+=temp.get(i)*temp.get(i);
		}
		length=Math.sqrt(length);
		for(int i=0;i<temp.size();i++){
			unitVector.add(temp.get(i)/length);
		}
		return unitVector;
	}
	public static void getPostingList(List<QueryWord> queryWords){
		try {
			int beginIndex,endIndex;
			double idf;
			String postingList=null;
			for( QueryWord queryWord: queryWords){
				
				if( (postingList = getPostingList(queryWord.getWord())) != null){
					queryWord.setRawPostingList(postingList);
					beginIndex=postingList.indexOf(ParsingConstants.wordDelimiter);
					endIndex=postingList.indexOf(ParsingConstants.CHAR_WORD_DELIMITER);
					idf=Double.parseDouble(postingList.substring(beginIndex+1, endIndex));
					queryWord.setIdf(idf);
					continue;
				}else{
					queryWords.remove(queryWord);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String getPostingList(String word) throws IOException{
		if(word == null || word.length() == 0)
			return null;
		int filePrefix=word.charAt(0) - 'a';
		if(filePrefix < 0){
			filePrefix=ParsingConstants.TITLES_FILE_PREFIX;
		}
		TreeMap<String,Long> secIndex=secIndexes.get(filePrefix);
		Entry<String,Long> entry = secIndex.floorEntry(word);
		if(entry == null)
			return null;
		long startOffset=entry.getValue(),offset=-1;
		RandomAccessFile primaryIndex=primayIndexeFiles.get(filePrefix),indexFile;
		
		primaryIndex.seek(startOffset);
		String line,tokens[];
		int diff;
		
		while((line = primaryIndex.readLine()) != null){
			tokens=line.split(ParsingConstants.wordDelimiter);
			if(tokens.length != 2)
				continue;
			diff=word.compareTo(tokens[0]);
			if(diff > 0)
				continue;
			else if(diff == 0)
				offset = Long.parseLong(tokens[1]);
			else
				offset = -1;
			break;
		}
		if(offset == -1){
			return null;
		}else{
			indexFile=indexFiles.get(filePrefix);
			indexFile.seek(offset);
			return indexFile.readLine();
		}
	}
	
	private static String getTitle(String word) throws IOException{
		
		if(word == null || word.length() == 0)
			return null;
		Integer docId=Integer.parseInt(word);
		int filePrefix=ParsingConstants.TITLES_FILE_PREFIX;
		Entry<Integer,Long> entry = titleSec.floorEntry(docId);
		if(entry == null)
			return null;
		long startOffset=entry.getValue(),offset=-1;
		
		titlePrimIndex.seek(startOffset);
		String line,tokens[];
		int diff;
		
		while((line = titlePrimIndex.readLine()) != null){
			tokens=line.split(ParsingConstants.wordDelimiter);
			if(tokens.length != 2)
				continue;
			diff=docId.compareTo(Integer.parseInt(tokens[0]));
			if(diff > 0)
				continue;
			else if(diff == 0)
				offset = Long.parseLong(tokens[1]);
			else
				offset = -1;
			break;
		}
		if(offset == -1){
			return null;
		}else{
			titleIndex.seek(offset);
			return titleIndex.readLine();
		}
	}
	
	//word#idf=docid-freq:weight;
	
	public static List<List<DocDetails>> findAndOfDocDetails(List<QueryWord> queryWords){
		
		/*for(String rawPostingList:rawPostingLists){
			postingList=new PostingList();
			
			idfStartPos = rawPostingList.indexOf(ParsingConstants.WORD_IDF_DELIMITER);
			postingList.setWord(rawPostingList.substring(0, idfStartPos));
			
			idfEndPos =rawPostingList.indexOf(ParsingConstants.CHAR_WORD_DELIMITER, idfStartPos);
			idf=Double.parseDouble(rawPostingList.substring(idfStartPos+1, idfEndPos));// +1 for ignore #
			postingList.setIdf(idf);
			
			postingList.setDocIds(rawPostingList.substring(idfEndPos+1)); // +1 for ignore =
			postingLists.add(postingList);
		}
		Collections.sort(postingLists, PostingList.SORT_BY_IDF);*/
		
		//List <  List<DocDetails> >
		List<DocDetails> andDocs,tempRelevenceDocs,orDocs;
		List<List<DocDetails>> andDocsList= new ArrayList<>();
		if(queryWords.size() == 0 )
			return andDocsList;
		QueryWord queryWord=queryWords.get(0);
 		andDocs = DocDetails.intersection(queryWord.getDocDetails(),null,queryWord.getField()); // to check field
		andDocsList.add(andDocs);
		
		for(int i=1;i<queryWords.size();i++){
			queryWord=queryWords.get(i);
			andDocs=DocDetails.intersection(
					andDocsList.get(andDocsList.size()-1),queryWord.getDocDetails() ,null);
			andDocsList.add(andDocs);
		}
		
		return andDocsList;
	}
	
	//word#idf=docid-tbci:tf;
	/*public static List<DocDetails> getDocDetails(String docIds){
		List<DocDetails> docDetails=new ArrayList<>();
		int  len=docIds.length(), endIndex,beginIndex=0;
		
		while(true){
			endIndex=docIds.indexOf(ParsingConstants.CHAR_DOC_DELIMITER,beginIndex);
			docDetails.add(new DocDetails(docIds.substring(beginIndex,endIndex)));
			if(endIndex == len-1)
				break;
			beginIndex=endIndex+1;
		}
		return docDetails;
	}*/
	public static void displayWordLine(String line){
		
		String prev="1",cur;
		String tokens[] = line.split(ParsingConstants.WORD_DELIMITER); // divide word and [doc,count] 
		
		
		String docCounts[] = tokens[1].split(ParsingConstants.DOC_DELIMITER); //divide docs to doc-count 
		
		//TODO: sort result
		StringBuilder result=new StringBuilder(docCounts.length * 9);
		
		Set<Integer> docIds=new TreeSet<Integer>();
		
		
		for(String docCount:docCounts ){
			//System.out.println(docCount.split(ParsingConstants.DOC_COUNT_DELIMITER)[0]); // print document id
			cur=docCount.split(ParsingConstants.DOC_COUNT_DELIMITER)[0];
			/*if(docCounts.length > 20)
			System.out.println(cur);
			if(cur.compareTo(prev) <= 0){
				System.out.println("Error at"+ cur + "       "+ prev);
			}*/
			docIds.add(Integer.parseInt(cur));
		}
		/*if(docCounts.length > 20)
		System.out.println("\n\n\n");*/
		for(Integer docId:docIds){
		     result.append(docId +",");	
		}
		result.delete(result.length() - 1 , result.length() );
		System.out.println(result);
	}
}
