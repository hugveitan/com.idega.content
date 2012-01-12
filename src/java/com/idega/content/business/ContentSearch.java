/*
 * $Id: ContentSearch.java,v 1.42 2009/05/15 07:23:54 valdas Exp $ Created on Jan
 * 17, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.content.business;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.jackrabbit.webdav.client.methods.SearchMethod;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.content.presentation.WebDAVDocumentDeleter;
import com.idega.core.builder.data.ICPage;
import com.idega.core.search.business.Search;
import com.idega.core.search.business.SearchPlugin;
import com.idega.core.search.business.SearchQuery;
import com.idega.core.search.business.SearchResult;
import com.idega.core.search.data.AdvancedSearchQuery;
import com.idega.core.search.data.BasicSearch;
import com.idega.core.search.data.SimpleSearchQuery;
import com.idega.core.search.presentation.SearchResults;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.repository.RepositoryService;
import com.idega.util.expression.ELUtil;

/**
 *
 * Last modified: $Date: 2009/05/15 07:23:54 $ by $Author: valdas $ This class
 * implements the Searchplugin interface and can therefore be used in a Search
 * block (com.idega.core.search)<br>
 * for searching contents and properties (metadata) of the files in the iwfile
 * system. To use it simply register this class as a iw.searchable component in
 * a bundle.
 *
 * TODO Load the dasl searches from files! (only once?)
 *
 * @author <a href="mailto:eiki@idega.com">Eirikur S. Hrafnsson</a>
 * @version $Revision: 1.42 $
 */
public class ContentSearch extends Object implements SearchPlugin{

	//private static final String DASL_WHERE_XML_SNIPPET = "</D:where>";
	public static final String SEARCH_NAME_LOCALIZABLE_KEY = "content_search.name";
	public static final String SEARCH_DESCRIPTION_LOCALIZABLE_KEY = "content_search.description";
	public static final String SEARCH_TYPE = "document";
	public static final String DOCUMENT_SEARCH_WORD_PARAMETER_NAME = "doc_s_word";
	public static final String DOCUMENT_TYPE_PARAMETER_NAME = "doc_type";
	public static final String DOCUMENT_ORDERING_PARAMETER_NAME = "doc_order";
	public static final String DOCUMENT_ORDERING_BY_DATE = "doc_order_date";
	public static final String DOCUMENT_ORDERING_BY_NAME = "doc_order_name";
	public static final String DOCUMENT_ORDERING_BY_SIZE = "doc_order_size";
//	static final PropertyName DISPLAYNAME = new PropertyName("DAV:", "displayname");
//	static final PropertyName CONTENTLENGTH = new PropertyName("DAV:", "getcontentlength");
//	static final PropertyName CREATOR_DISPLAY_NAME = new PropertyName("DAV:", "creator-displayname");
//	static final PropertyName COMMENT = new PropertyName("DAV:", "comment");
	private String httpURL;

	protected String scopeURI = "files";
	protected String propertyToOrderBy = "displayname";
	protected boolean useDescendingOrder = false;
	protected boolean ignoreFolders = true;
	protected int numberOfResultItems = -1;
	protected boolean useRootAccessForSearch = false;
	protected boolean hideParentFolderPath = false;
	protected boolean hideFileExtension = false;

	protected static final String ORDER_ASCENDING = "ascending";
	protected static final String ORDER_DESCENDING = "descending";
	private boolean showDeleteLink = false;
	private ICPage deletePage = null;

	@Autowired
	private RepositoryService repositoryService;

