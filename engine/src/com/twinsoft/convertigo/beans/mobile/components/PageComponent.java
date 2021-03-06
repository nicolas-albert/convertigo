/*
 * Copyright (c) 2001-2018 Convertigo SA.
 * 
 * This program  is free software; you  can redistribute it and/or
 * Modify  it  under the  terms of the  GNU  Affero General Public
 * License  as published by  the Free Software Foundation;  either
 * version  3  of  the  License,  or  (at your option)  any  later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;  without even the implied warranty of
 * MERCHANTABILITY  or  FITNESS  FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */

package com.twinsoft.convertigo.beans.mobile.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.twinsoft.convertigo.beans.common.FormatedContent;
import com.twinsoft.convertigo.beans.common.XMLVector;
import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.DatabaseObject.DboCategoryInfo;
import com.twinsoft.convertigo.beans.core.IContainerOrdered;
import com.twinsoft.convertigo.beans.core.IEnableAble;
import com.twinsoft.convertigo.beans.core.ITagsProperty;
import com.twinsoft.convertigo.beans.core.MobileComponent;
import com.twinsoft.convertigo.beans.mobile.components.UIPageEvent.ViewEvent;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.EngineException;
import com.twinsoft.convertigo.engine.mobile.MobileBuilder;
import com.twinsoft.convertigo.engine.util.EnumUtils;
import com.twinsoft.convertigo.engine.util.XMLUtils;

@DboCategoryInfo(
		getCategoryId = "PageComponent",
		getCategoryName = "Page",
		getIconClassCSS = "convertigo-action-newPageComponent"
	)
public class PageComponent extends MobileComponent implements ITagsProperty, IScriptComponent, IStyleGenerator, ITemplateGenerator, IScriptGenerator, IContainerOrdered, IEnableAble {

	private static final long serialVersionUID = 188562781669238824L;
	
	transient private XMLVector<XMLVector<Long>> orderedComponents = new XMLVector<XMLVector<Long>>();
	
	transient private Runnable _markPageAsDirty;
	
	public PageComponent() {
		super();
		
		this.priority = getNewOrderValue();
		
		orderedComponents = new XMLVector<XMLVector<Long>>();
		orderedComponents.add(new XMLVector<Long>());
	}

	@Override
	public PageComponent clone() throws CloneNotSupportedException {
		PageComponent cloned = (PageComponent) super.clone();
		cloned.vUIComponents = new LinkedList<UIComponent>();
		cloned.pageImports = new HashMap<String, String>();
		cloned.computedContents = null;
		cloned.contributors = null;
		cloned.isRoot = isRoot;
		return cloned;
	}

	transient public boolean isRoot = false;
	
	@Override
	public Element toXml(Document document) throws EngineException {
		Element element = super.toXml(document);
		
		// Storing the page "isRoot" flag
		element.setAttribute("isRoot", new Boolean(isRoot).toString());
        
		return element;
	}
	
	@Override
	public void preconfigure(Element element) throws Exception {
		super.preconfigure(element);
		
		try {
			long priority = new Long(element.getAttribute("priority")).longValue();
			if (priority == 0L) {
				priority = getNewOrderValue();
				element.setAttribute("priority", ""+priority);
			}
			
			NodeList properties = element.getElementsByTagName("property");
			
			// migration of scriptContent from String to FormatedContent
			Element propElement = (Element) XMLUtils.findNodeByAttributeValue(properties, "name", "scriptContent");
			if (propElement != null) {
				Element valueElement = (Element) XMLUtils.findChildNode(propElement, Node.ELEMENT_NODE);
				if (valueElement != null) {
					Document document = valueElement.getOwnerDocument();
					Object content = XMLUtils.readObjectFromXml(valueElement);
					if (content instanceof String) {
						FormatedContent formated = new FormatedContent((String) content);
						Element newValueElement = (Element)XMLUtils.writeObjectToXml(document, formated);
						propElement.replaceChild(newValueElement, valueElement);
						hasChanged = true;
						Engine.logBeans.warn("(PageComponent) 'scriptContent' has been updated for the object \"" + getName() + "\"");
					}
				}
			}
		}
        catch(Exception e) {
            throw new EngineException("Unable to preconfigure the page component \"" + getName() + "\".", e);
        }
	}
	
