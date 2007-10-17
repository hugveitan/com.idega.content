/*
 * $Id: ContentRSSProducer.java,v 1.8 2007/10/17 15:09:42 valdas Exp $
 * Created on Sep 13, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.business;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpException;
import org.apache.slide.event.ContentEvent;
import org.apache.webdav.lib.WebdavResources;

import com.idega.block.rss.business.RSSAbstractProducer;
import com.idega.block.rss.business.RSSProducer;
import com.idega.block.rss.data.RSSRequest;
import com.idega.slide.business.IWContentEvent;
import com.idega.slide.business.IWSlideChangeListener;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * This RSSProducer can produce rss files for folders in slide. Its RSSProducer identifier is by default "content".
 * The rss file for a folder is called "content.xml" and is stored in a hidden folder called ".rss" under the folder that was requested.
 * An IWSlideChangeListener see's to it that a cachemap containing which rssfiles are up to date is invalidated.
 * 
 *  Last modified: $Date: 2007/10/17 15:09:42 $ by $Author: valdas $
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.8 $
 */
public class ContentRSSProducer extends RSSAbstractProducer implements RSSProducer, IWSlideChangeListener {


	public static final String RSS_FOLDER_NAME = ".rss";
	public static final String RSS_FILE_NAME = "content.xml";
	private Map rssFileURIsCacheMap = new HashMap();

	public ContentRSSProducer() {
		super();
	}

	/**
	 * @see com.idega.block.rss.business.RSSProducer
	 */
	public void handleRSSRequest(RSSRequest rssRequest) throws IOException {
		//Take the request uri and get the extrauri part of it
		//the extra part refers to a folder and should end with a "/", add it if missing
		//Check with slidesession if the folder exists, this will return false if you don't have the right priviledges or the folder does not exist
		//If it exists (you there for have the rights) then check if the rss file exists in the hidden folder ".rss/content.xml"
		//If the feed file exists, dispatch to it
		//If not, create it and then dispatch to it.
		
		
		String uri = fixURI(rssRequest);
		
		
		String feedFile = uri+RSS_FOLDER_NAME+"/"+RSS_FILE_NAME;
		String realURI = CoreConstants.WEBDAV_SERVLET_URI+feedFile;
		try {
			//This is an important check since it will also determin if the user can view the folder
			if(this.isAFolderInSlide(uri,rssRequest)){
				
				//for testing
//				if(this.existsInSlide(feedFile,rssRequest) && false ){
				//Both check if the file has been created and if the uri is cached.
				//The uri is decached when something in the folder changes so the rss file gets regenerated
				if(this.existsInSlide(feedFile,rssRequest) && rssFileURIsCacheMap.containsKey(uri)){
					this.dispatch(realURI, rssRequest);
				}
				else{
					//generate rss and store and the dispatch to it
					//and add a listener to that directory
					try {
						createRSSFile(rssRequest, uri);
						rssFileURIsCacheMap.put(uri,realURI);
						
						this.dispatch(CoreConstants.WEBDAV_SERVLET_URI+feedFile, rssRequest);
					} catch (Exception e) {
						throw new IOException(e.getMessage());
					}


				}
			}
			else{
				//throw some exception?
				//this.dispatch(uri, rssRequest);
				//FOR NOW IT DOES NOTHING SO NOT TO WASTE CPU OR MEMORY FOR BOGUS REQUESTS
				
			}

		} catch (ServletException e) {
			e.printStackTrace();
		}


	}

	/**
	 * @param rssRequest
	 * @return
	 */
	protected String fixURI(RSSRequest rssRequest) {
		String uri = "/"+rssRequest.getExtraUri();
		if(!uri.endsWith("/")){
			uri+="/";
		}
		
		if(!uri.startsWith(CoreConstants.PATH_FILES_ROOT)){
			uri = CoreConstants.PATH_FILES_ROOT+uri;
		}
		return uri;
	}

