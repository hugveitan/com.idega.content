/*
 * $Id: ContentListViewerManagedBean.java,v 1.2 2005/02/21 16:12:45 gummi Exp $
 * Created on 27.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.bean;

import java.util.List;
import com.idega.content.presentation.ContentItemViewer;



/**
 * 
 *  Last modified: $Date: 2005/02/21 16:12:45 $ by $Author: gummi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.2 $
 */
public interface ContentListViewerManagedBean {
	/**
	 * 
	 * @return list of ContentItems
	 */
	public List getContentItems();
	public ContentItemViewer getContentViewer();
	
	/**
	 * 
	 * @return list of ContentItemViewers
	 */
	public List getAttachmentViewers();
	public void setResourcePath(String path);
	public void setDetailsViewerPath(String path);
	
}
