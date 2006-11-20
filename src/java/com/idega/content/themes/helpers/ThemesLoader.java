package com.idega.content.themes.helpers;

import java.util.List;
import java.util.Random;

public class ThemesLoader {
	
	private Theme theme = null;
	private ThemesHelper helper = null;
	private Random idGenerator = null;
	
	public ThemesLoader(ThemesHelper helper) {
		this.helper = helper;
		idGenerator = new Random();
	}
	
	public synchronized boolean loadTheme(String originalUri, String encodedUri, boolean newTheme, boolean manuallyCreated) {
		if (encodedUri == null || originalUri == null) {
			return false;
		}

		encodedUri = getUriWithoutContent(encodedUri);
		originalUri = getUriWithoutContent(originalUri);
		
		if (createNewTheme(originalUri, encodedUri, newTheme, manuallyCreated) == null) {
			return false;
		}
		
		return true;
	}
	
	public synchronized boolean loadThemes(List <String> urisToThemes, boolean newThemes, boolean manuallyCreated) {
		if (urisToThemes == null) {
			return false;
		}
		
		boolean result = true;
		for (int i = 0; (i < urisToThemes.size() && result); i++) {
			result = loadTheme(helper.decodeUrl(urisToThemes.get(i)), urisToThemes.get(i), newThemes, manuallyCreated);
		}
			
		return result;
	}
	
	private void initTheme(boolean newTheme) {
		if (theme == null) {
			theme = new Theme(getThemeId());
			theme.setNewTheme(newTheme);
		}
	}
	
	private void addThemeInfo() {
		if (theme == null) {
			return;
		}
		if (!helper.getThemesCollection().contains(theme)) {
			helper.addTheme(theme);
		}
		theme = null;
	}
	
	private String getUriWithoutContent(String uri) {
		int index = uri.indexOf(ThemesConstants.CONTENT);
		if (index == -1) {
			return uri;
		}
		index += ThemesConstants.CONTENT.length();
		return helper.extractValueFromString(uri, index, uri.length());
	}
	
	public synchronized String createNewTheme(String originalUri, String encodedUri, boolean newTheme, boolean manuallyCreated) {		
		helper.addUriToTheme(originalUri);
		
		initTheme(newTheme);
		
		String themeID = theme.getId();
		
		theme.setLinkToSkeleton(encodedUri);
		theme.setLinkToBase(helper.getLinkToBase(encodedUri));
		theme.setLinkToBaseAsItIs(helper.getLinkToBase(originalUri));
		
		if (manuallyCreated) {
			theme.setLoading(false);
		}
		
		addThemeInfo();
		
		return themeID;
	}
	
	private String getThemeId() {
		String id = String.valueOf(idGenerator.nextInt(Integer.MAX_VALUE));
		while (helper.getTheme(id) != null) { // Checking if exists Theme with the same ID
			id = String.valueOf(idGenerator.nextInt(Integer.MAX_VALUE));
		}
		return id;
	}

}
