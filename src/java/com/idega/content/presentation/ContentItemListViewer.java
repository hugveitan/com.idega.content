/*
 * $Id: ContentItemListViewer.java,v 1.26 2008/04/29 09:19:43 valdas Exp $
 * Created on 27.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.faces.component.UIColumn;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentListViewerManagedBean;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.business.categories.CategoryBean;
import com.idega.core.cache.CacheableUIComponent;
import com.idega.core.cache.UIComponentCacher;
import com.idega.presentation.IWContext;
import com.idega.webface.WFUtil;
import com.idega.webface.model.WFDataModel;


/**
 * 
 * Last modified: $Date: 2008/04/29 09:19:43 $ by $Author: valdas $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.26 $
 */
public class ContentItemListViewer extends UIData implements CacheableUIComponent {

	private String managedBeanId;
	private String resourcePath;
	private String detailsViewerPath;

	private String _styleClass;
	private String _style;
	private String _columnClasses;
	private String _rowClasses;
	
	private List<String> categoriesList = null;
	private WFDataModel model=null;
	private String firstArticleItemStyleClass = null;
	private boolean initialized = false;
	
	private static final String DEFAULT_RENDERER_TYPE = "content_list_viewer";
	private int maxNumberOfDisplayed=-1;
	
	private String articleItemViewerFilter = null;
	
	public static final String ITEMS_CATEGORY_VIEW = "items_list_category_view";
	
	/**
	 * 
	 */
	public ContentItemListViewer() {
		super();
		setRendererType(DEFAULT_RENDERER_TYPE);
	}	
	
	/**
	 * Constructs a new WFViewerList component with the specified list managed bean as data source.
	 */
	public ContentItemListViewer(String managedBeanId) {
		this();
		setBeanIdentifier(managedBeanId);
	}
	
	/**
	 * This method is for initalization only but is available e.g. for use in jsp pages.
	 * 
	 * @param managedBeanId
	 */
	public void setBeanIdentifier(String managedBeanId) {
		this.managedBeanId = managedBeanId;
		String var = managedBeanId + "_var";
		setVar(var);
	}
	
	protected String[] getToolbarActions(){
		return new String[] {"create"};
	}
	
	protected void initializeInEncodeBegin(){
		notifyManagedBeanOfVariableValues();
		
		ContentListViewerManagedBean bean = getManagedBean();
		ContentItemViewer viewer = bean.getContentViewer();
		viewer.setShowRequestedItem(false);
		addContentItemViewer(viewer);
		
		String[] actions = getToolbarActions();
		if(actions != null && actions.length > 0){
			ContentItemToolbar toolbar = new ContentItemToolbar(true);
			toolbar.setMenuStyleClass(toolbar.getMenuStyleClass() + " " + toolbar.getMenuStyleClass() + "_top");
			for (int i = 0; i < actions.length; i++) {
				toolbar.addToolbarButton(actions[i]);
			}
			String categories = this.getCategories();
			if(categories!=null){
				toolbar.setCategories(categories);
			}
			String basePath = getBaseFolderPath();
			if(basePath!=null){
				toolbar.setBaseFolderPath(basePath);
			}
			
			toolbar.setActionHandlerIdentifier(bean.getIWActionURIHandlerIdentifier());
			this.setHeader(toolbar);
		}
		
		List attachementViewers = bean.getAttachmentViewers();
		if(attachementViewers!=null){
			for (ListIterator iter = attachementViewers.listIterator(); iter.hasNext();) {
				ContentItemViewer attachmentViewer = (ContentItemViewer) iter.next();
				int index = iter.nextIndex();
				addAttachmentViewer(attachmentViewer,index);
			}
		}
		this.initialized = true;
	}
	
	
	@Override
	public String getFamily(){
		return ContentUtil.FAMILY_CONTENT;
	}
	
	/**
	 * @deprecated replaced with setBaseFolderPath
	 */
	@Deprecated
	public void setResourcePath(String resourcePath){
		setBaseFolderPath(resourcePath);
	}
	
	public void setBaseFolderPath(String path){
		this.resourcePath=path;
		notifyManagedBeanOfBaseFolderPath(path);
	}
	
