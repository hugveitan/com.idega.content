/*
 * WebDAVBean.java
 *
 * Created on 15. oktober 2004, 13:11
 */

package com.idega.content.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResources;

import com.idega.business.IBOLookup;
import com.idega.core.data.ICTreeNode;
import com.idega.core.file.business.FileIconSupplier;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.VersionHelper;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.util.FileUtil;

/**
 * @author Roar
 */
public class WebDAVBean extends Object implements ICTreeNode {
    
    public static final String PROP_ID = "id";
    public static final String PROP_NAME = "name";
    public static final String PROP_LENGTH = "length";
    public static final String PROP_IS_COLLECTION = "is_collection";
    public static final String PROP_MODIFIED_DATE = "mod_date";
    public static final String PROP_CREATION_DATE = "cre_date";
    public static final String PROP_MIME = "mime";
    public static final String PROP_WEB_DAV_URL = "webdav_url";
    public static final String PROP_ICON_URL = "icon_url";
    public static final String PROP_VERSION = "version";
    public static final String PROP_LOCKED = "locked";
    
    private int id;
    private String name;
    private long length;
    private boolean isCollection;
    private String modifiedDate;
    private String creationDate;
    private String mime;
    private String webDavUrl;
    private String iconURL;
    private String version;
    private PropertyChangeSupport propertySupport;
    private boolean isLocked = false;
    private boolean isCheckedOut = false;
    private String checkedOutString = null;
    private String comment = null;

	/**
	 * @return Returns the isCheckedOut.
	 */
	public boolean isCheckedOut() {
		return isCheckedOut;
	}
	    
    private WebdavExtendedResource me;
    private ICTreeNode parent;
    private Collection siblings;
    private int siblingCount = 0;
    private Vector children;
    private int childrenCount = 0;
    
    private static int idCounter = 1;
    
    public WebDAVBean() {
    	
    	propertySupport = new PropertyChangeSupport(this);
	    setId((int) (Math.random()*1000));
    }
   
    public WebDAVBean(String name) {
        this();
        setName(name);
    }      
   
    public WebDAVBean(WebdavExtendedResource resource) {
    		this();
			setName(resource.getDisplayName());
			setIsCollection(resource.isCollection());
			setLength(resource.getGetContentLength()); 
			setModifiedDate(resource.getGetLastModified());
			setMime(resource.getGetContentType());
			setCreationDate(resource.getCreationDate());
			setWebDavHttpURL(resource.getPath());
			setVersion(VersionHelper.getLatestVersion(resource));
			setIsLocked(resource.isLocked());
			setCheckedOutString(resource.getCheckedOut());
			setComment(resource.getComment());
    }
        
	public int getId() {
        return id;
    }
    
    public void setId(int value) {
        int oldValue = id;
        id = value;
        propertySupport.firePropertyChange(PROP_ID, oldValue, id);
    }
    
   
    public String getLength() {
        return (!isCollection)? FileUtil.getHumanReadableSize(length) : null;
    }
    
    public void setLength(long value) {
//        long oldValue = length;
        length = value;
//        propertySupport.firePropertyChange(PROP_LENGTH, oldValue, length);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String value) {
        String oldValue = name;
        name = value;
        propertySupport.firePropertyChange(PROP_NAME, oldValue, name);
    }
    
    public String getModifiedDate() {
        return modifiedDate;
    }
    
    public void setModifiedDate(long value) {
    	setModifiedDate(new java.util.Date(value).toString());
    }
    public void setModifiedDate(String value) {
        String oldValue = modifiedDate;
        modifiedDate = value;
        propertySupport.firePropertyChange(PROP_MODIFIED_DATE, oldValue, modifiedDate);
    }
    
    public String getCreationDate() {
      return creationDate;
    }
  
	  public void setCreationDate(long value) {
	  	setCreationDate(new java.util.Date(value).toString());
	  }

	  public void setCreationDate(String value) {
	      String oldValue = creationDate;
	      creationDate = value;
	      propertySupport.firePropertyChange(PROP_CREATION_DATE, oldValue, creationDate);
	  }
    
    public String getMime() {
        return mime;
    }
    
    public void setMime(String value) {
        String oldValue = mime;
        mime = value;
        propertySupport.firePropertyChange(PROP_MIME, oldValue, mime);
    }
    
    public boolean getIsCollection() {
        return isCollection;
    }
    
    public void setIsCollection(boolean value) {
        boolean oldValue = isCollection;
        isCollection = value; 
        propertySupport.firePropertyChange(PROP_IS_COLLECTION, oldValue, isCollection); 
    }
    
    public String getWebDavUrl() {
    		return webDavUrl;
    }
    
