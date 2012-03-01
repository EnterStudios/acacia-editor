/*
 * This library is part of the Acacia Editor -
 * an open source inline and form based content editor for GWT.
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.alkacon.acacia.client.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This class is used to start TinyMCE for editing the content of an element.<p>
 * 
 * After constructing the instance, the actual editor is opened using the init() method, and destroyed with the close()
 * method. While the editor is opened, the edited contents can be accessed using the methods of the HasValue interface.  
 */
public final class TinyMCE implements HasValue<String> {

    /** A flag which indicates whether the editor is currently active. */
    protected boolean m_active;

    /** The current content. */
    protected String m_currentContent;

    /** The TinyMCE editor instance. */
    protected JavaScriptObject m_editor;

    /** The handler manager for dispatching events. */
    protected HandlerManager m_handlerManager = new HandlerManager(this);

    /** The DOM ID of the editable element. */
    protected String m_id;

    /** The original HTML content of the editable element. */
    protected String m_originalContent;

    /** The saved CSS text of the inline editable element. */
    protected String m_savedCss;

    /** The editor element. */
    private Element m_element;

    /**
     * Creates a new instance for the given element.<p>
     * 
     * @param element the DOM element
     */
    public TinyMCE(Element element) {

        if (!RootPanel.getBodyElement().isOrHasChild(element)) {
            throw new RuntimeException("Element is not attached to the DOM!");
        }
        m_element = element;
        String id = ensureId(m_element);
        m_id = id;
        checkLibraries();
    }

    /**
     * Creates a new instance for the element with the given id.<p>
     * 
     * @param id the id of a DOM element 
     */
    public TinyMCE(String id) {

        m_id = id;
        checkLibraries();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return m_handlerManager.addHandler(ValueChangeEvent.getType(), handler);
    }

    /**
     * Closes the TinyMCE editor instance.<p> 
     */
    public void close() {

        checkActive();
        removeEditor();
        m_active = false;
        // Removing the editor sets the HTML content of the original DIV, so we have to restore it.  
        setMainElementContent(m_originalContent);
    }

    /**
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        m_handlerManager.fireEvent(event);
    }

    /**
     * Gets the content of the editor instance.<p>
     *  
     * @return the content of the editor instance 
     */
    public native String getContent() /*-{
        var editor = this.@com.alkacon.acacia.client.widgets.TinyMCE::m_editor;
        return editor.getContent();
    }-*/;

