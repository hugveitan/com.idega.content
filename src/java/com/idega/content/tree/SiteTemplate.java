package com.idega.content.tree;

import java.io.Serializable;
import java.util.ArrayList;

public class SiteTemplate extends Template implements Serializable {
	
	private static final long serialVersionUID = 6186247784825422897L;
	
	private ArrayList <SiteTemplate> childStructure = new ArrayList <SiteTemplate> ();
	
	public SiteTemplate() {
		super();
	}

	public ArrayList<SiteTemplate> getChildStructure() {
		return childStructure;
	}

	public void setChildStructure(ArrayList<SiteTemplate> childStructure) {
		this.childStructure = childStructure;
	}
	
	public void addChild (SiteTemplate child) {
		childStructure.add(child);
	}

}