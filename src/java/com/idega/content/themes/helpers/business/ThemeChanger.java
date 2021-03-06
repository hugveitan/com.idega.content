package com.idega.content.themes.helpers.business;

import java.util.List;
import java.util.Map;

import com.idega.content.themes.helpers.bean.Theme;
import com.idega.content.themes.helpers.bean.ThemeChange;
import com.idega.content.themes.helpers.bean.ThemeStyleGroupMember;

public interface ThemeChanger {
	
	public static final String SPRING_BEAN_IDENTIFIER = "themeChanger";
	
	public boolean restoreTheme(String themeKey) throws Exception;

	public boolean saveTheme(String themeKey, String themeName) throws Exception;
	
	public boolean setSelectedStyles(Theme theme, List<ThemeChange> enabledStyles) throws Exception;
	
	public String applyMultipleChangesToTheme(String themeKey, List<ThemeChange> changes, String themeName) throws Exception;
	
	public String changeTheme(String themeKey, String themeName, ThemeChange change, boolean lastChange) throws Exception;
	
	public List <ThemeStyleGroupMember> getEnabledStyles(Theme theme) throws Exception;
	
	public ThemeStyleGroupMember getMember(Map <String, ThemeStyleGroupMember> styleMembers, String styleGroupName, int index) throws Exception;
	
	public ThemeStyleGroupMember getStyleMember(Theme theme, String styleGroupName, String styleVariation) throws Exception;
	
	public boolean prepareThemeForUsage(Theme theme) throws Exception;
	
	public boolean prepareThemeStyleFiles(Theme theme) throws Exception;
	
	public boolean reloadThemeProperties(String themeKey, boolean checkConfig) throws Exception;
	
	public boolean setBuiltInStyle(String themeId, String builtInStyleId) throws Exception;
	
	public ThemeStyleGroupMember getColorGroupMember(Theme theme, String variable) throws Exception;
}