    public void setWebDavHttpURL(String webDavUrl) {
	    	String oldValue = this.webDavUrl;
	    	this.webDavUrl = webDavUrl;
	    	propertySupport.firePropertyChange(PROP_WEB_DAV_URL, oldValue, webDavUrl);
    }    
    
    public String getVersion() {
    	return version;
    }
    
    public void setVersion(String version) {
    	String oldVersion = this.version;
    	this.version = version;
    	propertySupport.firePropertyChange(PROP_VERSION, oldVersion, version);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
	public String getIconURL() {
		if(iconURL==null){
			FileIconSupplier iconSupplier = FileIconSupplier.getInstance();
			iconURL = iconSupplier.getFileIconURIByMimeType(mime);
		}
		return iconURL;
	}
	
	
	public void setIconURL(String iconURL) {
	  	String oldValue = this.iconURL;
	  	this.iconURL = iconURL;
	  	propertySupport.firePropertyChange(PROP_ICON_URL, oldValue, iconURL);
	}
	/**
	 * @return Returns the isLocked.
	 */
	public boolean getIsLocked() {
		return isLocked;
	}
	/**
	 * @param isLocked The isLocked to set.
	 */
	public void setIsLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	private WebdavExtendedResource getMe() {
		if (me == null) {
			try {
				IWContext iwc = IWContext.getInstance();
				IWSlideSession ss = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
				WebdavExtendedResource resource = ss.getWebdavResource(getWebDavUrl().replaceFirst(ss.getWebdavServerURI(), ""));
				this.me = resource;
			} catch (Exception e) {
//				e.printStackTrace();
				setName("Error getting resource ("+getWebDavUrl()+")");
			}
		}
		return me;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getChildren()
	 */
	public Collection getChildren() {
		try {
			if (children == null) {
				if (getMe() != null) {
					WebdavResources resources = getMe().getChildResources();
		  		Enumeration enumer = resources.getResources();
		  		children = new Vector();
		  		childrenCount = 0; 
		  		while (enumer.hasMoreElements()) {
		  			WebdavExtendedResource element = (WebdavExtendedResource) enumer.nextElement();
		  			if (element.isCollection()) {
		  				WebDAVBean bean = new WebDAVBean(element);
		    			children.add(bean);
		    			++childrenCount;
		  			}
		  		}
				} else { 
					children = new Vector();
					childrenCount = 0;
				}
			} 
			return children;
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getChildrenIterator()
	 */
	public Iterator getChildrenIterator() {
		return getChildren().iterator();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren() {
		return isCollection;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getChildAtIndex(int)
	 */
	public ICTreeNode getChildAtIndex(int childIndex) {
		if (children == null) {
			getChildren();
		}
		return (ICTreeNode) children.get(childIndex);
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getChildCount()
	 */
	public int getChildCount() {
		if (children == null) {
			getChildren();
		}
		return childrenCount;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getIndex(com.idega.core.data.ICTreeNode)
	 */
	public int getIndex(ICTreeNode node) {
		if (children == null) {
			getChildren();
		}
		return children.indexOf(node);
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getParentNode()
	 */
	public ICTreeNode getParentNode() {
		if (parent == null) {
			try {
				if (getMe() != null) {
					String url = getMe().getParentPath();
					IWContext iwc = IWContext.getInstance();
					IWSlideSession ss = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
					url = url.replaceFirst(ss.getWebdavServerURI(), "");
					WebdavExtendedResource selectedNode = ss.getWebdavResource(url);
					parent = new WebDAVBean(selectedNode);
				} else {
					parent = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return parent;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#isLeaf()
	 */
	public boolean isLeaf() {
		return !getIsCollection();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeName()
	 */
	public String getNodeName() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeName(java.util.Locale)
	 */
	public String getNodeName(Locale locale) {
		return getName();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeName(java.util.Locale, com.idega.idegaweb.IWApplicationContext)
	 */
	public String getNodeName(Locale locale, IWApplicationContext iwac) {
		return getName();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeID()
	 */
	public int getNodeID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getSiblingCount()
	 */
	public int getSiblingCount() {
		return siblingCount;
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		if(comment!=null && !"".equals(comment)){
			return comment;
		}
		else{
			if(isCheckedOut()){
				return getCheckedOutString().substring(getCheckedOutString().lastIndexOf("/")+1);
			}
		}
		return null;
	}
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * @param isCheckedOut The isCheckedOut to set.
	 */
	public void setCheckedOut(boolean isCheckedOut) {
		this.isCheckedOut = isCheckedOut;
	}
	
	/**
	 * @return Returns the checkedOutString.
	 */
	public String getCheckedOutString() {
		return checkedOutString;
	}
	
    /**
	 * @param checkedOut
	 */
	private void setCheckedOutString(String checkedOut) {
		if(checkedOut!=null && !"".equals(checkedOut)){
			setCheckedOut(true);
			checkedOutString = checkedOut;
		}
		
	}

}