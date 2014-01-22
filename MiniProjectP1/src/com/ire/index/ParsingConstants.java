package com.ire.index;

import java.util.ArrayList;
import java.util.List;

public class ParsingConstants {
	public static final String ELE_TITLE="title";
	public static final String ELE_ID="id";
	public static final String ELE_TEXT="text";
	
	public static final String STOP_WORD_FILE="stopwords.txt";
	public static final String STOP_WORD_DELIMITER=",";
	
	public static final String INDEX_FILE_NAME="index.txt";
	public static final String WORD_DELIMITER="=";
	public static final String DOC_DELIMITER=";";
	public static final String DOC_COUNT_DELIMITER="-";
	
	public static final String DOC_PARSIGN_REGEX="[^a-z]";
	
	public static final String OFFSETS_FILE="offsets.txt";
	public static int pageNumber=0;
	public static int NumOfPagesInMap=0;
	public static final int NUM_OF_PAGES_PER_CHUNK=2000;
	
	public static String indexFileDir;
	
	public static List<String> subIndexFiles=new ArrayList<String>();
	
	public static int lastSubIndexFile=1000;
	public static String absoluteIndexFilePath;
}
