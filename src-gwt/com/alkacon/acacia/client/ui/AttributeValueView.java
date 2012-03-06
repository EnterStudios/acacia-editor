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

import com.alkacon.acacia.client.AttributeHandler;
import com.alkacon.acacia.client.I_EntityRenderer;
import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.geranium.client.dnd.I_DragHandle;
import com.alkacon.geranium.client.dnd.I_Draggable;
import com.alkacon.geranium.client.dnd.I_DropTarget;
import com.alkacon.geranium.client.ui.I_Button.ButtonStyle;
import com.alkacon.geranium.client.ui.PushButton;
import com.alkacon.geranium.client.ui.css.I_ImageBundle;
import com.alkacon.geranium.client.util.DomUtil;
import com.alkacon.geranium.client.util.FadeAnimation;
import com.alkacon.vie.shared.I_Entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * UI object holding an attribute value.<p>
 */
public class AttributeValueView extends Composite
implements I_Draggable, HasMouseOverHandlers, HasMouseOutHandlers, HasClickHandlers {

    /**
     * The widget value change handler.<p>
     */
    protected class ChangeHandler implements ValueChangeHandler<String> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            getHandler().changeValue(AttributeValueView.this, event.getValue());
        }
    }

    /** The move handle. */
    protected class MoveHandle extends PushButton implements I_DragHandle {

        /** The draggable. */
        private AttributeValueView m_draggable;

        /**
         * Constructor.<p>
         * 
         * @param draggable the draggable
         */
        MoveHandle(AttributeValueView draggable) {

            setImageClass(I_ImageBundle.INSTANCE.style().changeOrderIcon());
            setButtonStyle(ButtonStyle.TRANSPARENT, null);
            setTitle("Move");
            m_draggable = draggable;
        }

        /**
         * @see com.alkacon.geranium.client.dnd.I_DragHandle#getDraggable()
         */
        public I_Draggable getDraggable() {

            return m_draggable;
        }

    }

    /**
     * The UI binder interface.<p>
     */
    interface AttributeValueUiBinder extends UiBinder<HTMLPanel, AttributeValueView> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static AttributeValueUiBinder uiBinder = GWT.create(AttributeValueUiBinder.class);

    /** The add button. */
    @UiField
    protected PushButton m_addButton;

    /** The down button. */
    @UiField
    protected PushButton m_downButton;

    /** The help bubble element. */
    @UiField
    protected DivElement m_helpBubble;

    /** The help bubble close button. */
    @UiField
    protected PushButton m_helpBubbleClose;

    /** The help bubble text element. */
    @UiField
    protected DivElement m_helpBubbleText;

    /** The label element. */
    @UiField
    protected SpanElement m_label;

    /** The move button. */
    @UiField(provided = true)
    protected MoveHandle m_moveButton;

    /** The remove button. */
    @UiField
    protected PushButton m_removeButton;

    /** The up button. */
    @UiField
    protected PushButton m_upButton;

    /** The widget holder elemenet. */
    @UiField
    protected SimplePanel m_widgetHolder;

    /** Drag and drop helper element. */
    private Element m_dragHelper;

    /** The attribute handler. */
    private AttributeHandler m_handler;

    /** Flag indicating if there is a value set for this UI object. */
    private boolean m_hasValue;

    /** Flag indicating that this view represents a simple value. */
    private boolean m_isSimpleValue;

    /** The drag and drop place holder element. */
    private Element m_placeHolder;

    /** The provisional drag and drop helper parent. */
    private Element m_provisionalParent;

    /**
     * Constructor.<p>
     * 
     * @param handler the attribute handler
     * @param label the attribute label
     * @param help the attribute help information
     */
    public AttributeValueView(AttributeHandler handler, String label, String help) {

        // important: provide the move button before initializing the widget
        m_moveButton = new MoveHandle(this);
        initWidget(uiBinder.createAndBindUi(this));
        m_handler = handler;
        m_handler.registerAttributeValue(this);
        m_moveButton.addMouseDownHandler(m_handler.getDNDHandler());
        m_label.setInnerHTML(label);
        m_label.setTitle(help);
        m_helpBubbleText.setInnerHTML(help);
        initHighlightingHandler();
        initButtons();
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(handler, MouseOutEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#getDragHelper(com.alkacon.geranium.client.dnd.I_DropTarget)
     */
    public Element getDragHelper(I_DropTarget target) {

        closeHelpBubble(null);
        m_dragHelper = DomUtil.clone(getElement());
        // we append the drag helper to the body to prevent any kind of issues 
        // (ie when the parent is styled with overflow:hidden)
        // and we put it additionally inside a absolute positioned provisional parent  
        // ON the original parent for the eventual animation when releasing 
        Element parentElement = getElement().getParentElement();
        if (parentElement == null) {
            parentElement = target.getElement();
        }
        int elementTop = getElement().getAbsoluteTop();
        int parentTop = parentElement.getAbsoluteTop();
        m_provisionalParent = DOM.createElement(parentElement.getTagName());
        RootPanel.getBodyElement().appendChild(m_provisionalParent);
        m_provisionalParent.addClassName(com.alkacon.geranium.client.ui.css.I_LayoutBundle.INSTANCE.generalCss().clearStyles());
        m_provisionalParent.getStyle().setWidth(parentElement.getOffsetWidth(), Unit.PX);
        m_provisionalParent.appendChild(m_dragHelper);
        Style style = m_dragHelper.getStyle();
        style.setWidth(m_dragHelper.getOffsetWidth(), Unit.PX);
        // the dragging class will set position absolute
        style.setTop(elementTop - parentTop, Unit.PX);
        m_dragHelper.addClassName(I_LayoutBundle.INSTANCE.form().dragHelper());
        m_provisionalParent.getStyle().setPosition(Position.ABSOLUTE);
        m_provisionalParent.getStyle().setTop(parentTop, Unit.PX);
        m_provisionalParent.getStyle().setLeft(parentElement.getAbsoluteLeft(), Unit.PX);
        m_provisionalParent.getStyle().setZIndex(
            com.alkacon.geranium.client.ui.css.I_LayoutBundle.INSTANCE.constants().css().zIndexDND());
        return m_dragHelper;
    }

    /**
     * Returns the attribute handler.<p>
     * 
     * @return the attribute handler
     */
    public AttributeHandler getHandler() {

        return m_handler;
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#getId()
     */
    public String getId() {

        String id = getElement().getId();
        if ((id == null) || "".equals(id)) {
            id = Document.get().createUniqueId();
            getElement().setId(id);
        }
        return id;
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#getParentTarget()
     */
    public I_DropTarget getParentTarget() {

        return (I_DropTarget)getParent();
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#getPlaceholder(com.alkacon.geranium.client.dnd.I_DropTarget)
     */
    public Element getPlaceholder(I_DropTarget target) {

        m_placeHolder = DOM.createDiv();
        m_placeHolder.addClassName(I_LayoutBundle.INSTANCE.form().placeHolder());
        return m_placeHolder;
    }

    /**
     * Returns the attribute value index.<p>
     * 
     * @return the attribute value index
     */
    public int getValueIndex() {

        int result = 0;
        Node previousSibling = getElement().getPreviousSibling();
        while (previousSibling != null) {
            result++;
            previousSibling = previousSibling.getPreviousSibling();
        }
        return result;
    }

    /**
     * Returns if there is a value set for this attribute.<p>
     * 
     * @return <code>true</code> if there is a value set for this attribute
     */
    public boolean hasValue() {

        return m_hasValue;
    }

    /**
     * Returns if this view represents a simple value.<p>
     * 
     * @return <code>true</code> if this view represents a simple value
     */
    public boolean isSimpleValue() {

        return m_isSimpleValue;
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#onDragCancel()
     */
    public void onDragCancel() {

        clearDrag();
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#onDrop(com.alkacon.geranium.client.dnd.I_DropTarget)
     */
    public void onDrop(I_DropTarget target) {

        clearDrag();
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#onStartDrag(com.alkacon.geranium.client.dnd.I_DropTarget)
     */
    public void onStartDrag(I_DropTarget target) {

        getElement().getStyle().setOpacity(0.5);
    }

    /**
     * Removes the value.<p>
     */
    public void removeValue() {

        m_hasValue = false;
        m_widgetHolder.clear();
        m_widgetHolder.getElement().setInnerHTML("");
    }

    /**
     * Sets the value entity.<p>
     * 
     * @param renderer the entity renderer
     * @param value the value entity
     */
    public void setValueEntity(I_EntityRenderer renderer, I_Entity value) {

        if (m_hasValue) {
            throw new RuntimeException("Value has already been set");
        }
        m_hasValue = true;
        m_isSimpleValue = false;
        FlowPanel entityPanel = new FlowPanel();
        m_widgetHolder.setWidget(entityPanel);
        renderer.renderForm(value, entityPanel);
    }

    /**
     * Sets the value widget.<p>
     * 
     * @param widget the widget
     * @param value the value
     */
    public void setValueWidget(I_EditWidget widget, String value) {

        if (m_hasValue) {
            throw new RuntimeException("Value has already been set");
        }
        m_hasValue = true;
        m_isSimpleValue = true;
        Element valueDiv = DOM.createDiv();
        m_widgetHolder.getElement().appendChild(valueDiv);
        valueDiv.setInnerHTML(value);
        valueDiv.addClassName(I_LayoutBundle.INSTANCE.form().widget());
        widget.initWidget(valueDiv);
        widget.addValueChangeHandler(new ChangeHandler());
        widget.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                HighlightingHandler.getInstance().setFocusHighlighted(AttributeValueView.this);
            }
        });
    }

    /**
     * Updates the visibility of the add, remove, up and down buttons.<p>
     * 
     * @param hasAddButton <code>true</code> if the add button should be visible
     * @param hasRemoveButton <code>true</code> if the remove button should be visible
     * @param hasSortButtons <code>true</code> if the sort buttons should be visible
     */
    public void updateButtonVisibility(boolean hasAddButton, boolean hasRemoveButton, boolean hasSortButtons) {

        if (hasAddButton) {
            m_addButton.getElement().getStyle().clearDisplay();
        } else {
            m_addButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasRemoveButton) {
            m_removeButton.getElement().getStyle().clearDisplay();
        } else {
            m_removeButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        //                if (hasSortButtons && (getValueIndex() != 0)) {
        //                    m_upButton.getElement().getStyle().clearDisplay();
        //                } else {
        m_upButton.getElement().getStyle().setDisplay(Display.NONE);
        //                }
        //                if (hasSortButtons && (getElement().getNextSibling() != null)) {
        //                    m_downButton.getElement().getStyle().clearDisplay();
        //                } else {
        m_downButton.getElement().getStyle().setDisplay(Display.NONE);
        //                }
        if (hasSortButtons) {
            m_moveButton.getElement().getStyle().clearDisplay();
        } else {
            m_moveButton.getElement().getStyle().setDisplay(Display.NONE);
        }
    }

    /**
     * Handles the click event to add a new attribute value.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_addButton")
    protected void addNewAttributeValue(ClickEvent event) {

        m_handler.addNewAttributeValue(this);
    }

    /**
     * Handles the click event to close the help bubble.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_helpBubbleClose")
    protected void closeHelpBubble(ClickEvent event) {

        addStyleName(I_LayoutBundle.INSTANCE.form().closedBubble());
    }

    /**
     * Handles the click event to move the attribute value down.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_downButton")
    protected void moveAttributeValueDown(ClickEvent event) {

        m_handler.moveAttributeValueDown(this);
    }

    /**
     * Handles the click event to move the attribute value up.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_upButton")
    protected void moveAttributeValueUp(ClickEvent event) {

        m_handler.moveAttributeValueUp(this);
    }

    /**
     * Handles the click event to remove the attribute value.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_removeButton")
    protected void removeAttributeValue(ClickEvent event) {

        m_handler.removeAttributeValue(this);
    }

    /**
     * Toggles the permanent highlighting.<p>
     * 
     * @param highlightingOn <code>true</code> to turn the highlighting on
     */
    protected void toggleFocusHighlighting(boolean highlightingOn) {

        if (highlightingOn) {
            addStyleName(I_LayoutBundle.INSTANCE.form().focused());
            if (shouldDisplayTooltipAbove()) {
                addStyleName(I_LayoutBundle.INSTANCE.form().displayAbove());
            } else {
                removeStyleName(I_LayoutBundle.INSTANCE.form().displayAbove());
            }
            FadeAnimation.fadeIn(m_helpBubble, null, 200);
        } else {
            removeStyleName(I_LayoutBundle.INSTANCE.form().focused());
        }
    }

    /**
     * Toggles the highlighting.<p>
     * 
     * @param highlightingOn <code>true</code> to turn the highlighting on
     */
    protected void toggleHoverHighlighting(boolean highlightingOn) {

        boolean focused = DomUtil.hasClass(I_LayoutBundle.INSTANCE.form().focused(), getElement());
        if (highlightingOn) {
            addStyleName(I_LayoutBundle.INSTANCE.form().highlighting());
            if (focused) {
                FadeAnimation.fadeIn(m_helpBubble, null, 200);
            }
        } else {
            if (focused) {
                FadeAnimation.fadeOut(m_helpBubble, new Command() {

                    public void execute() {

                        removeStyleName(I_LayoutBundle.INSTANCE.form().highlighting());
                    }
                }, 200);
            } else {
                removeStyleName(I_LayoutBundle.INSTANCE.form().highlighting());
            }
        }
    }

    /**
     * Called when a drag operation for this widget is stopped.<p>
     */
    private void clearDrag() {

        if (m_dragHelper != null) {
            m_dragHelper.removeFromParent();
            m_dragHelper = null;
        }
        if (m_provisionalParent != null) {
            m_provisionalParent.removeFromParent();
            m_provisionalParent = null;
        }
        DomUtil.clearOpacity(getElement());
    }

    /**
     * Initializes the button styling.<p>
     */
    private void initButtons() {

        m_addButton.setImageClass(I_ImageBundle.INSTANCE.style().addIcon());
        m_addButton.setTitle("Add");
        m_addButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_removeButton.setImageClass(I_ImageBundle.INSTANCE.style().deleteIcon());
        m_removeButton.setTitle("Delete");
        m_removeButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_upButton.setImageClass(I_ImageBundle.INSTANCE.style().moveIcon());
        m_upButton.setTitle("Move up");
        m_upButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_downButton.setImageClass(I_ImageBundle.INSTANCE.style().moveIcon());
        m_downButton.setTitle("Move down");
        m_downButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_helpBubbleClose.setImageClass(I_ImageBundle.INSTANCE.style().closeIcon());
        m_helpBubbleClose.setTitle("Close");
        m_helpBubbleClose.setButtonStyle(ButtonStyle.TRANSPARENT, null);
    }

    /**
     * Initializes the highlighting handler.<p>
     */
    private void initHighlightingHandler() {

        addMouseOverHandler(HighlightingHandler.getInstance());
        addMouseOutHandler(HighlightingHandler.getInstance());
        addClickHandler(HighlightingHandler.getInstance());
    }

    /**
     * Returns if the help bubble should be displayed above the value field.<p>
     * 
     * @return <code>true</code> if the help bubble should be displayed above
     */
    private boolean shouldDisplayTooltipAbove() {

        Element formParent = DomUtil.getAncestor(getElement(), I_LayoutBundle.INSTANCE.form().formParent());
        if (formParent != null) {
            int elementTop = getElement().getAbsoluteTop();
            int elementHeight = getElement().getOffsetHeight();
            int formTop = formParent.getAbsoluteTop();
            int formHeight = formParent.getOffsetHeight();
            if (((elementTop - formTop) + elementHeight) > (formHeight - 100)) {
                return true;
            }
        }
        return false;
    }
    //
    //    public AttributeValueView findFirstSimpleValue() {
    //
    //        if (!m_hasValue) {
    //            return null;
    //        }
    //        if (m_isSimpleValue) {
    //            return this;
    //        } else {
    //            FlowPanel entityPanel = (FlowPanel)m_widgetHolder.getWidget();
    //            for (Widget child : entityPanel) {
    //                ValuePanel valuePanel = (ValuePanel)child;
    //                for (Widget valueWidget : valuePanel) {
    //                    AttributeValueView attributeValue = ((AttributeValueView)valueWidget).findFirstSimpleValue();
    //                    if (attributeValue != null) {
    //                        return attributeValue;
    //                    }
    //                }
    //            }
    //        }
    //        return null;
    //    }
}
