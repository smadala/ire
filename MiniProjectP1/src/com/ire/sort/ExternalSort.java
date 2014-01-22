package com.ire.sort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.ire.index.ParsingConstants;

public class ExternalSort {

	/**
	 * @param args
	 * @throws IOException 
	 */
	/*public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	   	
		
	}*/
	
	public static Map<String,Long> getOffsets(String offSetFileDir) {
		
		Map<String,Long> offsets=new HashMap<String,Long>();
		BufferedReader address=null;
		try{
			address=new BufferedReader(new FileReader(new File(offSetFileDir,ParsingConstants.OFFSETS_FILE)));
		
			for(String line; (line = address.readLine()) != null ;){
			
			String tokens[]= line.split(ParsingConstants.WORD_DELIMITER);
			if(tokens.length != 2)
				continue;
			offsets.put(tokens[0], Long.parseLong(tokens[1]));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		/*RandomAccessFile index=new RandomAccessFile(new File("index.txt"), "r");
		  int lines=0;
		for( Long offset: offsets.values()){
			index.seek(offset);
			System.out.println( index.readLine() + ++lines );
		}*/
		
		return offsets;
	}
	
	/**
		 find starting address of each line from indexFile and save to offsetsFile 
	*/
	public static void createOffsetsFile(String indexFile, String offsetsFileDir){
		PrintWriter wordsWriter=null;
		BufferedReader br=null;
		try{
			File wordsFile=new File(offsetsFileDir,ParsingConstants.OFFSETS_FILE);
			//System.out.println(indexFileDir+  "  "+ParsingConstants.INDEX_FILE_NAME);
			br = new BufferedReader(new FileReader(new File(indexFile)));
			
            String word=null;
            String line=null;
            wordsWriter=new PrintWriter(new FileOutputStream(wordsFile));
            long lineStart=0;
            long lineLength=0;
            //String words[];
            
            //TODO: optimize by using byte array instead of converting to String
            while( (line = br.readLine()) != null ){
            	 lineLength = line.length();
            	 word=line.split(ParsingConstants.WORD_DELIMITER)[0]; // get word
            	 wordsWriter.println(word + ParsingConstants.WORD_DELIMITER + lineStart); 
            	 lineStart+=lineLength+1; // include newline character 
            }
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(wordsWriter != null)
				wordsWriter.close();
		}
	}
}
