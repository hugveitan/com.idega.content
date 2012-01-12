package com.idega.content.presentation;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentPathBean;
import com.idega.content.business.WebDAVFilePermissionResource;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.webface.WFBlock;
import com.idega.webface.WFTitlebar;
import com.idega.webface.WFToolbar;
import com.idega.webface.WFToolbarButton;
import com.idega.webface.WFUtil;

/**
 * @author gimmi
 */
public class ContentViewer extends ContentBlock implements ActionListener{

	public static final String PARAMETER_ROOT_FOLDER = "cv_prt";

	public static final String PARAMETER_ACTION = "iw_content_action";
	public static final String PARAMETER_CONTENT_RESOURCE = "iw_content_rs_url";
	private static final String PARAMETER_CURRENT_PATH = "current_path_in_slide_for_documents";

	public static final String ACTION_LIST = "ac_list";
	public static final String ACTION_FILE_DETAILS = "ac_file_details";
	public static final String ACTION_FILE_DETAILS_LESS = "ac_less_file_details";
	public static final String ACTION_PREVIEW = "ac_preview";
	public static final String ACTION_PERMISSIONS = "ac_permissions";
	public static final String ACTION_NEW_FOLDER = "ac_folder";
	public static final String ACTION_UPLOAD = "ac_upload";
	public static final String ACTION_DELETE = "ac_delete";

	public static final String CONTENT_VIEWER_EDITOR_NEEDS_FORM = "contentViewerEditorNeedsForm";

	static final String PATH_TO_DELETE = "ac_path2del";

	private String currentAction = null;

	private boolean renderListLink = true;
	private boolean renderDetailsLink = false;
	private boolean renderPreviewLink = false;
	private boolean renderNewFolderLink = true;
	private boolean renderPermissionsLink = true;

	private boolean renderWebDAVList = true;
	private boolean renderWebDAVFileDetails = false;
	private boolean renderWebDAVFilePreview = false;
	private boolean renderWebDAVNewFolder = false;
	private boolean renderWebDAVFilePermissions = false;
	private boolean renderWebDAVDeleter = false;
	private boolean renderWebDAVUploadeComponent = true;

	private String rootFolder = null;
	private boolean useUserHomeFolder = false;
	private String startFolder = null;
	private String iconTheme = null;
	private boolean showFolders = true;
	private boolean showPublicFolder = true;
	private boolean showDropboxFolder = true;
	private Collection<String> columnsToHide = null;
	private boolean maintainPath = false;
	private boolean useVersionControl = true;
	private boolean showPermissionTab = true;
	private boolean showUploadComponent = true;
	private String onFileClickEvent = null;

	private String currentFolderPath = null;
	private String currentFileName = null;
	private String currentResourceName = null;

	public ContentViewer() {
		super();
	}

