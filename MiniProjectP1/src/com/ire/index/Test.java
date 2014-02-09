package com.ire.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.ire.search.DocDetails;
import com.ire.search.QueryWord;

public class Test {
	static Random random;
	static List<DocDetails> list=new ArrayList<>();
	//word#idf=docid-freq:weight;
	public static void main(String arg[]){
		random=new Random();
		DocDetails docDetails;
		String s;
		for(int i=0;i<5;i++){
			s=random.nextInt(100)+"-"+random.nextInt(32)+":" +random.nextDouble();
          	docDetails=new DocDetails(s);
			System.out.println(s);
          	list.add(docDetails);
		}
		Collections.sort(list,QueryWord.SORT_BY_TF);
		for(DocDetails doc:list){
			System.out.println( doc.getDocId() +"  "+doc.getFieldType() +" "+doc.getTf());
		}
	}
}