	public String getBaseFolderPath(){
		if (this.resourcePath != null) {
			return this.resourcePath;
		}
        ValueBinding vb = getValueBinding("baseFolderPath");
        String path = vb != null ? (String)vb.getValue(getFacesContext()) : null;
        if(path==null){
	        	if(this.managedBeanId!=null){
	        		path = getManagedBean().getBaseFolderPath();
	        	}
        }
        return path;
	}

	protected void addContentItemViewer(ContentItemViewer viewer){
		UIColumn c = new UIColumn();
		viewer.setContentItemValueBinding(getVar()+".contentItem");  //binded with ContentItemBindingBean#getContentItem()
		WFUtil.setValueBinding(viewer,"rendered",getVar()+".rendered");
		c.getChildren().add(viewer);
		this.getChildren().add(c);
	}
	
	protected void addAttachmentViewer(ContentItemViewer viewer, int index){
		UIColumn c = new UIColumn();
		viewer.setContentItemValueBinding(getVar()+".attachedments["+index+"]");  //binded with ContentItemBindingBean#getContentItem()
		c.getChildren().add(viewer);
		this.getChildren().add(c);
	}
	
	@Override
	public Object getValue(){
		if(this.model==null){
			List items = getManagedBean().getContentItems();
			if(items!=null){
				this.model = new WFDataModel();
				for (ListIterator iter = items.listIterator(); iter.hasNext();) {
					int index = iter.nextIndex();
					ContentItem item = (ContentItem) iter.next();
					ContentItemBindingBean bean = new ContentItemBindingBean(item);
					this.model.set(bean,index);
				}
				return this.model;
			}
			return super.getValue();
		}
		return this.model;
	}
	
