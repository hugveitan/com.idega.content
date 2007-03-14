package com.idega.content.themes.presentation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.Setting;
import com.idega.content.themes.helpers.ThemesConstants;
import com.idega.content.themes.helpers.ThemesHelper;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Heading2;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;

/**
 * This class diplays the current value of an application property, localized if available.
 * You have to use the method setApplicationPropertyKey to make it display anything.
 * You can also use setDefaultValue to initilize a key that has not been set.
 * 
 * @author valdas
 *
 */
public class ApplicationPropertyViewer extends Block {
	
	private static final String LOGO = "logo";
	
	private static final String LIST_STYLE = "list-style-type: none; width: 100%";
	private static final String USELESS_STYLE_CLASS = "empty_useless_style_class";

	private String applicationPropertyKey = null;
	
	public ApplicationPropertyViewer() {}
	
	public void main(IWContext iwc) {
		if (applicationPropertyKey == null) {
			return;
		}
		String settingKey = applicationPropertyKey;
		String key = null;
		if (applicationPropertyKey.indexOf(ThemesConstants.THEMES_PROPERTY_START) == -1) {
			applicationPropertyKey = ThemesConstants.THEMES_PROPERTY_START + applicationPropertyKey;
		}
		
		String value = null;
		Locale locale = iwc.getCurrentLocale();
		if (locale != null) { // Getting value, based on Locale
			key = applicationPropertyKey + ThemesConstants.DOT + locale.getLanguage();
			value = iwc.getApplicationSettings().getProperty(key);
		}
		if (value == null) { // Didn't find localized value, getting default value
			key = applicationPropertyKey + ThemesConstants.THEMES_PROPERTY_END;
			value = iwc.getApplicationSettings().getProperty(key);
		}
		
		if (value == null) {
			return;
		}
		
		if (key.indexOf(ThemesConstants.THEMES_PROPERTY_START + LOGO + ThemesConstants.DOT) != -1) {
			String siteLogo = "site_logo";
			if (!ThemesConstants.EMPTY.equals(value)) {
				Image image = new Image(value, ContentUtil.getBundle().getLocalizedString(siteLogo));
				image.setID(siteLogo);
				addPropertyEditAction(iwc, image, key, settingKey);
				this.add(image);
			}
			return;
		}
		
		if (value.equals(ContentConstants.EMPTY)) {
			value = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		}
		
		if (key.indexOf(ThemesConstants.THEMES_PROPERTY_START + ThemesConstants.NAVIGATION + ThemesConstants.DOT) != -1) {
			ICPage page = ThemesHelper.getInstance().getThemesService().getICPage(iwc.getCurrentIBPageID());
			if (page != null) {
				if (page.getWebDavUri() != null) {
					return;
				}
			}
			String[] values = value.split(ThemesConstants.COMMA);
			Lists list = new Lists();
			list.setStyleAttribute(LIST_STYLE);
			ListItem item = null;
			for (int i = 0; i < values.length; i++) {
				item = new ListItem();
				item.addText(values[i].trim());
				list.add(item);
			}
			addPropertyEditAction(iwc, list, key, settingKey);
			this.add(list);
			return;
		}
		
		if (key.indexOf(ThemesConstants.THEMES_PROPERTY_START + ThemesConstants.TOOLBAR + ThemesConstants.DOT) != -1) {
			Link l = new Link();
			l.setText(value);
			addPropertyEditAction(iwc, l, key, settingKey);
			this.add(l);
			return;
		}
		
		if (key.indexOf(ThemesConstants.THEMES_PROPERTY_START + ThemesConstants.BREADCRUMB + ThemesConstants.DOT) != -1) {
			Link l = new Link();
			l.setText(value);
			addPropertyEditAction(iwc, l, key, settingKey);
			this.add(l);
			return;
		}
		
		if (key.indexOf(ThemesConstants.THEMES_PROPERTY_START + ThemesConstants.SITE_TITLE + ThemesConstants.DOT) != -1) {
			Heading1 h1 = new Heading1(value);
			addPropertyEditAction(iwc, h1, key, settingKey);
			this.add(h1);
			return;
		}
		
		if (key.indexOf(ThemesConstants.THEMES_PROPERTY_START + ThemesConstants.SITE_SLOGAN + ThemesConstants.DOT) != -1) {
			Heading2 h2 = new Heading2(value);
			addPropertyEditAction(iwc, h2, key, settingKey);
			this.add(h2);
			return;
		}
		
		Text text = new Text(value); // Simple text
		addPropertyEditAction(iwc, text, key, settingKey);
		text.setStyleClass(USELESS_STYLE_CLASS);
		this.add(text);
	}
	
	public void setApplicationPropertyKey(String key){
		this.applicationPropertyKey = key;
	}
	
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		this.applicationPropertyKey = (String) values[1];
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[2];
		values[0] = super.saveState(context);
		values[1] = this.applicationPropertyKey;
		return values;
	}
	
	@SuppressWarnings("unchecked")
	private void addPropertyEditAction(IWContext iwc, PresentationObject component, String key, String settingKey) {
		if (iwc == null || component == null || key == null) {
			return;
		}
		if (ContentUtil.hasContentEditorRoles(iwc)) {
			String property = ThemesHelper.getInstance().extractValueFromString(key, key.indexOf(ThemesConstants.DOT) + 1,
					key.lastIndexOf(ThemesConstants.DOT));
			
			Map<String, Setting> settings = ThemesHelper.getInstance().getThemeSettings();
			Setting s = settings.get(settingKey);
			String title = ContentConstants.EMPTY;
			if (s != null) {
				title = s.getLabel();
			}
			
			component.setID(property + ThemesConstants.ADD_FOR_PROPERTY_CHANGE);
			if (component.attributes == null) {
				component.attributes = new HashMap();
			}
			StringBuffer javaScript = new StringBuffer();
			javaScript.append("changeSiteInfo(this.id, '").append(ContentUtil.getBundle().getLocalizedString("saving"));
			javaScript.append("');");
			component.attributes.put("ondblclick", javaScript.toString());
			component.attributes.put("onmouseover", "addStyleProperty(this.id, 'background-color: #ffff99; cursor: pointer;');");
			component.attributes.put("onmouseout", "removeStyleProperty(this.id);");
			component.setToolTip(title + ": " + ContentUtil.getBundle().getLocalizedString("double_click_to_edit"));
		}
	}
	
	public String getBuilderName(IWUserContext iwuc) {
		String name = ContentUtil.getBundle().getComponentName(ApplicationPropertyViewer.class);
		if (name == null || ThemesConstants.EMPTY.equals(name)) {
			return "ApplicationPropertyViewer";
		}
		return name;
	}
}
