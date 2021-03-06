/*
 * $Id: HTMLAreaDocumentImageChooser.java,v 1.5 2007/08/17 13:41:58 valdas Exp $
 * Created on 8.3.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.presentation;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;

import com.idega.idegaweb.IWBundle;
import com.idega.util.CoreConstants;
import com.idega.webface.htmlarea.HTMLAreaImageType;

public class HTMLAreaDocumentImageChooser implements HTMLAreaImageType {

	public ValueExpression getLinkTypeName(IWBundle iwb) {
		return iwb.getValueExpression("image_type_document");
	}

	public UIComponent getCreationComponent(String url) {
		ContentViewer list = new ContentViewer();
		list.setShowPermissionTab(false);
		list.setShowUploadComponent(true);
		list.setRootPath(CoreConstants.PATH_FILES_ROOT);
		if (url != null) {
			list.setStartPath(url);
		} else {
			list.setStartPath("/files/public");
		}
		list.setOnFileClickEvent("SelectDocument(this);onPreview();return false;");
		list.setColumnsToHide(WebDAVListManagedBean.COLUMN_DELETE+","+WebDAVListManagedBean.COLUMN_CHECKOUT+","+WebDAVListManagedBean.COLUMN_LOCK);
		return list;
	}

	public String getLinkType() {
		return "document";
	}
}