    /** 
     * Gets the main editable element.<p>
     * 
     * @return the editable element 
     */
    public native Element getMainElement() /*-{
        var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCE::m_id;
        var mainElement = $wnd.document.getElementById(elementId);
        return mainElement;
    }-*/;

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return getContent();
    }

    /**
     * Initializes the TinyMCE instance.
     */
    public native void init() /*-{
        this.@com.alkacon.acacia.client.widgets.TinyMCE::checkNotActive()();
        this.@com.alkacon.acacia.client.widgets.TinyMCE::m_active = true;

        var self = this;
        var elementId = self.@com.alkacon.acacia.client.widgets.TinyMCE::m_id;
        var iframeId = elementId + "_ifr";
        var mainElement = $wnd.document.getElementById(elementId);
        self.@com.alkacon.acacia.client.widgets.TinyMCE::m_originalContent = mainElement.innerHTML;
        self.@com.alkacon.acacia.client.widgets.TinyMCE::m_currentContent = mainElement.innerHTML;

        $wnd.goog.cssom.iframe.style.resetDomCache();
        self.@com.alkacon.acacia.client.widgets.TinyMCE::m_savedCss = $wnd.goog.cssom.iframe.style
                .getElementContext(mainElement);
        var fireChange = function() {
            self.@com.alkacon.acacia.client.widgets.TinyMCE::fireChange()();
        };

        var fireChangeDelayed = function() {
            // delay because we want TinyMCE to process the event first 
            $wnd
                    .setTimeout(
                            function() {
                                try {
                                    self.@com.alkacon.acacia.client.widgets.TinyMCE::fireChange()();
                                } catch (e) {
                                    var handler = @com.google.gwt.core.client.GWT::getUncaughtExceptionHandler()();
                                    handler.@com.google.gwt.core.client.GWT.UncaughtExceptionHandler::onUncaughtException(Ljava/lang/Throwable;)(e);
                                    throw e;
                                }
                            }, 1);
        };

        $wnd.tinyMCE
                .init({
                    setup : function(ed) {
                        self.@com.alkacon.acacia.client.widgets.TinyMCE::m_editor = ed;

                        ed.onChange.add(fireChange);
                        ed.onKeyDown.add(fireChangeDelayed);
                        ed.onLoad
                                .add(function() {
                                    $wnd.document.getElementById(iframeId).style.minHeight = "400px";
                                    var iframe = $wnd.document
                                            .getElementById(iframeId);
                                    var doc = $wnd.goog.dom
                                            .getFrameContentDocument(iframe);
                                    var domHelper = new $wnd.goog.dom.DomHelper(
                                            doc);
                                    var savedCss = self.@com.alkacon.acacia.client.widgets.TinyMCE::m_savedCss;
                                    $wnd.goog.cssom.addCssText(savedCss,
                                            domHelper);

                                });
                        ed.onClick
                                .add(function() {
                                    self.@com.alkacon.acacia.client.widgets.TinyMCE::fixToolbar()();
                                    self.@com.alkacon.acacia.client.widgets.TinyMCE::propagateClickEvent()();
                                });
                    },
                    // General options
                    mode : "exact",
                    theme : "advanced",
                    elements : self.@com.alkacon.acacia.client.widgets.TinyMCE::m_id,
                    plugins : "autolink,lists,pagebreak,style,layer,table,advhr,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist,autosave",
                    save_onsavecallback : function() {
                    },

                    // Theme options
                    theme_advanced_buttons1 : "bold,italic,underline,strikethrough",
                    theme_advanced_buttons2 : "",
                    theme_advanced_buttons3 : "",
                    theme_advanced_buttons4 : "",
                    theme_advanced_toolbar_location : "external",
                    theme_advanced_toolbar_align : "right",
                    theme_advanced_statusbar_location : "bottom",
                    theme_advanced_resizing : true,

                    // Drop lists for link/image/media/template dialogs
                    template_external_list_url : "lists/template_list.js",
                    external_link_list_url : "lists/link_list.js",
                    external_image_list_url : "lists/image_list.js",
                    media_external_list_url : "lists/media_list.js"

                });
        self.@com.alkacon.acacia.client.widgets.TinyMCE::fixStyles()();
    }-*/;

    /**
     * Sets the content of the TinyMCE editor.<p>
     * 
     * @param newContent the new content 
     */
    public native void setContent(String newContent) /*-{
        var editor = this.@com.alkacon.acacia.client.widgets.TinyMCE::m_editor;
        editor.setContent(newContent);
    }-*/;

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setContent(value);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        setValue(value);
        if (fireEvents) {
            fireChange();
        }
    }

    /**
     * Checks whether the TinyMCE instance is active.<p>
     */
    protected void checkActive() {

        if (!m_active) {
            throw new IllegalStateException("CmsTinyMCE not active!");
        }
    }

    /**
     * Checks whether the necessary Javascript libraries are available by accessing them. 
     */
    protected native void checkLibraries() /*-{
        // fail early if tinymce or goog.cssom.iframe.style is not available
        var w = $wnd;
        var init = w.tinyMCE.init;
        var _ = w.goog.cssom.iframe.style;
    }-*/;

    /**
     * Checks whether the TinyMCE instance is not active.<p>
     */
    protected void checkNotActive() {

        if (m_active) {
            throw new IllegalStateException("CmsTinyMCE already active!");
        }
    }

    /**
     * Gives an element an id if it doesn't already have an id, and then returns the element's id.<p>
     * 
     * @param element the element for which we want to add the id
     *  
     * @return the id 
     */
    protected String ensureId(Element element) {

        String id = element.getId();
        if ((id == null) || "".equals(id)) {
            id = Document.get().createUniqueId();
            element.setId(id);
        }
        return id;
    }

    /**
     * Fires a change event.<p>
     */
    protected void fireChange() {

        String newContent = getContent();
        if (!newContent.equals(m_currentContent)) {
            m_currentContent = newContent;
            ValueChangeEvent.fire(this, newContent);
        }
    }

    /**
     * Fixes the styling of the editor widget.<p>
     */
    protected void fixStyles() {

        // it may take some time until the editor has been initialized, repeat until layout fix can be applied
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
             */
            public boolean execute() {

                Element parent = getEditorParentElement();
                if (parent != null) {
                    String cssClass = getMainElement().getClassName();
                    if ((cssClass != null) && (cssClass.trim().length() > 0)) {
                        parent.addClassName(cssClass);
                    }
                    parent.getStyle().setDisplay(Display.BLOCK);
                    getEditorTableElement().getStyle().setWidth(100, Unit.PCT);
                    return false;
                }
                return true;
            }
        }, 100);
    }

    /**
     * Fixes the layout when the toolbar's top is above the body's top.<p>
     */
    protected void fixToolbar() {

        Element element = getToolbarElement();
        int top = element.getAbsoluteTop();
        if (top < 0) {
            Element parent = (Element)(element.getParentNode());
            parent.getStyle().setMarginTop(-top, Unit.PX);
        }
    }

    /**
     * Returns the editor parent element.<p>
     * 
     * @return the editor parent element
     */
    protected native Element getEditorParentElement() /*-{
        var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCE::m_id;
        var parentId = elementId + "_parent";
        return $doc.getElementById(parentId);
    }-*/;

    /**
     * Returns the editor table element.<p>
     * 
     * @return the editor table element
     */
    protected native Element getEditorTableElement() /*-{
        var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCE::m_id;
        var tableId = elementId + "_tbl";
        return $doc.getElementById(tableId);
    }-*/;

    /**
     * Gets the toolbar element.<p>
     * 
     * @return the toolbar element 
     */
    protected native Element getToolbarElement() /*-{
        var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCE::m_id;
        var toolbarId = elementId + "_external";
        return $doc.getElementById(toolbarId);
    }-*/;

    /**
     * Propagates the click event.<p>
     */
    protected void propagateClickEvent() {

        NativeEvent nativeEvent = Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false);
        m_element.dispatchEvent(nativeEvent);
    }

    /**
     * Removes the editor instance.<p>
     */
    protected native void removeEditor() /*-{
        var editor = this.@com.alkacon.acacia.client.widgets.TinyMCE::m_editor;
        editor.remove();
    }-*/;

    /**
     * Sets the main content of the element which is inline editable.<p>
     * 
     * @param html the new content html 
     */
    protected native void setMainElementContent(String html) /*-{
        var instance = this;
        var elementId = instance.@com.alkacon.acacia.client.widgets.TinyMCE::m_id;
        var mainElement = $wnd.document.getElementById(elementId);
        mainElement.innerHTML = html;
    }-*/;
}
