/*
 * $Id: MetadataListManagedBean.java,v 1.5 2005/01/24 17:26:34 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.content.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.PropertyName;
import com.idega.business.IBOLookup;
import com.idega.content.business.MetadataUtil;
import com.idega.content.data.MetadataValueBean;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavRootResource;
import com.idega.webface.WFResourceUtil;
import com.idega.webface.WFUtil;
import com.idega.webface.bean.AbstractWFEditableListManagedBean;
import com.idega.webface.bean.WFEditableListDataBean;
import com.idega.webface.bean.WFListBean;


/**
 * 
 * Last modified: $Date: 2005/01/24 17:26:34 $ by $Author: joakim $
 *
 * @author Joakim Johnson
 * @version $Revision: 1.5 $
 */
public class MetadataListManagedBean extends AbstractWFEditableListManagedBean implements WFListBean, ActionListener {

	protected String[] componentIDToken = new String[] { "type", "Values", "Button" };
	protected String[] localizationKey = new String[] { "type", "values", "" };

	private String resourcePath = null;
	
	private String rootPath = null;

	private static WFResourceUtil localizer = WFResourceUtil.getResourceUtilContent();

	public MetadataListManagedBean() {
	}

	public void setResourcePath(String path){
		resourcePath = path;
	}
	
	private MetadataValueBean[] getMetadata() {
		MetadataValueBean[] data;
		ArrayList arrayList = new ArrayList();
		
		try {

			IWContext iwc = IWContext.getInstance();
			IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
			IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwc,IWSlideService.class);
	
			WebdavRootResource rootResource = session.getWebdavRootResource();

			String filePath = service.getURI(resourcePath);
			
//			IWUserContext iwuc = IWContext.getInstance();
//	    	String root = iwuc.getApplicationContext().getIWMainApplication().getApplicationContextURI();

			//Debug
//			System.out.println("Resource path is "+filePath+" # of metadata types:"+MetadataUtil.getMetadataTypes().size());
//			rootResource.proppatchMethod(filePath,new PropertyName("DAV:","keywords"),"Test",true);
			
			Iterator iter = MetadataUtil.getMetadataTypes().iterator();
			while(iter.hasNext()) {
				String type = (String)iter.next();
//			for(int i=0;i<WebDAVMetadata.metadataType.length;i++) {
//				String type = WebDAVMetadata.metadataType[i];
//				System.out.println("Type is "+type);

				Enumeration enum = rootResource.propfindMethod(filePath,new PropertyName("DAV",type).toString());
	
				StringBuffer value = new StringBuffer();
				while(enum.hasMoreElements()) {
					value.append(enum.nextElement());
				}
//				System.out.println("Value is "+value);
				if(value.length()>0) {
					MetadataValueBean mvb = new MetadataValueBean(type, value.toString(),"delete");
					arrayList.add(mvb);
				}
			}
			data = (MetadataValueBean[])arrayList.toArray(new MetadataValueBean[0]);
			
		} catch (HttpException ex) {
			System.out.println("[HTTPException]:"+ex.getMessage());
			System.out.println("[HTTPException]:"+ex.getReason());
			System.out.println("[HTTPException]:"+ex.getReasonCode());
			ex.printStackTrace();
			data = new MetadataValueBean[] { new MetadataValueBean("Caught HttpException","","") };
		} catch (IOException ex) {
			ex.printStackTrace();
			data = new MetadataValueBean[] { new MetadataValueBean("Caught IOException","","") };
		} catch (NullPointerException ex) {
			StackTraceElement[] trace = ex.getStackTrace();
			String traceString = null;
			for (int i = 0; i < trace.length; i++) {
				traceString = traceString + trace[i].toString() + "    \n\r";
			}
			data = new MetadataValueBean[] { new MetadataValueBean("Nullpointer: " + traceString,"","") };
		}
		return data;
	}
	
	public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
		UIComponent comp = actionEvent.getComponent();
