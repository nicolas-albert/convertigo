/*
 * Copyright (c) 2001-2017 Convertigo SA.
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
 * $URL$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.twinsoft.convertigo.beans.mobile.components.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IonConfig implements Cloneable {
	
	enum Key {
		action_ts_imports,
		module_ts_imports,
		module_ng_imports,
		module_ng_providers,
		;
	}
	
	private JSONObject jsonConfig;
	
	public IonConfig() {
		jsonConfig = new JSONObject();
	}

	public IonConfig(JSONObject jsonOb) {
		this();
		for (Key k: Key.values()) {
			if (jsonOb.has(k.name())) {
				try {
					jsonConfig.put(k.name(), jsonOb.get(k.name()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public JSONObject getJSONObject() {
		return jsonConfig;
	}
	
	public Map<String, List<String>> getActionTsImports() {
		return getTsImports(Key.action_ts_imports);
	}
	
	public Map<String, List<String>> getModuleTsImports() {
		return getTsImports(Key.module_ts_imports);
	}
	
	public Set<String> getModuleNgImports() {
		return getNgImports(Key.module_ng_imports);
	}
	
	public Set<String> getModuleNgProviders() {
		return getNgImports(Key.module_ng_providers);
	}
	
	protected Map<String, List<String>> getTsImports(Key key) {
		try {
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			JSONArray ar = jsonConfig.getJSONArray(key.name());
			for (int i=0; i<ar.length(); i++) {
				Object ob = ar.get(i);
				if (ob instanceof JSONObject) {
					JSONObject jsonImport = (JSONObject)ob;
					String from = jsonImport.getString("from");
					if (!from.isEmpty()) {
						List<String> list = map.get(from);
						if (list == null) {
							list = new ArrayList<String>();
						}
						
						JSONArray arc = jsonImport.getJSONArray("components");
						for (int j=0; j<arc.length(); j++) {
							String s = arc.getString(j);
							if (!s.isEmpty()) {
								list.add(s);
							}
						}
						
						map.put(from, list);
					}
				}
			}
			return map;
		} catch (JSONException e) {
			return new HashMap<String, List<String>>();
		}
	}
	
	protected Set<String> getNgImports(Key key) {
		try {
			Set<String> set = new HashSet<String>();
			JSONArray ar = jsonConfig.getJSONArray(key.name());
			for (int i=0; i<ar.length(); i++) {
				String s = ar.getString(i);
				if (!s.isEmpty()) {
					set.add(s);
				}
			}
			return set;
		} catch (JSONException e) {
			return new HashSet<String>();
		}
	}
	
	public String toString() {
		return jsonConfig.toString();
	}
}