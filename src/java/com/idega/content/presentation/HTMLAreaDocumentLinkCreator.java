/*
 * $Id: HTMLAreaDocumentLinkCreator.java,v 1.1 2005/03/08 10:41:08 gimmi Exp $
 * Created on 1.3.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.presentation;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import com.idega.idegaweb.IWBundle;
import com.idega.webface.htmlarea.HTMLAreaLinkType;


public class HTMLAreaDocumentLinkCreator implements HTMLAreaLinkType {
	
	public ValueBinding getLinkTypeName(IWBundle bundle) {
		return bundle.getValueBinding("link_type_document");
	}
	
	public UIComponent getLinkCreation() {
		ContentViewer list = new ContentViewer();
		list.setShowPermissionTab(false);
		list.setShowUploadComponent(true);
		list.setRootFolder("/files");
		list.setOnFileClickEvent("Set()");
		list.setColumnsToHide(WebDAVListManagedBean.COLUMN_DELETE+","+WebDAVListManagedBean.COLUMN_CHECKOUT+","+WebDAVListManagedBean.COLUMN_LOCK);
		return list;
	}

	public String getStartingURL() {
		return null;
	}

	public String getStartingTitle() {
		return null;
	}

	public String getStartingTarget() {
		return null;
	}

	public String getLinkType() {
		return "document";
	}

}