	@Override
	public void initializeComponent(FacesContext context) {
		WFBlock listBlock = new WFBlock();
		listBlock.setStyleClass(listBlock.getStyleClass()+" contentListBlock");
		WFTitlebar tb = new WFTitlebar();
		String listBlockId = getId()+"_list";

		tb.addTitleText(getBundle().getLocalizedText("document_list"));
		tb.addTitleText(getCurrentResourceName());
		tb.setToolTip(getCurrentFolderPath());
		listBlock.setTitlebar(tb);
		listBlock.setToolbar(getToolbar(listBlockId));

		WebDAVList list = new WebDAVList();
		list.setId(listBlockId);
		list.setRendered(this.renderWebDAVList);
		list.setStartFolder(this.startFolder);
		list.setRootFolder(this.rootFolder);
		list.setIconTheme(this.iconTheme);
		list.setShowFolders(this.showFolders);
		list.setShowDropboxFolder(this.showDropboxFolder);
		list.setShowPublicFolder(this.showPublicFolder);
		list.setColumnsToHide(this.columnsToHide);
		list.setUseVersionControl(this.useVersionControl);
		list.setOnFileClickEvent(this.onFileClickEvent);

		listBlock.add(list);

		WFBlock detailsBlock = new WFBlock();
		WFTitlebar detailsBar = new WFTitlebar();
		String detailsBlockId = getId()+"_details";
		detailsBar.addTitleText(getBundle().getLocalizedText("document_details"));
		detailsBar.addTitleText(" (");
		detailsBar.addTitleText(getCurrentFileName());
		detailsBar.addTitleText(")");
		detailsBlock.setTitlebar(detailsBar);
		detailsBlock.setToolbar(getToolbar(detailsBlockId));
		WebDAVFileDetails details = new WebDAVFileDetails();
		details.setRendered(this.renderWebDAVFileDetails);
		details.setId(detailsBlockId);
		details.setUseVersionControl(this.useVersionControl);
		detailsBlock.add(details);

		WFBlock previewBlock = new WFBlock();
		WFTitlebar previewBar = new WFTitlebar();
		String previewBlockId = getId()+"_preview";
		String details2BlockId = getId()+"_details2";

		previewBar.addTitleText(getBundle().getLocalizedText("document_details"));
		previewBar.addTitleText(" (");
		previewBar.addTitleText(getCurrentFileName());
		previewBar.addTitleText(")");
		previewBlock.setTitlebar(previewBar);
		previewBlock.setToolbar(getToolbar(previewBlockId));
		WebDAVFileDetails details2 = new WebDAVFileDetails();
		details2.setRendered(this.renderWebDAVFilePreview);
		details2.setId(details2BlockId);
		details2.setDetailed(false);
		details2.setUseVersionControl(this.useVersionControl);
		WebDAVFilePreview preview = new WebDAVFilePreview();
		preview.setRendered(this.renderWebDAVFilePreview);
		preview.setId(previewBlockId);
		previewBlock.add(details2);

		WFBlock folderBlock = new WFBlock();
		WFTitlebar folderBar = new WFTitlebar();
		folderBar.addTitleText(getBundle().getLocalizedText("create_a_folder"));
		folderBar.addTitleText(getCurrentResourceName());
		folderBlock.setTitlebar(folderBar);
		folderBlock.setToolbar(new WFToolbar());
		WebDAVFolderCreation folder = new WebDAVFolderCreation();
		folder.setRendered(this.renderWebDAVNewFolder);
		folder.setId(getId()+"_folder");
		folderBlock.add(folder);

		WFBlock uploadBlock = new WFBlock();
		WFTitlebar uploadBar = new WFTitlebar();
		uploadBar.addTitleText(getBundle().getLocalizedText("upload"));
		uploadBar.addTitleText(getCurrentResourceName());
		uploadBlock.setTitlebar(uploadBar);
		WebDAVUpload upload = new WebDAVUpload();
		if (startFolder == null) {
			upload.setUploadPath(currentFolderPath);
		}
		else {
			if (startFolder.equals(CoreConstants.PATH_FILES_ROOT) && !(startFolder.equals(currentFolderPath))) {
				upload.setUploadPath(currentFolderPath);
			}
			else {
				upload.setUploadPath(startFolder);
			}
		}
		upload.setShowStatusAfterUploadAttempt(true);
		upload.setId(getId()+"_upload");
		upload.setUseVersionComment(true);
		uploadBlock.add(upload);

		WFBlock deleteBlock = new WFBlock();
		WFTitlebar deleteBar = new WFTitlebar();
		deleteBar.addTitleText(getBundle().getLocalizedText("delete"));
		deleteBar.addTitleText(getCurrentResourcePath());
		deleteBlock.setTitlebar(deleteBar);
		deleteBlock.setToolbar(new WFToolbar());

		WebDAVDocumentDeleter deleter = new WebDAVDocumentDeleter();
		deleter.setRendered(this.renderWebDAVDeleter);
		deleter.setId(getId()+"_deleter");
		deleteBlock.add(deleter);

		WFBlock permissionsBlock = new WFBlock();
		WFTitlebar permissionsBar = new WFTitlebar();
		String permissionsBlockId = getId()+"_permissions";
		permissionsBar.addTitleText(getBundle().getLocalizedText("permissions"));
		permissionsBar.addTitleText(getCurrentResourceName());
		permissionsBlock.setTitlebar(permissionsBar);
		permissionsBlock.setToolbar(getToolbar(permissionsBlockId));
		WebDAVFilePermissions permissions = new WebDAVFilePermissions();
		permissions.setRendered(this.renderWebDAVFilePermissions);
		permissions.setId(permissionsBlockId);
		permissionsBlock.add(permissions);

		getFacets().put(ACTION_LIST, listBlock);
		getFacets().put(ACTION_FILE_DETAILS, detailsBlock);
		getFacets().put(ACTION_FILE_DETAILS_LESS, previewBlock);
		getFacets().put(ACTION_PREVIEW, preview);
		getFacets().put(ACTION_PERMISSIONS, permissionsBlock);
		getFacets().put(ACTION_NEW_FOLDER, folderBlock);
		getFacets().put(ACTION_UPLOAD, uploadBlock);
		getFacets().put(ACTION_DELETE, deleteBlock);

	}

	public void setRootPath(String rootFolder) {
		this.rootFolder = rootFolder;
	}

	public void setUseUserHomeFolder(boolean useUserHomeFolder) {
		this.useUserHomeFolder = useUserHomeFolder;
	}