	@Override
	public void configure(Element element) throws Exception {
		super.configure(element);
		
		try {
			isRoot = new Boolean(element.getAttribute("isRoot")).booleanValue();
		} catch(Exception e) {
			throw new EngineException("Unable to configure the property 'isRoot' of the page \"" + getName() + "\".", e);
		}
	}
	
	public XMLVector<XMLVector<Long>> getOrderedComponents() {
		return orderedComponents;
	}
    
	public void setOrderedComponents(XMLVector<XMLVector<Long>> orderedComponents) {
		this.orderedComponents = orderedComponents;
	}
	
    private void insertOrderedComponent(UIComponent component, Long after) {
    	List<Long> ordered = orderedComponents.get(0);
    	int size = ordered.size();
    	
    	if (ordered.contains(component.priority))
    		return;
    	
    	if (after == null) {
    		after = 0L;
    		if (size > 0)
    			after = ordered.get(ordered.size()-1);
    	}
    	
   		int order = ordered.indexOf(after);
    	ordered.add(order+1, component.priority);
    	hasChanged = !isImporting;
    }
    
    private void removeOrderedComponent(Long value) {
        Collection<Long> ordered = orderedComponents.get(0);
        ordered.remove(value);
        hasChanged = true;
    }
    
	public void insertAtOrder(DatabaseObject databaseObject, long priority) throws EngineException {
		increaseOrder(databaseObject, priority);
	}
    
    private void increaseOrder(DatabaseObject databaseObject, Long before) throws EngineException {
    	List<Long> ordered = null;
    	Long value = new Long(databaseObject.priority);
    	
    	if (databaseObject instanceof UIComponent)
    		ordered = orderedComponents.get(0);
    	
    	if (ordered == null || !ordered.contains(value))
    		return;
    	int pos = ordered.indexOf(value);
    	if (pos == 0)
    		return;
    	
    	if (before == null)
    		before = ordered.get(pos-1);
    	int pos1 = ordered.indexOf(before);
    	
    	ordered.add(pos1, value);
    	ordered.remove(pos+1);
    	hasChanged = true;
    	
    	markPageAsDirty();
    }
    
    private void decreaseOrder(DatabaseObject databaseObject, Long after) throws EngineException {
    	List<Long> ordered = null;
    	long value = databaseObject.priority;
    	
    	if (databaseObject instanceof UIComponent)
    		ordered = orderedComponents.get(0);
    	
    	if (ordered == null || !ordered.contains(value))
    		return;
    	int pos = ordered.indexOf(value);
    	if (pos+1 == ordered.size())
    		return;
    	
    	if (after == null)
    		after = ordered.get(pos+1);
    	int pos1 = ordered.indexOf(after);
    	
    	ordered.add(pos1+1, value);
    	ordered.remove(pos);
    	hasChanged = true;
    	
    	markPageAsDirty();
    }
    
	public void increasePriority(DatabaseObject databaseObject) throws EngineException {
		if (databaseObject instanceof UIComponent)
			increaseOrder(databaseObject,null);
	}

	public void decreasePriority(DatabaseObject databaseObject) throws EngineException {
		if (databaseObject instanceof UIComponent)
			decreaseOrder(databaseObject,null);
	}
    
    /**
     * Get representation of order for quick sort of a given database object.
     */
	@Override
    public Object getOrder(Object object) throws EngineException	{
        if (object instanceof UIComponent) {
        	List<Long> ordered = orderedComponents.get(0);
        	long time = ((UIComponent)object).priority;
        	if (ordered.contains(time))
        		return (long)ordered.indexOf(time);
        	else throw new EngineException("Corrupted component for page \""+ getName() +"\". UIComponent \""+ ((UIComponent)object).getName() +"\" with priority \""+ time +"\" isn't referenced anymore.");
        }
        else return super.getOrder(object);
    }
    
    @Override
    public Object getOrderedValue() {
    	return priority;
    }
	
	/**
	 * The list of available page component for this application.
	 */
	transient private List<UIComponent> vUIComponents = new LinkedList<UIComponent>();
	
	protected void addUIComponent(UIComponent uiComponent, Long after) throws EngineException {
		checkSubLoaded();
		
		boolean isNew = uiComponent.bNew;
		boolean isCut = !isNew && uiComponent.getParent() == null && uiComponent.isSubLoaded;
		
		String newDatabaseObjectName = getChildBeanName(vUIComponents, uiComponent.getName(), uiComponent.bNew);
		uiComponent.setName(newDatabaseObjectName);
		
		vUIComponents.add(uiComponent);
		uiComponent.setParent(this);
		
        insertOrderedComponent(uiComponent, after);
        
        if (isNew || isCut) {
        	markPageAsDirty();
        }
	}
	
