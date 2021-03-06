package com.idega.content.presentation;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.savestate.UISaveState;

import com.idega.content.bean.ContentPathBean;
import com.idega.content.business.ContentConstants;
import com.idega.presentation.IWBaseComponent;
import com.idega.util.CoreConstants;
import com.idega.webface.WFList;
import com.idega.webface.WFUtil;

/**
 * @author gimmi
 *
 * Component that lists up a content of a WebDAV folder
 */
public class WebDAVList extends IWBaseComponent {
	
	public final static String WEB_DAV_LIST_BEAN_ID = "WebDAVListBean";
	
	private String startFolder = null;
	private String rootFolder = null;
	private String iconTheme = null;
	private boolean showFolders = true;
	private boolean showPublicFolder = true;
	private boolean showDropboxFolder = true;
	private Collection<String> columnsToHide = null;
	private boolean useVersionControl = true;
	private String onFileClickEvent = null;
	
	public WebDAVList() {
	}
	
	@Override
	protected void initializeComponent(FacesContext context) {
		Object o = WFUtil.invoke(ContentPathBean.BEAN_ID, "getPath");
		String pathFromContent = null;
		if (o instanceof String) {
			pathFromContent = (String) o;
			if (!ContentConstants.EMPTY.equals(pathFromContent)) {
				setStartFolder(pathFromContent);
			}
		}
		
		WebDAVListManagedBean bean = getWebDAVListManagedBean();
		
		if (this.startFolder != null) {
			bean.setStartFolder(this.startFolder);
		} else {
			bean.setStartFolder(CoreConstants.EMPTY);
		}
		if (this.rootFolder != null) {
			bean.setRootFolder(this.rootFolder);
		} else {
			bean.setRootFolder(CoreConstants.EMPTY);
		}
		if (this.iconTheme != null) {
			bean.setIconTheme(this.iconTheme);
		} else {
			bean.setIconTheme(CoreConstants.EMPTY);
		}
		if (this.onFileClickEvent != null) {
			bean.setOnFileClickEvent(this.onFileClickEvent);
		} else {
			bean.setOnFileClickEvent(CoreConstants.EMPTY);
		}
		
		bean.setShowFolders(new Boolean(this.showFolders));
		bean.setShowPublicFolder(new Boolean(this.showPublicFolder));
		bean.setShowDropboxFolder(new Boolean(this.showDropboxFolder));
		if (this.columnsToHide != null) {
			bean.addColumnsToHide(this.columnsToHide);
		}
		bean.setUseVersionControl(new Boolean(this.useVersionControl));
		
		this.setId(this.getId());
		WFList list = new WFList(WEB_DAV_LIST_BEAN_ID, 0, 0);
		list.setBodyScrollable(true);
		list.setId(this.getId()+"_l");
		add(list);
		
		//	To make this object request safe, extension from myfaces. Ask Tryggvi
		UISaveState beanSaveState = new UISaveState();
		beanSaveState.setValueExpression("value", WFUtil.createValueExpression(context.getELContext(),
				"#{".concat(WEB_DAV_LIST_BEAN_ID).concat(".dataModel}"), Object.class));
		beanSaveState.setId("WebDavListBeanSaveState");
		add(beanSaveState);
	}
	
	protected WebDAVListManagedBean getWebDAVListManagedBean(){
		return WFUtil.getBeanInstance(WEB_DAV_LIST_BEAN_ID, WebDAVListManagedBean.class);
	}
	
	public void setStartFolder(String start) {
		this.startFolder = start;
	}
	
	public void setRootFolder(String root) {
		this.rootFolder = root;
	}
	
	public void setIconTheme(String theme) {
		this.iconTheme = theme;
	}
	
	public void setShowFolders(boolean showFolders) {
		this.showFolders = showFolders;
	}
	
	public void setShowPublicFolder(boolean showPublicFolder){
		this.showPublicFolder = showPublicFolder;
	}
	
	public void setShowDropboxFolder(boolean showDropboxFolder){
		this.showDropboxFolder = showDropboxFolder;
	}
	
	public void setColumnsToHide(Collection<String> columns) {
		this.columnsToHide = columns;
	}
	
	public void setUseVersionControl(boolean useVersionControl) {
		this.useVersionControl = useVersionControl;
	}
	
	public void setOnFileClickEvent(String event) {
		this.onFileClickEvent = event;
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException{
		super.encodeChildren(context);
		for (Iterator<UIComponent> iter = getChildren().iterator(); iter.hasNext();) {
			UIComponent child = iter.next();
			renderChild(context,child);
		}
	}
	
	@Override
	public boolean getRendersChildren() {
		return true;
	}
}