	public void setStartPath(String startFolder) {
		this.startFolder = startFolder;
	}

	public void setIconTheme(String iconTheme) {
		this.iconTheme = iconTheme;
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

	public void setColumnsToHide(String columns) {
		if (columns != null) {
			Collection<String> v = new Vector<String>();
			int index = columns.indexOf(",");
			while (index > -1) {
				String tmp = columns.substring(0, index);
				v.add(tmp.trim());
				columns = columns.substring(index+1);
				index = columns.indexOf(",");
			}
			v.add(columns.trim());

			this.columnsToHide = v;
		}
	}

	public String getColumnsToHide(){
		if(this.columnsToHide!=null){
			return ListUtil.convertListOfStringsToCommaseparatedString((List<String>)this.columnsToHide);
		}

		return null;
	}

	@Override
	public void decode(FacesContext context) {
		//TODO USE DECODE, DOES NOT WORK BECAUSE IT IS NEVER CALLED!

		Map<String, Object> parameters = context.getExternalContext().getRequestMap();

		String action = (String) parameters.get(PARAMETER_ACTION);
		String resourceURL = (String) parameters.get(PARAMETER_CONTENT_RESOURCE);
		if(resourceURL!=null){
			setCurrentResourcePath(resourceURL);
		}

		if(action!=null){
			setRenderFlags(action);

			if(ACTION_PERMISSIONS.equals(action)){
				IWContext iwc = IWContext.getInstance();
				try {
					WebDAVFilePermissionResource resource = (WebDAVFilePermissionResource) IBOLookup.getSessionInstance(iwc, WebDAVFilePermissionResource.class);
					resource.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			maintainPath(true);
		}

	}

	public boolean doRenderPermissionLink(){
		if(this.renderPermissionsLink){
			//	TODO: implement
//			try {
//				IWContext iwc = IWContext.getInstance();
//				IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
//				return session.hasPermission(getCurrentResourcePath(),IWSlideConstants.PRIVILEGE_READ_ACL);
//			}
//			catch (IBOLookupException e) {
//				e.printStackTrace();
//			}
//			catch (UnavailableIWContext e) {
//				e.printStackTrace();
//			}
//			catch (RemoteException e) {
//				e.printStackTrace();
//			}
		}
		return false;
	}

	public boolean doRenderUploadeComponent(){
		if(this.renderWebDAVUploadeComponent){
			//	TODO: implement
//			try {
//				IWContext iwc = IWContext.getInstance();
//				IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
//				return session.hasPermission(getCurrentResourcePath(),IWSlideConstants.PRIVILEGE_WRITE);
//			}
//			catch (IBOLookupException e) {
//				e.printStackTrace();
//			}
//			catch (UnavailableIWContext e) {
//				e.printStackTrace();
//			}
//			catch (RemoteException e) {
//				e.printStackTrace();
//			}
		}
		return false;
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		if (this.useUserHomeFolder) {
			IWContext iwc = IWContext.getIWContext(context);
			this.rootFolder = getRepositoryService().getUserHomeFolder(iwc.getLoggedInUser());
		}

		if (this.rootFolder == null) {
			this.rootFolder = (String) this.getAttributes().get("rootFolder");
		}

		Map parameters = context.getExternalContext().getRequestParameterMap();

		String action = (String) parameters.get(PARAMETER_ACTION);
		String resourceURL = (String) parameters.get(PARAMETER_CONTENT_RESOURCE);
		if(resourceURL!=null){
			setCurrentResourcePath(resourceURL);
		}

		if(action!=null){
			setRenderFlags(action);

			if(ACTION_PERMISSIONS.equals(action)){
				IWContext iwc = IWContext.getInstance();
				try {
					WebDAVFilePermissionResource resource = (WebDAVFilePermissionResource) IBOLookup.getSessionInstance(iwc, WebDAVFilePermissionResource.class);
					resource.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			maintainPath(true);

		}

		if (!this.maintainPath) {
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "resetSorter");
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setClickedFilePath", null, String.class);
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setWebDAVPath", this.rootFolder, String.class);
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setRootFolder", this.rootFolder, String.class);
		}

		Boolean fileSelected = (Boolean) WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "getIsClickedFile");

		String tmp = context.getExternalContext().getRequestParameterMap().get(PARAMETER_ROOT_FOLDER);
		if (tmp != null) {
			IWUserContext iwuc = IWContext.getInstance();
			String webDAVServerURI = getRepositoryService().getWebdavServerURL();
			tmp = tmp.replaceFirst(webDAVServerURI, "");
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setWebDAVPath", tmp);
			this.rootFolder = tmp;
		}
		if (ACTION_LIST.equals(this.currentAction)) {

			this.renderListLink = true;
			this.renderDetailsLink = false;
			this.renderPreviewLink = false;
			this.renderNewFolderLink = true;
			this.renderPermissionsLink=true;

		}
		else {
			if (fileSelected.booleanValue()) {

				this.renderListLink = true;
				if (ACTION_PREVIEW.equals(this.currentAction)) {
					this.renderDetailsLink = true;
					this.renderPreviewLink = false;
				} else {
					this.renderDetailsLink = false;
					this.renderPreviewLink = true;
				}
				this.renderNewFolderLink = false;
				this.renderPermissionsLink=true;

				if (this.currentAction == null) {
					setRenderFlags(ACTION_FILE_DETAILS);
				}
				else if(ACTION_PREVIEW.equals(this.currentAction)){
					setRenderFlags(ACTION_PREVIEW);
				}

			}
		}

		if(!getShowPermissionTab()){
			this.renderPermissionsLink=false;
		}

		if(!getShowUploadComponent()){
			this.renderWebDAVUploadeComponent = false;
		}

		super.encodeBegin(context);
	}

	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);

		UIComponent list = getFacet(ACTION_LIST);
		if (list != null) {
			list.setRendered(this.renderWebDAVList);
			renderChild(context, list);
		}

		UIComponent details = getFacet(ACTION_FILE_DETAILS);
		if (details != null) {
			details.setRendered(this.renderWebDAVFileDetails);
			renderChild(context, details);
		}

		UIComponent detailsLess = getFacet(ACTION_FILE_DETAILS_LESS);
		if (detailsLess != null) {
			detailsLess.setRendered(this.renderWebDAVFilePreview);
			renderChild(context, detailsLess);
		}

		UIComponent preview = getFacet(ACTION_PREVIEW);
		if (preview != null) {
			preview.setRendered(this.renderWebDAVFilePreview);
			renderChild(context, preview);
		}

		UIComponent folder = getFacet(ACTION_NEW_FOLDER);
		if (folder != null) {
			folder.setRendered(this.renderWebDAVNewFolder);
			renderChild(context, folder);
		}

		UIComponent permissions = getFacet(ACTION_PERMISSIONS);
		if (permissions != null) {
			permissions.setRendered(this.renderWebDAVFilePermissions);
			renderChild(context, permissions);
		}

		UIComponent deleter = getFacet(ACTION_DELETE);
		if (deleter != null) {
			deleter.setRendered(this.renderWebDAVDeleter);
			renderChild(context, deleter);
		}

		UIComponent upload = getFacet(ACTION_UPLOAD);
		if (upload != null) {
			upload.setRendered(doRenderUploadeComponent());
			renderChild(context, upload);
		}
	}

	public WFToolbar getToolbar(String baseId) {
		WFToolbar bar = new WFToolbar();
		bar.setId(baseId+"_toolbar");
		WFToolbarButton list = new WFToolbarButton();
		list.getAttributes().put(PARAMETER_ACTION, ACTION_LIST);
		list.setId(baseId+"_btnList");
		list.setStyleClass("content_viewer_document_list");
		list.setToolTip(getBundle().getLocalizedString("document_list"));
		list.setDisplayText(getBundle().getLocalizedString("document_list"));
		list.setActionListener(WFUtil.createMethodBinding("#{contentviewerbean.processAction}", new Class[]{ActionEvent.class}));
		list.setRendered(this.renderListLink);

		WFToolbarButton details = new WFToolbarButton();
		details.getAttributes().put(PARAMETER_ACTION, ACTION_FILE_DETAILS);
		details.setStyleClass("content_viewer_details");
		details.setId(baseId+"_btnDetails");
		details.setToolTip(getBundle().getLocalizedString("document_details"));
		details.setDisplayText(getBundle().getLocalizedString("document_details"));
		details.setActionListener(WFUtil.createMethodBinding("#{contentviewerbean.processAction}", new Class[]{ActionEvent.class}));
		details.setRendered(this.renderDetailsLink);

		WFToolbarButton preview = new WFToolbarButton();
		preview.getAttributes().put(PARAMETER_ACTION, ACTION_PREVIEW);
		preview.setId(baseId+"_btnPreview");
		preview.setStyleClass("content_viewer_preview");
		preview.setToolTip(getBundle().getLocalizedString("preview"));
		preview.setDisplayText(getBundle().getLocalizedString("preview"));
		preview.setActionListener(WFUtil.createMethodBinding("#{contentviewerbean.processAction}", new Class[]{ActionEvent.class}));
		preview.setRendered(this.renderPreviewLink);

		WFToolbarButton newFolder = new WFToolbarButton();
		newFolder.getAttributes().put(PARAMETER_ACTION, ACTION_NEW_FOLDER);
		newFolder.getAttributes().put(PARAMETER_CURRENT_PATH, currentFolderPath);
		newFolder.setId(baseId+"_btnNewFolder");
		newFolder.setStyleClass("content_viewer_new_folder");
		newFolder.setToolTip(getBundle().getLocalizedString("create_a_folder"));
		newFolder.setDisplayText(getBundle().getLocalizedString("create_a_folder"));
		newFolder.setActionListener(WFUtil.createMethodBinding("#{contentviewerbean.processAction}", new Class[]{ActionEvent.class}));
		newFolder.setRendered(this.showFolders && this.renderNewFolderLink);

		WFToolbarButton permissions = new WFToolbarButton();
		permissions.getAttributes().put(PARAMETER_ACTION, ACTION_PERMISSIONS);
		permissions.setId(baseId+"_btnPermissions");
		permissions.setStyleClass("content_viewer_permissions");
		permissions.setToolTip(getBundle().getLocalizedString("permissions"));
		permissions.setDisplayText(getBundle().getLocalizedString("permissions"));
		permissions.setActionListener(WFUtil.createMethodBinding("#{contentviewerbean.processAction}", new Class[]{ActionEvent.class}));
		permissions.setRendered(doRenderPermissionLink());

		bar.addButton(newFolder);
		bar.addButton(list);
		bar.addButton(details);
		bar.addButton(preview);
		bar.addButton(permissions);

		return bar;
	}

	@Override
	public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
		Object source = actionEvent.getSource();
		if (source instanceof WFToolbarButton) {
			maintainPathParameter(((WFToolbarButton) source).getAttributes(), PARAMETER_CURRENT_PATH);

			WFToolbarButton bSource = (WFToolbarButton) source;
			String action = (String) bSource.getAttributes().get(PARAMETER_ACTION);
			if (action != null) {
				setRenderFlags(action);
				if(ACTION_PERMISSIONS.equals(action)){
					IWContext iwc = IWContext.getInstance();
					try {
						WebDAVFilePermissionResource resource = (WebDAVFilePermissionResource) IBOLookup.getSessionInstance(iwc, WebDAVFilePermissionResource.class);
						resource.clear();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else if (source instanceof UIComponent){
			String action = (String) ((UIComponent) source).getAttributes().get(PARAMETER_ACTION);
			if (ACTION_DELETE.equals(action)) {
				maintainPathParameter(((UIComponent) source).getAttributes(), PATH_TO_DELETE, true);
			} else if (ACTION_NEW_FOLDER.equals(action)) {
				maintainPathParameter(((UIComponent) source).getAttributes(), WebDAVFolderCreation.PARAMETER_RESOURCE_PATH);
			}
			setRenderFlags(action);
		}

		maintainPath(true);
	}

	private static final void maintainPathParameter(Map<String, Object> parameters, String parameterName, boolean markClickedFile) {
		if (parameters == null || StringUtil.isEmpty(parameterName)) {
			return;
		}

		String pathToMaintain = null;

		String key = null;
		boolean parameterFound = false;
		for (Iterator<String> paramsIter = parameters.keySet().iterator(); (!parameterFound && paramsIter.hasNext());) {
			key = paramsIter.next();

			if (parameterName.equals(key)) {
				Object o = parameters.get(key);

				if (o instanceof String) {
					pathToMaintain = o.toString();
				} else if (o instanceof UIParameter) {
					pathToMaintain = ((UIParameter) o).getValue().toString();
				}

				if (!StringUtil.isEmpty(pathToMaintain)) {
					parameterFound = true;


				}
			}
		}

		if (!parameterFound) {
			IWContext iwc = CoreUtil.getIWContext();
			if (iwc.isParameterSet(parameterName)) {
				pathToMaintain = iwc.getParameter(parameterName);
				parameterFound = true;
			}
		}

		if (parameterFound) {
			if (markClickedFile) {
				WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setClickedFilePath", pathToMaintain);

				if (pathToMaintain.indexOf(CoreConstants.SLASH) != -1) {
					pathToMaintain = pathToMaintain.substring(0, pathToMaintain.lastIndexOf(CoreConstants.SLASH));
				}
			}
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setWebDAVPath", pathToMaintain);
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setUseStartPathIfAvailable", Boolean.FALSE);
			WFUtil.invoke(ContentPathBean.BEAN_ID, "setPath", pathToMaintain);
		}
	}

	protected static final void maintainPathParameter(Map<String, Object> parameters, String parameterName) {
		maintainPathParameter(parameters, parameterName, false);
	}

	protected void maintainPath(boolean maintain) {
		this.maintainPath = maintain;
	}

	protected boolean getMaintainPath() {
		return this.maintainPath;
	}

	public void setRenderFlags(String action) {
		this.currentAction = action;
		if (ACTION_LIST.equals(action)) {
			this.renderWebDAVList = true;
			this.renderWebDAVFileDetails = false;
			this.renderWebDAVFilePreview = false;
			this.renderWebDAVNewFolder = false;
			this.renderWebDAVFilePermissions = false;
			this.renderWebDAVDeleter = false;
			this.renderWebDAVUploadeComponent = true;
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setClickedFilePath", null, String.class);

		} else if (ACTION_FILE_DETAILS.equals(action)) {
			this.renderWebDAVList = false;
			this.renderWebDAVFileDetails = true;
			this.renderWebDAVFilePreview = false;
			this.renderWebDAVNewFolder = false;
			this.renderWebDAVFilePermissions = false;
			this.renderWebDAVDeleter = false;
			this.renderWebDAVUploadeComponent = true;
		} else if (ACTION_PREVIEW.equals(action)) {
			this.renderWebDAVList = false;
			this.renderWebDAVFileDetails = false;
			this.renderWebDAVFilePreview = true;
			this.renderWebDAVNewFolder = false;
			this.renderWebDAVFilePermissions = false;
			this.renderWebDAVDeleter = false;
			this.renderWebDAVUploadeComponent = true;
		}else if (ACTION_NEW_FOLDER.equals(action)) {
			this.renderWebDAVList = true;
			this.renderWebDAVFileDetails = false;
			this.renderWebDAVFilePreview = false;
			this.renderWebDAVNewFolder = true;
			this.renderWebDAVFilePermissions = false;
			this.renderWebDAVUploadeComponent = true;
		} else if (ACTION_PERMISSIONS.equals(action)) {
			this.renderWebDAVList = false;
			this.renderWebDAVFileDetails = false;
			this.renderWebDAVFilePreview = false;
			this.renderWebDAVNewFolder = false;
			this.renderWebDAVFilePermissions = true;
			this.renderWebDAVUploadeComponent = false;
		}else if (ACTION_DELETE.equals(action)) {
			this.renderWebDAVList = true;
			this.renderWebDAVFileDetails = false;
			this.renderWebDAVFilePreview = false;
			this.renderWebDAVNewFolder = false;
			this.renderWebDAVFilePermissions = false;
			this.renderWebDAVDeleter = true;
			this.renderWebDAVUploadeComponent = true;
		}
	}

	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[26];
		values[0] = super.saveState(ctx);
		values[1] = new Boolean(this.renderWebDAVList);
		values[2] = new Boolean(this.renderWebDAVFileDetails);
		values[3] = new Boolean(this.renderWebDAVFilePreview);
		values[4] = new Boolean(this.renderWebDAVNewFolder);
		values[5] = new Boolean(this.renderListLink);
		values[6] = new Boolean(this.renderDetailsLink);
		values[7] = new Boolean(this.renderPreviewLink);
		values[8] = new Boolean(this.renderNewFolderLink);
		values[9] = this.rootFolder;
		values[10] = new Boolean(this.renderPermissionsLink);
		values[11] = new Boolean(this.renderWebDAVFilePermissions);
		values[12] = new Boolean(this.renderWebDAVDeleter);
		values[13] = this.currentFolderPath;
		values[14] = this.currentFileName;
		values[15] = this.startFolder;
		values[16] = new Boolean(this.useUserHomeFolder);
		values[17] = new Boolean(this.showFolders);
		values[18] = this.columnsToHide;
		values[19] = new Boolean(this.useVersionControl);
		values[20] = new Boolean(this.renderWebDAVUploadeComponent);
		values[21] = new Boolean(this.showPermissionTab);
		values[22] = new Boolean(this.showUploadComponent);
		values[23] = this.onFileClickEvent;
		values[24] = new Boolean(this.showPublicFolder);
		values[25] = new Boolean(this.showDropboxFolder);

		return values;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.renderWebDAVList = ((Boolean) values[1]).booleanValue();
		this.renderWebDAVFileDetails = ((Boolean) values[2]).booleanValue();
		this.renderWebDAVFilePreview = ((Boolean) values[3]).booleanValue();
		this.renderWebDAVNewFolder = ((Boolean) values[4]).booleanValue();
		this.renderListLink = ((Boolean) values[5]).booleanValue();
		this.renderDetailsLink = ((Boolean) values[6]).booleanValue();
		this.renderPreviewLink = ((Boolean) values[7]).booleanValue();
		this.renderNewFolderLink = ((Boolean) values[8]).booleanValue();
		this.rootFolder = (String) values[9];
		this.renderPermissionsLink = ((Boolean) values[10]).booleanValue();
		this.renderWebDAVFilePermissions = ((Boolean) values[11]).booleanValue();
		this.renderWebDAVDeleter = ((Boolean) values[12]).booleanValue();
		this.currentFolderPath = ((String)values[13]);
		this.currentFileName = ((String)values[14]);
		this.startFolder = ((String) values[15]);
		this.useUserHomeFolder = ((Boolean) values[16]).booleanValue();
		this.showFolders = ((Boolean) values[17]).booleanValue();
		this.columnsToHide = ((Collection<String>) values[18]);
		this.useVersionControl = ((Boolean) values[19]).booleanValue();
		this.renderWebDAVUploadeComponent = ((Boolean) values[20]).booleanValue();
		this.showPermissionTab = ((Boolean) values[21]).booleanValue();
		this.showUploadComponent = ((Boolean) values[22]).booleanValue();
		this.onFileClickEvent = ((String) values[23]);
		this.showPublicFolder = ((Boolean) values[24]).booleanValue();
		this.showDropboxFolder = ((Boolean) values[25]).booleanValue();
		maintainPath(true);
	}
	/**
	 * @return Returns the currentFileName.
	 */
	public String getCurrentFileName() {
		return this.currentFileName;
	}
	/**
	 * @param currentFileName The currentFileName to set.
	 */
	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}
	/**
	 * @return Returns the currentFolderPath.
	 */
	public String getCurrentFolderPath() {
		if(this.currentFolderPath == null){
			FacesContext context = getFacesContext();
			this.currentFolderPath = (String) WFUtil.createMethodBinding("#{WebDAVListBean.getWebDAVPath}", null).invoke(context,null);
			if(this.currentFolderPath == null || "".equals(this.currentFolderPath)){
				this.currentFolderPath = "/";
			}
		}
		return this.currentFolderPath;
	}
	/**
	 * @param currentFolderPath The currentFolderPath to set.
	 */
	public void setCurrentFolderPath(String currentFolderPath) {
		this.currentFolderPath = currentFolderPath;
	}

	@Override
	public void setCurrentResourcePath(String resource) {
		super.setCurrentResourcePath(resource);
		int index = resource.lastIndexOf("/");
		if (index > -1) {
			String path = resource.substring(0, index);
			setCurrentFolderPath(path);
			WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setWebDAVPath", path);

			if (!resource.endsWith("/")) {
				WFUtil.invoke(WebDAVList.WEB_DAV_LIST_BEAN_ID, "setClickedFilePath", resource);
				String file = resource.substring(index+1);
				setCurrentFileName(file);
			}
		}

	}

	/**
	 * @return Returns the current resource path.
	 */
	@Override
	public String getCurrentResourcePath() {
		if (super.currentResourcePath != null) {
			return super.currentResourcePath;
		}
		else {
			String path = getCurrentFolderPath();

			String fileName = getCurrentFileName();
			if(fileName != null){
				return path+(("/".equals(path.substring(path.length()-1)))?"":"/")+fileName;
			} else {
				return path;
			}
		}
	}

	public String getCurrentResourceName() {
		if (this.currentResourceName == null) {
			this.currentResourceName = "";
			int index = this.currentFolderPath.lastIndexOf("/");
			try {
				if (index >= 0 && !this.currentFolderPath.equals("/")) {

					this.currentResourceName = " ("+this.currentFolderPath.substring(index+1)+")";
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				this.currentResourceName = "";
			}
		}
		return this.currentResourceName;
	}

	public void setUseVersionControl(boolean useVersionControl) {
		this.useVersionControl = useVersionControl;
	}
	/**
	 * @return Returns the showPermissionTab.
	 */
	public boolean getShowPermissionTab() {
		return this.showPermissionTab;
	}
	/**
	 * @param showPermissionTab The showPermissionTab to set.
	 */
	public void setShowPermissionTab(boolean showPermissionTab) {
		this.showPermissionTab = showPermissionTab;
	}

	/**
	 * @return Returns the showUploadComponent.
	 */
	public boolean getShowUploadComponent() {
		return this.showUploadComponent;
	}
	/**
	 * @param showUploadComponent The showUploadComponent to set.
	 */
	public void setShowUploadComponent(boolean showUploadComponent) {
		this.showUploadComponent = showUploadComponent;
	}

	/**
	 * Set the onClick event, for a file click
	 * example .setOnFileClickEvent("event([NAME])"); or event([ID]); or just event()
	 * @param event
	 */
	public void setOnFileClickEvent(String event) {
		this.onFileClickEvent = event;
	}


	/**
	 * @return the currentAction
	 */
	public String getCurrentAction() {
		return this.currentAction;
	}


	/**
	 * @param currentAction the currentAction to set
	 */
	public void setCurrentAction(String currentAction) {
		this.currentAction = currentAction;
	}


	/**
	 * @return the iconTheme
	 */
	public String getIconTheme() {
		return this.iconTheme;
	}


	/**
	 * @return the onFileClickEvent
	 */
	public String getOnFileClickEvent() {
		return this.onFileClickEvent;
	}


	/**
	 * @return the rootFolder
	 */
	public String getRootPath() {
		return this.rootFolder;
	}


	/**
	 * @return the showDropboxFolder
	 */
	public boolean isShowDropboxFolder() {
		return this.showDropboxFolder;
	}


	/**
	 * @return the showFolders
	 */
	public boolean isShowFolders() {
		return this.showFolders;
	}


	/**
	 * @return the showPublicFolder
	 */
	public boolean isShowPublicFolder() {
		return this.showPublicFolder;
	}


	/**
	 * @return the startFolder
	 */
	public String getStartPath() {
		return this.startFolder;
	}


	/**
	 * @return the useUserHomeFolder
	 */
	public boolean isUseUserHomeFolder() {
		return this.useUserHomeFolder;
	}


	/**
	 * @return the useVersionControl
	 */
	public boolean isUseVersionControl() {
		return this.useVersionControl;
	}


	/**
	 * @param currentResourceName the currentResourceName to set
	 */
	public void setCurrentResourceName(String currentResourceName) {
		this.currentResourceName = currentResourceName;
	}


	/**
	 * @param maintainPath the maintainPath to set
	 */
	public void setMaintainPath(boolean maintainPath) {
		this.maintainPath = maintainPath;
	}


	/**
	 * @return the renderDetailsLink
	 */
	public boolean isRenderDetailsLink() {
		return this.renderDetailsLink;
	}


	/**
	 * @param renderDetailsLink the renderDetailsLink to set
	 */
	public void setRenderDetailsLink(boolean renderDetailsLink) {
		this.renderDetailsLink = renderDetailsLink;
	}


	/**
	 * @return the renderListLink
	 */
	public boolean isRenderListLink() {
		return this.renderListLink;
	}


	/**
	 * @param renderListLink the renderListLink to set
	 */
	public void setRenderListLink(boolean renderListLink) {
		this.renderListLink = renderListLink;
	}


	/**
	 * @return the renderNewFolderLink
	 */
	public boolean isRenderNewFolderLink() {
		return this.renderNewFolderLink;
	}


	/**
	 * @param renderNewFolderLink the renderNewFolderLink to set
	 */
	public void setRenderNewFolderLink(boolean renderNewFolderLink) {
		this.renderNewFolderLink = renderNewFolderLink;
	}


	/**
	 * @return the renderPermissionsLink
	 */
	public boolean isRenderPermissionsLink() {
		return this.renderPermissionsLink;
	}


	/**
	 * @param renderPermissionsLink the renderPermissionsLink to set
	 */
	public void setRenderPermissionsLink(boolean renderPermissionsLink) {
		this.renderPermissionsLink = renderPermissionsLink;
	}


	/**
	 * @return the renderPreviewLink
	 */
	public boolean isRenderPreviewLink() {
		return this.renderPreviewLink;
	}


	/**
	 * @param renderPreviewLink the renderPreviewLink to set
	 */
	public void setRenderPreviewLink(boolean renderPreviewLink) {
		this.renderPreviewLink = renderPreviewLink;
	}


	/**
	 * @return the renderWebDAVDeleter
	 */
	public boolean isRenderWebDAVDeleter() {
		return this.renderWebDAVDeleter;
	}


	/**
	 * @param renderWebDAVDeleter the renderWebDAVDeleter to set
	 */
	public void setRenderWebDAVDeleter(boolean renderWebDAVDeleter) {
		this.renderWebDAVDeleter = renderWebDAVDeleter;
	}


	/**
	 * @return the renderWebDAVNewFolder
	 */
	public boolean isRenderWebDAVNewFolder() {
		return this.renderWebDAVNewFolder;
	}


	/**
	 * @param renderWebDAVNewFolder the renderWebDAVNewFolder to set
	 */
	public void setRenderWebDAVNewFolder(boolean renderWebDAVNewFolder) {
		this.renderWebDAVNewFolder = renderWebDAVNewFolder;
	}

}
