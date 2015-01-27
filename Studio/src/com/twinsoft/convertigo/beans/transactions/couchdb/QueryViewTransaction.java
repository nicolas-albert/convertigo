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
 * $URL: $
 * $Author: $
 * $Revision: $
 * $Date: $
 */
package com.twinsoft.convertigo.beans.transactions.couchdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twinsoft.convertigo.engine.util.ParameterUtils;

public class QueryViewTransaction extends AbstractDocumentTransaction {

	private static final long serialVersionUID = -3684374492418313635L;

	public QueryViewTransaction() {
		super();
	}

	@Override
	public QueryViewTransaction clone() throws CloneNotSupportedException {
		QueryViewTransaction clonedObject = (QueryViewTransaction) super.clone();
		return clonedObject;
	}
	
	@Override
	public List<CouchDbParameter> getDeclaredParameters() {
		return Arrays.asList(new CouchDbParameter[] {var_database, var_docid, var_viewname, 
				var_view_limit, var_view_skip, var_view_key, var_view_startkey, var_view_endkey});
	}

	@Override
	protected Object invoke() throws Exception {
		String docId = ParameterUtils.toString(getParameterValue(var_docid));
		String viewName = ParameterUtils.toString(getParameterValue(var_viewname));
		
		String key = ParameterUtils.toString(getParameterValue(var_view_key));
		String startkey = ParameterUtils.toString(getParameterValue(var_view_startkey));
		String endkey = ParameterUtils.toString(getParameterValue(var_view_endkey));
		String _limit = ParameterUtils.toString(getParameterValue(var_view_limit));
		String _skip = ParameterUtils.toString(getParameterValue(var_view_skip));
		Integer limit = (_limit == null ? null:Double.valueOf(_limit).intValue());
		Integer skip = (_skip == null ? null:Double.valueOf(_skip).intValue());
		
		Map<String,Object> options = new HashMap<String, Object>();
		options.put(var_view_key.variableName(), key);
		options.put(var_view_startkey.variableName(), startkey);
		options.put(var_view_endkey.variableName(), endkey);
		options.put(var_view_limit.variableName(), limit);
		options.put(var_view_skip.variableName(), skip);
		
		return getCouchDBDocument().view(docId, viewName, options, null);
	}

}