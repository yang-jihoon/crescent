package com.tistory.devyongsik.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tistory.devyongsik.domain.CrescentCollectionField;

/**
 * author : need4spd, need4spd@naver.com, 2012. 3. 4.
 */
public class LuceneDocumentBuilder {
	public static List<Document> buildDocumentList(List<Map<String, String>> docList, 
												   Map<String, CrescentCollectionField> fieldsByName) {
		
		Logger logger = LoggerFactory.getLogger(LuceneDocumentBuilder.class);
		
		List<Document> documentList = new ArrayList<Document>();
		
		LuceneFieldBuilder luceneFieldBuilder = new LuceneFieldBuilder();
		
		for(Map<String, String> doc : docList) {
			//data filed에 있는 필드들..
			Set<String> fieldNamesFromDataFile = doc.keySet();
			
			Document document = new Document();
			
			for(String fieldName : fieldNamesFromDataFile) {
				String value = doc.get(fieldName);
				
				CrescentCollectionField crescentCollectionField = fieldsByName.get(fieldName);
				
				if(crescentCollectionField == null) {
					logger.error("해당 collection에 존재하지 않는 필드입니다. [{}]", fieldName);
					throw new IllegalStateException("해당 collection에 존재하지 않는 필드입니다. ["+fieldName+"]");
				}
				
				Field field = luceneFieldBuilder.create(fieldsByName.get(fieldName), value);
				document.add(field);
			}
			
			documentList.add(document);
		}
		
		return documentList;
	}
}
