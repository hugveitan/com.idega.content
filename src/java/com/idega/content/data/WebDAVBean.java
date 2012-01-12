/*
 * WebDAVBean.java
 *
 * Created on 15. oktober 2004, 13:11
 */

package com.idega.content.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.idega.core.data.ICTreeNode;
import com.idega.core.file.business.FileIconSupplier;
import com.idega.core.uri.IWActionURIManager;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.repository.RepositoryService;
import com.idega.repository.bean.RepositoryItem;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 * @author Roar
 */
public class WebDAVBean implements ICTreeNode, Serializable {

	private static final long serialVersionUID = -7214381001863096621L;

	public static final String PROP_ID = "id";
    public static final String PROP_NAME = "name";
    public static final String PROP_LENGTH = "length";
    public static final String PROP_IS_COLLECTION = "is_collection";
    public static final String PROP_MODIFIED_DATE = "mod_date";
    public static final String PROP_MODIFIED_DATE_LONG = "mod_date_long";
    public static final String PROP_CREATION_DATE = "cre_date";
    public static final String PROP_MIME = "mime";
    public static final String PROP_WEB_DAV_URL = "webdav_url";
    public static final String PROP_ICON_URL = "icon_url";
    public static final String PROP_VERSION = "version";
    public static final String PROP_LOCKED = "locked";
    public static final String PROP_ENCODED_URL = "enc_url";

    private int id;
    private String name;
    private long length;
    private boolean isCollection;
    private String modifiedDate;
    private Long modifiedDateLong;
    private String creationDate;
    private String mime;
    private String webDavUrl;
    private String iconURL;
    private String version;
    private String encodedUrl;

    private PropertyChangeSupport propertySupport;
    private boolean isLocked = false;
    private boolean isCheckedOut = false;
    private String checkedOutString = null;
    private String comment = null;
    private String iconTheme = null;
    private boolean real = true;

    private RepositoryItem me;
    private ICTreeNode parent;
    private int siblingCount = 0;
    private List<RepositoryItem> children;
    private int childrenCount = -1;

    private String previewActionURI;
	private String permissionActionURI;
	private boolean renderPermissionLink = true;

	/**
	 * @return Returns the isCheckedOut.
	 */
	public boolean isCheckedOut() {
		return this.isCheckedOut;
	}

    public WebDAVBean() {
    	this.propertySupport = new PropertyChangeSupport(this);
	    setId((int) (Math.random()*1000));
    }

    public WebDAVBean(String name) {
        this();
        setName(name);
    }

