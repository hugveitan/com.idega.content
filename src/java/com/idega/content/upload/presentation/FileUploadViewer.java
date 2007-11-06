package com.idega.content.upload.presentation;

import javax.faces.context.FacesContext;

import com.idega.block.web2.business.Web2Business;
import com.idega.business.SpringBeanLookup;
import com.idega.content.business.ContentConstants;
import com.idega.content.themes.helpers.ThemesHelper;
import com.idega.content.upload.business.FileUploader;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObjectContainer;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.util.CoreConstants;

public class FileUploadViewer extends Block {
	
	private String actionAfterUpload = null;
	private String uploadPath = CoreConstants.CONTENT_PATH;
	private String formId = null;
	
	private boolean zipFile = false;
	private boolean extractContent = false;
	private boolean themePack = false;
	private boolean showProgressBar = true;
	private boolean showLoadingMessage = true;
	private boolean allowMultipleFiles = false;
	
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		
		this.actionAfterUpload = values[1].toString();
		this.uploadPath = values[2].toString();
		
		this.zipFile = (Boolean) values[3];
		this.extractContent = (Boolean) values[4];
		this.themePack = (Boolean) values[5];
		this.showProgressBar = (Boolean) values[6];
		this.showLoadingMessage = (Boolean) values[7];
		this.allowMultipleFiles = (Boolean) values[8];
	}
	
	public Object saveState(FacesContext context) {
		Object values[] = new Object[9];
		values[0] = super.saveState(context);
		
		values[1] = this.actionAfterUpload;
		values[2] = this.uploadPath;
		
		values[3] = this.zipFile;
		values[4] = this.extractContent;
		values[5] = this.themePack;
		values[6] = this.showProgressBar;
		values[7] = this.showLoadingMessage;
		values[8] = this.allowMultipleFiles;
		
		return values;
	}
	
	public void main(IWContext iwc) {
		FileUploader uploader = null;
		try {
			uploader = (FileUploader) SpringBeanLookup.getInstance().getSpringBean(iwc, FileUploader.class);
			uploader.initializeUploader(iwc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if (themePack) {
			ThemesHelper.getInstance(false).getSlideService(iwc);
			zipFile = true;
			extractContent = true;
		}
		
		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		Layer container = new Layer();
		add(container);

		StringBuffer script = new StringBuffer(getJavaScriptSourceLine(bundle.getVirtualPathWithFileNameString("javascript/FileUploadHelper.js")));
		Web2Business web2 = (Web2Business) SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);
		script.append(getJavaScriptSourceLine(web2.getBundleURIToYUIScript()));
		script.append(getJavaScriptSourceLine("/dwr/engine.js"));
		script.append(getJavaScriptSourceLine("/dwr/interface/FileUploader.js"));
		container.add(script.toString());
		
		PresentationObjectContainer mainContainer = container;
		if (formId == null) {
			Form form = new Form();
			form.setMultiPart();
			form.setAction("/servlet/ContentFileUploadServlet");
			form.setMethod("post");
			container.add(form);
			mainContainer = form;
			formId = form.getId();
		}
		
		HiddenInput path = new HiddenInput(ContentConstants.UPLOADER_PATH, uploadPath);
		mainContainer.add(path);
		HiddenInput zipFileValue = new HiddenInput(ContentConstants.UPLOADER_UPLOAD_ZIP_FILE, String.valueOf(zipFile));
		mainContainer.add(zipFileValue);
		HiddenInput themePackValue = new HiddenInput(ContentConstants.UPLOADER_UPLOAD_THEME_PACK, String.valueOf(themePack));
		mainContainer.add(themePackValue);
		HiddenInput extractContentValue = new HiddenInput(ContentConstants.UPLOADER_UPLOAD_EXTRACT_ARCHIVED_FILE, String.valueOf(extractContent));
		mainContainer.add(extractContentValue);
		
		Layer fileInputs = new Layer();
		String id = fileInputs.getId();
		fileInputs.setStyleClass("fileUploadInputsContainerStyle");
		fileInputs.add(uploader.getFileInput(iwc, allowMultipleFiles));
		mainContainer.add(fileInputs);
		
		Layer buttonsContainer = new Layer();
		if (allowMultipleFiles) {
			GenericButton addFileInput = new GenericButton(iwrb.getLocalizedString("add_file", "Add file"));
			StringBuffer action = new StringBuffer("addFileInputForUpload('").append(id).append("', '").append(iwrb.getLocalizedString("loading", "Loading..."));
			action.append("');");
			addFileInput.setOnClick(action.toString());
			buttonsContainer.add(addFileInput);
		}
		
		String inavlidTypeMessage = iwrb.getLocalizedString("incorrect_file_type", "Unsupported file type! Only zip files allowed");
		GenericButton upload = new GenericButton(iwrb.getLocalizedString("upload", "Upload"));
		upload.setOnClick(getAction(id, iwrb.getLocalizedString("uploading", "Uploading..."), inavlidTypeMessage));
		buttonsContainer.add(upload);
		
		mainContainer.add(buttonsContainer);
	}
	
	private String getAction(String id, String loadingMessage, String invalidTypeMessage) {
		StringBuffer action = new StringBuffer("uploadFiles('").append(id).append("', '").append(loadingMessage).append("', ").append(showProgressBar).append(", ");
		action.append(showLoadingMessage).append(", ").append(zipFile).append(", '").append(invalidTypeMessage).append("', '").append(formId).append("', ");
		
		if (actionAfterUpload == null) {
			action.append("null");
		}
		else {
			if (actionAfterUpload.indexOf("\"") != -1) {
				actionAfterUpload = actionAfterUpload.replaceAll("\"", "'");
			}
			if (actionAfterUpload.indexOf("'") != -1) {
				String[] actionParts = actionAfterUpload.split("'");
				StringBuffer changedAction = new StringBuffer();
				for (int i = 0; i < actionParts.length; i++) {
					changedAction.append(actionParts[i]);
					if ((i + 1) < actionParts.length) {
						changedAction.append("\\").append("'");
					}
				}
				actionAfterUpload = changedAction.toString();
			}
			action.append("'").append(actionAfterUpload).append("'");
		}
		action.append(");");
		
		return action.toString();
	}
	
	private String getJavaScriptSourceLine(String source) {
		return new StringBuffer("<script type=\"text/javascript\" src=\"").append(source).append("\"></script>\n").toString();
	}
	
	public String getBundleIdentifier() {
		return ContentConstants.IW_BUNDLE_IDENTIFIER;
	}

	public String getActionAfterUpload() {
		return actionAfterUpload;
	}

	public void setActionAfterUpload(String actionAfterUpload) {
		this.actionAfterUpload = actionAfterUpload;
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public boolean isZipFile() {
		return zipFile;
	}

	public void setZipFile(boolean zipFile) {
		this.zipFile = zipFile;
	}

	public boolean isExtractContent() {
		return extractContent;
	}

	public void setExtractContent(boolean extractContent) {
		this.extractContent = extractContent;
	}

	public boolean isThemePack() {
		return themePack;
	}

	public void setThemePack(boolean themePack) {
		this.themePack = themePack;
	}

	public boolean isShowProgressBar() {
		return showProgressBar;
	}

	public void setShowProgressBar(boolean showProgressBar) {
		this.showProgressBar = showProgressBar;
	}

	public boolean isShowLoadingMessage() {
		return showLoadingMessage;
	}

	public void setShowLoadingMessage(boolean showLoadingMessage) {
		this.showLoadingMessage = showLoadingMessage;
	}

	public boolean isAllowMultipleFiles() {
		return allowMultipleFiles;
	}

	public void setAllowMultipleFiles(boolean allowMultipleFiles) {
		this.allowMultipleFiles = allowMultipleFiles;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

}
