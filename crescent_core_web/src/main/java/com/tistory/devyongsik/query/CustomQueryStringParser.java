package com.tistory.devyongsik.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tistory.devyongsik.domain.CrescentCollectionField;
import com.tistory.devyongsik.exception.CrescentUnvalidRequestException;

public class CustomQueryStringParser {

	private Logger logger = LoggerFactory.getLogger(CustomQueryStringParser.class);
	private static Pattern pattern = Pattern.compile("(.*?)(:)(\".*?\")");
	
	public Query getQueryFromCustomQuery(List<CrescentCollectionField> indexedFields, String customQueryString, Analyzer analyzer) 
			throws CrescentUnvalidRequestException {
		
		//패턴분석
		Matcher m = pattern.matcher(customQueryString);
		
		String fieldName = "";
		Occur occur = Occur.SHOULD;
		String userRequestQuery = "";
		float boost = 0F;
		
		boolean isRangeQuery = false;
		BooleanQuery resultQuery = new BooleanQuery();
		
		CrescentCollectionField searchTargetField = null;
		
		while(m.find()) {
			if(m.groupCount() != 3) {
				throw new CrescentUnvalidRequestException("쿼리 문법 오류. [" + customQueryString + "]");
			}
			
			fieldName = m.group(1).trim();
			if(fieldName.startsWith("-")) {
				occur = Occur.MUST_NOT;
				fieldName = fieldName.substring(1);
			} else if (fieldName.startsWith("+")) {
				occur = Occur.MUST;
				fieldName = fieldName.substring(1);
			}
			
			//field가 검색 대상에 있는지 확인..
			boolean any = true;
			for(CrescentCollectionField crescentField : indexedFields) {
				if(fieldName.equals(crescentField.getName())) {
					any = false;
					searchTargetField = crescentField;
					
					logger.debug("selected searchTargetField : {} ", searchTargetField);
					break;
				}
			}
			
			if(any) {
				logger.error("검색 할 수 없는 필드입니다. {} " , fieldName);
				throw new CrescentUnvalidRequestException("검색 할 수 없는 필드입니다. [" + fieldName + "]");
			}
			
			
			userRequestQuery = m.group(3).trim().replaceAll("\"", "");
			if((userRequestQuery.startsWith("[") && userRequestQuery.endsWith("]")) 
					|| (userRequestQuery.startsWith("{") && userRequestQuery.endsWith("}"))) {
				
				isRangeQuery = true;
			
			}
			
			//boost 정보 추출
			int indexOfBoostSign = userRequestQuery.indexOf("^");
			if(indexOfBoostSign >= 0) {
				boost = Float.parseFloat(userRequestQuery.substring(indexOfBoostSign+1));
				userRequestQuery = userRequestQuery.substring(0, indexOfBoostSign);
			}
			
			logger.info("user Request Query : {} ", userRequestQuery);
			logger.info("boost : {} ", boost);
			
			//range쿼리인 경우에는 RangeQuery 생성
			if(isRangeQuery) {
	
				QueryParser qp = new QueryParser(Version.LUCENE_36, fieldName, analyzer);
	
				try {
					Query query = qp.parse(userRequestQuery.replace("to", "TO"));
					resultQuery.add(query, occur);
					
					logger.debug("Query : {} ", query);
					logger.debug("Result Query : {} ", resultQuery);
					
				} catch (ParseException e) {
					logger.error("Exception in CustomQuery Parser ", e);
					throw new CrescentUnvalidRequestException("Range Query 문법 오류 [" + userRequestQuery + "]");
				}
			} else {
				//쿼리 생성..
				String[] keywords = userRequestQuery.split( " " );
				
				if(logger.isDebugEnabled()) {
					logger.debug("split keyword : {}", Arrays.toString(keywords));
				}
				
				for(int i = 0; i < keywords.length; i++) {
					ArrayList<String> analyzedTokenList = analyzedTokenList(analyzer, keywords[i]);

					if(analyzedTokenList.size() == 0) {
						
						Term t = new Term(fieldName, keywords[i]);
						Query query = new TermQuery(t);
						
						if(searchTargetField.getBoost() > 1F && boost > 1F) {
							query.setBoost(searchTargetField.getBoost() + boost);
						} else if (boost > 1F) {
							query.setBoost(boost);
						} else if (searchTargetField.getBoost() > 1F) {
							query.setBoost(searchTargetField.getBoost());
						}
						
						resultQuery.add(query, occur);
						
						logger.debug("query : {} ", query.toString());
						logger.debug("result query : {} ", resultQuery.toString());
						
					} else {
						
						for(String str : analyzedTokenList) {
							
							Term t = new Term(fieldName, str);
							Query query = new TermQuery(t);
							
							if(searchTargetField.getBoost() > 1F && boost > 1F) {
								query.setBoost(searchTargetField.getBoost() + boost);
							} else if (boost > 1F) {
								query.setBoost(boost);
							} else if (searchTargetField.getBoost() > 1F) {
								query.setBoost(searchTargetField.getBoost());
							}
							
							resultQuery.add(query, occur);
							
							logger.debug("query : {} ", query.toString());
							logger.debug("result query : {} ", resultQuery.toString());
						}
					}
				}
			}
		}
		
		return resultQuery;
	}
	
	private ArrayList<String> analyzedTokenList(Analyzer analyzer, String splitedKeyword) {
		Logger logger = LoggerFactory.getLogger(DefaultKeywordParser.class);
		
		ArrayList<String> rst = new ArrayList<String>();
		//split된 검색어를 Analyze..
		TokenStream stream = analyzer.tokenStream("", new StringReader(splitedKeyword));
		CharTermAttribute charTerm = stream.getAttribute(CharTermAttribute.class);
		

		try {
			stream.reset();
			
			while(stream.incrementToken()) {
				rst.add(charTerm.toString());
			}
			
		} catch (IOException e) {
			logger.error("error in DefaultKeywordParser : ", e);
			throw new RuntimeException(e);
		}

		logger.debug("[{}] 에서 추출된 명사 : [{}]", new String[]{splitedKeyword, rst.toString()});
			

		return rst;
	}
}