	/**
	 * @param rssRequest
	 * @param uri
	 * @throws HttpException
	 * @throws IOException
	 * @throws RemoteException
	 */
	protected synchronized void createRSSFile(RSSRequest rssRequest, String uri) throws HttpException, IOException, RemoteException {
		WebdavExtendedResource folder = this.getIWSlideSession(rssRequest).getWebdavResource(uri);
		WebdavResources resources = folder.listWithDeltaV();
		Enumeration children = resources.getResources();

		SyndFeed feed = new SyndFeedImpl();
		feed.setTitle(uri+" : Generated by IdegaWeb ePlatform");
		feed.setLink(this.getServerURLWithURI(rssRequest.getRequestWrapped().getServletPath()+"/"+rssRequest.getIdentifier()+uri,rssRequest));
		feed.setDescription("File feed generated by IdegaWeb ePlatform, Idega Software, http://www.idega.com. This feed lists the latest documents from the folder: "+uri);
		feed.setPublishedDate(new Date());
		feed.setEncoding(CoreConstants.ENCODING_UTF8);
		feed.setCopyright("Idega Software");
		List entries = new ArrayList();
		SyndEntry entry;
		SyndContent description;

		while (children.hasMoreElements()) {
			WebdavExtendedResource resource = (WebdavExtendedResource) children.nextElement();
			String fileName = resource.getDisplayName();
			if(! this.getIWSlideService(rssRequest).isHiddenFile(fileName)){
				boolean isFolder = resource.isCollection();
				
				entry = new SyndEntryImpl();
				entry.setTitle(fileName);
				
				if(isFolder){
					entry.setLink(this.getServerURLWithURI("/rss"+resource.getPath(),rssRequest));
				}
				else{
					entry.setLink(this.getServerURLWithURI(resource.getPath(),rssRequest));
				}
				
				long creationDate = resource.getCreationDate();
				long modifiedDate = resource.getGetLastModified();
				
				if(creationDate==0){
					creationDate = modifiedDate;
				}
				
				entry.setPublishedDate(new Date(creationDate));
				entry.setUpdatedDate(new Date(modifiedDate));

				description = new SyndContentImpl();
				description.setType("text/html");
				
				if(!isFolder){
					if(resource.getGetContentType().indexOf("image")>-1){
						description.setValue("<img src='"+this.getServerURLWithURI(resource.getPath(),rssRequest)+"'/><br/>Size : "+FileUtil.getHumanReadableSize(resource.getGetContentLength())+"<br/>Content type: "+resource.getGetContentType());									
					}
					else{
						description.setValue("Size : "+FileUtil.getHumanReadableSize(resource.getGetContentLength())+"</br>Content type: "+resource.getGetContentType());	
					}
				}
				else{
					description.setValue("Folder");
				}
				entry.setDescription(description);
				
				entries.add(entry);
			}
		}

		feed.setEntries(entries);
		String feedXML = this.getRSSBusiness().convertFeedToRSS2XMLString(feed);	
		//deletes the previous version
		this.getIWSlideService(rssRequest).uploadFileAndCreateFoldersFromStringAsRoot(uri+RSS_FOLDER_NAME+"/", RSS_FILE_NAME, feedXML,this.getRSSContentType(),true);
	}


	/**
	 * @return Returns the rssFileURIsCacheMap.
	 */
	protected Map getRssFileURIsCacheMap() {
		return rssFileURIsCacheMap;
	}

	/**
	 * @param rssFileURIsCacheMap The rssFileURIsCacheMap to set.
	 */
	protected void setRssFileURIsCacheMap(Map rssFileURIsCacheMap) {
		this.rssFileURIsCacheMap = rssFileURIsCacheMap;
	}

	public void onSlideChange(IWContentEvent contentEvent) {
//		On a file change this code checks if an rss file already exists and if so updates it (overwrites) with a new folder list
		String URI = contentEvent.getContentEvent().getUri();
		int index = URI.lastIndexOf("/");
		URI = URI.substring(0,index+1);
		
		getRssFileURIsCacheMap().remove(URI);
	}
	
}