//		UIComponent comp = actionEvent.getSource();
		String var = (String)comp.getAttributes().get("var");
		resourcePath = (String)comp.getAttributes().get("resourcePath");
//		System.out.println("var = "+var);
		String type = WFUtil.getStringValue(var,"type");
//		System.out.println("About to delete type "+type);
		
		try {

			IWContext iwc = IWContext.getInstance();
			IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
			IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwc,IWSlideService.class);
	
			WebdavRootResource rootResource = session.getWebdavRootResource();

			String filePath = service.getURI(resourcePath);
//			System.out.println("Filepath = "+filePath);
			rootResource.proppatchMethod(filePath,new PropertyName("DAV:",type),"",true);
		} catch (HttpException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
//		UIComponent tmp = comp.getParent();
//		while ( tmp != null) {
//			tmp = tmp.getParent();
//		}
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getData()
	 */
	public WFEditableListDataBean[] getData() {
		return getMetadata();
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getNumberOfColumns()
	 */
	public int getNumberOfColumns() {
		return componentIDToken.length;
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getUIComponent(java.lang.String, int)
	 * Returns the UIComponent for the specific object (one for each row) and row
	 */
	public UIComponent getUIComponent(String var, int columnIndex) {
		int index = columnIndex;
		UIComponent component = null;
		//For this bean it will be and HtmlOutputText, HtmlInputText or HtmlCommandButton depending on collumn
		switch (index) {
			case 0:
				component = getTypeUIComponent(var);
				break;
			case 1:
				component = getValuesUIComponent(var);
				break;
			default:
				component = getButtonUIComponent(var);
		}
		component.setId(getUIComponentID(var, componentIDToken[index]));
		return component;
	}

	/**
	 * Creates the UIComponent ID based on the object and the idToken provided
	 */
	protected String getUIComponentID(String var, String idToken) {
		return String.valueOf(var + "." + componentIDToken + ".id");
	}

	/**
	 * Returns the Metadata type as an HtmlOutputText
	 * @param var String used to do the lookup for the data bean
	 * @return UIComponent
	 */
	private UIComponent getTypeUIComponent(String var) {
		UIColumn typeCol = new UIColumn();
		typeCol.setHeader(ContentBlock.getBundle().getLocalizedText("type"));
		//TODO This probably has to be localized
		HtmlOutputText creation = WFUtil.getTextVB(var + ".type");
		creation.setStyleClass("wf_listtext");
		typeCol.getChildren().add(creation);

		return typeCol;
	}

	/**
	 * Returns the Metadata values as an HtmlInputText
	 * @param var String used to do the lookup for the data bean
	 * @return UIComponent
	 */
	private UIComponent getValuesUIComponent(String var) {
		UIColumn valuesCol = new UIColumn();
		valuesCol.setHeader(ContentBlock.getBundle().getLocalizedText("values"));
		HtmlInputText t = WFUtil.getInputText("metadatavalues", var + ".metadatavalues");
//		t.setStyleClass("wf_listtext");
		valuesCol.getChildren().add(t);
		return valuesCol;
	}

	/**
	 * Returns Delete Button
	 * @param var String used to do the lookup for the data bean
	 * @return
	 */
	private UIComponent getButtonUIComponent(String var) {
		UIColumn buttonCol = new UIColumn();
		buttonCol.setHeader(ContentBlock.getBundle().getLocalizedText("type"));

		HtmlCommandButton deleteButton = localizer.getButtonVB("delete", "delete", this);
		deleteButton.getAttributes().put("var",var);
		deleteButton.getAttributes().put("resourcePath",resourcePath);
		
		buttonCol.getChildren().add(deleteButton);
		return buttonCol;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getHeader(int)
	 */
	public UIComponent getHeader(int columnIndex) {
		return ContentBlock.getBundle().getLocalizedText(localizationKey[columnIndex]);
	}
}
