/**
 * $Id: ContentCategory.java,v 1.4 2007/02/20 10:57:51 gediminas Exp $
 * Created in 2007 by gediminas
 *
 * Copyright (C) 2000-2007 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;


/**
 * Describes a category
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 */
public class ContentCategory {
	
	private String id;
	private boolean disabled = false;
	private Map<String, String> names;
	
	private ContentCategory() {
	}

	public ContentCategory(String id) {
		this.id = id;
		this.names = new HashMap();
	}
	
	/**
	 * @param cat
	 */
	public ContentCategory(Element cat) {
		setFromXML(cat);
	}

	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public Map<String, String> getNames() {
		return this.names;
	}
	
	public void setNames(Map names) {
		this.names = names;
	}
	
	public String getName(String lang) {
		String name = getNames().get(lang);
		if (name == null) {
			// try without locale variant is@Reykjavik => is
			int i = lang.lastIndexOf("@");
			if (i < 0) {
				// try without country, e.g. is_IS => is
				i = lang.lastIndexOf("_");
			}
			if (i > 1) {
				name = getName(lang.substring(0, i - 1));
			}
		}
		return name;
	}
	
	public void addName(String lang, String name) {
		getNames().put(lang, name);
	}

	public void setFromXML(Element cat) {
		this.id = cat.getAttributeValue("id");
		Attribute disabledAttr = cat.getAttribute("disabled");
		if (disabledAttr != null) {
			this.disabled = Boolean.getBoolean(disabledAttr.getValue());
		}
		else {
			this.disabled = false;
		}
		this.names = new HashMap();
		List namesEl = cat.getChildren("name");
		for (Iterator iter = namesEl.iterator(); iter.hasNext(); ) {
			Element name = (Element) iter.next();
			String lang = name.getAttributeValue("lang", Namespace.XML_NAMESPACE);
			String localizedName = name.getText();
			names.put(lang, localizedName);
		}

	}
	
	public Element getAsXML() {
		Element cat = new Element("category");
		cat.setAttribute("id", getId());
		if (isDisabled()) {
			cat.setAttribute("disabled", String.valueOf(isDisabled()));
		}
		for (Iterator iter = getNames().keySet().iterator(); iter.hasNext(); ) {
			String lang = (String) iter.next();
			String name = getNames().get(lang);
			Element locName = new Element("name");
			locName.setAttribute("lang", lang, Namespace.XML_NAMESPACE);
			locName.setText(name);
			cat.addContent(locName);
		}
		return cat;
	}
	
}
