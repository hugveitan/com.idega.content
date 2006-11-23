var globalPageID = 1;

var scrollerImageWidth = 23;
var SPACE_FROM_LEFT = 290;

var KEYWORDS = null;

function getScrollerImageWidth() {
	return scrollerImageWidth;
}

function savePageInfo() {
	showLoadingMessage("Saving...");
	if (KEYWORDS == null) {
		ThemesEngine.getPageInfoElements(getPageInfoElementsCallback);
	}
	else {
		getPageInfoElementsCallback(KEYWORDS);
	}
}

function getPageInfoElementsCallback(allKeywords) {
	if (globalPageID == null || allKeywords == null) {
		closeLoadingMessage();
		return;
	}
	var keywords = new Array();
	var values = new Array();
	var element = null;
	for (var i = 0; i < allKeywords.length; i++) {
		element = document.getElementById(allKeywords[i]);
		if (element != null) {
			if (element.value != "") {
				keywords.push(allKeywords[i]);
				values.push(element.value);
				element.value = "";
			}
		}
	}
	ThemesEngine.savePageInfo(globalPageID, keywords, values, savePageInfoCallback);
}

function savePageInfoCallback(result) {
	closeLoadingMessage();
}

function showSlider(container) {
	resizeSlider();
	container.style.position = "absolute";
	container.style.bottom = "24px";
	container.style.left = SPACE_FROM_LEFT + "px";
	container.className = "theme_slider";
	new Effect.Appear(container);
	getThemes(null, true);
}

function manageSlider(buttonID) {
	var container = document.getElementById("themesSliderContainer");
	if (container == null) {
		return;
	}
	var button = document.getElementById(buttonID);
	if (button == null) {
		return;
	}
	if (container.style.display == "none") {
		button.value = "Hide Themes";
		showSlider(container);
	}
	else {
		removeStyleOptions();
		new Effect.Fade(container);
		button.value = "Show Themes";
	}
}

function setPageID(ID) {
	globalPageID = ID;
}

function chooseStyle(themeID) {
	if (themeID == null) {
		return;
	}
	var theme = getTheme(themeID);
	if (theme != null) {
		theme.applyStyle = true;
		setTimeout("sleepAndApply('"+themeID+"')", waitForStyle);
	}
}

function recallStyle(themeID) {
	if (themeID == null) {
		return;
	}
	var theme = getTheme(themeID);
	if (theme != null) {
		theme.applyStyle = false;
	}
}

function sleepAndApply(themeID) {
	if (themeID == null) {
		return;
	}
	var theme = getTheme(themeID);
	if (theme != null) {
		if (theme.applyStyle) {
			theme.applyStyle = false;
			chooseOption(themeID);
		}
	}
}

function chooseOption(themeID) {
	var leftPosition = (getAbsoluteLeft(themeID + "_container") + 3);
	if (getTotalWidth() - (leftPosition + getImageWidth())< 0) {
		return; // There is not enough space
	}
	
	var div = document.getElementById("chooseStyleLayer");
	if (div == null) {
		div = document.createElement("div");
		div.style.display = "none";
		div.setAttribute("id", "chooseStyleLayer");
		div.className = "themeChooseStyle";
		
		var divp = document.createElement("div");
		divp.className = "themeChooseStyleText";
		divp.style.opacity = "0.5";
		var pageSpan = document.createElement("span");
		pageSpan.setAttribute("id", "pageStyle");
		pageSpan.appendChild(document.createTextNode("Page"));
		divp.appendChild(pageSpan);
	
		var divs = document.createElement("div");
		divs.className = "themeChooseStyleText";
		divs.style.opacity = "0.3";
		var siteSpan = document.createElement("span");
		siteSpan.setAttribute("id", "siteStyle");
		siteSpan.appendChild(document.createTextNode("Site"));
		divs.appendChild(siteSpan);
		
		if (typeof div.attachEvent != 'undefined') {
			pageSpan.attachEvent('onclick', function(e){setStyle(themeID, true);});
	    	siteSpan.attachEvent('onclick', function(e){setStyle(themeID, false);});
	    	div.attachEvent('onclick', function(e){removeStyleOptions();});
	    } else {
	    	pageSpan.addEventListener('click', function(e){setStyle(themeID, true);}, true);
	    	siteSpan.addEventListener('click', function(e){setStyle(themeID, false);}, true);
	    	div.addEventListener('click', function(e){removeStyleOptions();}, true);
	   	}
		div.appendChild(divp);
		div.appendChild(divs);
		document.body.appendChild(div);
	}
	div.style.left = leftPosition + "px";
	div.style.top = (getAbsoluteTop(themeID + "_container") + 3) + "px";
	//new Effect.Appear(div);
	div.style.display = "block";
}

function getAbsoluteLeft(objectId) {
	o = document.getElementById(objectId);
	if (o == null) {
		return 0;
	}
	oLeft = o.offsetLeft;
	while(o.offsetParent != null) {
		oParent = o.offsetParent;
		oLeft += oParent.offsetLeft;
		o = oParent;
	}
	return oLeft;
}

function getAbsoluteTop(objectId) {
	o = document.getElementById(objectId);
	if (o == null) {
		return 0;
	}
	oTop = o.offsetTop;
	while(o.offsetParent != null) {
		oParent = o.offsetParent;
		oTop += oParent.offsetTop;
		o = oParent;
	}
	return oTop;
}

function setStyle(themeID, page) {
	removeStyleOptions();
	if (themeID == null) {
		return;
	}
	showLoadingMessage("Applying style...");
	ThemesEngine.setSelectedStyle(themeID, page, setStyleCallback);
}

function setStyleCallback(result) {
	closeLoadingMessage();
}

function resizeSlider() {
	var rightScroller = document.getElementById("rightScrollerContainer");
	var themesTicker = document.getElementById("themesTickerContainer");
	var container = document.getElementById("themesSliderContainer");
	if (rightScroller == null || themesTicker == null || container == null) {
		return;
	}
	var available = getTotalWidth() - 500;
	if (available > 0) {
		container.style.width = available + "px";
		themesTicker.style.left = (getScrollerImageWidth() - 3) + "px";
		rightScroller.style.left = (available - 23) + "px";
		themesTicker.style.width = (available - 44) + "px";
	}
}

function getTotalWidth() {
  var width = 0;
  if(typeof(window.innerWidth) == "number") {
    width = window.innerWidth; // Non-IE
  } else if(document.documentElement && document.documentElement.clientWidth) {
    width = document.documentElement.clientWidth; // IE 6+ in 'standards compliant mode'
  } else if(document.body && document.body.clientWidth) {
    width = document.body.clientWidth; // IE 4 compatible
  }
  return width;
}

function getTotalHeight() {
  var height = 0;
  if(typeof(window.innerHeight) == "number") {
    height = window.innerHeight; // Non-IE
  } else if(document.documentElement && document.documentElement.clientHeight) {
    height = document.documentElement.clientHeight; // IE 6+ in 'standards compliant mode'
  } else if(document.body && document.body.clientHeight) {
    height = document.body.clientHeight; // IE 4 compatible
  }
  return height;
}

function newPage() {
}

function resizeFrame() {
	var frame = document.getElementById("treePages");
	if (frame == null) {
		return;
	}
	frame.style.left = SPACE_FROM_LEFT + "px";
	var availableWidth = getTotalWidth() - 500;
	if (availableWidth > 0) {
		frame.style.width = availableWidth + "px";
	}
	var availableHeight = getTotalHeight() - 331;
	if (availableHeight > 0) {
		frame.style.height = availableHeight + "px";
	}
}