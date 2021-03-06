<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:h="http://java.sun.com/jsf/html"
        xmlns:f="http://java.sun.com/jsf/core"
        xmlns:wf="http://xmlns.idega.com/com.idega.webface"
        xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
        xmlns:b="http://xmlns.idega.com/com.idega.builder"
        xmlns:x="http://myfaces.apache.org/tomahawk"
version="1.2">
<jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
	<f:view>
        <ws:page id="createpage">
                <h:form id="createpageform">
					<wf:wfblock id="createPageBlock" title="#{localizedStrings['com.idega.content']['create_page']}">
						<f:facet name="wf_block_toolbar">
							<wf:toolbar id="toolbar">
								<wf:toolbarbutton id="button1" displayText="#{localizedStrings['com.idega.content']['createpage.Create']}/#{localizedStrings['com.idega.content']['createpage.Edit']}" styleClass="page_create_edit_link"/>
								
							   	<h:commandLink id="saveCommand" action="save" actionListener="#{pageCreationBean.processAction}" styleClass="page_save_link">
			                    		<h:outputText value="#{localizedStrings['com.idega.content']['createpage.Save']}"/>
							   	</h:commandLink>
							   
							</wf:toolbar>
						</f:facet>
						
						<wf:container id="create_page_fieldset" styleClass="create_page_fieldset">
						
						<h:panelGroup id="createPagePanelGroup">						

						<h:outputLabel for="pageType">
						  <h:outputText id="pageTypeLabel" value="Page Type: " styleClass="label"/>
						</h:outputLabel>
						<h:selectOneMenu id="pageType" value="#{pageCreationBean.templateIdentifier}" styleClass="formInput">
							<f:selectItems value="#{pageCreationBean.simpleTemplateSelectItemList}" />
						</h:selectOneMenu>	


						<h:outputLabel for="pageName">
						  <h:outputText id="pageNameLabel" value="Page Name: " styleClass="label"/>
						</h:outputLabel>
						<h:inputText id="pageName" value="#{pageCreationBean.pageName}" styleClass="formInput"/>
						

						<h:outputLabel for="pageLocation">
						  <h:outputText id="locationLabel" value="Location: " styleClass="label"/>
						</h:outputLabel>
						<h:inputText id="pageLocation" value="#{pageCreationBean.selectedPageLocationName}" styleClass="formInput" disabled="true"/>
						
						<h:inputHidden id="selectedPageLocationName" value="#{pageCreationBean.selectedPageLocationName}" />

						<h:inputHidden id="selectedPageLocationIdentifier" value="#{pageCreationBean.selectedPageLocationIdentifier}" />
						
						<wf:container id="page_tree_div" styleClass="page_tree">
						<x:tree2 value="#{pageCreationBean.pageSelectorTopNode}" id="page_chooser" var="node" varNodeToggler="t" clientSideToggle="false">
 							<f:facet name="PageTreeNode">
			                  <h:panelGroup>
			                    <h:commandLink immediate="true" action="#{t.toggleExpanded}">
			                      <h:graphicImage value="#{pageCreationBean.coreBundle.resourcesPath}/treeviewer/ui/iw/treeviewer_node_leaf.gif"/>
			                    </h:commandLink>
							   <h:commandLink  onclick="document.forms['createpageform'].elements['createpageform:selectedPageLocationIdentifier'].value='#{node.identifier}';document.forms['createpageform'].elements['createpageform:selectedPageLocationName'].value='#{node.description}';document.forms['createpageform'].elements['createpageform:pageLocation'].value='#{node.description}'">
			                    	 <h:outputText value="#{node.description}" styleClass="nodeFolder"/>
							   </h:commandLink>
			                  </h:panelGroup>
			              	</f:facet>
			              </x:tree2>
						</wf:container>

						<h:selectOneMenu id="relativeLocation" value="#{pageCreationBean.relativeLocation}" styleClass="formInput">
							<f:selectItem itemValue="before" itemLabel="Before"/>
							<f:selectItem itemValue="under" itemLabel="Under"/>
						</h:selectOneMenu>
						
						</h:panelGroup>	
						
						</wf:container>
						
					</wf:wfblock>
                </h:form>
        </ws:page>
	</f:view>
</jsp:root>