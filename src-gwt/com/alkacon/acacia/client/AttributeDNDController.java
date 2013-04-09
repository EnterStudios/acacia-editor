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

package com.alkacon.acacia.client;

import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.ui.AttributeValueView;
import com.alkacon.acacia.client.ui.ValuePanel;
import com.alkacon.geranium.client.dnd.DNDHandler;
import com.alkacon.geranium.client.dnd.DNDHandler.Orientation;
import com.alkacon.geranium.client.dnd.I_DNDController;
import com.alkacon.geranium.client.dnd.I_Draggable;
import com.alkacon.geranium.client.dnd.I_DropTarget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.DOM;

/**
 * The drag and drop controller for attribute value sorting.<p>
 */
public class AttributeDNDController implements I_DNDController {

    /** The drag overlay. */
    private Element m_dragOverlay;

    /** The starting position. */
    private int m_startPosition;

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onAnimationStart(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public void onAnimationStart(I_Draggable draggable, I_DropTarget target, DNDHandler handler) {

        // nothing to do
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onBeforeDrop(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public boolean onBeforeDrop(I_Draggable draggable, I_DropTarget target, DNDHandler handler) {

        // nothing to do
        return true;
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onDragCancel(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public void onDragCancel(I_Draggable draggable, I_DropTarget target, final DNDHandler handler) {

        removeDragOverlay();
        clearTargets(handler);
        // remove the drag helper reference from handler, to avoid helper.removeFromParent() call
        handler.setDragHelper(null);
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onDragStart(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public boolean onDragStart(I_Draggable draggable, I_DropTarget target, DNDHandler handler) {

        installDragOverlay();
        handler.setOrientation(Orientation.VERTICAL);
        if ((target instanceof ValuePanel) && (draggable instanceof AttributeValueView)) {
            m_startPosition = ((AttributeValueView)draggable).getValueIndex();
            handler.clearTargets();
            handler.addTarget(target);
            target.getElement().getStyle().setPosition(Position.RELATIVE);
            target.getElement().insertBefore(handler.getPlaceholder(), draggable.getElement());
            ((ValuePanel)target).highlightOutline();
            return true;
        }
        return false;
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onDrop(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public void onDrop(I_Draggable draggable, I_DropTarget target, DNDHandler handler) {

        AttributeValueView attributeValue = (AttributeValueView)draggable;
        int targetIndex = target.getPlaceholderIndex();
        if (targetIndex > m_startPosition) {
            targetIndex--;
        }
        attributeValue.getHandler().moveAttributeValue(attributeValue, m_startPosition, targetIndex);
        removeDragOverlay();
        clearTargets(handler);
        // remove the drag helper reference from handler, to avoid helper.removeFromParent() call
        handler.setDragHelper(null);
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onPositionedPlaceholder(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public void onPositionedPlaceholder(I_Draggable draggable, I_DropTarget target, DNDHandler handler) {

        ((ValuePanel)target).updateHighlightingPosition();
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onTargetEnter(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public boolean onTargetEnter(I_Draggable draggable, I_DropTarget target, DNDHandler handler) {

        return true;
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_DNDController#onTargetLeave(com.alkacon.geranium.client.dnd.I_Draggable, com.alkacon.geranium.client.dnd.I_DropTarget, com.alkacon.geranium.client.dnd.DNDHandler)
     */
    public void onTargetLeave(I_Draggable draggable, I_DropTarget target, DNDHandler handler) {

        // nothing to do
    }

    /**
     * Clears the handlers drag and drop targets.<p>
     * 
     * @param handler the drag and drop handler
     */
    private void clearTargets(final DNDHandler handler) {

        ((ValuePanel)handler.getCurrentTarget()).removeHighlighting();
        handler.getCurrentTarget().getElement().getStyle().clearPosition();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                handler.clearTargets();
            }
        });
    }

    /**
     * Installs the drag overlay to avoid any mouse over issues or similar.<p>
     */
    private void installDragOverlay() {

        if (m_dragOverlay != null) {
            m_dragOverlay.removeFromParent();
        }
        m_dragOverlay = DOM.createDiv();
        m_dragOverlay.addClassName(I_LayoutBundle.INSTANCE.form().dragOverlay());
        Document.get().getBody().appendChild(m_dragOverlay);
    }

    /**
     * Removes the drag overlay.<p>
     */
    private void removeDragOverlay() {

        if (m_dragOverlay != null) {
            m_dragOverlay.removeFromParent();
            m_dragOverlay = null;
        }
    }

}