	protected void addUIComponent(UIComponent uiComponent) throws EngineException {
		addUIComponent(uiComponent, null);
	}

	protected void removeUIComponent(UIComponent uiComponent) throws EngineException {
		checkSubLoaded();
		
		vUIComponents.remove(uiComponent);
		uiComponent.setParent(null);
		
        removeOrderedComponent(uiComponent.priority);
        
        markPageAsDirty();
	}

	public List<UIComponent> getUIComponentList() {
		checkSubLoaded();
		return sort(vUIComponents);
	}

	public List<UIPageEvent> getUIPageEventList() {
		List<UIPageEvent> eventList = new ArrayList<>();
		for (UIComponent uiComponent : getUIComponentList()) {
			if (uiComponent instanceof UIPageEvent) {
				eventList.add((UIPageEvent) uiComponent);
			}
		}
		return eventList;
	}

	public List<UIEventSubscriber> getUIEventSubscriberList() {
		List<UIEventSubscriber> eventList = new ArrayList<>();
		for (UIComponent uiComponent : getUIComponentList()) {
			if (uiComponent instanceof UIEventSubscriber) {
				eventList.add((UIEventSubscriber) uiComponent);
			}
		}
		return eventList;
	}

	public UIComponent getUIComponentByName(String uiName) throws EngineException {
		checkSubLoaded();
		for (UIComponent uiComponent : vUIComponents)
			if (uiComponent.getName().equalsIgnoreCase(uiName)) return uiComponent;
		throw new EngineException("There is no UI component named \"" + uiName + "\" found into this page.");
	}
	
	@Override
	public List<DatabaseObject> getAllChildren() {	
		List<DatabaseObject> rep = super.getAllChildren();
		rep.addAll(getUIComponentList());
		return rep;
	}

	@Override
	public void add(DatabaseObject databaseObject, Long after) throws EngineException {
		if (databaseObject instanceof UIComponent) {
			addUIComponent((UIComponent) databaseObject, after);
		} else {
			throw new EngineException("You cannot add to a page component a database object of type " + databaseObject.getClass().getName());
		}		
	}
	
	@Override
    public void add(DatabaseObject databaseObject) throws EngineException {
		add(databaseObject, null);
    }

    @Override
    public void remove(DatabaseObject databaseObject) throws EngineException {
		if (databaseObject instanceof UIComponent) {
			removeUIComponent((UIComponent) databaseObject);
		} else {
			throw new EngineException("You cannot remove from a page component a database object of type " + databaseObject.getClass().getName());
		}
		super.remove(databaseObject);
    }
    
    private String segment = "";

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	private String title = "";
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private String icon = "";
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	private String iconPosition = "";
	
	public String getIconPosition() {
		return iconPosition;
	}

	public void setIconPosition(String iconPosition) {
		this.iconPosition = iconPosition;
	}
	
	private boolean inAutoMenu = true;
	
	public boolean isInAutoMenu() {
		return inAutoMenu;
	}

	public void setInAutoMenu(boolean inAutoMenu) {
		this.inAutoMenu = inAutoMenu;
	}

