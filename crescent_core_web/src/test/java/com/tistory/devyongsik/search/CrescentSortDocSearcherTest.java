package com.tistory.devyongsik.search;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.lucene.document.Document;
import org.junit.Test;

import com.tistory.devyongsik.domain.SearchRequest;
import com.tistory.devyongsik.query.CrescentSearchRequestWrapper;

public class CrescentSortDocSearcherTest {
	
	@Test
	public void search() throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setKeyword("2");
		searchRequest.setCollectionName("sample");
		searchRequest.setSort("title_sort desc");
		
		CrescentSearchRequestWrapper csrw = new CrescentSearchRequestWrapper(searchRequest);
		
		CrescentDocSearcher crescentDocSearcher = new CrescentSortDocSearcher(csrw);
		List<Document> resultList = crescentDocSearcher.search();
		
		Assert.assertTrue(resultList.size() > 0);
	}
}
