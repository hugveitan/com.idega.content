package com.idega.content.themes.business;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.content.business.ContentConstants;
import com.idega.content.business.WebDAVUploadBean;
import com.idega.content.presentation.WebDAVUpload;
import com.idega.content.themes.helpers.bean.Theme;
import com.idega.content.themes.helpers.business.ThemesConstants;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.IWMainSlideStartedEvent;
import com.idega.servlet.filter.IWBundleResourceFilter;
import com.idega.slide.business.IWSlideService;
import com.idega.webface.WFUtil;

/**
 * Installs basic themes on Slide startup
 * @author valdas
 *
 */

@Scope("singleton")
@Service
public class ThemesInstaller implements ApplicationListener {

	private ThemesEngine themesEngine = null;
	
	public void onApplicationEvent(ApplicationEvent event) {
		if (!(event instanceof IWMainSlideStartedEvent)) {
			return;
		}

		IWMainSlideStartedEvent slideEvent = (IWMainSlideStartedEvent) event;
		IWMainApplication iwma = slideEvent.getIWMA();
		Thread themesInstaller = new Thread(new ThemesInstallerRunner(getThemesEngine(), iwma));
		themesInstaller.start();
	}
	
	private class ThemesInstallerRunner implements Runnable {
		private IWMainApplication iwma = null;
		private ThemesEngine themesEngine = null;
		
		private ThemesInstallerRunner(ThemesEngine themesEngine, IWMainApplication iwma) {
			this.iwma = iwma;
			this.themesEngine = themesEngine;
		}
		
		public void run() {
			if (1 == 1) {
				//	TODO:	Temporarily auto deploying is turned off!
				activateThemes();
				return;
			}
			
			IWMainApplicationSettings settings = iwma.getSettings();
			if (settings == null) {
				activateThemes();
				return;
			}
			
			String property = settings.getProperty(ContentConstants.BASIC_THEMES_ADDED_PROPERTY, Boolean.FALSE.toString());
			boolean themesAlreadyInstalled = property != null && !property.equalsIgnoreCase(Boolean.FALSE.toString());
			if (themesAlreadyInstalled) {
				activateThemes();
				return;
			}
			
			ThemesHelper helper = ThemesHelper.getInstance(true);
			helper.searchForThemes();
			Collection<Theme> allThemes = helper.getAllThemes();
			if (allThemes != null && !allThemes.isEmpty()) {
				//	There some themes already - not importing basic themes
				activateThemes();
				return;
			}
		
			WebDAVUploadBean uploadBean = null;
			try {
				uploadBean = (WebDAVUploadBean) WFUtil.getBeanInstance(WebDAVUpload.BEAN_ID);
			} catch(Exception e) {
				uploadBean = new WebDAVUploadBean();
			}

			IWSlideService slide = helper.getSlideService(iwma.getIWApplicationContext());
			if (slide == null) {
				return;
			}
			
			IWBundle bundle = iwma.getBundle(ContentConstants.IW_BUNDLE_IDENTIFIER);
			File themesPackage = IWBundleResourceFilter.copyResourceFromJarToWebapp(iwma, bundle.getVirtualPathWithFileNameString("themes/basic_themes.zip"));
			if (themesPackage == null) {
				return;
			}
			
			InputStream stream = null;
			try {
				stream = new BufferedInputStream(new FileInputStream(themesPackage));
			} catch(Exception e) {
				e.printStackTrace();
			}
			if (stream == null) {
				return;
			}
			
			uploadBean.setUploadFilePath(ThemesConstants.THEMES_PATH);
			try {
				boolean selfInstallingResult = uploadBean.uploadZipFile(true, "Basic_themes", stream, slide);
				settings.setProperty(ContentConstants.BASIC_THEMES_ADDED_PROPERTY, String.valueOf(selfInstallingResult));
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				helper.closeInputStream(stream);
			}
		}
		
		private void activateThemes() {
			if (themesEngine == null) {
				return;
			}
			themesEngine.getThemes();
		}
	}

	public ThemesEngine getThemesEngine() {
		return themesEngine;
	}

	@Autowired
	public void setThemesEngine(ThemesEngine themesEngine) {
		this.themesEngine = themesEngine;
	}

}