	private String menu = "";
	
	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}
	
	protected String getMenuId() {
		String menuId = "";
		if (!menu.isEmpty() && menu.startsWith(getProject().getName())) {
			try {
				menuId = menu.substring(menu.lastIndexOf('.')+1);
			} catch (IndexOutOfBoundsException e) {}
		}
		return menuId;
	}
	protected FormatedContent scriptContent = new FormatedContent("");

	public FormatedContent getScriptContent() {
		return scriptContent;
	}

	public void setScriptContent(FormatedContent scriptContent) {
		this.scriptContent = scriptContent;
	}
	
	private transient Map<String, String> pageImports = new HashMap<String, String>();
	
	private boolean hasImport(String name) {
		return pageImports.containsKey(name) ||
				getProject().getMobileBuilder().hasPageTplImport(name);
	}
	
	private boolean hasCustomImport(String name) {
		synchronized (scriptContent) {
			String c8o_UserCustoms = scriptContent.getString();
			String importMarker = MobileBuilder.getMarker(c8o_UserCustoms, "PageImport");
			Map<String, String> map = new HashMap<String, String>(10);
			MobileBuilder.initMapImports(map, importMarker);
			return map.containsKey(name);
		}
	}
	
	public boolean addImport(String name, String path) {
		if (name != null && path != null && !name.isEmpty() && !path.isEmpty()) {
			synchronized (pageImports) {
				if (!hasImport(name) && !hasCustomImport(name)) {
					pageImports.put(name, path);
					return true;
				}
			}
		}
		return false;
	}
	
	protected Map<String, Set<String>> getInfoMap() {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		for (UIComponent uiComponent : getUIComponentList()) {
			uiComponent.addInfos(map);
		}
		return map;
	}
	
	private transient List<Contributor> contributors = null;
	
	public List<Contributor> getContributors() {
		if (contributors == null) {
			doGetContributors();
		}
		return contributors;
	}
	
	protected void doGetContributors() {
		contributors = new ArrayList<Contributor>();
		//if (isEnabled()) { // Commented until we can delete page folder again... : see forceEnable in MobileBuilder 
			for (UIComponent uiComponent : getUIComponentList()) {
				uiComponent.addContributors(contributors);
			}
		//}		
	}
	
	private transient JSONObject computedContents = null;
	
	private JSONObject initJsonComputed() {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject()
						.put("scripts", 
								new JSONObject().put("imports", "")
												.put("declarations", "")
												.put("constructors", "")
												.put("functions", ""))
						.put("template", "")
						.put("style", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	public JSONObject getComputedContents() {
		if (computedContents == null) {
			doComputeContents();
		}
		return computedContents;
	}
	
	protected synchronized void doComputeContents() {
		try {
			pageImports.clear();
			JSONObject newComputedContent = initJsonComputed();
			
			JSONObject jsonScripts = newComputedContent.getJSONObject("scripts");
			computeScripts(jsonScripts);
			
			newComputedContent.put("style", computeStyle());
			newComputedContent.put("template", computeTemplate());
			
			computedContents = newComputedContent;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void markPageAsDirty() throws EngineException {
		if (_markPageAsDirty == null) {
			_markPageAsDirty = () -> {
				if (isImporting) {
					return;
				}
				try {
					JSONObject oldComputedContent = computedContents == null ? 
							null :new JSONObject(computedContents.toString());
					
					doComputeContents();
					
					JSONObject newComputedContent = computedContents == null ? 
							null :new JSONObject(computedContents.toString());
					
					if (oldComputedContent != null && newComputedContent != null) {
						if (!(newComputedContent.getJSONObject("scripts").toString()
								.equals(oldComputedContent.getJSONObject("scripts").toString()))) {
							getProject().getMobileBuilder().pageTsChanged(this, true);
						}
					}
					if (oldComputedContent != null && newComputedContent != null) {
						if (!(newComputedContent.getString("style")
								.equals(oldComputedContent.getString("style")))) {
							getProject().getMobileBuilder().pageStyleChanged(this);
						}
					}
					if (oldComputedContent != null && newComputedContent != null) {
						if (!(newComputedContent.getString("template")
								.equals(oldComputedContent.getString("template")))) {
							getProject().getMobileBuilder().pageTemplateChanged(this);
						}
					}
					
					String oldContributors = contributors == null ? null: contributors.toString();
					doGetContributors();
					String newContributors = contributors == null ? null: contributors.toString();
					if (oldContributors != null && newContributors != null) {
						if (!(oldContributors.equals(newContributors))) {
					getProject().getMobileBuilder().appContributorsChanged(this.getApplication());
						}
					}
					
					
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		}
		checkBatchOperation(_markPageAsDirty);
	}
	
	public void markPageTsAsDirty() throws EngineException {
		getProject().getMobileBuilder().pageTsChanged(this, false);
	}
	
	public void markPageEnabledAsDirty() throws EngineException {
		if (isEnabled())
			getProject().getMobileBuilder().pageEnabled(this);
		else
			getProject().getMobileBuilder().pageDisabled(this);
	}

	public String getComputedImports() {
		try {
			return getComputedContents().getJSONObject("scripts").getString("imports");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getComputedDeclarations() {
		try {
			return getComputedContents().getJSONObject("scripts").getString("declarations");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getComputedConstructors() {
		try {
			return getComputedContents().getJSONObject("scripts").getString("constructors");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getComputedFunctions() {
		try {
			return getComputedContents().getJSONObject("scripts").getString("functions");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void computeScripts(JSONObject jsonScripts) {
		// Page menu
		String menuId = getMenuId();
		if (!menuId.isEmpty()) {
			try {
				String constructor = System.lineSeparator() + "\t\tthis.menuId = '" + menuId +"';"
									+ System.lineSeparator() + "\t\t";
				String constructors = jsonScripts.getString("constructors") + constructor;
				jsonScripts.put("constructors", constructors);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		// Page subscribers
		List<UIEventSubscriber> subscriberList = getUIEventSubscriberList();
		if (!subscriberList.isEmpty()) {
			try {
				String declaration = "public static nbInstance = 0;" + System.lineSeparator();
				String declarations = jsonScripts.getString("declarations") + declaration;
				jsonScripts.put("declarations", declarations);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			String nbi = getName() +".nbInstance";
			try {
				String subscribers = UIEventSubscriber.computeConstructors(nbi, subscriberList)
									+ System.lineSeparator() + "\t\t";
				String constructors = jsonScripts.getString("constructors") + subscribers;
				jsonScripts.put("constructors", constructors);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			try {
				String function = UIEventSubscriber.computeNgDestroy(nbi, subscriberList) 
								+ System.lineSeparator() + "\t";
				String functions = jsonScripts.getString("functions") + function;
				jsonScripts.put("functions", functions);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		// Page events
		List<UIPageEvent> eventList = getUIPageEventList();
		if (!eventList.isEmpty()) {
			for (ViewEvent viewEvent: ViewEvent.values()) {
				String computedEvent = viewEvent.computeEvent(eventList);
				if (!computedEvent.isEmpty()) {
					try {
						String function = computedEvent + System.lineSeparator();
						String functions = jsonScripts.getString("functions") + function;
						jsonScripts.put("functions", functions);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// Component typescripts
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			component.computeScripts(jsonScripts);
		}
	}
	
	public String getComputedTemplate() {
		try {
			return getComputedContents().getString("template");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@Override
	public String computeTemplate() {
		StringBuilder sb = new StringBuilder();
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (!(component instanceof UIStyle)) {
				String tpl = component.computeTemplate();
				if (!tpl.isEmpty()) {
					sb.append(tpl).append(System.getProperty("line.separator"));
				}
			}
		}
		return sb.toString();
	}

	public String getComputedStyle() {
		try {
			return getComputedContents().getString("style");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@Override
	public String computeStyle() {
		StringBuilder sb = new StringBuilder();
		sb.append("page-"+ getName().toLowerCase()).append(" {")
			.append(System.getProperty("line.separator"));
		
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (component instanceof UIStyle) {
				String tpl = component.computeTemplate();
				if (!tpl.isEmpty()) {
					sb.append(tpl).append(System.getProperty("line.separator"));
				}
			}
			else if (component instanceof UIElement) {
				String tpl = ((UIElement)component).computeStyle();
				if (!tpl.isEmpty()) {
					sb.append(tpl).append(System.getProperty("line.separator"));
				}
			}
		}
		
		sb.append("}")
			.append(System.getProperty("line.separator"));
		
		return sb.toString();
	}
	
	@Override
	public boolean testAttribute(String name, String value) {
		if (name.equals("isRoot")) {
			Boolean bool = Boolean.valueOf(value);
			return bool.equals(Boolean.valueOf(isRoot));
		}
		if (name.equals("isEnabled")) {
			Boolean bool = Boolean.valueOf(value);
			return bool.equals(Boolean.valueOf(isEnabled()));
		}
		return super.testAttribute(name, value);
	}

	private boolean isEnabled = true;
	
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean updateSmartSource(String oldString, String newString) {
		boolean updated = false;
		for (UIComponent uic : getUIComponentList()) {
			if (uic.updateSmartSource(oldString, newString)) {
				updated = true;
			}
		}
		return updated;
	}
	
	@Override
	public String[] getTagsForProperty(String propertyName) {
		if (propertyName.equals("icon")) {
			return EnumUtils.toStrings(IonIcon.class);
		}
		if (propertyName.equals("iconPosition")) {
			return new String[] {"item-left","item-end","item-right","item-start"};
		}
		return new String[0];
	}
	
	@Override
	public String requiredTplVersion() {
		String tplVersion = getRequiredTplVersion();
		for (UIComponent uic : getUIComponentList()) {
			String uicTplVersion = uic.requiredTplVersion();
			if (MobileBuilder.compareVersions(tplVersion, uicTplVersion) <= 0) {
				tplVersion = uicTplVersion;
			}
		}
		return tplVersion;
	}
	
}
