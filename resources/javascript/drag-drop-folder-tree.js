	var JSTreeObj;
	var treeUlCounter = 0;
	var nodeId = 1;
	var thisTree;
	var divClass = '';
	var globalDivId = null;
	var saveOnDrop = false;
	var movingNode = false;
	var iconFolder = '';
	var imageFolder = '';	
	var idsOfTrees = new Array();
	var idsOfAdvancedTrees = new Array();
	var treeObj = null;
	if(changePageName == null) var changePageName = false;
		
	/* Constructor */
	function JSDragDropTree() {
		var thisTree = false;
		var idOfTree;
		
		var sourceTree;
		var actionOnMouseUp;
		
		var imageFolder;
		var folderImage;
		var plusImage;
		var minusImage;
		var maximumDepth;
		var dragNode_source;
		var dragNode_parent;
		var dragNode_sourceNextSib;
		var dragNode_noSiblings; 
		var deleteNodes;
		
		var previousSiteTreeNode = null;
		var nextSiteTreeNode = null;
		
		var dragNode_destination;
		var floatingContainer;
		var dragDropTimer;
		var dropTargetIndicator;
		var insertAsSub;
		var indicator_offsetX;
		var indicator_offsetX_sub;
		var indicator_offsetY;
		var newPageId;
		var treeStructure;
		var parentId;
		var firstTopPage;
		var previousParentId;
		var previousPlaceInLevel;
				
		this.firstTopPage = false;
		this.parentid = -1;
		this.newPageId = -1;
		this.deleteNodes = false;
		this.sourceTreee = false;
		this.actionOnMouseUp = 'empty';
		
		this.imageFolder = '';
		this.iconFolder = '';
		this.folderImage = 'default.png';
		this.plusImage = 'nav-plus.gif';
		this.minusImage = 'nav-minus.gif';
		this.maximumDepth = 6;
		var messageMaximumDepthReached;
		
		var tempFloatCont = $('floatingContainer');
		if (tempFloatCont == null) {
			tempFloatCont = new Element('ul');
			tempFloatCont.setStyle('position', 'absolute');
			tempFloatCont.setStyle('display', 'none');
			tempFloatCont.setProperty('id', 'floatingContainer');
			tempFloatCont.injectInside($(document.body));
		}
		this.floatingContainer = tempFloatCont
		
		this.insertAsSub = false;
		this.dragDropTimer = -1;
		this.dragNode_noSiblings = false;
		
		if (document.all) {
			this.indicator_offsetX = 2;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 4;
			this.indicator_offsetY = 2;
		} else {
			this.indicator_offsetX = 1;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 3;
			this.indicator_offsetY = 2;			
		}
		if (navigator.userAgent.indexOf('Opera') >= 0) {
			this.indicator_offsetX = 2;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 3;
			this.indicator_offsetY = -7;				
		}

		this.messageMaximumDepthReached = ''; // Use '' if you don't want to display a message 
	}
	
	/* JSDragDropTree class */
	JSDragDropTree.prototype = {
		Get_Cookie : function(name) { 
		   var start = document.cookie.indexOf(name+'='); 
		   var len = start+name.length+1; 
		   if ((!start) && (name != document.cookie.substring(0,name.length))) return null; 
		   if (start == -1) return null; 
		   var end = document.cookie.indexOf(';',len); 
		   if (end == -1) end = document.cookie.length; 
		   return unescape(document.cookie.substring(len,end)); 
		},
		
		// This function has been slightly modified
		Set_Cookie : function(name,value,expires,path,domain,secure) { 			
			expires = expires * 60*60*24*1000;
			var today = new Date();
			var expires_date = new Date( today.getTime() + (expires) );
		    var cookieString = name + '=' +escape(value) + 
		       ( (expires) ? ';expires=' + expires_date.toGMTString() : '') + 
		       ( (path) ? ';path=' + path : '') + 
		       ( (domain) ? ';domain=' + domain : '') + 
		       ( (secure) ? ';secure' : ''); 
		    document.cookie = cookieString; 
		},
		setMaximumDepth : function(maxDepth) {
			this.maximumDepth = maxDepth;	
		},
		setMessageMaximumDepthReached : function(newMessage) {
			this.messageMaximumDepthReached = newMessage;
		},
		setImageFolder : function(path) {
			this.imageFolder = path;	
		},
		setFolderImage : function(imagePath) {
			this.folderImage = imagePath;			
		},
		setPlusImage : function(imagePath) {
			this.plusImage = imagePath;				
		},
		setMinusImage : function(imagePath) {
			this.minusImage = imagePath;			
		},
		setTreeId : function(idOfTree) {
			this.idOfTree = idOfTree;		
		},
		expandAll : function() {
			var siteTree = document.getElementById(this.idOfTree);
			if (!siteTree) {
				return false;
			}
			
			var menuItems = siteTree.getElementsByTagName('LI');
			if (!menuItems) {
				return false;
			}
			for (var no = 0; no < menuItems.length; no++) {
				var subItems = menuItems[no].getElementsByTagName('UL');
				if (subItems) {
					if (subItems.length > 0 && subItems[0].style.display != 'block') {				
						JSTreeObj.showHideNode(false, menuItems[no].id);
					}
				}
			}
		},
		collapseAll : function() {
			var menuItems = document.getElementById(this.idOfTree).getElementsByTagName('LI');
			for(var no=0;no<menuItems.length;no++){
				var subItems = menuItems[no].getElementsByTagName('UL');
				if(subItems.length>0 && subItems[0].style.display=='block'){
					JSTreeObj.showHideNode(false,menuItems[no].id);
				}			
			}		
		},
		// Find top pos of a tree node
		getTopPos : function(obj) {
			var top = obj.offsetTop/1;
			while((obj = obj.offsetParent) != null){
				if(obj.tagName!='HTML')top += obj.offsetTop;
			}			
			if(document.all)
				top = top/1 + 13;
			else top = top/1 + 4;		
			return top;
		},	
		// Find left pos of a tree node
		getLeftPos : function(obj) {
			var left = obj.offsetLeft/1 + 1;
			while((obj = obj.offsetParent) != null){
				if(obj.tagName!='HTML')left += obj.offsetLeft;
			}
	  			
			if(document.all)
				left = left/1 - 2;
			return left;
		},
		showHideNode : function(e,inputId) {			
			if (inputId) {
				if (!document.getElementById(inputId)) {
					return false;
				}
				thisNode = document.getElementById(inputId).getElementsByTagName('IMG')[0]; 
			}
			else {
				thisNode = this;
				if (this != null && this.tagName=='A') {
					thisNode = this.parentNode.getElementsByTagName('IMG')[0];
				}
			}
			
			if (thisNode == null) {
				return false;
			}
			
			if (thisNode.style.visibility == 'hidden') {
				return false;
			}
			var parentNode = thisNode.parentNode;
			inputId = parentNode.id.replace(/[^0-9]/g,'');
			if(thisNode.src.indexOf(JSTreeObj.plusImage)>=0){
				thisNode.src = thisNode.src.replace(JSTreeObj.plusImage,JSTreeObj.minusImage);
				var ul = parentNode.getElementsByTagName('UL')[0];
				ul.style.display='block';
				if(!initExpandedNodes)initExpandedNodes = ',';
				if(initExpandedNodes.indexOf(',' + inputId + ',')<0) initExpandedNodes = initExpandedNodes + inputId + ',';
			}else{					
				thisNode.src = thisNode.src.replace(JSTreeObj.minusImage,JSTreeObj.plusImage);
				parentNode.getElementsByTagName('UL')[0].style.display='none';
				initExpandedNodes = initExpandedNodes.replace(',' + inputId,'');
			}	

			JSTreeObj.Set_Cookie('dhtmlgoodies_expandedNodes',initExpandedNodes,500);			
			return false;						
		},
		copyDragableNode : function(e) {
			if (saveOnDrop == true) {
				movingNode = true;
			}
			else {
				movingNode = false;
			}
			var sourceTree = false;			
			var liTag = document.getElementsByTagName('LI')[0];
			
			var subs = JSTreeObj.floatingContainer.getElementsByTagName('LI');

			if(subs.length>0){
				if(JSTreeObj.dragNode_sourceNextSib){
					JSTreeObj.dragNode_parent.insertBefore(JSTreeObj.dragNode_source,JSTreeObj.dragNode_sourceNextSib);
				}
				else{
					JSTreeObj.dragNode_parent.appendChild(JSTreeObj.dragNode_source);
				}
			}
			
			JSTreeObj.dragNode_source = this.parentNode;
			JSTreeObj.dragNode_parent = this.parentNode.parentNode;

			var parentDiv = JSTreeObj.dragNode_parent;
			while(true){
				if (parentDiv.getElementsByTagName('DIV')[0]){
					globalDivId = parentDiv.getElementsByTagName('DIV')[0].id;
					break;					
				}
				parentDiv = parentDiv.parentNode;
			}
			JSTreeObj.dragNode_sourceNextSib = false;
			
			if(JSTreeObj.dragNode_source.nextSibling)JSTreeObj.dragNode_sourceNextSib = JSTreeObj.dragNode_source.nextSibling;
			JSTreeObj.dragNode_destination = false;
			JSTreeObj.dragDropTimer = 0;
			JSTreeObj.timerDragCopy();
			
			return false;
		},
		timerDragCopy : function() {	
			if (this.dragDropTimer >= 0 && this.dragDropTimer < 10) {
				this.dragDropTimer = this.dragDropTimer + 1;
				setTimeout('JSTreeObj.timerDragCopy()', 20);
				return false;
			}
			if (this.dragDropTimer == 10) {
				removeChildren(JSTreeObj.floatingContainer);
				JSTreeObj.floatingContainer.style.display='block';
				var tempNode = JSTreeObj.dragNode_source.cloneNode(true);
				tempNode.id = 'floatingContainer'+ tempNode.id;
				JSTreeObj.floatingContainer.appendChild(tempNode);
			}
		},		
		// Initialize drag
		initDrag : function(e) {
			if (e != null) {
				e = new Event(e);
				if (e.rightClick) {
					return false;
				}
			}
			
			if (saveOnDrop == true) {
				movingNode = true;
			}
			else {
				movingNode = false;
			}
			
			var liTag = document.getElementsByTagName('LI')[0];
			var subs = JSTreeObj.floatingContainer.getElementsByTagName('LI');

			if (subs.length>0) {
				if(JSTreeObj.dragNode_sourceNextSib) {
					JSTreeObj.dragNode_parent.insertBefore(JSTreeObj.dragNode_source, JSTreeObj.dragNode_sourceNextSib);
				}
				else {
					JSTreeObj.dragNode_parent.appendChild(JSTreeObj.dragNode_source);
				}					
			}
			
			JSTreeObj.dragNode_source = this.parentNode;
			JSTreeObj.dragNode_parent = this.parentNode.parentNode;
			JSTreeObj.previousPlaceInLevel = JSTreeObj.getOrderInLevel(JSTreeObj.dragNode_source.id, JSTreeObj.dragNode_parent.parentNode.id);
			JSTreeObj.previousParentId = JSTreeObj.dragNode_parent.parentNode.id;

			var parentDiv = JSTreeObj.dragNode_parent;
			while(true){
				if (parentDiv.getElementsByTagName('DIV')[0]){
					globalDivId = parentDiv.getElementsByTagName('DIV')[0].id;
					break;					
				}
				parentDiv = parentDiv.parentNode;
			}
			JSTreeObj.dragNode_sourceNextSib = false;
			
			if (JSTreeObj.dragNode_source.nextSibling) {
				JSTreeObj.dragNode_sourceNextSib = JSTreeObj.dragNode_source.nextSibling;
			}
			
			previousSiteTreeNode = $(JSTreeObj.dragNode_source).getPrevious();
			nextSiteTreeNode = $(JSTreeObj.dragNode_source).getNext();
			
			JSTreeObj.dragNode_destination = false;
			JSTreeObj.dragDropTimer = 0;
			JSTreeObj.timerDrag();

			return false;
		},
		timerDrag : function() {	
			if (this.dragDropTimer >= 0 && this.dragDropTimer < 10) {
				this.dragDropTimer = this.dragDropTimer + 1;
				setTimeout('JSTreeObj.timerDrag()', 20);
				return false;
			}
			if (this.dragDropTimer == 10) {
				removeChildren(JSTreeObj.floatingContainer);
				JSTreeObj.floatingContainer.style.display='block';
				JSTreeObj.floatingContainer.appendChild(JSTreeObj.dragNode_source);	
			}
		},
		moveDragableNodes: function(e) {
			try {
				if (JSTreeObj == null) {
					return false;
				}
			} catch(e) {
				return false;
			}
			
			if (JSTreeObj.dragDropTimer < 10) {
				return false;
			}
			
			e = new Event(e);

			dragDrop_x = e.client.x - 55;
			dragDrop_y = e.client.y

			JSTreeObj.floatingContainer.style.left = dragDrop_x + 'px';
			JSTreeObj.floatingContainer.style.top = dragDrop_y + 'px';
			JSTreeObj.floatingContainer.style.opacity = 0.5;

			var thisObj = $(this);
			var isOverLink = thisObj.getTag() == 'a';
			if (thisObj.tagName =='A' || thisObj.tagName=='IMG') {
				thisObj = thisObj.parentNode;
			}
			JSTreeObj.dragNode_noSiblings = false;
			var tmpVar = thisObj.getAttribute('noSiblings');
			if (!tmpVar) {
				tmpVar = thisObj.noSiblings;
			}
			if (tmpVar == 'true') {
				JSTreeObj.dragNode_noSiblings = true;
			}

			var tmpVar = thisObj.getAttribute('sourceTree');
			if(!tmpVar) {
				tmpVar = thisObj.sourceTree;
			}
			if (tmpVar == 'true') {
				JSTreeObj.dragNode_sourceTree = true;
				JSTreeObj.sourceTreee = true;
			}
				
			if (thisObj && thisObj.id) {
				var overflownContainers = new Array();
				overflownContainers.push($(ALL_CURRENT_SITE_STRUCTURE_TREE_ID));
				
				JSTreeObj.dragNode_destination = thisObj;
				var tmpObj = $('dragDropIndicatorContainer');
				tmpObj.setStyle('display', 'block');
				
				var eventSourceObj = this;
				if (JSTreeObj.dragNode_noSiblings && eventSourceObj.tagName == 'IMG') {
					eventSourceObj = eventSourceObj.nextSibling;
				}
				
				eventSourceObj = $(eventSourceObj);
				var indicatorLeftValue = 0;
				var tmpImg = $('dragDropIndicatorImage');
				if (this.tagName=='A' || JSTreeObj.dragNode_noSiblings) {
					tmpImg.src = tmpImg.src.replace('ind1','ind2');	
					JSTreeObj.insertAsSub = true;
				} 
				else {
					tmpImg.src = tmpImg.src.replace('ind2','ind1');
					JSTreeObj.insertAsSub = false;
				}
				try {
					indicatorLeftValue = eventSourceObj.getLeft(overflownContainers);
				} catch(ex) {
					indicatorLeftValue = (JSTreeObj.getLeftPos(eventSourceObj) + JSTreeObj.indicator_offsetX);
				}
				tmpObj.style.left = indicatorLeftValue + 'px';
				
				if (!thisObj || !thisObj.id) {
					tmpImg.style.visibility = 'hidden';
				}
				
				var indicatorTopValue = 0;
				var target = e.target;
				try {
					indicatorTopValue = target.getTop(overflownContainers);
					if (isOverLink) {
						indicatorTopValue += 13;
					}
					else {
						indicatorTopValue += 16;
					}
				} catch (ex) {
					indicatorTopValue = JSTreeObj.getTopPos(thisObj) + JSTreeObj.indicator_offsetY + 15;
				}
				
				tmpObj.style.top = indicatorTopValue + 'px';
			}			
			return false;
			
		},
		dropDragableNodes:function() {
			if(JSTreeObj.dragDropTimer<10){				
				JSTreeObj.dragDropTimer = -1;
				return false;
			}
				
			var showMessage = false;
			if(JSTreeObj.dragNode_destination){	// Check depth
				var countUp = JSTreeObj.dragDropCountLevels(JSTreeObj.dragNode_destination,'up');
				var countDown = JSTreeObj.dragDropCountLevels(JSTreeObj.dragNode_source,'down');
				var countLevels = countUp/1 + countDown/1 + (JSTreeObj.insertAsSub?1:0);		
				
				if(countLevels>JSTreeObj.maximumDepth){
					JSTreeObj.dragNode_destination = false;
					showMessage = true; 	// Used later down in this function
				}
			}
						
			var sourceTree = document.getElementById(JSTreeObj.dragNode_source.id).getAttribute('sourceTree');
						
			if (JSTreeObj.dragNode_destination) {
				if (JSTreeObj.insertAsSub) {
					var uls = JSTreeObj.dragNode_destination.getElementsByTagName('UL');
					if (uls.length > 0) {
						ul = uls[0];
						ul.style.display='block';
						
						var lis = ul.getElementsByTagName('LI');						
						
						var li = JSTreeObj.dragNode_source.getElementsByTagName('LI')[0];
						
						if (lis.length > 0) {
							//	Sub elements exists - drop dragable node before the first one
							ul.insertBefore(JSTreeObj.dragNode_source,lis[0]);	
						}
						else {
							//	No sub exists - use the appendChild method - This line should not be executed unless there's something wrong in the HTML, i.e empty <ul>
							ul.appendChild(JSTreeObj.dragNode_source);	
						}
					}
					else {
						var ul = document.createElement('UL');
						ul.style.display='block';
						JSTreeObj.dragNode_destination.appendChild(ul);
						ul.appendChild(JSTreeObj.dragNode_source);
					}
					var img = JSTreeObj.dragNode_destination.getElementsByTagName('IMG')[0];	

					img.style.visibility='visible';
					img.src = img.src.replace(JSTreeObj.plusImage,JSTreeObj.minusImage);					
					
				}
				else {
					if (JSTreeObj.dragNode_destination.nextSibling) {
						var nextSib = JSTreeObj.dragNode_destination.nextSibling;
						nextSib.parentNode.insertBefore(JSTreeObj.dragNode_source, nextSib);
					}
					else {
						JSTreeObj.dragNode_destination.parentNode.appendChild(JSTreeObj.dragNode_source);
					}

				}	
				/* Clear parent object */
				var tmpObj = JSTreeObj.dragNode_parent;
				var lis = tmpObj.getElementsByTagName('LI');

				if(lis.length==0){
					var tmpSpan = tmpObj.parentNode;
					var img = tmpSpan.parentNode.getElementsByTagName('IMG')[0];
					img.style.visibility='hidden';	// Hide [+],[-] icon
				}
				
			}
			else{
				// Putting the item back to it's original location
				if(JSTreeObj.dragNode_sourceNextSib){
					JSTreeObj.dragNode_parent.insertBefore(JSTreeObj.dragNode_source,JSTreeObj.dragNode_sourceNextSib);
				}else{
					JSTreeObj.dragNode_parent.appendChild(JSTreeObj.dragNode_source);
				}		
				return false;						
			}
			
			JSTreeObj.dropTargetIndicator.style.display='none';		
			JSTreeObj.dragDropTimer = -1;	
			if (showMessage && JSTreeObj.messageMaximumDepthReached) {
				alert(JSTreeObj.messageMaximumDepthReached);
			}
			var parentDiv = JSTreeObj.dragNode_destination;

			while(true){
				if (parentDiv.getElementsByTagName('DIV')){
					if (parentDiv.getElementsByTagName('DIV')[0]){
	
						if(globalDivId == parentDiv.getElementsByTagName('DIV')[0].id){
							saveMyTree(treeObj.getNewParent(null,null,JSTreeObj.dragNode_source.id, null), JSTreeObj.dragNode_source.id, null, null, null);									
						}
						else
							break;	
										
					}
					parentDiv = parentDiv.parentNode;
				}
			}			
		},
		deleteNodeChilds : function(nodeParent){
			var childs = nodeParent.getElementsByTagName('li');
			var nodeChilds = nodeParent.childNodes;
			for(var i = 0; i < childs.length; i++){
				if (nodeChilds[i].parentNode == nodeParent){
					JSTreeObj.getNodeChilds(nodeChilds[i]);
					deletePage(childs[i].id, null, null);
				}
			}
		},
		drawTable : function() {
			drawTableForEmptySite(ALL_CURRENT_SITE_STRUCTURE_TREE_ID);
		},
		dropDragableNodesCopy: function(nodeBeingDeletedId) {
			if (JSTreeObj.dragDropTimer < 10) {
				JSTreeObj.dragDropTimer = -1;
				return false;
			}
			if (JSTreeObj.firstTopPage == true) {
				var rootUl = document.getElementById('rootUl');
				if (JSTreeObj.floatingContainer.getElementsByTagName('LI')[0]) {
					rootUl.appendChild(document.getElementById(JSTreeObj.floatingContainer.getElementsByTagName('LI')[0].id));	
					var emptyTextContainer = $(EMPTY_SITE_TREE_TEXT_CONTAINER_ID);
					if (emptyTextContainer != null) {
						setNeedReloadBuilderPage(true);
						emptyTextContainer.remove();
					}
					JSTreeObj.saveRoot(JSTreeObj.dragNode_source.id, JSTreeObj.dragNode_source.getAttribute('pagetype'), JSTreeObj.dragNode_source.getAttribute('templatefile'), 
								(JSTreeObj.dragNode_source.getElementsByTagName('a')[0]).innerHTML, true, '1', null);
								
					JSTreeObj.dropTargetIndicator.style.display='none';				
					JSTreeObj.dragDropTimer = -1;								
				}
				JSTreeObj.firstTopPage = false;
				return false;
			}
			
			if (JSTreeObj.deleteNodes == true) {
				var followingNodes = JSTreeObj.getFollowingNodes(JSTreeObj.previousParentId, JSTreeObj.previousPlaceInLevel);
				deletePage(JSTreeObj.dragNode_source.id, followingNodes, nodeBeingDeletedId);
				removeChildren(JSTreeObj.floatingContainer);
				var tmpObj = JSTreeObj.dragNode_parent;
				var lis = tmpObj.getElementsByTagName('LI');
				
				temp = document.getElementById(ALL_CURRENT_SITE_STRUCTURE_TREE_ID);

				if (temp.childNodes.length == 0) {
					JSTreeObj.drawTable();
				}
				else {
					if (lis.length == 0) {
						var tmpSpan = tmpObj.parentNode;
						var img = tmpSpan.parentNode.getElementsByTagName('IMG')[0];
						if (img) {
							img.style.visibility='hidden';	// Hide [+],[-] icon
						}
						tmpObj.parentNode.removeChild(tmpObj);
					}
					if (temp.childNodes.length == 0) {
						JSTreeObj.drawTable();
					}
				}
				JSTreeObj.dropTargetIndicator.style.display = 'none';				
				JSTreeObj.dragDropTimer = -1;
				if (JSTreeObj.deleteNodes == true) {
					JSTreeObj.prepareToDelete();	
				}
				
				return false;
			}				

			if (saveOnDrop != true) {
				JSTreeObj.restoreTreeStructure();
				return false;
			}
			
			var showMessage = false;
			if (JSTreeObj.dragNode_destination) {	// Check depth
				var countUp = JSTreeObj.dragDropCountLevels(JSTreeObj.dragNode_destination,'up');
				var countDown = JSTreeObj.dragDropCountLevels(JSTreeObj.dragNode_source,'down');
				var countLevels = countUp/1 + countDown/1 + (JSTreeObj.insertAsSub?1:0);		
			}
			if (JSTreeObj.dragNode_destination) {
				if (JSTreeObj.insertAsSub) {
					JSTreeObj.insertNewSiteTreeNodes();
					var img = JSTreeObj.dragNode_destination.getElementsByTagName('IMG')[0];
					img.style.visibility='visible';
					img.src = img.src.replace(JSTreeObj.plusImage,JSTreeObj.minusImage);
				}
				else {
					var nodeToInsert = $(JSTreeObj.floatingContainer.getElementsByTagName('LI')[0]);
					if (nodeToInsert) {
						var nextDestinationSibling = $(JSTreeObj.dragNode_destination.nextSibling);
						if (nextDestinationSibling) {
							nodeToInsert.injectBefore(nextDestinationSibling);
						}
						else {
							var previousDestinationSibling = $(JSTreeObj.dragNode_destination);
							if (previousDestinationSibling) {
								nodeToInsert.injectAfter(previousDestinationSibling);
							}
							else {
								nodeToInsert.injectInside($(JSTreeObj.dragNode_destination).getParent());
							}
						}
					}
				}
				// Clear parent object
				var tmpObj = $(JSTreeObj.dragNode_parent);
				var lis = tmpObj.getElementsByTagName('LI');
				if (lis.length == 0) {
					var tmpSpan = tmpObj.parentNode;
					var img = tmpSpan.parentNode.getElementsByTagName('IMG')[0];
					if (img != null) {
						img.style.visibility='hidden';	// Hide [+],[-] icon
					}
					tmpObj.remove();										
				}								
			}
			else {			
				//	Putting the item back to it's original location
				JSTreeObj.restoreTreeStructure();
				return false;
			}
			
			JSTreeObj.dropTargetIndicator.style.display='none';		
			JSTreeObj.dragDropTimer = -1;	
			if (showMessage && JSTreeObj.messageMaximumDepthReached) {
				alert(JSTreeObj.messageMaximumDepthReached);
			}
			
			var parentDiv = JSTreeObj.dragNode_destination;
			if (parentDiv == null) {
				return false;
			}
			while (true) {
				if (parentDiv.getElementsByTagName('DIV')) {
					if (parentDiv.getElementsByTagName('DIV')[0]) {
						if (globalDivId == parentDiv.getElementsByTagName('DIV')[0].id) {
							var newParentId = treeObj.getNewParent(null,null,JSTreeObj.dragNode_source.id, null);
							
							var numberInLevel = treeObj.getOrderInLevel(JSTreeObj.dragNode_source.id, newParentId);					

							var movingBackward = false;
							var nodesToIncrease = null;
							var nodesToDecrease = null;

							var newParent = document.getElementById(JSTreeObj.previousParentId);
							if (newParent.tagName == 'SPAN'){
								newParent = newParent.parentNode;
								JSTreeObj.previousParentId = newParent.id;		
							}
							if ((newParentId == JSTreeObj.previousParentId) || ((document.getElementById(JSTreeObj.previousParentId).tagName != 'LI')&&(!newParentId))){
								// Same parent								
								if(JSTreeObj.previousPlaceInLevel > numberInLevel){
									nodesToIncrease = JSTreeObj.getNodesBetween(JSTreeObj.dragNode_source.id, JSTreeObj.previousParentId, numberInLevel+1, JSTreeObj.previousPlaceInLevel);
								}
								else
									nodesToDecrease = JSTreeObj.getNodesBetween(JSTreeObj.dragNode_source.id, JSTreeObj.previousParentId, JSTreeObj.previousPlaceInLevel, numberInLevel-1);
							}
							else {
								var tempParentId = newParentId;
								if(!tempParentId){
									tempParentId = JSTreeObj.dragNode_source.parentNode.parentNode.id;
								}
								nodesToDecrease = JSTreeObj.getFollowingNodes(JSTreeObj.previousParentId, JSTreeObj.previousPlaceInLevel);
								nodesToIncrease = JSTreeObj.getFollowingNodes(tempParentId, numberInLevel+1);
							}																
																
							if (!newParentId) {
								newParentId = -1;
							}
							saveMyTree(newParentId, JSTreeObj.dragNode_source.id, numberInLevel, nodesToIncrease, nodesToDecrease);
							var newPageUri = 'undefined';
							var linkFirstChild = JSTreeObj.dragNode_source.getElementsByTagName('a')[0];
							if (linkFirstChild != null) {
								newPageUri = linkFirstChild.innerHTML;
							}
							changePageTitleCallback(null);
						}
						else{
							var newParentId = treeObj.getNewParent(null,null,'floatingContainer'+JSTreeObj.dragNode_source.id, null);					
							
							var numberInLevel = treeObj.getOrderInLevel('floatingContainer'+JSTreeObj.dragNode_source.id, newParentId);
							
							var followingNodes = treeObj.getFollowingNodes(newParentId, numberInLevel+1);
							if (followingNodes == null) {
								JSTreeObj.restoreTreeStructure();
								return false;
							}
										
							if(!newParentId) {
								JSTreeObj.saveRoot(JSTreeObj.dragNode_source.id, JSTreeObj.dragNode_source.getAttribute('pagetype'), JSTreeObj.dragNode_source.getAttribute('templatefile'), 
								(JSTreeObj.dragNode_source.getElementsByTagName('a')[0]).innerHTML, false, numberInLevel, followingNodes);
							}
							else {
								JSTreeObj.saveNewPage(newParentId, JSTreeObj.dragNode_source.getAttribute('pagetype'), JSTreeObj.dragNode_source.getAttribute('templatefile'), 
								(JSTreeObj.dragNode_source.getElementsByTagName('a')[0]).innerHTML, numberInLevel, followingNodes);
							}								
						}
						// Need name
						break;	
					}
					parentDiv = parentDiv.parentNode;
				}
			}			
		},
		restoreTreeStructure : function() {
			var liTagsInContainer = $(JSTreeObj.floatingContainer).getElements('li');
			if (liTagsInContainer != null && liTagsInContainer.length > 0) {
				var firstElement = liTagsInContainer[0];
				
				if (movingNode == true) {
					var nodeToRestore = null;
					try {
						nodeToRestore = firstElement;
						
						if (previousSiteTreeNode) {
							nodeToRestore.injectAfter(previousSiteTreeNode);
						}
						else if (nextSiteTreeNode) {
							nodeToRestore.injectBefore(nextSiteTreeNode);
						}
						else {
							if ($(JSTreeObj.dragNode_parent).getParent() == null) {
								$(JSTreeObj.dragNode_parent).injectInside($(ALL_CURRENT_SITE_STRUCTURE_TREE_ID));
							}
							
							nodeToRestore.injectInside($(JSTreeObj.dragNode_parent));
						}
						
						previousSiteTreeNode = null;
						nextSiteTreeNode = null;
					} catch(e) {
						JSTreeObj.dragNode_parent.appendChild(firstElement);
					}
				}
				else if (document.getElementById(JSTreeObj.floatingContainer.getElementsByTagName('LI')[0].id)) {
					JSTreeObj.floatingContainer.removeChild(firstElement);
				}
			}
			
			$(JSTreeObj.floatingContainer).empty();
			JSTreeObj.dropTargetIndicator.style.display='none';
			JSTreeObj.dragDropTimer = -1;
	
			return false;
		},
		saveRoot : function (nodeId, pagetype, templatefile, pageName, isFirst, numberInLevel, followingNodes){
			treeStructure = new Array();
			var nodes = JSTreeObj.getRootStructure('floatingContainer'+nodeId, numberInLevel);
			if (nodes.length == 0) {
				JSTreeObj.restoreTreeStructure();
				return false;
			}
			var tempFloatingContainer = $('floatingContainer'+nodeId);
			if (tempFloatingContainer != null) {
				tempFloatingContainer.setProperty('id', 'rootTemporary');
			}
			showLoadingMessage(CREATING_TEXT);
			LucidEngine.createPage(nodes, isFirst, numberInLevel, followingNodes, JSTreeObj.getNewRootId);
		},
		getNewRootId : function(id) {
			closeAllLoadingMessages();

			if (id == null || id.length == 0) {
				return false;
			}
						
			var root = $('rootTemporary');
			if (root == null) {
				return false;
			}
			root.setAttribute('id', id[0]);
			
			var createdNode = $(id[0]);
			if (createdNode == null) {
				return false;
			}
			
			JSTreeObj.initNode(createdNode);
			var childsLink = $(createdNode.getElementsByTagName('a')[0]);
			if (childsLink) {
				childsLink.addClass('pageTreeNames');
			}
			
			var newChilds = root.getElementsByTagName('li');
			var newChildsElement = null;
			for (var i = 0; i < newChilds.length; i++) {
				newChildsElement = $(newChilds[i]);
				if (newChildsElement != null) {
					newChildsElement.setProperty('id', id[i+1]);
					JSTreeObj.initNode(newChildsElement);
					childsLink = $(newChildsElement.getElementsByTagName('a')[0]);
					if (childsLink) {
						childsLink.addClass('pageTreeNames');
					}
				}
			}
			
			var lastID = id[id.length - 1];
			setPageID(lastID, true);
			getPrewUrl(lastID);
			isChangingSiteMap();
			registerActionsForSiteTree();
			boldCurrentTreeElementWithPageId(lastID);
			
			if (isNeedRelaodBuilderPage() && isSiteMap() && isNeedRedirect()) {
				showLoadingMessage(REDIRECTING_TEXT);
				setNeedReloadBuilderPage(false);
				redirectAction('/workspace/builder/application', 0);
			}
		},		
		getRootStructure : function(rootId, numberInLevel) {
			var newTreeNodes = new Array();
			
			var root = document.getElementById(rootId);
			if (root == null) {
				return newTreeNodes;
			}
			
			var newChilds = root.getElementsByTagName('li');
			var nodeId = root.id;
			var nodeName = (root.getElementsByTagName('a')[0]).innerHTML;
			var pageType = root.getAttribute('pagetype');
			var parentId = null;
			var templateFile = root.getAttribute('templatefile');		
			
			treeStructure.push(nodeId);		
			treeStructure.push(parentId);			
			treeStructure.push(nodeName);
			treeStructure.push(pageType);
			treeStructure.push(templateFile);				
			
			newTreeNodes.push(new newTreeNode (nodeId, parentId, nodeName, pageType, templateFile, numberInLevel));						
			
			parentId = nodeId;
			for (var i = 0; i < newChilds.length; i++){			
				nodeId = newChilds[i].id;
				nodeName = (newChilds[i].getElementsByTagName('a')[0]).innerHTML;
				pageType = newChilds[i].getAttribute('pagetype');
				if(i != 0)
					parentId = newChilds[i].parentNode.parentNode.parentNode.id;
				templateFile = newChilds[i].getAttribute('templatefile');		

				treeStructure.push(nodeId);		
				treeStructure.push(parentId);
				treeStructure.push(nodeName);
				treeStructure.push(pageType);
				treeStructure.push(templateFile);				

				newTreeNodes.push(new newTreeNode (nodeId, parentId, nodeName, pageType, templateFile, null));			
				
				parentId = newChilds[i].getAttribute('id');
			}
			return newTreeNodes;					
		},
		createDropIndicator : function() {
			var tempDropTargetIndicator = $('dragDropIndicatorContainer');
			if (tempDropTargetIndicator == null) {
				tempDropTargetIndicator = new Element('div');
				tempDropTargetIndicator.setStyle('position', 'absolute');			
				tempDropTargetIndicator.setStyle('display', 'none');	
				tempDropTargetIndicator.setProperty('id', 'dragDropIndicatorContainer');		
				
				var img = new Element('img');
				img.setProperty('src', imageFolder + 'dragDrop_ind1.gif');
				img.setProperty('id', 'dragDropIndicatorImage');
				img.injectInside(tempDropTargetIndicator);
			
				tempDropTargetIndicator.injectInside($(document.body));
			}
			this.dropTargetIndicator = tempDropTargetIndicator;
		},
		dragDropCountLevels : function(obj,direction,stopAtObject){
			var countLevels = 0;
			if(direction=='up'){
				while(obj.parentNode && obj.parentNode!=stopAtObject){
					obj = obj.parentNode;
					if(obj.tagName=='UL')countLevels = countLevels/1 +1;
				}		
				return countLevels;
			}	
			
			if(direction=='down'){ 
				var subObjects = obj.getElementsByTagName('LI');
				for(var no=0;no<subObjects.length;no++){
					countLevels = Math.max(countLevels,JSTreeObj.dragDropCountLevels(subObjects[no],'up',obj));
				}
				return countLevels;
			}	
		},
		cancelEvent : function() {
			return false;	
		},
		cancelSelectionEvent : function() {		
			if(JSTreeObj.dragDropTimer<10)return true;
			return false;	
		},
		getParentId : function(childNodeId) {
			var childNode = document.getElementById(childNodeId);
			var possibleParent = childNode.parentNode;
			if(possibleParent.tagName != 'SPAN'){

				possibleParent.id = JSTreeObj.getParentId(possibleParent.id);
			}
			return possibleParent.id;
		},
		getParentLiTag : function(child, treeId){
				var possibleParent = child.parentNode;
				if(possibleParent.id)
					if(possibleParent.id.toString() == treeId.toString()){
						return null;
					}
				if (possibleParent.tagName.toString() != 'LI'){
					possibleParent = JSTreeObj.getParentLiTag(possibleParent, treeId);
				}	
			return possibleParent;
		},
		getNewParent : function(initObj, saveString, child, newParent, treeId) {
			var possibleParent = null;
			if(!saveString)var saveString = '';
			if(!initObj){
				initObj = document.getElementById(this.idOfTree);
				treeId = initObj.id;
			}
			var lis = initObj.getElementsByTagName('LI');
			if(lis.length>0){
				var li = lis[0];
				while(li){
					if(li.id){
						if(saveString.length>0)saveString = saveString + ',';
						var numericID = li.id;
						if(numericID.length==0)numericID='A';
						if(numericParentID){
							possibleParent = JSTreeObj.getParentLiTag(li, treeId);
							if (possibleParent)
								numericParentID = possibleParent.id;							
							else
								return null;
						}
						else
							if(li.id.toString() == child.toString()){
								possibleParent = JSTreeObj.getParentLiTag(li, treeId);
								if (possibleParent)
									var numericParentID = possibleParent.id;							
								else
									return null;
							}		
						if(numericID!='0'){
							saveString = saveString + numericID;
							saveString = saveString + '-';
							if(li.parentNode.id!=this.idOfTree)
								saveString = saveString + numericParentID;
							else saveString = saveString + '0';
							if (numericID.toString() == child.toString()) {
								newParent = numericParentID;
								return newParent;
							}
						}
						var ul = li.getElementsByTagName('UL');
						if(ul.length>0){
							newParent = this.getNewParent(ul[0],saveString,child,newParent, treeId);	
						}	
						possibleParentId = li.id;
					}			
					li = li.nextSibling;
				}
			}

			if(initObj.id == this.idOfTree){
				return newParent;							
			}
			return newParent;		
		},
		getNodeOrders : function(initObj,saveString) {
			if(!saveString)var saveString = '';
			if(!initObj){
				initObj = document.getElementById(this.idOfTree);
			}
			var lis = initObj.getElementsByTagName('LI');

			if(lis.length>0){
				var li = lis[0];
				while(li){
					if(li.id){
						if(saveString.length>0)saveString = saveString + ',';
						var numericID = li.id.replace(/[^0-9]/gi,'');
						if(numericID.length==0)numericID='A';
						var numericParentID = li.parentNode.parentNode.id.replace(/[^0-9]/gi,'');
						if(numericID!='0'){
							saveString = saveString + numericID;
							saveString = saveString + '-';														
							if(li.parentNode.id!=this.idOfTree)
								saveString = saveString + numericParentID;
							else saveString = saveString + '0';
						}
						var ul = li.getElementsByTagName('UL');
						if(ul.length>0){
							saveString = this.getNodeOrders(ul[0],saveString);	
						}	
					}			
					li = li.nextSibling;
				}
			}

			if(initObj.id == this.idOfTree){
				return saveString;
							
			}
			return saveString;
		},		
		prepareToDelete : function(){
			var trashCan = document.getElementById('trash');
			if(JSTreeObj.deleteNodes == true){
				JSTreeObj.deleteNodes = false;
				trashCan.style.opacity = 0.5;
				}
			else {
				JSTreeObj.deleteNodes = true;
				trashCan.style.opacity = 1;
			}
		},
		mouseOverRecycleBin : function(){
			var trashCan = document.getElementById('trash');
			JSTreeObj.deleteNodes = true;
			trashCan.style.opacity = 1;			
		},
		mouseOutOfRecycleBin : function(){
			var trashCan = document.getElementById('trash');			
			JSTreeObj.deleteNodes = false;
			trashCan.style.opacity = 0.5;
		},
		initTree : function() {
			JSTreeObj = this;
			treeObj = this;
			if (!this) {
				return false;
			}
			
			JSTreeObj.createDropIndicator();
			document.documentElement.onselectstart = JSTreeObj.cancelSelectionEvent;
			document.documentElement.ondragstart = JSTreeObj.cancelEvent;
			var nodeId = 0;
			
			if (!this.idOfTree) {
				return false;
			}
			var dhtmlgoodies_tree = document.getElementById(this.idOfTree);
			if (!dhtmlgoodies_tree) {
				return false;
			}
			
			var menuItems = dhtmlgoodies_tree.getElementsByTagName('LI');	// Get an array of all menu items
			if (!menuItems) {
				return false;
			}
			for (var no = 0; no < menuItems.length; no++) {
				// No children var set ?
				
				var noChildren = false;
				var tmpVar = menuItems[no].getAttribute('noChildren');
				if(!tmpVar)tmpVar = menuItems[no].noChildren;
				if(tmpVar=='true')noChildren=true;

				var sourceTree = false;
				var tmpVar = menuItems[no].getAttribute('sourceTree');
				if (!tmpVar) {
					tmpVar = menuItems[no].sourceTree;
				}
				if (tmpVar == 'true') {
					sourceTree = true;
					JSTreeObj.sourceTreee = true;
				}
				// No drag var set ?
				var noDrag = false;
				var tmpVar = menuItems[no].getAttribute('noDrag');
				if(!tmpVar)tmpVar = menuItems[no].noDrag;
				if(tmpVar=='true')noDrag=true;
				
				var iconfile = null;		 
				var tmpVar = menuItems[no].getAttribute('iconfile');
				if(!tmpVar)				
					tmpVar = menuItems[no].iconfile;
				if(tmpVar)
					iconfile = tmpVar;
				else {
					iconfile = iconFolder + this.folderImage;						
				}

				var templatefile = null;		 
				var tmpVar = menuItems[no].getAttribute('templatefile');
				if(!tmpVar)				
					tmpVar = menuItems[no].templatefile;
				if(tmpVar)
					templatefile = tmpVar;
									
				nodeId++;
				var subItems = menuItems[no].getElementsByTagName('UL');
				var img = document.createElement('IMG');
				img.src = imageFolder + this.plusImage;
				img.onclick = JSTreeObj.showHideNode;
				
				if(subItems.length==0)
					img.style.visibility='hidden';
				else{
					subItems[0].id = 'tree_ul_' + treeUlCounter;
					treeUlCounter++;
				}
				
				var input = document.createElement('INPUT');
				input.style.display='none';
				
				var aTag = menuItems[no].getElementsByTagName('A')[0];
				if (aTag) {
					if(aTag.id)
						numericId = aTag.id.replace(/[^0-9]/g, '');
					else
						numericId = (no+1);			
		
					aTag.id = menuItems[no].id + 'a';
		
					menuItems[no].insertBefore(input, aTag);
		
					input.id = menuItems[no].id + 'input';
					addEventsToSiteTreeInput($(input.id));
					
					aTag.setAttribute('href', 'javascript:void(0)');
					
					try {
						if(!noDrag)aTag.onmousedown = JSTreeObj.initDrag;
						if(!noChildren)aTag.onmousemove = JSTreeObj.moveDragableNodes;
						if(sourceTree)aTag.onmousedown = JSTreeObj.copyDragableNode;
						if(sourceTree)aTag.onclick = JSTreeObj.createNodeOnClick;
					} catch(e) {
						return false;
					}
				}
								
				menuItems[no].insertBefore(img,input);

				var folderImg = new Element('img');
				folderImg.addClass('pageTreeNameIconStyle');
				
				try {
					if (!noDrag) {
						if (sourceTree) {
							folderImg.onmousedown = JSTreeObj.copyDragableNode;
						}
						else {
							folderImg.onmousedown = JSTreeObj.initDrag;
						}
					}
					if (!noChildren) {
						folderImg.onmousemove = JSTreeObj.moveDragableNodes;
					}
				} catch(e) {
					return false;
				}

				if(iconfile){	
					if (iconfile.toString() == '')
						folderImg.src = imageFolder + this.folderImage;	
					else
						folderImg.src = iconfile;
				}
				else if(menuItems[no].className){
					folderImg.src = imageFolder + menuItems[no].className;
				}else{			
					folderImg.src = imageFolder + this.folderImage;					
				}
				menuItems[no].insertBefore(folderImg,input);				
			}	
		
			initExpandedNodes = this.Get_Cookie('dhtmlgoodies_expandedNodes');
			if (initExpandedNodes) {
				var nodes = initExpandedNodes.split(',');
			}
			
			try {
				document.documentElement.onmousemove = JSTreeObj.moveDragableNodes;
				document.documentElement.onmouseup = JSTreeObj.dropDragableNodesCopy;
			} catch(e) {
				return false;
			}
			
			if (sourceTree) {
				this.actionOnMouseUp = 'copy';
			}
			else {
				this.actionOnMouseUp = 'move';
			}
		},
		initNode : function(node) {
			if (node == null) {
				return false;
			}
			node = $(node);
			if (node.getTag() != 'li') {
				return false;
			}
			
			var noChildren = false;
			var sourceTree = false;
			var noDrag = false;
			var iconfile = null;
			
			var images = node.getElementsByTagName('img');
			var folderImg = images[1];
			
			var tmpVar = node.getProperty('iconfile');
			if (!tmpVar) {
				tmpVar = node.iconfile;
			}
			
			if (tmpVar) {
				iconfile = tmpVar;
			}
			else {
				folderImg.src = imageFolder + this.iconFolder;	
			}

			var templatefile = null;		 
			var tmpVar = node.getProperty('templatefile');
			if (!tmpVar) {
				tmpVar = node.templatefile;
			}
			if (tmpVar) {
				templatefile = tmpVar;
			}
			
			nodeId++;
			var subItems = node.getElementsByTagName('UL');
			var aTag = node.getElementsByTagName('A')[0];

			var input = node.getElementsByTagName('INPUT')[0];
			input.style.display='none';
			input.id = node.id + 'input';
			addEventsToSiteTreeInput($(input.id));
			
			try {
				if (!noDrag) {
					aTag.onmousedown = JSTreeObj.initDrag;
				}
				if (!noChildren) {
					aTag.onmousemove = JSTreeObj.moveDragableNodes;
				}
				if (sourceTree) {
					aTag.onmousedown = JSTreeObj.copyDragableNode;
				}	
				
				if (!noDrag) {
					if (sourceTree) {
						folderImg.onmousedown = JSTreeObj.copyDragableNode;
					}
					else {
						folderImg.onmousedown = JSTreeObj.initDrag;
					}
				}
				
				if (!noChildren) {
					folderImg.onmousemove = JSTreeObj.moveDragableNodes;
				}
			} catch(e) {
				return false;
			}
			
			if (node.className) {
				folderImg.src = imageFolder + node.className;
			}
			else {		
				folderImg.src = imageFolder + this.iconFolder;					
			}
			if (iconfile) {
				folderImg.src = iconfile;
			}
			
			try {
				document.documentElement.onmousemove = JSTreeObj.moveDragableNodes;
				document.documentElement.onmouseup = JSTreeObj.dropDragableNodesCopy;
			} catch(e) {
				return false;
			}
			
			this.actionOnMouseUp = 'move';
		},					
		saveNewPage : function (newParentNodeId, pagetype, templatefile, pageName, numberInLevel, followingNodes){
			treeStructure = new Array();
			var nodes = JSTreeObj.getStructure('floatingContainer'+JSTreeObj.dragNode_source.id, newParentNodeId, numberInLevel);			
			document.getElementById('floatingContainer'+JSTreeObj.dragNode_source.id).id = 'rootTemporary';				
			showLoadingMessage(CREATING_TEXT);
			LucidEngine.createPage(nodes, false, numberInLevel, followingNodes, JSTreeObj.getNewRootId);
		},
		getStructure : function(rootId, parentId, numberInLevel){
			var newTreeNodes = new Array();
			var root = document.getElementById(rootId);
			var newChilds = root.getElementsByTagName('li');

			var nodeId = root.id;
			var nodeName = (root.getElementsByTagName('a')[0]).innerHTML;
			var pageType = root.getAttribute('pagetype');
			var parentId = parentId;
			var templateFile = root.getAttribute('templatefile');		

			treeStructure.push(nodeId);		
			treeStructure.push(parentId);			
			treeStructure.push(nodeName);
			treeStructure.push(pageType);
			treeStructure.push(templateFile);				
			
			newTreeNodes.push(new newTreeNode (nodeId, parentId, nodeName, pageType, templateFile, numberInLevel));			
			
			parentId = nodeId;
			
			for (var i = 0; i < newChilds.length; i++){			
				nodeId = newChilds[i].id;
				nodeName = (newChilds[i].getElementsByTagName('a')[0]).innerHTML;
				pageType = newChilds[i].getAttribute('pagetype');
				if(i != 0)
					parentId = newChilds[i].parentNode.parentNode.parentNode.id;
				templateFile = newChilds[i].getAttribute('templatefile');		

				treeStructure.push(nodeId);
				treeStructure.push(parentId);			
				treeStructure.push(nodeName);
				treeStructure.push(pageType);
				treeStructure.push(templateFile);				
				
				newTreeNodes.push(new newTreeNode (nodeId, parentId, nodeName, pageType, templateFile, null));			
				
				parentId = newChilds[i].getAttribute('id');
			}
			return newTreeNodes;			
		},	
		getNewId : function(id){
			closeAllLoadingMessages();
			if (id == null) {
				return false;
			}
			var newParent = document.getElementById(JSTreeObj.parentId);
			var newChilds = newParent.getElementsByTagName('li');		

			for(var i = 0; i < newChilds.length; i++){
				(document.getElementById(newChilds[i].id)).setAttribute('id', id[i]);	
				JSTreeObj.initNode(document.getElementById(id[i]));		
				var newName = (document.getElementById(id[i]).getElementsByTagName('a')[0]).innerHTML;
				if (movingNode) {
					changePageTitleCallback(null);
				}
			}
			var lastID = id[id.length - 1];
			setPageID(lastID, true);
			getPrewUrl(lastID);
			isChangingSiteMap();
		},
		getNodeChilds : function(nodeParent, newParentId){
			var childs = nodeParent.getElementsByTagName('li');
			var nodeChilds = nodeParent.childNodes;
			for(var i = 0; i < childs.length; i++){
				if (nodeChilds[i].parentNode == nodeParent){
					JSTreeObj.saveNewPage(newParentId, childs[i].getAttribute('pagetype'), childs[i].getAttribute('templatefile'), 
						(childs[i].getElementsByTagName('a')[0]).innerHTML, null, null);
					JSTreeObj.getNodeChilds(nodeChilds[i], JSTreeObj.newPageId);
				}
			}
		},
		setReceved : function(rec){
			JSTreeObj.receved = rec;	
		},
		getReceved : function(){
			return JSTreeObj.receved;
		},
		prepareToSetTopPage : function() {
			var container = $(EMPTY_SITE_TREE_TEXT_CONTAINER_ID);
			highlightElement(container, 500, '#fff');
			JSTreeObj.firstTopPage = true;
		},
		topPageNotSet : function() {
			JSTreeObj.firstTopPage = false;			
		},
		aboveTree : function(){
			saveOnDrop = true;
		},
		notAboveTree : function(){
			saveOnDrop = false;			
			JSTreeObj.dropTargetIndicator.style.display='none';				
		},
		checkIfOverTree : function(idOfTree){
			var treeElement = $(idOfTree);
			if (!treeElement) {
				return false;
			}
			treeElement.addEvent('mouseover', function() {
				JSTreeObj.aboveTree();
			});
			treeElement.addEvent('mouseout', function() {
				JSTreeObj.notAboveTree();
			});
		},
		getOrderInLevel : function(nodeId, parentNodeId) {
			var ulTag = null;
			if (parentNodeId == null) {
				var parentNode = document.getElementById(this.idOfTree);
				ulTag = parentNode.childNodes[0].childNodes[0];
			}
			else {
				var parentNode = $(parentNodeId);
				var childrenOfParent = parentNode.childNodes;
				var spanTag = $(childrenOfParent[childrenOfParent.length - 1]);
				if (spanTag == null) {
					return 0;
				}
				if (spanTag.getTag() == 'span') {
					ulTag = spanTag.getElements('ul')[0];			
				}
				else{
					ulTag = spanTag;				
				}
			}
			
			if (ulTag == null) {
				return 0;
			}
			
			var childrenOfUlTag = $(ulTag).getChildren();
			if (childrenOfUlTag == null) {
				return 0;
			}
			
			var number = 0;
			if ($(ulTag).getElements('li').length == 0) {
				return 1;
			}
			else {
				for (var i = 0; i < childrenOfUlTag.length; i++) {
					if (childrenOfUlTag[i].id == nodeId) {
						number = i + 1;
						return number;
					}
				}
			}
			return 0;
		},
		getFollowingNodes : function(parentNodeId, placeInLevel) {
			var ulTag = null;
			var result = new Array();	//	Ids of following nodes
			
			if (parentNodeId == null){
				var parentNode = document.getElementById(this.idOfTree);
				ulTag = parentNode.childNodes[0].childNodes[0];
			}
			else {
				var parentNode = $(parentNodeId);
				var childrenOfParent = parentNode.childNodes;
				var spanTag = $(childrenOfParent[childrenOfParent.length - 1]);
				if (spanTag == null) {
					return null;
				}
				if (spanTag.childNodes.length == 0) {
					return null;
				}
				if (spanTag.getTag() == 'span') {
					ulTag = spanTag.getElements('ul')[0];			
				}
				else{
					ulTag = spanTag;
				}
			}
			
			if (ulTag == null) {
				return null;
			}
			
			var childrenOfUlTag = $(ulTag).getChildren();
			if (childrenOfUlTag == null) {
				return null;
			}
			
			var number = null;

			if ($(ulTag).getElements('li').length == 0) {
				return null;
			}
			else {
				for(var i = placeInLevel-1; i < childrenOfUlTag.length; i++) {
					var childOfUlTag = childrenOfUlTag[i];
					if (childOfUlTag != null) {
						if ($(childOfUlTag).getTag() == 'li') {
							result.push(childOfUlTag.id);
						}
					}
				}
			}
			return result;
		},
		getNodesBetween : function(nodeId, parentNodeId, placeFrom, placeTo) {
			var ulTag = null;
			var result = new Array();	//	Ids of nodes
			
			if (parentNodeId == null) {
				var parentNode = document.getElementById(this.idOfTree);
				ulTag = parentNode.childNodes[0].childNodes[0];
			}
			else {
				var parentNode = $(parentNodeId);
				var childrenOfParent = parentNode.childNodes;
				var spanTag = $(childrenOfParent[childrenOfParent.length - 1]);
				if (spanTag == null) {
					return null;
				}
				if (spanTag.getTag() == 'span') {
					ulTag = spanTag.getElements('ul')[0];			
				}
				else {
					ulTag = spanTag;				
				}
			}
			
			if (ulTag == null) {
				return null;
			}
			
			var childrenOfUlTag = $(ulTag).getChildren();
			var number = null;

			if ($(ulTag).getElements('li').length == 0){
				return null;
			}
			else {
				for (var i = placeFrom-1; i < placeTo; i++) {
					var childOfUlTag = childrenOfUlTag[i];
					if (childOfUlTag != null) {
						if ($(childOfUlTag).getTag() == 'li') {
							result.push(childOfUlTag.id);
						}
					}
				}
			}
			
			return result;					
		},
		createNodeOnClick : function() {
			removeChildren(JSTreeObj.floatingContainer);
			JSTreeObj.dragNode_source = this.parentNode;			
			JSTreeObj.floatingContainer.style.display='block';
			var tempNode = JSTreeObj.dragNode_source.cloneNode(true);
			tempNode.id = 'floatingContainer'+ tempNode.id;
			JSTreeObj.floatingContainer.appendChild(tempNode);
			
			var activePageId = getPageID();
			JSTreeObj.dragNode_destination = document.getElementById(activePageId);
			if (!JSTreeObj.dragNode_destination) {
				JSTreeObj.dragNode_destination = $(ALL_CURRENT_SITE_STRUCTURE_TREE_ID);
				if (JSTreeObj.dragNode_destination) {	//	Will create root page
					JSTreeObj.firstTopPage = true;
					JSTreeObj.deleteNodes = false;
					JSTreeObj.dragDropTimer = 11;
					JSTreeObj.dropDragableNodesCopy(null);
					return true;
				}
				else {
					return false;
				}
			}

			JSTreeObj.insertNewSiteTreeNodes();
			
			var img = JSTreeObj.dragNode_destination.getElementsByTagName('IMG')[0];
			img.style.visibility='visible';
			img.src = img.src.replace(JSTreeObj.plusImage,JSTreeObj.minusImage);			
			
			var followingNodes = treeObj.getFollowingNodes(activePageId, 3);
			JSTreeObj.saveNewPage(activePageId, JSTreeObj.dragNode_source.getAttribute('pagetype'), JSTreeObj.dragNode_source.getAttribute('templatefile'), 
								(JSTreeObj.dragNode_source.getElementsByTagName('a')[0]).innerHTML, 1, followingNodes);
			return false;
		},
		insertNewSiteTreeNodes: function() {
			var li = $(JSTreeObj.floatingContainer.getElementsByTagName('LI')[0]);
			var uls = JSTreeObj.dragNode_destination.getElementsByTagName('UL');
			if (uls.length > 0) {
				ul = $(uls[0]);
				ul.style.display='block';
				
				var lis = ul.getElementsByTagName('LI');
				if (lis.length > 0) {
					//	Sub elements exists - drop dragable node before the first one
					li.injectBefore($(lis[0]));
				}
				else {
					//	No sub exists - use the appendChild method - This line should not be executed unless there's something wrong in the HTML, i.e empty <ul>
					li.injectInside(ul);
				}
			}
			else {
				var ul = new Element('ul');
				ul.style.display='block';
				ul.injectInside($(JSTreeObj.dragNode_destination));
				var childElement = $(JSTreeObj.floatingContainer.getElementsByTagName('LI')[0]);
				childElement.injectInside(ul);
			}
		}
	}
	
	function addEventsToSiteTreeInput(input) {
		input.addEvent('blur', hideEdit);
		input.addEvent('keypress', function(e) {
			e = new Event(e);
			withEnter(e);
		});
	}

	function withEnter(e) {
		if (e.key == 'enter') {
			hideEdit();
			return false;
		}

		if (e.code == 3) {
			hideEdit();
			return false;
		}
		return false;
	}

	function okToNavigate() {
		if (editCounter < 10) {
			return true;
		}
		return false;		
	}
	
	var editCounter = -1;
	var editEl = false;
	var oldSitePageName = null;
	
	function initEditLabel(element) {
		changePageName = true;
		if (saveOnDrop == false) {
			return false;
		}
		if (editEl) {
			hideEdit();
		}
		
		editCounter = 0;
		editEl = element;
		startEditLabel();
	}
	
	function startEditLabel() {
		var el = editEl.previousSibling;
		el.value = editEl.innerHTML;
		oldSitePageName = el.value;
		editEl.style.display = 'none';
		el.style.display = 'inline';
		el.className = 'siteTreeNodeNameEditInputStyle';
		el.select();
		el.focus();
	}
	
	function showUpdate() {
		var message = document.getElementById('ajaxMessage');
		message = replaceHtml(message, ajax.response);
	}
	
	function hideEdit() {
		var editObj = editEl.previousSibling;	
		if (editObj == null) {
			return false;
		}

		var newSitePageName = editObj.value;
		if (newSitePageName != null && newSitePageName.length > 0 && newSitePageName != oldSitePageName) {
			editEl = replaceHtml(editEl, newSitePageName);

			var changeNameId = editObj.id.replace(/[^0-9]/g, '');
			
			showLoadingMessage(CHANGING_THEME);
			LucidEngine.changePageName(changeNameId, newSitePageName, executeActionsAfterSiteTreeInLucidWasChanged);

			changePageTitleInPageInfo(newSitePageName);
			LucidEngine.changePageUri(changeNameId, newSitePageName, true, changePageTitleCallback);
		
			if (!IE) {
				registerActionsOnSiteTreeElement($(editEl));
			}
		}
		
		oldSitePageName = null;
		editEl.style.display='inline';
		editObj.style.display='none';
		editEl = false;			
		editCounter = -1;
		return false;
	}
	
	function mouseUpEvent() {
		editCounter=-1;		
	}
			
	function newTreeNode (nodeId, parentId, nodeName, pageType, templateFile) {
		this.nodeId = nodeId;
		this.parentId = parentId;
		this.nodeName = nodeName;
		this.pageType = pageType;
		this.templateFile = templateFile;
	}
	
	function setFolderPath(path){
		imageFolder = path;
		iconFolder = path + 'pageIcons/';
		initializeTrees();
	}

	function initializeTrees(){
		var treeObj = null;
		for (var i = 0; i < idsOfTrees.length; i++) {
			var treeObj2 = new JSDragDropTree();
			treeObj2.setTreeId(idsOfTrees[i]);
			treeObj2.initTree();			
			treeObj2.expandAll();
		}
 
		var treeObj = new JSDragDropTree();
		treeObj.setTreeId(idsOfAdvancedTrees[0]);
		treeObj.initTree();
		treeObj.checkIfOverTree(idsOfAdvancedTrees[0]);	
		treeObj.getNodeOrders();
		treeObj.expandAll();
	}
	
	function appendIdOfTree(id){
		idsOfTrees.push(id);		
	}

	function appendIdOfAdvancedTree(id){
		idsOfAdvancedTrees.push(id);		
	}
	