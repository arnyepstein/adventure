/**
 * THis package manages a tabbed page metaphor.  Each page is represented in javascript by an object
 * that has some well-known members:
 *   obj[pageSelector]: a "jquery" selector that represents to container for the page.
 *   obj[onActivate]: A function that is called when the page transitions from visible to invisible
 *   obj[onDeactivate]: A function that is called when the page transitions from invisible to visible
 *   obj[title]: A string containing the page title, or a function that returns such a string.  If
 *     present, the string will update the contents of an element selected by
 *   obj[pageTitleSelector]: a "jquery" selector that represents an element to display the title.
 */


(function() {
	var THIS = {};


	var ACTIVE_PAGE = null;
	var PAGES = {}

	var tabTemplateFunc = function() {/*
	 <div id="<%= id %>" class="tab"><%= title %></div>
	 */};
	var spacerTemplate = "<span class='tabSep'>|</span>";



	// When the application is fully loaded, initialzie each page and
	// register click events on tab text.
	$ae.onAppLoaded(
		function() {
			var tabsEl = $("#tabs");
			var tabTemplate = _.template($ae.fn2str(tabTemplateFunc), null);
			var firstTab = true;
			_.each(
				PAGES,
				function(page, key) {
					// Initialize the page
					if(page.initialize) page.initialize();
					page.setPageKey(key);
					var tabSelectorId = "tab_"+key;
					page.tabSelector = "#" + tabSelectorId;

					// Add a Tab for each page
					var tabTitle = page.tabTitle || key;
					var values = {title: tabTitle, id: tabSelectorId};
					var newEl = $(tabTemplate(values))
					if(! firstTab ) {
						tabsEl.append($(spacerTemplate))
					} else {
						firstTab = false;
					}
					tabsEl.append(newEl);
				}
			)
			// Now register event manager for each tab
			$( ".tab" ).click(function( event ) {
				event.preventDefault();
				event.stopPropagation();
				var id = event.currentTarget.id;
				var pageid = id.substr(4);
				var page = PAGES[pageid];
				$ae.showPage(page);
			});
		}
	);

	var pageFromElement = function(el) {
		var pageEl = el.closest("div[ae_page]");
		return PAGES[pageEl.attr("ae_page")];
	}

	//==============================================================================
	// Base Page
	// This is the base processing for a page.  A page instance is created and registered
	// by calling the $ae.createPage method as follows:
	//    $ae.createPage( pageId, <page code> );
	// When the page is loaded, all registered pages will be invoked at the initialize() method
	//  which must call the "baseInit(blockPrefix, pageSelector)" method where:
	//     pageSelector - is a JQuery selector (typically an '#' selector) to locate the
	//                    top-level page element within the HTML document
	// This enables a page that contains any number of forms and lists and top-level buttons
	// These elements are identified by special attributes in the HTML:
	//	AE_listblock=<listid> - Identifies a list that can be populated and assigns a name
	//			that must be unique among listblock's on the page.
	//
	//	AE_formblock=<formid> - Identifies a form that can be submitted and assigns a name
	//			that must be unique among formblock's on the page.
	//
	//	AE_pagebutton=<buttonid> - Identifies a button that on the page and assigns a name
	//			that must be unique among pagebutton's on the page.

	// Each form managed by the page must
	// displays a list of elements for a set of unique entries and
	// enables editing of an element.  A 'itemListElement' is the containing element for the collection of
	// objects to be administered (typically a list).  An item is the containing element associated
	// with a single object.
	// Members to be defined by subclass
	//   tabSelector - JQuery selector for the tab associated with the page
	//
	//==============================================================================
	var basePage =  {
		pageId: null,
		pageElement: null,
		itemListElement: null,
		currentData: null,
		forms: null,
		lists: null,
		data: null,

		setPageKey: function(key) {
			this.pageElement.attr("AE_page", key);
			this.pageId = key;
		},

		baseInit: function(pageSelector) {
			// Initialize per-instance maps
			this.forms = {};
			this.lists = {};
			this.data = {};

			var pageElement = this.pageElement = $(pageSelector)
			var me = this;


			// Initialize the Top Level Buttons
			pageElement.find("button[AE_pagebutton]").each(
				function() {
					$(this).click(function (event) {
						event.preventDefault();
						event.stopPropagation();
						var el = $(event.currentTarget);
						var page = pageFromElement(el);
						var method = "on" + el.attr("AE_pagebutton") + "Clicked";
						if(page[method]) {
							page[method](el);
						} else {
							console.log("No action; missing method: "+method);
						}
					})
				}
			);

			pageElement.find("form[AE_form]").each(
				function() {
					// Register the form in our list
					var form = $(this);
					var formid = form.attr("AE_form");
					me.forms[formid] = form;

					// Activate the Cancel Button
					form.find("button[name=cancel]").click(
						function(event) {
							var page = pageFromElement($(event.currentTarget));
							var method = "onCancel"+formid+"Form";
							event.preventDefault();
							event.stopPropagation();
							page.hideEditor();
							if(page[method])
							{
								page[method](form);
							}
						}
					)
					// Active the submit handler
					form.submit(
						function( event ) {
							var method = "onSubmit"+formid+"Form";
							event.preventDefault();
							event.stopPropagation();
							var page = pageFromElement($(event.currentTarget));
							if(page[method]) {
								page[method](form);
							} else {
								console.log("No action; missing method: "+method);
							}
						}
					);

				}
			);

			pageElement.find("div[AE_listblock]").each(
				function() {
					var list = $(this);
					var listid = list.attr("AE_listblock");
					me.lists[listid] = { el: list};
				}
			);
		},
		// --------------------------------------------------------------
		findForm: function(formid) {
			return this.forms[formid];
		},
		// --------------------------------------------------------------
		// Each entry in the 'lists' object contains 3 members:
		//  list.data - is a list of items received from the server
		//  list.el - the jquery element on the page where the list is displayed
		//  list.index - a map if the items (pk -> item)
		_findList: function(listid) {
			var list = this.lists[listid];
			if(list == null) {
				console.info("No such list ("+listid+")");
				return {};
			}
			return list;
		},
		// --------------------------------------------------------------
		setListData: function(listid, data, keyFieldName) {
			var list = this._findList(listid)
			list.data = data;
			var newIndex = {}
			_.each(
				data,
				function (item, index, list) {
					newIndex[item[keyFieldName]] = item;
				}
			);
			list.index = newIndex;
		},
		// --------------------------------------------------------------
		getListData: function(listid) {
			return this._findList(listid).data
		},
		// --------------------------------------------------------------
		getListElement: function(listid) {
			return this._findList(listid).el;
		},
		// --------------------------------------------------------------
		getListIndex: function(listid) {
			return this._findList(listid).index;
		},
		// --------------------------------------------------------------
		getListItem: function(listid, itemKey) {
			var index = this.getListIndex(listid);
			return index ? index[itemKey] : null;
		},
		// --------------------------------------------------------------
		findItemElementForKey: function(listId, key) {
			var list = this.getListElement(listId);
			return list.find("div[AE_listitem='"+key+"']");
		},
		// --------------------------------------------------------------
		updateListData: function(items, itemListId, keyFieldName) {
			var me = this;

			this.setListData(itemListId, items, keyFieldName);
			// Get the Lists JQuery Element and item name prefix
			var itemListElement = this.getListElement(itemListId);

			// Remove all previous blocks
			itemListElement.find("div[AE_listitem]").remove();

			// Add back rows
			_.each(items,
				function (item, index, list) {
					var values = {item: item, rowNum: index};
					var itemTemplate = me.getTemplateForItem(itemListId, values);
					var newEl = $(itemTemplate(values))
					itemListElement.append(newEl);
					// newEl.attr("AE_listitem", itemPrefix + items[itemKey]);
					newEl.attr("AE_listitem", item[keyFieldName]);

					// Now register all Links within the item
					newEl.find("button[AE_itemaction]").click(function(event) {
						event.preventDefault();
						event.stopPropagation();
						var actionid = $(this).attr("AE_itemaction");
						var method = "on" + itemListId + actionid + "Clicked";
						//var appId = $(this).closest(itemSelector)[0].id;
						// var itemKey = me.keyFromItemId(itemListId, newEl.attr("AE_listitem"));
						if(me[method]) {
							me[method].call(me, item, item[keyFieldName]);
							// me[method].call(me, items[itemKey], itemKey);
						} else {
							console.log("No action; missing method: "+method);
						}
					});

					//body = body + me.blockTemplate(values);
				}
			);
		},
		// --------------------------------------------------------------
		blockBeingEdited:null,
		visibleEditor:null,
		// --------------------------------------------------------------
		showEditor: function(formId, listId, itemKeyForPositioning) {
			this.hideEditor();
			var editor = this.findForm(formId);
			// If listId is missing, it is a top-level form (like Add).
			if(listId) {
				block = this.findItemElementForKey(listId, itemKeyForPositioning);
				if(block) {
					editor.insertAfter(block);
					$ae.hide(block);
					this.blockBeingEdited = block;
				}
			}
			this.visibleEditor = editor;
			$ae.show(editor);
			return editor;
		},

		// --------------------------------------------------------------
		hideEditor: function() {
			if(this.blockBeingEdited) {
				$ae.show(this.blockBeingEdited);
				this.blockBeingEdited = null;
			}
			if(this.visibleEditor) {
				$ae.hide(this.visibleEditor);
				this.visibleEditor = null;
			}
		},
		// --------------------------------------------------------------
		populateFormFromData: function (formId, formData) {
			var form = this.forms[formId];
			this.walkObject(form, "", formData);
		},
		// --------------------------------------------------------------
		walkObject: function(form, prefix, obj) {
			var isArray = $.isArray(obj);
			_.each(
				obj,
				function(val, key) {
					var name;
					if(isArray)
						name = prefix + "[" + key + "]";
					else
						name = (prefix ? prefix + "." : prefix) + key;
					if(typeof(val) == "object" || typeof(val) == "array") {
						this.walkObject(form, name, val);
					} else {
						// console.log(name + " = " + val);
						var sel = "input[name='"+name+"']";
						var ctl = form.find(sel);
						if(ctl.length && ctl[0].type == "radio") {
							ctl.each(
								function(index, element) {
									if(element.value == val) {
										$(element).trigger("click");
									}
								}
							);
						} else {
							ctl.val(val);
						}
						// Also substitute into AE_name elements
						sel = "[AE_name='"+name+"']";
						form.find(sel).text(val);
					}
				},
				this
			)
		}
	}

	// --------------------------------------------------------------
	THIS.createPage = function(pageid, pagedef) {
		var page = _.extend({}, basePage, pagedef);
		PAGES[pageid] = page;
	}
	// --------------------------------------------------------------

	/**
	 * Shows the requested page and hides the currently viewed page.  If the requested page is
	 * the currently viewed page, then no action is taken
	 * @param page The page to show, or a string containing the name of the page to show.
	 * @returns true if the viewed page changed, false if not
	 */
	THIS.showPage = function(page) {
		if((typeof page) == "string") {
			page = PAGES[page];
		}
		if(page == ACTIVE_PAGE) {
			return false;
		}
		if(ACTIVE_PAGE) {
			$ae.hide(ACTIVE_PAGE.pageElement);
			if(ACTIVE_PAGE.onDeactivate) {
				ACTIVE_PAGE.onDeactivate();
			}
			$( ".tab").removeClass("tabActive");
		}
		ACTIVE_PAGE = page;
		if(page) {
			$ae.show(ACTIVE_PAGE.pageElement);
			var title = $ae.getPropertyValue(page.title);
			$("#pageTitle").text(title);
			$(page.tabSelector).addClass("tabActive");
			if(ACTIVE_PAGE.onActivate) {
				ACTIVE_PAGE.onActivate();
			}
		}
		return true;
	}

	window.$ae = _.extend({}, window.$ae, THIS);


}).call(this);