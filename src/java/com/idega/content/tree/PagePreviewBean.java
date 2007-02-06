package com.idega.content.tree;

import java.rmi.RemoteException;

import com.idega.business.IBOServiceBean;
import com.idega.content.themes.helpers.ThemesConstants;
import com.idega.content.themes.helpers.ThemesHelper;
import com.idega.core.builder.business.BuilderService;
import com.idega.presentation.IWContext;

public class PagePreviewBean extends IBOServiceBean implements PagePreview{
	
	private static final long serialVersionUID = -4609798037266246269L;

	public String getPreviewUrl(String ID){
		if (ID == null) {
			return ThemesConstants.EMPTY;
		}
		int id = -1;
		try {
			id = Integer.valueOf(ID).intValue();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return ThemesConstants.EMPTY;
		}
		if (id < 1) {
			return ThemesConstants.EMPTY;
		}
		String uri = null;
		BuilderService builderService = ThemesHelper.getInstance().getThemesService().getBuilderService();
		if (builderService == null) {
			return ThemesConstants.EMPTY;
		}
		try {
			uri = builderService.getPageURI(ID);
		} catch (RemoteException e) {
			e.printStackTrace();
			return ThemesConstants.EMPTY;
		}
		IWContext iwc = IWContext.getInstance();
		if (iwc != null) {
			builderService.setCurrentPageId(iwc, ID);
		}
		return uri;
	}
}