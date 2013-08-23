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

import com.alkacon.acacia.client.ChoiceMenuEntryBean;
import com.alkacon.acacia.client.css.I_LayoutBundle;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A choice submenu widget.<p>
 */
public class ChoiceSubmenu extends Composite {

    /** The composite widget. */
    private FlowPanel m_root = new FlowPanel();

    /**
     * Creates a new submenu.<p>
     * 
     * @param parentEntry the parent menu entry bean 
     */
    public ChoiceSubmenu(ChoiceMenuEntryBean parentEntry) {

        initWidget(m_root);
        addStyleName(I_LayoutBundle.INSTANCE.attributeChoice().choices());
        addStyleName(I_LayoutBundle.INSTANCE.attributeChoice().submenu());
    }

    /**
     * Adds a new choice widget.<p>
     * 
     * @param choice the choice widget
     */
    public void addChoice(ChoiceMenuEntryWidget choice) {

        m_root.add(choice);
    }

    /**
     * Checks whether the submenu should be opened above instead of below.<p>
     * 
     * @param referenceElement the reference element 
     * @return true if the new submenu should be opened above 
     */
    public boolean openAbove(Element referenceElement) {

        int windowTop = Window.getScrollTop();
        int windowBottom = Window.getScrollTop() + Window.getClientHeight();
        int spaceAbove = referenceElement.getAbsoluteTop() - windowTop;
        int spaceBelow = windowBottom - referenceElement.getAbsoluteBottom();
        return spaceAbove > spaceBelow;
    }

    /**
     * Positions a new submenu asynchronously.<p>
     * 
     * @param widgetEntry the menu entry relative to which the submenu should be positioned 
     */
    public void positionDeferred(final ChoiceMenuEntryWidget widgetEntry) {

        getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                positionNextToMenuEntry(widgetEntry);
                getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
            }

        });
    }

    /**
     * Helper method to position a submenu on the left side of a menu entry.<p>
     * 
     * @param widgetEntry the widget entry relative to which the submenu should  be positioned 
     */
    protected void positionNextToMenuEntry(final ChoiceMenuEntryWidget widgetEntry) {

        Element elem = getElement();
        elem.getStyle().setPosition(Style.Position.ABSOLUTE);
        Element referenceElement = null;
        int startX = -2000;
        int startY = -2000;
        int deltaX = 0;
        int deltaY = 0;
        referenceElement = widgetEntry.getElement();
        Style style = elem.getStyle();
        style.setLeft(startX, Unit.PX);
        style.setTop(startY, Unit.PX);
        int myRight = elem.getAbsoluteRight();
        int myTop = elem.getAbsoluteTop();
        int refLeft = referenceElement.getAbsoluteLeft();
        int refTop = referenceElement.getAbsoluteTop();
        int newLeft = startX + (refLeft - myRight) + deltaX;
        int newTop;
        if (openAbove(referenceElement)) {
            int myHeight = elem.getOffsetHeight();
            int refHeight = referenceElement.getOffsetHeight();
            newTop = startY + ((refTop + refHeight) - (myTop + myHeight)) + deltaY;
        } else {
            newTop = startY + (refTop - myTop) + deltaY;
        }
        style.setLeft(newLeft, Unit.PX);
        style.setTop(newTop, Unit.PX);
    }

}