	/* STUFF FROM WebdavResource to handle better dates from slide */
	 /**
    * Date formats using for Date parsing.
    */
   public static final SimpleDateFormat formats[] = {
       new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
           new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
           new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
           new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US),
           new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
           new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US)
   };


   /**
    * GMT timezone.
    */
   protected final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");


   static {
       for (int i = 0; i < formats.length; i++) {
           formats[i].setTimeZone(gmtZone);
       }
   }

	/* STUFF ENDS FROM WebdavResource to handle better dates from slide */

	/**
	 * @return the scopeURI
	 */
	public String getScopeURI() {
		return this.scopeURI;
	}


	/**
	 * @param scopeURI the scopeURI to set
	 */
	public void setScopeURI(String scopeURI) {
		this.scopeURI = scopeURI;
	}

	public ContentSearch() {
		super();
	}

	public ContentSearch(IWMainApplication iwma) {
		this();
		initialize(iwma);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.core.search.business.SearchPlugin#getAdvancedSearchSupportedParameters()
	 */
	@Override
	public List<String> getAdvancedSearchSupportedParameters() {
		return Arrays.asList(
				DOCUMENT_SEARCH_WORD_PARAMETER_NAME,
				DOCUMENT_TYPE_PARAMETER_NAME,
				DOCUMENT_ORDERING_PARAMETER_NAME
		);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.core.search.business.SearchPlugin#getSupportsSimpleSearch()
	 */
	@Override
	public boolean getSupportsSimpleSearch() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.core.search.business.SearchPlugin#getSupportsAdvancedSearch()
	 */
	@Override
	public boolean getSupportsAdvancedSearch() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.core.search.business.SearchPlugin#initialize(com.idega.idegaweb.IWMainApplication)
	 */
	@Override
	public boolean initialize(IWMainApplication iwma) {
		this.httpURL = getRepositoryService().getWebdavServerURL();
		return httpURL != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.core.search.business.SearchPlugin#destroy(com.idega.idegaweb.IWMainApplication)
	 */
	@Override
	public void destroy(IWMainApplication iwma) {
	}

	//	TODO: implement
	public Search createSearch(SearchQuery searchQuery, List<Query> searchRequests) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		BasicSearch searcher = new BasicSearch();
		searcher.setSearchName(getSearchName());
		searcher.setSearchType(SEARCH_TYPE);
		searcher.setSearchQuery(searchQuery);
//		try {
//			Credentials hostCredentials = null;
//
//			if (isUsingRootAccessForSearch()) {
//				hostCredentials = service.getRootUserCredentials();
//			}
//			else {
//				IWContext iwc = CoreUtil.getIWContext();
//				if (iwc != null) {
//					IWSlideSession session = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
//					hostCredentials = session.getUserCredentials();
//				}
//			}
//
//			String servletMapping = service.getWebdavServerURL();
//			HttpClient client = new HttpClient();
//			client.setState(new WebdavState());
//			HostConfiguration hostConfig = client.getHostConfiguration();
//			hostConfig.setHost(this.httpURL);
//
//			if (hostCredentials != null) {
//				HttpState clientState = client.getState();
//				clientState.setCredentials(null, this.httpURL.getHost(), hostCredentials);
//				clientState.setAuthenticationPreemptive(true);
//			}
//			for (Iterator iter = searchRequests.iterator(); iter.hasNext();) {
//				Object request = iter.next();
//				String queryXML = null;
//				if (request instanceof String) {
//					queryXML = (String) request;
//				}
//				else if (request instanceof SearchRequest) {
//					SearchRequest query = (SearchRequest) request;
//
//					// executeSearch uses comparator which needs a property set in propertyToOrderBy
//					// in search results. add it if it's not there
//					boolean selectionCorrect = false;
//					for (Iterator<PropertyName> selection = query.getSelection(); selection.hasNext(); ) {
//						PropertyName prop = selection.next();
//						if (prop.getLocalName().equals(getPropertyToOrderBy())) {
//							selectionCorrect = true;
//							break;
//						}
//					}
//					if (!selectionCorrect) {
//						query.addSelection(new PropertyName("DAV:", getPropertyToOrderBy()));
//						System.err.println("WARNING: content search set to be ordered by " + getPropertyToOrderBy() + ", but this property is not in selection. Adding it.");
//					}
//
//					queryXML = query.asString();
//				}
//				SearchMethod contentSearch = new SearchMethod(servletMapping, queryXML);
//				executeSearch(results, servletMapping, contentSearch, client);
//			}
//			searcher.setSearchResults(results);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return searcher;
	}

	public Search createSearch(List<Query> searchRequests) {
		return createSearch(null, searchRequests);
	}

	public Search createSearch(Query searchRequest) {
		return createSearch(null, Arrays.asList(searchRequest));
	}

	@Override
	public Search createSearch(SearchQuery searchQuery) {
		List<String> l = new ArrayList<String>();
		try {
			if (searchQuery instanceof AdvancedSearchQuery) {
				String content = getAdvancedContentSearch(searchQuery);
				l.add(content);
			}
			else if (searchQuery instanceof SimpleSearchQuery) {
				// switched to string until we have order by in the query objects SearchRequest content = getContentSearch(searchQuery);
//				String contentSearchXML = getContentSearch(searchQuery);
//				String propertySearchXML = getPropertySearch(searchQuery);
//				l.add(contentSearchXML);
//				l.add(propertySearchXML);
				//Searches both at the same time
				String combinedSearch = getCombinedContentAndPropertySearch(searchQuery);
				//System.out.println(combinedSearch);

				l.add(combinedSearch);

			}
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		return null;	//	TODO
//		return createSearch(searchQuery, l);
	}

	//	TODO: implement
	protected String getAdvancedContentSearch(SearchQuery searchQuery) throws RepositoryException {
		return null;
//		SearchRequest s = new SearchRequest();
//		s.addSelection(CONTENTLENGTH);
//		s.addScope(new SearchScope(getScopeURI()));
//		Map params = searchQuery.getSearchParameters();
//		String queryString = (String) params.get(DOCUMENT_SEARCH_WORD_PARAMETER_NAME);
//		String type = (String) params.get(DOCUMENT_TYPE_PARAMETER_NAME);
//		String ordering = (String) params.get(DOCUMENT_ORDERING_PARAMETER_NAME);
//		if(ordering!=null){
//			if(ORDER_ASCENDING.equalsIgnoreCase(ordering)){
//				setToUseDescendingOrder(false);
//			}
//		}
//
//		SearchExpression expression = null;
//		if (queryString != null && !"".equals(queryString)) {
//			// does contain have to be separate?
//			String[] tokens = queryString.split(" ");
//			for (int i = 0; i < tokens.length; i++) {
//				String searchWord = tokens[i];
//				SearchExpression orExpression = s.contains(queryString);
//				//Don't ever do **asdf** searches, only *asdf* the extra * causes errors in Slide
//				String wildCardSearchWord = getWildCardSearchWord(searchWord);
//
//
//				orExpression = s.or(orExpression, s.or(s.compare(CompareOperator.LIKE, DISPLAYNAME, wildCardSearchWord), s.or(s.compare(CompareOperator.LIKE, CREATOR_DISPLAY_NAME, searchWord), s.compare(
//						CompareOperator.LIKE, COMMENT, wildCardSearchWord))));
//				if (expression != null) {
//					expression = s.and(orExpression, expression);
//				}
//				else {
//					expression = orExpression;
//				}
//			}
//		}
//		if (type != null && !"".equals(type) && !"*".equals(type)) {
//			if (expression != null) {
//				expression = s.and(expression, s.compare(CompareOperator.LIKE, DISPLAYNAME, "*." + type));
//			}
//			else {
//				expression = s.compare(CompareOperator.LIKE, DISPLAYNAME, "*." + type);
//			}
//		}
//		if (expression != null) {
//			s.setWhereExpression(expression);
//		}
//		String searchXML = s.asString();
//		searchXML = addOrderingAndLimitingToDASLSearchXML(searchXML);
//
//		return searchXML;
	}

/**
 * Returns the searchword as "*searchword*"
 * @param modSearchWord
 * @return
 */
	protected String getWildCardSearchWord(String modSearchWord) {
		if(!"*".equals(modSearchWord)){
			if(!modSearchWord.startsWith("*")){
				modSearchWord = "*" + modSearchWord;
			}
			if(!modSearchWord.endsWith("*")){
				modSearchWord = modSearchWord+"*";
			}
		}
		return modSearchWord;
	}

	protected String getContentSearch(SearchQuery searchQuery) throws RepositoryException {
		//TODO update to pure XML with folder ignoring
//		SearchRequest s = new SearchRequest();
//		s.addSelection(CONTENTLENGTH);
//		s.addScope(new SearchScope(getScopeURI()));
//		String queryString = ((SimpleSearchQuery) searchQuery).getSimpleSearchQuery();
//		SearchExpression expression = null;
//		String[] tokens = queryString.split(" ");
//		for (int i = 0; i < tokens.length; i++) {
//			String searchWord = tokens[i];
//			SearchExpression contains = s.contains(searchWord);
//			if (expression != null) {
//				expression = s.and(contains, expression);
//			}
//			else {
//				expression = contains;
//			}
//		}
//		s.setWhereExpression(expression);
//		String searchXML = s.asString();
//
//		searchXML = addOrderingAndLimitingToDASLSearchXML(searchXML);
//
//		return searchXML;
		//	TODO: implement
		return null;
	}

	protected String getPropertySearch(SearchQuery searchQuery) throws RepositoryException {
		//TODO update to pure XML with folder ignoring
//		SearchRequest s = new SearchRequest();
//		s.addSelection(CONTENTLENGTH);
//
//		s.addScope(new SearchScope(getScopeURI()));
//		String queryString = ((SimpleSearchQuery) searchQuery).getSimpleSearchQuery();
//		SearchExpression expression = null;
//		String[] tokens = queryString.split(" ");
//		for (int i = 0; i < tokens.length; i++) {
//			String searchWord = tokens[i];
//			String wildCardSearchWord = getWildCardSearchWord(searchWord);
//			SearchExpression orExpression = s.or(s.compare(CompareOperator.LIKE, DISPLAYNAME, wildCardSearchWord),
//					s.or(s.compare(CompareOperator.LIKE, CREATOR_DISPLAY_NAME, searchWord), s.compare(
//							CompareOperator.LIKE, COMMENT,wildCardSearchWord)));
//			if (expression != null) {
//				expression = s.and(orExpression, expression);
//			}
//			else {
//				expression = orExpression;
//			}
//		}
//		// add other properties
//		s.setWhereExpression(expression);
//
//		String searchXML = s.asString();
//		searchXML = addOrderingAndLimitingToDASLSearchXML(searchXML);
//
//		return searchXML;
		//	TODO: implement
		return null;
	}

	protected String getCombinedContentAndPropertySearch(SearchQuery searchQuery) throws RepositoryException {
//		SearchRequest s = new SearchRequest();
//		s.addSelection(CONTENTLENGTH);
//		s.addScope(new SearchScope(getScopeURI()));
//		String queryString = ((SimpleSearchQuery) searchQuery).getSimpleSearchQuery();
//		SearchExpression expression = null;

		//OLD SCHOOL XMLing is more flexible and ALOT more readable
		//TODO load from file with markers for the query etc.
		//TODO allow AND search also in for loop
		String queryString = ((SimpleSearchQuery) searchQuery).getSimpleSearchQuery();
		StringBuffer searchXML = new StringBuffer();
		searchXML.append("<D:searchrequest xmlns:D='DAV:' xmlns:S='http://jakarta.apache.org/slide/'><D:basicsearch><D:select><D:prop><D:getcontentlength/><D:creationdate/><D:displayname/><D:getlastmodified/></D:prop></D:select>")
		.append("<D:from>")
		.append("<D:scope><D:href>").append(getScopeURI()).append("</D:href><D:depth>infinity</D:depth></D:scope>")
		.append("</D:from>")
		.append("<D:where>");
		if(isSetToIgnoreFolders()){
			searchXML.append("<D:and>");
		}
		searchXML.append("<D:or>");

		String[] tokens = queryString.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			String searchWord = getWildCardSearchWord(tokens[i]);

			searchXML.append("<D:contains>").append(searchWord).append("</D:contains>")
			.append("<S:property-contains><D:prop><D:displayname/></D:prop><D:literal>").append(searchWord).append("</D:literal></S:property-contains>")
			.append("<S:property-contains><D:prop><D:creator-displayname/></D:prop><D:literal>").append(searchWord).append("</D:literal></S:property-contains>")
			.append("<S:property-contains><D:prop><D:comment/></D:prop><D:literal>").append(searchWord).append("</D:literal></S:property-contains>");
		}

		searchXML.append("</D:or>");
		if(isSetToIgnoreFolders()){
			searchXML.append("<D:not-is-collection/>")
			.append("</D:and>");
		}

		searchXML.append("</D:where>")
		.append("</D:basicsearch>")
		.append("</D:searchrequest>");

//Add ordering and limiting
		String daslXML = addOrderingAndLimitingToDASLSearchXML(searchXML.toString());

		return daslXML;
	}

	/**
	 * Only searches the display name for files
	 * @param searchQuery
	 * @return
	 */
	protected String getDisplayNameSearch(SearchQuery searchQuery) {
		String queryString = ((SimpleSearchQuery) searchQuery).getSimpleSearchQuery();
		StringBuffer searchXML = new StringBuffer();
		searchXML.append("<D:searchrequest xmlns:D='DAV:' xmlns:S='http://jakarta.apache.org/slide/'><D:basicsearch><D:select><D:prop><D:getcontentlength/><D:creationdate/><D:displayname/><D:getlastmodified/></D:prop></D:select>")
		.append("<D:from>")
		.append("<D:scope><D:href>").append(getScopeURI()).append("</D:href><D:depth>infinity</D:depth></D:scope>")
		.append("</D:from>")
		.append("<D:where>");
		if(isSetToIgnoreFolders()){
			searchXML.append("<D:and>");
		}
		String[] tokens = queryString.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			String searchWord = tokens[i];
			String wildCardSearchWord = getWildCardSearchWord(searchWord);
			searchXML.append("<S:property-contains><D:prop><D:displayname/></D:prop><D:literal>").append(wildCardSearchWord).append("</D:literal></S:property-contains>");
		}

		if(isSetToIgnoreFolders()){
			searchXML.append("<D:not-is-collection/>")
			.append("</D:and>");
		}

		searchXML.append("</D:where>")
		.append("</D:basicsearch>")
		.append("</D:searchrequest>");

//Add ordering and limiting
	//	String daslXML = addOrderingAndLimitingToDASLSearchXML(searchXML.toString());

		return searchXML.toString();
	}


	/**
	 * @param searchXML
	 * @return
	 */
	protected String addOrderingAndLimitingToDASLSearchXML(String searchXML) {
		//		Fixme ordering in Slide doesn't really work yet but limiting does, uncomment and remove manual ordering and limiting when it works again
		//Eiki, see my email to the slide developers list 2.may 2007
		/*String orderAscendingOrDescending = ContentSearch.ORDER_ASCENDING;
		if(isSetToUseDescendingOrder()){
			orderAscendingOrDescending = ContentSearch.ORDER_DESCENDING;
		}

		StringBuffer extraXML = new StringBuffer();
		extraXML.append("<D:orderby><D:order><D:prop><D:").append(getPropertyToOrderBy()).append("/></D:prop>")
		.append("<D:").append(orderAscendingOrDescending).append("/></D:order></D:orderby>");

		if(getNumberOfResultItemsToReturn()>0){
			extraXML.append("<D:limit><D:nresults>").append(getNumberOfResultItemsToReturn()).append("</D:nresults></D:limit>");
		}

		int index = searchXML.indexOf(DASL_WHERE_XML_SNIPPET);
		searchXML = searchXML.substring(0, index + DASL_WHERE_XML_SNIPPET.length()) + extraXML.toString()+ searchXML.substring(index + DASL_WHERE_XML_SNIPPET.length());
		*/
		return searchXML;
	}

	public boolean isSetToUseDescendingOrder() {
		return this.useDescendingOrder;
	}

	/**
	 * Set to true to reverse the order of the resultset
	 * @param useDescendingOrder
	 * @return
	 */
	public void setToUseDescendingOrder(boolean useDescendingOrder) {
		this.useDescendingOrder  = useDescendingOrder;
	}

	public boolean isSetToIgnoreFolders() {
		return this.ignoreFolders;
	}

	/**
	 * Set to true to ignore folders completely in searches
	 * @param ignoreFolders
	 * @return
	 */
	public void setToIgnoreFolders(boolean ignoreFolders) {
		this.ignoreFolders   = ignoreFolders;
	}

	protected void executeSearch(List<SearchResult> results, String servletMapping, SearchMethod method, HttpClient client) throws IOException, HttpException {
		//	TODO: implement
//		client.executeMethod(method);
//		Enumeration<ResponseEntity> enumerator = method.getResponses();
//		List<ResponseEntity> searchResults = Collections.list(enumerator);
//
//		String fileName;
//		String fileURI;
//		String lastModifiedDate;
//		String parentFolderPath;
//		BasicSearchResult result;
//		List<SearchResult> tempResults = new ArrayList<SearchResult>();
//		Property prop;
//
//		Locale locale = null;
//		//FIXME EIKI once ordering works withing DASL in Slide we won't need this comparator
//
//		try {
//			IWContext iwc = IWContext.getInstance();
//			locale = iwc.getCurrentLocale();
//		} catch (UnavailableIWContext e) {
//			//not being run by a user, e.g. backend search
//			locale = IWMainApplication.getDefaultIWApplicationContext().getApplicationSettings().getDefaultLocale();
//		}
//
//		//fixme ONLY NEEDED UNTIL ORDERING / SORTING WORKS IN SLIDE!!, remove when ordering works and put back in the ordering and limiting xml
//		SearchResultComparator comparator = new SearchResultComparator(locale,getPropertyToOrderBy(),isSetToUseDescendingOrder());
//
//		for (ResponseEntity entity: searchResults) {
//			fileURI = entity.getHref();
//
//			if (!fileURI.equalsIgnoreCase(servletMapping)) {
//				fileName = URLDecoder.decode(fileURI.substring(fileURI.lastIndexOf(CoreConstants.SLASH) + 1), CoreConstants.ENCODING_UTF8);
//				//don't return "hidden files" that start with a "."
//				if(fileName.startsWith(CoreConstants.DOT)){
//					continue;
//				}
//				Enumeration<Property> props = entity.getProperties();
//				Map<String, Object> properties = new HashMap<String, Object>();
//				while (props.hasMoreElements()) {
//					prop = props.nextElement();
//					String name = prop.getLocalName();
//					String value = prop.getPropertyAsString();
//					properties.put(name,value);
//				}
//
//				// PARSE PROPERTIES AND CONVERT SOME
//				if(isSetToHideFileExtensions()){
//					int dotIndex = fileName.lastIndexOf(CoreConstants.DOT);
//					if(dotIndex>-1){
//						fileName = fileName.substring(0,dotIndex);
//					}
//				}
//
//				parentFolderPath = URLDecoder.decode(fileURI.substring(0,fileURI.lastIndexOf(CoreConstants.SLASH) + 1), CoreConstants.ENCODING_UTF8);
//
//				if(isSetToHideParentFolderPath()){
//					parentFolderPath = (parentFolderPath.endsWith(CoreConstants.SLASH)) ?
//							parentFolderPath.substring(0,parentFolderPath.lastIndexOf(CoreConstants.SLASH)):parentFolderPath;
//					parentFolderPath = parentFolderPath.substring(parentFolderPath.lastIndexOf(CoreConstants.SLASH)+1);
//				}
//				String modDate = (String)properties.get("getlastmodified");
//				String createdDate =  (String)properties.get("creationdate");
//
//				if(modDate==null){
//					modDate =  createdDate;
//				}
//
//				Date modifiedDate = parseDate(modDate);
//				properties.put("getlastmodified",modifiedDate);
//				Date creationDate = parseDate(createdDate);
//				properties.put("creationdate",creationDate);
//
//				// PARSING DONE
//
//				result = new BasicSearchResult();
//				result.setSearchResultType(SEARCH_TYPE);
//				result.setSearchResultURI(fileURI);
//				result.setSearchResultName(fileName);
//				result.setSearchResultExtraInformation(parentFolderPath);
//				if(modifiedDate!=null){
//					lastModifiedDate = new IWTimestamp(modifiedDate).getLocaleDate(locale, IWTimestamp.MEDIUM);
//					result.setSearchResultAbstract(lastModifiedDate);
//				}
//
//				result.setSearchResultAttributes(properties);
//				tempResults.add(result);
//			}
//		}
//
//		Collections.sort(tempResults,comparator);
//
//		//fixme LIMITING THE DIRTY WAY, must be done after the order, remove when ordering works in slide and put back in xml ordering and limiting
//		if(getNumberOfResultItemsToReturn()!=-1){
//			results.addAll(tempResults.subList(0, Math.min(tempResults.size(),getNumberOfResultItemsToReturn())));
//		} else {
//			results.addAll(tempResults);
//		}
	}


	public void setToShowDeleteLink(boolean show) {
		this.showDeleteLink = show;
	}

	public boolean isSetToShowDeleteLink() {
		return showDeleteLink;
	}

	public ICPage getDeletePage() {
		return deletePage;
	}

	public void setDeletePage(ICPage deletePage) {
		this.deletePage = deletePage;
	}

	@Override
	public Collection<Layer> getExtraRowElements(SearchResult result, IWResourceBundle iwrb) {
		Collection<Layer> coll = new Vector<Layer>();
		if (isSetToShowDeleteLink()) {
			Layer deleteL = new Layer();
			deleteL.setStyleClass(SearchResults.DEFAULT_LINK_STYLE_CLASS+"_delete");
			Link dLink = new Link(new Span(new Text(iwrb.getLocalizedString("search_results.delete", "Delete"))));
			dLink.setToolTip(iwrb.getLocalizedString("search_results.delete", "Delete"));
			if (deletePage != null) {
				dLink.setPage(deletePage);
			}
			String uri =  result.getSearchResultURI();
			if (uri != null) {
				dLink.addParameter(WebDAVDocumentDeleter.PARAMETER_PATH, uri);
			}
			deleteL.add(dLink);
			coll.add(deleteL);
		}

		return coll;
	}

	/*
	* (non-Javadoc)
	*
	* @see com.idega.core.search.business.SearchPlugin#getSearchName()
	*/
	@Override
	public String getSearchName() {
	IWBundle bundle = ContentUtil.getBundle();

	try {
	IWContext iwc = IWContext.getInstance();
	return bundle.getResourceBundle(iwc).getLocalizedString(SEARCH_NAME_LOCALIZABLE_KEY,"Documents");
	} catch (UnavailableIWContext e) {
//	e.printStackTrace();
//	no context, we must be running in a servlet or just a pojo
	}

	return "Documents";
	}
//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see com.idega.core.search.business.SearchPlugin#getSearchDescription()
//	 */
//	public String getSearchDescription() {
//		IWBundle bundle = ContentUtil.getBundle();
//		return bundle.getResourceBundle(IWContext.getInstance()).getLocalizedString(SEARCH_DESCRIPTION_LOCALIZABLE_KEY,
//				"Searches the contents of documents in an IdegaWeb file system.");
//	}

	/*
	* (non-Javadoc)
	*
	* @see com.idega.core.search.business.SearchPlugin#getSearchDescription()
	*/
	@Override
	public String getSearchDescription() {
	IWBundle bundle = ContentUtil.getBundle();

	try {
	IWContext iwc = IWContext.getInstance();
	return bundle.getResourceBundle(iwc).getLocalizedString(SEARCH_DESCRIPTION_LOCALIZABLE_KEY,"Searches the contents of documents in an IdegaWeb file system.");
	} catch (UnavailableIWContext e) {
//	e.printStackTrace();
//	no context, we must be running in a servlet or just a pojo
	}

	return "Searches the contents of documents in an IdegaWeb file system.";
	}

	/**
	 * @return the propertyToOrderBy
	 */
	public String getPropertyToOrderBy() {
		return this.propertyToOrderBy;
	}



	/**
	 * @param propertyToOrderBy the propertyToOrderBy to set
	 */
	public void setPropertyToOrderBy(String propertyToOrderBy) {
		this.propertyToOrderBy = propertyToOrderBy;
	}



	/**
	 * @return the numberOfResultItems
	 */
	public int getNumberOfResultItemsToReturn() {
		return this.numberOfResultItems;
	}



	/**
	 * Sets the number of or limit of result items to return
	 * @param numberOfResultItems the numberOfResultItems to set
	 */
	public void setNumberOfResultItemsToReturn(int numberOfResultItems) {
		this.numberOfResultItems = numberOfResultItems;
	}

	@Override
	public Object clone(){
		ContentSearch obj = null;
		try {
			obj = (ContentSearch) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return obj;
	}



	/**
	 * @return the useRootAccessForSearch
	 */
	public boolean isUsingRootAccessForSearch() {
		return this.useRootAccessForSearch;
	}



	/**
	 * Set to true if the content search should use the ROOT access for searching.<br>
	 * Does not give the user rights to open files beyond his access though.
	 * @param useRootAccessForSearch
	 */
	public void setToUseRootAccessForSearch(boolean useRootAccessForSearch) {
		this.useRootAccessForSearch = useRootAccessForSearch;
	}

	/**
	 * @return the hideFolderPath
	 */
	public boolean isSetToHideParentFolderPath() {
		return this.hideParentFolderPath;
	}



	/**
	 * If set to true the result will only state the parent folder of the result itm and not the full path
	 * @param hideParentFolderPath
	 */
	public void setToHideParentFolderPath(boolean hideParentFolderPath) {
		this.hideParentFolderPath = hideParentFolderPath;
	}


	/**
	 * @return if we are hiding the file extension or not
	 */
	public boolean isSetToHideFileExtensions() {
		return this.hideFileExtension;
	}


	/**
	 * If true the everything after the last "." of a file name is cut away
	 * @param hideFileExtension the hideFileExtension to set
	 */
	public void setToHideFileExtensions(boolean hideFileExtensions) {
		this.hideFileExtension = hideFileExtensions;
	}

    /**
     * Parse the <code>java.util.Date</code> string for HTTP-date.
     *
     * @return The parsed date.
     */
    protected Date parseDate(String dateValue) {

        Date date = null;
        if(dateValue!=null){
	        for (int i = 0; (date == null) && (i < formats.length); i++) {
	            try {
	                synchronized (formats[i]) {
	                    date = formats[i].parse(dateValue);
	                }
	            } catch (ParseException e) {
	            }
	        }
        }
        return date;
    }

	/**
	 * Does a simple DASL search (only displayname) and returns a collection of SearchResult objects. ContentSearch must have been initialized!
	 * @param searchString The display name you want to search for
	 * @param scope from what point in Slide you want to start e.g. /files/public
	 * @return returns a collection of SearchResult objects
	 */
	public Collection<SearchResult> doSimpleDASLSearch(String searchString, String scope){
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("mysearchstring", searchString);
		SearchQuery query = new SimpleSearchQuery(queryMap);

		this.setScopeURI(scope);
		String combinedSearch = getDisplayNameSearch(query);

//		Search search = createSearch(query, Arrays.asList(combinedSearch));
//		return search.getSearchResults();
		//	TODO: implement
		return null;
	}

	@Override
	public String getSearchIdentifier() {
		return ContentUtil.getBundle().getComponentName(this.getClass());
	}

	@Override
	public String getResultImgByResultURI(String result_uri) {
		return null;
	}

	RepositoryService getRepositoryService() {
		if (repositoryService == null) {
			ELUtil.getInstance().autowire(this);
		}
		return repositoryService;
	}
}