/*
 * Copyright (c) 2001-2011 Convertigo SA.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 * $URL: http://sourceus/svn/convertigo/CEMS_opensource/trunk/Studio/src/com/twinsoft/convertigo/beans/connectors/HttpConnectorBeanInfo.java $
 * $Author: nicolasa $
 * $Revision: 37955 $
 * $Date: 2014-09-08 15:27:13 +0200 (lun., 08 sept. 2014) $
 */

package com.twinsoft.convertigo.beans.connectors;

import java.beans.PropertyDescriptor;

import com.twinsoft.convertigo.beans.core.MySimpleBeanInfo;

public class FullSyncConnectorBeanInfo extends MySimpleBeanInfo {
    
	public FullSyncConnectorBeanInfo() {
		try {
			beanClass = FullSyncConnector.class;
			additionalBeanClass = com.twinsoft.convertigo.beans.connectors.CouchDbConnector.class;

			iconNameC16 = "/com/twinsoft/convertigo/beans/connectors/images/fullsyncconnector_color_16x16.png";
			iconNameC32 = "/com/twinsoft/convertigo/beans/connectors/images/fullsyncconnector_color_32x32.png";

			resourceBundle = getResourceBundle("res/FullSyncConnector");

			displayName = getExternalizedString("display_name");
			shortDescription = getExternalizedString("short_description");

			properties = new PropertyDescriptor[4];
			
			properties[0] = new PropertyDescriptor("databaseName", beanClass, "getDatabaseName", "setDatabaseName");
			properties[0].setDisplayName(getExternalizedString("property.databaseName.display_name"));
			properties[0].setShortDescription(getExternalizedString("property.databaseName.short_description"));

			properties[1] = new PropertyDescriptor("https", beanClass, "isHttps", "setHttps");
			properties[1].setDisplayName(getExternalizedString("property.https.display_name"));
			properties[1].setShortDescription(getExternalizedString("property.https.short_description"));
			
			properties[2] = new PropertyDescriptor("port", beanClass, "getPort", "setPort");
			properties[2].setDisplayName(getExternalizedString("property.port.display_name"));
			properties[2].setShortDescription(getExternalizedString("property.port.short_description"));
			
			properties[3] = new PropertyDescriptor("server", beanClass, "getServer", "setServer");
			properties[3].setDisplayName(getExternalizedString("property.server.display_name"));
			properties[3].setShortDescription(getExternalizedString("property.server.short_description"));
			
			getPropertyDescriptor("https").setHidden(true);
			getPropertyDescriptor("port").setHidden(true);
			getPropertyDescriptor("server").setHidden(true);
		}
		catch(Exception e) {
			com.twinsoft.convertigo.engine.Engine.logBeans.error("Exception with bean info; beanClass=" + beanClass.toString(), e);
		}
	}

}

