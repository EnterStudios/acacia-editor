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

package com.alkacon.acacia.client.ui;

import com.alkacon.acacia.client.ButtonBarHandler;
import com.alkacon.acacia.client.ChoiceMenuEntryBean;
import com.alkacon.acacia.client.I_WidgetService;
import com.alkacon.acacia.client.css.I_LayoutBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * A menu entry widget for selecting choices for complex choice values.<p>
 */
public class ChoiceMenuEntryWidget extends Composite {

    /** The root attribute choice menu to which this entry belongs. */
    private AttributeChoiceWidget m_attributeChoiceWidget;

    /** The bean to which this entry belongs. */
    private ChoiceMenuEntryBean m_entryBean;

    /** The callback to invoke when a widget is selected. */
    private AsyncCallback<ChoiceMenuEntryBean> m_selectCallback;

    /** The submenu to which this entry widget belongs. */
    private ChoiceSubmenu m_submenu;

    /** The widget service to use. */
    private I_WidgetService m_widgetService;

    /**
     * Creates a new menu entry instance.<p>
     * 
     * @param widgetService the widget service to use 
     * @param menuEntry the menu entry bean 
     * @param selectHandler the select handler 
     * @param choiceWidget the root choice menu 
     * @param submenu the submenu for which this entry is being created 
     */
    public ChoiceMenuEntryWidget(
        I_WidgetService widgetService,
        final ChoiceMenuEntryBean menuEntry,
        final AsyncCallback<ChoiceMenuEntryBean> selectHandler,
        AttributeChoiceWidget choiceWidget,
        ChoiceSubmenu submenu) {

        HTML baseWidget = new HTML(widgetService.getAttributeLabel(menuEntry.getPathComponent()));
        initWidget(baseWidget);
        setStyleName(I_LayoutBundle.INSTANCE.attributeChoice().choice());
        m_entryBean = menuEntry;
        m_selectCallback = selectHandler;
        m_submenu = submenu;
        m_attributeChoiceWidget = choiceWidget;
        m_widgetService = widgetService;
        String help = widgetService.getAttributeHelp(menuEntry.getPathComponent());
        setTitle(help);
        if (menuEntry.isLeaf()) {
            baseWidget.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    selectHandler.onSuccess(menuEntry);
                    ButtonBarHandler.INSTANCE.closeAll();

                }
            });
        }
        addDomHandler(ButtonBarHandler.INSTANCE, MouseOverEvent.getType());
        addDomHandler(ButtonBarHandler.INSTANCE, MouseOutEvent.getType());
    }

    /**
     * Gets the root choice menu.<p>
     * 
     * @return the root choice menu 
     */
    public AttributeChoiceWidget getAttributeChoiceWidget() {

        return m_attributeChoiceWidget;
    }

    /** 
     * Gets the menu entry bean.<p>
     * 
     * @return the menu entry bean 
     */
    public ChoiceMenuEntryBean getEntryBean() {

        return m_entryBean;
    }

    /**
     * Gets the select handler.<p>
     * 
     * @return the select handler 
     */
    public AsyncCallback<ChoiceMenuEntryBean> getSelectHandler() {

        return m_selectCallback;
    }

    /**
     * Gets the submenu to which this entry belongs (or null if it belongs to a root menu).<p>
     * 
     * @return the submenu of this entry 
     */
    public ChoiceSubmenu getSubmenu() {

        return m_submenu;
    }

    /**
     * Gets the widget service to use.<p>
     * 
     * @return the widget service 
     */
    public I_WidgetService getWidgetService() {

        return m_widgetService;
    }

}
