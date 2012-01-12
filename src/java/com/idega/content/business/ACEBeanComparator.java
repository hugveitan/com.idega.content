/*
 * $Id: ACEBeanComparator.java,v 1.2 2006/04/09 12:01:55 laddi Exp $
 * Created on 12.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.business;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.idega.content.data.ACEBean;
import com.idega.repository.access.AccessControlEntry;

/**
 *
 *  Last modified: $Date: 2006/04/09 12:01:55 $ by $Author: laddi $
 *
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.2 $
 */
public class ACEBeanComparator implements Comparator<ACEBean> {

	private Collator collator;
	private Locale iLocale;

	private List<String> principalTypePriorityList = null;

	public ACEBeanComparator() {
		this.principalTypePriorityList = new ArrayList<String>();
		this.principalTypePriorityList.add(String.valueOf(AccessControlEntry.PRINCIPAL_TYPE_STANDARD));
		this.principalTypePriorityList.add(String.valueOf(AccessControlEntry.PRINCIPAL_TYPE_OTHER));
		this.principalTypePriorityList.add(String.valueOf(AccessControlEntry.PRINCIPAL_TYPE_ROLE));
		this.principalTypePriorityList.add(String.valueOf(AccessControlEntry.PRINCIPAL_TYPE_GROUP));
		this.principalTypePriorityList.add(String.valueOf(AccessControlEntry.PRINCIPAL_TYPE_USER));
	}

	public ACEBeanComparator(Locale locale) {
		this();
		this.iLocale = locale;
	}

	@Override
	public int compare(ACEBean o1, ACEBean o2) {
		if (o1.getPrincipalType() != o2.getPrincipalType()) {
			return ((this.principalTypePriorityList.indexOf(String.valueOf(o1.getPrincipalType())) >
				this.principalTypePriorityList.indexOf(String.valueOf(o2.getPrincipalType())))?1:-1);
		} else if (this.iLocale != null) {
			this.collator = Collator.getInstance(this.iLocale);
			return this.collator.compare(o1.getPrincipalDisplayName(), o2.getPrincipalDisplayName());
		} else {
			this.collator = Collator.getInstance();
			return this.collator.compare(o1.getPrincipalDisplayName(), o2.getPrincipalDisplayName());
		}
	}
}