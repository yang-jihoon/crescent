package com.tistory.devyongsik.index;

import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.apache.lucene.index.CorruptIndexException;
import org.junit.Test;

import com.tistory.devyongsik.config.CrescentCollectionHandler;
import com.tistory.devyongsik.domain.CrescentCollection;
import com.tistory.devyongsik.domain.CrescentCollections;
import com.tistory.devyongsik.handler.Handler;
import com.tistory.devyongsik.handler.IndexingRequestForm;
import com.tistory.devyongsik.handler.JsonDataHandler;
import com.tistory.devyongsik.utils.FormattedTextBuilder;

public class CrescentIndexerExecutorTest {
	
//	@AfterClass
//	public static void closeIndexWriter() throws CorruptIndexException, IOException {
//		IndexWriterManager indexWriterManager = IndexWriterManager.getIndexWriterManager();
//		IndexWriter indexWriter = indexWriterManager.getIndexWriter("sample");
//		
//		indexWriter.close();
//	}
	
	@Test
	public void addDocument() throws CorruptIndexException, IOException {
		CrescentCollections crescentCollections = CrescentCollectionHandler.getInstance().getCrescentCollections();
		Map<String, CrescentCollection> collections = crescentCollections.getCrescentCollectionsMap();

		CrescentCollection sampleCollection = collections.get("sample");

		Handler handler = new JsonDataHandler();
		IndexingRequestForm indexingRequestForm = handler.handledData(FormattedTextBuilder.getAddDocBulkJsonForm());
		
		CrescentIndexerExecutor executor = new CrescentIndexerExecutor(sampleCollection, indexingRequestForm);
		String returnMessage = executor.indexing();
		
		Assert.assertEquals("1건의 색인이 완료되었습니다.", returnMessage);
	}
	
	@Test
	public void deleteDocument() throws CorruptIndexException, IOException {
		CrescentCollections crescentCollections = CrescentCollectionHandler.getInstance().getCrescentCollections();
		Map<String, CrescentCollection> collections = crescentCollections.getCrescentCollectionsMap();

		CrescentCollection sampleCollection = collections.get("sample");

		Handler handler = new JsonDataHandler();
		IndexingRequestForm indexingRequestForm = handler.handledData(FormattedTextBuilder.getDeleteDocBulkJsonForm());
		
		CrescentIndexerExecutor executor = new CrescentIndexerExecutor(sampleCollection, indexingRequestForm);
		String returnMessage = executor.indexing();
		
		Assert.assertEquals("creuser:test에 대한 delete가 완료되었습니다.", returnMessage);
	}
	
	@Test
	public void updateDocument() throws CorruptIndexException, IOException {
		CrescentCollections crescentCollections = CrescentCollectionHandler.getInstance().getCrescentCollections();
		Map<String, CrescentCollection> collections = crescentCollections.getCrescentCollectionsMap();

		CrescentCollection sampleCollection = collections.get("sample");

		Handler handler = new JsonDataHandler();
		IndexingRequestForm indexingRequestForm = handler.handledData(FormattedTextBuilder.getUpdateDocBulkJsonForm());
		
		CrescentIndexerExecutor executor = new CrescentIndexerExecutor(sampleCollection, indexingRequestForm);
		String returnMessage = executor.indexing();
		
		Assert.assertEquals("creuser:test에 대한 update가 완료되었습니다.", returnMessage);
	}
}