	@Override
	public void encodeBegin(FacesContext context) throws IOException{
		UIComponentCacher cacher = getCacher(context);
		setItemCategoryFromRequest(context);
		setViewerIdentifier(context);
		
		if(cacher.existsInCache(this,context)){
			// do nothing:
		}
		else{
			if(cacher.isCacheEnbled(this,context)){
				cacher.beginCache(this,context);
			}
			
			if(!this.initialized){
				initializeInEncodeBegin();
			}
			super.encodeBegin(context);
		}
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException{
		UIComponentCacher cacher = getCacher(context);
		if(cacher.existsInCache(this,context)){
			// do nothing:
		}
		else{
			super.encodeChildren(context);
		}	
	}
	
	@Override
	public void encodeEnd(FacesContext context) throws IOException{
		UIComponentCacher cacher = getCacher(context);
		if(cacher.existsInCache(this,context)){
			cacher.encodeCached(this,context);
		}
		else{
			super.encodeEnd(context);
			if(cacher.isCacheEnbled(this,context)){
				cacher.endCache(this,context);
			}
		}
	}
	
	public String getDefultStyleClass(){
		return "content_list";
	}
	
	public String getDefultRowClass(){
		if(getChildCount() > 1){
			return "content_list_item";
		} else {
			return null;
		}
		
	}
	
	public void setStyle(String style)
    {
        this._style = style;
    }

    public String getStyle()
    {
        if (this._style != null) {
					return this._style;
				}
        ValueBinding vb = getValueBinding("style");
        return vb != null ? (String)vb.getValue(getFacesContext()) : null;
    }

    public void setStyleClass(String styleClass)
    {
        this._styleClass = styleClass;
    }

    public String getStyleClass()
    {
        if (this._styleClass != null) {
					return this._styleClass;
				}
        ValueBinding vb = getValueBinding("styleClass");
        String sClass = vb != null ? (String)vb.getValue(getFacesContext()) : null;
        return (sClass != null)? sClass : getDefultStyleClass(); 
    }
	
	public void setColumnClasses(String columnClasses)
    {
        this._columnClasses = columnClasses;
    }

    public String getColumnClasses()
    {
        if (this._columnClasses != null) {
					return this._columnClasses;
				}
        ValueBinding vb = getValueBinding("columnClasses");
        return vb != null ? (String)vb.getValue(getFacesContext()) : null;
    }
    
    public void setRowClasses(String rowClasses)
    {
        this._rowClasses = rowClasses;
    }

    public String getRowClasses()
    {
        if (this._rowClasses != null) {
					return this._rowClasses;
				}
        ValueBinding vb = getValueBinding("rowClasses");
        String sClass = vb != null ? (String)vb.getValue(getFacesContext()) : null;
        return (sClass != null)? sClass : getDefultRowClass();
    }
	
	/**
	 * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[12];
		values[0] = super.saveState(ctx);
		values[1] = this.managedBeanId;
		values[2] = this.resourcePath;
		values[3] = this._styleClass;
		values[4] = this._style;
		values[5] = this._columnClasses;
		values[6] = this._rowClasses;
		values[7] = this.detailsViewerPath;
		values[8] = Boolean.valueOf(this.initialized);
		values[9] = this.categoriesList;
		values[10] = new Integer(this.maxNumberOfDisplayed);
		values[11] = this.articleItemViewerFilter;
		return values;
	}
	
	/**
	 * @see javax.faces.component.StatHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[])state;
		super.restoreState(ctx, values[0]);
		this.managedBeanId = (String) values[1];
		this.resourcePath = (String) values[2];
		this._styleClass = (String) values[3];
		this._style = (String) values[4];
		this._columnClasses = (String) values[5];
		this._rowClasses = (String) values[6];
		this.detailsViewerPath = (String)values[7];
		this.initialized = ((Boolean)values[8]).booleanValue();
		this.categoriesList = (List<String>) values[9];
		this.maxNumberOfDisplayed=((Integer)values[10]).intValue();
		this.articleItemViewerFilter = values[11] == null ? null : String.valueOf(values[11]);
		
		notifyManagedBeanOfVariableValues();
		
	}
	
	protected void notifyManagedBeanOfVariableValues() {
		notifyManagedBeanOfBaseFolderPath(this.resourcePath);
		notifyManagedBeanOfDetailsViewerPath(this.detailsViewerPath);
		notifyManagedBeanOfCategories(this.categoriesList);
		notifyManagedBeanOfViewerIdentifier(this.articleItemViewerFilter);
		
		int maxItems = getMaxNumberOfDisplayed();
		if(maxItems!=-1){
			getManagedBean().setMaxNumberOfDisplayed(maxItems);
		}
	}

	/**
	 * @param resourcePath
	 */
	private void notifyManagedBeanOfBaseFolderPath(String resourcePath) {
		if(this.managedBeanId!=null){
			getManagedBean().setBaseFolderPath(resourcePath);
		}
	}
	
	/**
	 * @param resourcePath
	 */
	private void notifyManagedBeanOfDetailsViewerPath(String path) {
		if(this.managedBeanId!=null){
			getManagedBean().setDetailsViewerPath(this.detailsViewerPath);
		}
	}
	
	private void notifyManagedBeanOfCategories(List<String> categories) {
		if(this.managedBeanId!=null){
			getManagedBean().setCategories(categories);
		}
	}
	
	private void notifyManagedBeanOfViewerIdentifier(String identifier) {
		if (this.managedBeanId != null) {
			getManagedBean().setViewerIdentifier(identifier);
		}
	}

	public class ContentItemBindingBean {
		
		private ContentItem item;
		
		public ContentItemBindingBean(ContentItem item){
			this.item = item;
		}
		
		public ContentItem getContentItem(){
			return this.item;
		}
		
		public void setContentItem(Object obj){
			//does nothing
		}
		
		public void setAttachments(){
			//does nothing
		}
		
	}
	
	/**
	 * @return Returns the detailsViewerPath.
	 */
	public String getDetailsViewerPath() {
		return this.detailsViewerPath;
	}
	/**
	 * @param detailsViewerPath The path to set.
	 */
	public void setDetailsViewerPath(String path) {
		this.detailsViewerPath = path;
		notifyManagedBeanOfDetailsViewerPath(this.detailsViewerPath);
	}
	/**
	 * @return Returns the categoriesList.
	 */
	public List<String> getCategoriesList() {
		return this.categoriesList;
	}
	/**
	 * <p>
	 * Sets the categoriesList as a comma separated list
	 * </p>
	 * @param categoriesList
	 */
	public void setCategories(String categories){
		if(categories!=null){
			List<String> cats = new ArrayList<String>();
			StringTokenizer tokenizer = new StringTokenizer(categories, CategoryBean.CATEGORY_DELIMETER);
			while(tokenizer.hasMoreTokens()){
				cats.add(tokenizer.nextToken().trim());
			}
			List<String> cats2 = (cats.isEmpty())?null:cats;
			setCategoriesList(cats2);
		} else {	
			//null
		}
	}
	
	public String getCategories() {
		if(this.categoriesList!=null){
			Iterator iter = this.categoriesList.iterator();
			if(iter.hasNext()){
				StringBuffer catString = new StringBuffer();
				catString.append(iter.next());
				while(iter.hasNext()){
					catString.append(CategoryBean.CATEGORY_DELIMETER);
					catString.append(iter.next());
				}
				return catString.toString();
			}
		}
		return null;
	}
	
	
	/**
	 * @param categoriesList The categoriesList to set.
	 */
	public void setCategoriesList(List<String> categories) {
		this.categoriesList = categories;
		notifyManagedBeanOfCategories(categories);
	}

	public String getFirstArticleItemStyleClass() {
		return this.firstArticleItemStyleClass;
	}

	public void setFirstArticleItemStyleClass(String firstArticleItemStyleClass) {
		this.firstArticleItemStyleClass = firstArticleItemStyleClass;
	}
	
	public ContentListViewerManagedBean getManagedBean(){
		ContentListViewerManagedBean bean = (ContentListViewerManagedBean) WFUtil.getBeanInstance(this.managedBeanId);
		return bean;
	}

	
	/**
	 * @return Returns the maxNumberOfItems.
	 */
	public int getMaxNumberOfDisplayed() {
		return this.maxNumberOfDisplayed;
	}

	
	/**
	 * @param maxNumberOfItems The maxNumberOfItems to set.
	 */
	public void setMaxNumberOfDisplayed(int maxNumberOfItems) {
		this.maxNumberOfDisplayed = maxNumberOfItems;
	}


	public UIComponentCacher getCacher(FacesContext context){
		return UIComponentCacher.getDefaultCacher(context);
	}

	/* (non-Javadoc)
	 * @see com.idega.core.cache.CacheableUIComponent#getViewState(javax.faces.context.FacesContext)
	 */
	public String getViewState(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		StringBuffer buf = new StringBuffer();
		if(ContentUtil.hasContentEditorRoles(iwc)){
			buf.append("edit");
		}
		else{
			buf.append("view");
		}
		
		//	Categories
		String categories = this.getCategories();
		if(categories!=null){
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(categories);
		}
		
		//	Viewer identifier
		String viewerIdentifier = getArticleItemViewerFilter();
		if (viewerIdentifier != null) {
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(viewerIdentifier);
		}
		
		//	Number of max items
		buf.append(UIComponentCacher.UNDERSCORE);
		buf.append(this.maxNumberOfDisplayed);
		
		if(this.detailsViewerPath!=null){
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(this.detailsViewerPath);
		}
		
		if(this._columnClasses!=null){
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(this._columnClasses);
		}
		if(this._style!=null){
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(this._style);
		}
		if(this._styleClass!=null){
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(this._styleClass);
		}
		if(this.firstArticleItemStyleClass!=null){
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(this.firstArticleItemStyleClass);
		}
		if(this.resourcePath!=null){
			buf.append(UIComponentCacher.UNDERSCORE);
			buf.append(this.resourcePath);
		}
		
		//	Resource path set in request?
		String resourcePathFromRequest = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		if (resourcePathFromRequest != null) {
			buf.append(UIComponentCacher.UNDERSCORE).append(resourcePathFromRequest);
		}
		return buf.toString();
	}
	
	private void setItemCategoryFromRequest(FacesContext context) {
		if (context == null) {
			return;
		}
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		String category = request.getParameter(ITEMS_CATEGORY_VIEW);
		if (category != null) { // Just to be sure not overriding (maybe) existing category
			setCategories(category);
		}
	}
	
	private void setViewerIdentifier(FacesContext context) {
		if (context == null) {
			return;
		}
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		String identifier = request.getParameter(ContentConstants.CONTENT_ITEM_VIEWER_IDENTIFIER_PARAMETER);
		if (identifier != null) {
			setArticleItemViewerFilter(identifier);
		}
	}

	public String getArticleItemViewerFilter() {
		return articleItemViewerFilter;
	}

	public void setArticleItemViewerFilter(String articleItemViewerFilter) {
		this.articleItemViewerFilter = articleItemViewerFilter;
		notifyManagedBeanOfViewerIdentifier(articleItemViewerFilter);
	}
}
