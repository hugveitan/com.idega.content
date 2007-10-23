var LANGUAGE = null;

var KEYWORDS = null;

function setActiveLanguage() {
	setLanguageForSiteInfo(document.getElementById('siteInfoLocale'));
}

function setLanguageForSiteInfo(select) {
	if (select == null) {
		return;
	}
	if (select.options == null) {
		return;
	}
	var selected = select.options[select.selectedIndex];
	if (selected == null) {
		return;
	}
	if (selected.value == null) {
		return;
	}
	
	LANGUAGE = selected.value;
}

function getValues(object) {
	setLanguageForSiteInfo(object);
	if (KEYWORDS == null) {
		ThemesEngine.getSiteInfoElements(getSiteInfoElementsCallback);
	}
	else {
		getSiteInfoElementsCallback(KEYWORDS);
	}
}

function getSiteInfoElementsCallback(keywords) {
	KEYWORDS = keywords;
	if (LANGUAGE != null && KEYWORDS != null) {
		ThemesEngine.getSiteInfoValues(KEYWORDS, LANGUAGE, getSiteInfoValuesCallback);
	}
}

function getSiteInfoValuesCallback(values) {
	if (KEYWORDS == null || values == null) {
		return;
	}
	if (KEYWORDS.length != values.length) {
		return;
	}
	
	var element = null;
	for (var i = 0; i < KEYWORDS.length; i++) {
		element = document.getElementById(KEYWORDS[i]);
		if (element != null) {
			element.value = values[i];
		}
	}
}

function saveSiteInfo() {
	showLoadingMessage(getThemeSavingText());
	if (KEYWORDS == null) {
		ThemesEngine.getSiteInfoElements(proceedSaving);
	}
	else {
		proceedSaving(KEYWORDS);
	}
}

function getElementByClassName(elementName, style) {
	if (elementName == null || style == null) {
		return null;
	}
	var elements = document.getElementsByTagName(elementName);
	if (elements == null) {
		return null;
	}
	var element = null;
	var found = false;
	for (var i = 0; (i < elements.length && !found); i++) {
		element = elements[i];
		if (element.className != null) {
			if (element.className == style) {
				found = true;
			}
		}
	}
	return element;
}

function proceedSaving(keywords) {
	if (keywords == null) {
		closeLoadingMessage();
		return;
	}
	if (LANGUAGE == null) {
		setActiveLanguage();
		if (LANGUAGE == null) {
			closeLoadingMessage();
			return;
		}
	}
	
	KEYWORDS = keywords;
	var values = new Array();
	var element = null;
	for (var i = 0; i < KEYWORDS.length; i++) {
		element = document.getElementById(KEYWORDS[i]);
		if (element != null) {
			values.push(element.value);
			if (KEYWORDS[i] == 'mainDomainName') {
				var siteName = getElementByClassName('div', 'ws_appinfo');
				if (siteName != null) {
					var newName = document.createTextNode(element.value);
					if (siteName.firstChild == null) {
						siteName.appendChild(newName);
					}
					else {
						siteName.firstChild.data = element.value;
					}
				}
			}
		}
	}
	ThemesEngine.saveSiteInfo(LANGUAGE, KEYWORDS, values, saveSiteInfoCallback);
}

function saveSiteInfoCallback(result) {
	closeLoadingMessage();
}

function initialiazeSiteManager() {
	setIsSiteMap(true);
	setNeedRedirect(false);
	setActiveLanguage();
	
	var containerId = 'div_id_current_structure_tree';
	var siteTreeContainer = $('div_id_current_structure_tree');
	if (siteTreeContainer) {
		var height = getTotalHeight() - 145;
		if (height > 0) {
			siteTreeContainer.setStyle('height', height + 'px');
		}
	}
	
	checkIfNotEmptySiteTree(containerId);

	var width = getTotalWidth() - 462;
	if (width > 0) {
		var pagesTemplatesContainer = $('pagesTypesContainer');
		if (pagesTemplatesContainer) {
			pagesTemplatesContainer.setStyle('width', width + 'px');
		}
		
		var siteTemplatesContainer = $('siteTemplatesContainer');
		if (siteTemplatesContainer) {
			siteTemplatesContainer.setStyle('width', width + 'px');
		}
	}

}

function registerSiteActions() {
    registerActionsForSiteTree();
    boldCurrentTreeElement();
}