    public WebDAVBean(RepositoryItem resource) {
    		this();
    		try {
    			String name = resource.getName();
				setName(name);
				setIsCollection(resource.isCollection());
				setLength(resource.getLength());
				setModifiedDate(resource.getLastModified());
				setMime(resource.getMimeType());
				setCreationDate(resource.getCreationDate());
				setWebDavHttpURL(resource.getPath());
				//	FIXME: finish implementation
//				setVersion(VersionHelper.getLatestVersionName(resource));

	//			try {
	//				setVersion(VersionHelper.getLatestVersion(resource.getEncodedPath()));
	//			}
	//			catch (HttpException e) {
	//				e.printStackTrace();
	//			}
	//			catch (RemoteException e) {
	//				e.printStackTrace();
	//			}
	//			catch (IOException e) {
	//				e.printStackTrace();
	//			}

				//	TODO
	//			setIsLocked(resource.isLocked());
	//			setCheckedOutString(resource.getCheckedOut());
	//			setComment(resource.getComment());
	//			setEncodedURL(resource.getEncodedPath());

				//action uri for preview
				setPreviewActionURI(IWActionURIManager.getInstance().getActionURIPrefixWithContext("preview",getEncodedURL()));
				setPermissionActionURI(IWActionURIManager.getInstance().getActionURIPrefixWithContext("permission",getEncodedURL()));
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    }

	@Override
	public String getId() {
        return Integer.toString(getNodeID());
    }

    public void setId(int value) {
        int oldValue = this.id;
        this.id = value;
        this.propertySupport.firePropertyChange(PROP_ID, oldValue, this.id);
    }

    public long getLengthLong() {
    	return this.length;
    }

    public String getLength() {
        return (!this.isCollection)? FileUtil.getHumanReadableSize(this.length) : " ";
    }

    public void setLength(long value) {
//        long oldValue = length;
        this.length = value;
//        propertySupport.firePropertyChange(PROP_LENGTH, oldValue, length);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        String oldValue = this.name;
        this.name = value;
        this.propertySupport.firePropertyChange(PROP_NAME, oldValue, this.name);
    }

    public String getEncodedURL() {
    	return this.encodedUrl;
    }

    public void setEncodedURL(String value) {
    	String old = this.encodedUrl;
    	this.encodedUrl = value;
    	this.propertySupport.firePropertyChange(PROP_ENCODED_URL, old, this.encodedUrl);
    }

    public String getPreviewActionURI() {
    		return this.previewActionURI;
    }

    public void setPreviewActionURI(String value) {
	    	String old = this.previewActionURI;
	    	this.previewActionURI = value;
	    	this.propertySupport.firePropertyChange(PROP_ENCODED_URL, old, this.previewActionURI);
    }

    public String getPermissionActionURI() {
			return this.permissionActionURI;
	}

	public void setPermissionActionURI(String value) {
	    	String old = this.permissionActionURI;
	    	this.permissionActionURI = value;
	    	this.propertySupport.firePropertyChange(PROP_ENCODED_URL, old, this.permissionActionURI);
	}

	public boolean getRenderPermissionLink(){
		if (getIsFile() && this.renderPermissionLink) {
			Logger.getLogger(getClass().getName()).warning("Not implemented!");
			return true;	//	TODO
//			try {
//				IWContext iwc = IWContext.getInstance();
//				IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
//				return session.hasPermission(getEncodedURL(),IWSlideConstants.PRIVILEGE_READ_ACL);
//			}
//			catch (IBOLookupException e) {
//				e.printStackTrace();
//			}
//			catch (UnavailableIWContext e) {
//				e.printStackTrace();
//			}
//			catch (RemoteException e) {
//				e.printStackTrace();
//			}
		}
		return false;
	}

	public void setRenderPermissionLink(boolean value){
		this.renderPermissionLink = value;
	}

    public String getModifiedDate() {
        return this.modifiedDate;
    }

    public long getModifiedDateLong() {
    	if (this.modifiedDateLong != null) {
    		return this.modifiedDateLong.longValue();
    	} else {
    		return 0;
    	}
    }

    public boolean getIsReal() {
    	return this.real;
    }

    public void setIsReal(boolean isReal) {
    	this.real = isReal;
    }

    public void setModifiedDate(long value) {
    	Long oldValue = this.modifiedDateLong;
    	this.modifiedDateLong = new Long(value);
        this.propertySupport.firePropertyChange(PROP_MODIFIED_DATE_LONG, oldValue, this.modifiedDateLong);

        setModifiedDate(new java.util.Date(value).toString());
    }

    public void setModifiedDate(String value) {
        String oldValue = this.modifiedDate;
        this.modifiedDate = value;
        this.propertySupport.firePropertyChange(PROP_MODIFIED_DATE, oldValue, this.modifiedDate);
    }



    public String getCreationDate() {
      return this.creationDate;
    }

	  public void setCreationDate(long value) {
	  	setCreationDate(new java.util.Date(value).toString());
	  }

	  public void setCreationDate(String value) {
	      String oldValue = this.creationDate;
	      this.creationDate = value;
	      this.propertySupport.firePropertyChange(PROP_CREATION_DATE, oldValue, this.creationDate);
	  }

    public String getMime() {
        return this.mime;
    }

    public void setMime(String value) {
        String oldValue = this.mime;
        this.mime = value;
        this.propertySupport.firePropertyChange(PROP_MIME, oldValue, this.mime);
    }

    public boolean getIsCollection() {
        return this.isCollection;
    }

    public boolean getIsFile() {
    	return !getIsCollection();
    }

    public void setIsCollection(boolean value) {
        boolean oldValue = this.isCollection;
        this.isCollection = value;
        this.propertySupport.firePropertyChange(PROP_IS_COLLECTION, oldValue, this.isCollection);
    }

    public String getWebDavUrl() {
    		return this.webDavUrl;
    }

    public void setWebDavHttpURL(String webDavUrl) {
	    	String oldValue = this.webDavUrl;
	    	this.webDavUrl = webDavUrl;
	    	this.propertySupport.firePropertyChange(PROP_WEB_DAV_URL, oldValue, webDavUrl);
    }

    public String getVersion() {
    	if (this.version == null) {
    		this.version = " ";
    	}
    	return this.version;
    }

    public void setVersion(String version) {
    	String oldVersion = this.version;
    	this.version = version;
    	this.propertySupport.firePropertyChange(PROP_VERSION, oldVersion, version);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertySupport.removePropertyChangeListener(listener);
    }

	public String getIconURL() {
		if(this.iconURL==null){
			if (this.iconTheme == null) {
				FileIconSupplier iconSupplier = FileIconSupplier.getInstance();
				this.iconURL = iconSupplier.getFileIconURIByMimeType(this.mime);
			} else {
				FileIconSupplier iconSupplier = FileIconSupplier.getInstance(this.iconTheme);
				this.iconURL = iconSupplier.getFileIconURIByMimeType(this.mime);
			}
		}
		return this.iconURL;
	}


	public void setIconURL(String iconURL) {
	  	String oldValue = this.iconURL;
	  	this.iconURL = iconURL;
	  	this.propertySupport.firePropertyChange(PROP_ICON_URL, oldValue, iconURL);
	}
	/**
	 * @return Returns the isLocked.
	 */
	public boolean getIsLocked() {
		return this.isLocked;
	}

	public boolean getIsUnlocked() {
		return !getIsLocked();
	}
	/**
	 * @param isLocked The isLocked to set.
	 */
	public void setIsLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	private RepositoryItem getMe() {
		if (this.me == null) {
			try {
				RepositoryItem resource = getRepositoryService().getRepositoryItem(
						getWebDavUrl().replaceFirst(getRepositoryService().getWebdavServerURL(), CoreConstants.EMPTY));
				this.me = resource;
			} catch (Exception e) {
//				e.printStackTrace();
				setName("Error getting resource ("+getWebDavUrl()+")");
			}
		}
		return this.me;
	}

	@Override
	public List<RepositoryItem> getChildren() {
		if (children == null) {
			RepositoryItem resource = getMe();
			Collection<RepositoryItem> children = resource.getChildResources();
			if (children == null)
				this.children = Collections.emptyList();
			else
				this.children = new ArrayList<RepositoryItem>(children);
		}
		childrenCount = children.size();
		return children;
//		try {
//			if (this.children == null) {
//				if (getMe() != null) {
//					WebdavResources resources = getMe().getChildResources();
//		  		Enumeration enumer = resources.getResources();
//		  		this.children = new Vector();
//		  		this.childrenCount = 0;
//		  		while (enumer.hasMoreElements()) {
//		  			WebdavExtendedResource element = (WebdavExtendedResource) enumer.nextElement();
//		  			if (element.isCollection()) {
//		  				WebDAVBean bean = new WebDAVBean(element);
//		    			this.children.add(bean);
//		    			++this.childrenCount;
//		  			}
//		  		}
//				} else {
//					this.children = new Vector();
//					this.childrenCount = 0;
//				}
//			}
//			return this.children;
//		} catch (HttpException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
	}

	public void setIconTheme(String theme) {
		this.iconTheme = theme;
	}

	@Override
	public Iterator<RepositoryItem> getChildrenIterator() {
		return getChildren().iterator();
	}

	@Override
	public boolean getAllowsChildren() {
		return this.isCollection;
	}

	@Override
	public ICTreeNode getChildAtIndex(int childIndex) {
		if (this.children == null) {
			getChildren();
		}
		if (children == null) {
			return null;
		}

		List<ICTreeNode> tmp = new ArrayList<ICTreeNode>();
		return tmp.get(childIndex);
	}

	@Override
	public int getChildCount() {
		if (this.children == null) {
			getChildren();
		}
		return this.childrenCount;
//		System.out.print("Children for "+webDavUrl);
//		try {
//			if (childrenCount < 0) {
//				if (getMe() != null) {
//					WebdavResources resources = getMe().getChildResources();
//		  		Enumeration enumer = resources.getResources();
//		  		childrenCount = 0;
//		  		while (enumer.hasMoreElements()) {
//	    			++childrenCount;
//		  		}
//				} else {
//					System.out.println(" = 0 (getMe() == null)");
//					return 0;
//				}
//			}
//			System.out.println(" = "+childrenCount);
//			return childrenCount;
//		} catch (HttpException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return 0;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getIndex(com.idega.core.data.ICTreeNode)
	 */
	@Override
	public int getIndex(ICTreeNode node) {
		if (this.children == null) {
			getChildren();
		}
		return this.children.indexOf(node);
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getParentNode()
	 */
	@Override
	public ICTreeNode getParentNode() {
		if (this.parent == null) {
			try {
				if (getMe() != null) {
					String url = getMe().getParentPath();
					url = url.replaceFirst(getRepositoryService().getWebdavServerURL(), CoreConstants.EMPTY);
					RepositoryItem selectedNode = getRepositoryService().getRepositoryItem(url);
					this.parent = new WebDAVBean(selectedNode);
				} else {
					this.parent = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.parent;
	}

	@Override
	public boolean isLeaf() {
		return !getIsCollection();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeName(java.util.Locale)
	 */
	@Override
	public String getNodeName(Locale locale) {
		return getName();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeName(java.util.Locale, com.idega.idegaweb.IWApplicationContext)
	 */
	@Override
	public String getNodeName(Locale locale, IWApplicationContext iwac) {
		return getName();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getNodeID()
	 */
	@Override
	public int getNodeID() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.data.ICTreeNode#getSiblingCount()
	 */
	@Override
	public int getSiblingCount() {
		return this.siblingCount;
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		if (!StringUtil.isEmpty(this.comment)) {
			return this.comment;
		} else {
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
		return this.checkedOutString;
	}

    /**
	 * @param checkedOut
	 */
	private void setCheckedOutString(String checkedOut) {
		if(checkedOut!=null && !"".equals(checkedOut)){
			setCheckedOut(true);
			this.checkedOutString = checkedOut;
		}

	}

	RepositoryService getRepositoryService() {
		RepositoryService service = ELUtil.getInstance().getBean(RepositoryService.class);
		return service;
	}

}
