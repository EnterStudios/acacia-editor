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
import com.alkacon.acacia.client.ChoiceMenuEntryBean;
import com.alkacon.acacia.client.EditorBase;
import com.alkacon.acacia.client.HighlightingHandler;
import com.alkacon.acacia.client.I_EntityRenderer;
import com.alkacon.acacia.client.I_WidgetService;
import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.I_FormEditWidget;
import com.alkacon.geranium.client.dnd.I_DragHandle;
import com.alkacon.geranium.client.dnd.I_Draggable;
import com.alkacon.geranium.client.dnd.I_DropTarget;
import com.alkacon.geranium.client.ui.HoverPanel;
import com.alkacon.geranium.client.ui.I_Button.ButtonStyle;
import com.alkacon.geranium.client.ui.PushButton;
import com.alkacon.geranium.client.ui.css.I_ImageBundle;
import com.alkacon.geranium.client.util.DomUtil;
import com.alkacon.geranium.client.util.StyleVariable;
import com.alkacon.vie.shared.I_Entity;

import java.util.List;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

/**
 * UI object holding an attribute value.<p>
 */
public class AttributeValueView extends Composite
implements I_Draggable, HasMouseOverHandlers, HasMouseOutHandlers, HasMouseDownHandlers {

    /**
     * The widget value change handler.<p>
     */
    protected class ChangeHandler implements ValueChangeHandler<String> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            getHandler().changeValue(AttributeValueView.this, event.getValue());
            removeValidationMessage();
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

            setImageClass(I_ImageBundle.INSTANCE.style().moveIcon());
            setButtonStyle(ButtonStyle.TRANSPARENT, null);
            if (EditorBase.getDictionary() != null) {
                setTitle(EditorBase.getDictionary().get(EditorBase.GUI_VIEW_MOVE_0));
            }
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

    /** The first column compact view mode. */
    public static final int COMPACT_MODE_FIRST_COLUMN = 1;

    /** The second column compact view mode. */
    public static final int COMPACT_MODE_SECOND_COLUMN = 2;

    /** The wide compact view mode. */
    public static final int COMPACT_MODE_WIDE = 0;

    /** The nested compact view mode. */
    public static final int COMPACT_MODE_NESTED = 3;

    /** The UI binder instance. */
    private static AttributeValueUiBinder uiBinder = GWT.create(AttributeValueUiBinder.class);

    /** The add button. */
    @UiField
    protected PushButton m_addButton;

    /** The attribute choice button. */
    @UiField
    protected AttributeChoiceWidget m_attributeChoice;

    /** The button bar. */
    @UiField
    protected HoverPanel m_buttonBar;

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

    /** The message text element. */
    @UiField
    protected SpanElement m_messageText;

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
    protected FlowPanel m_widgetHolder;

    /** The currently running animation. */
    Animation m_currentAnimation;

    /** The activation mouse down handler registration. */
    private HandlerRegistration m_activationHandlerRegistration;

    /** The compact view style variable. */
    private StyleVariable m_compacteModeStyle;

    /** The default widget value. */
    private String m_defaultValue;

    /** Drag and drop helper element. */
    private Element m_dragHelper;

    /** The attribute handler. */
    private AttributeHandler m_handler;

    /** Flag indicating a validation error. */
    private boolean m_hasError;

    /** Flag indicating if there is a value set for this UI object. */
    private boolean m_hasValue;

    /** The help text. */
    private String m_help;

    /** Flag indicating this is a representing an attribute choice value. */
    private boolean m_isChoice;

    /** Flag indicating that this view represents a simple value. */
    private boolean m_isSimpleValue;

    /** The label text. */
    private String m_label;

    /** The drag and drop place holder element. */
    private Element m_placeHolder;

    /** The provisional drag and drop helper parent. */
    private Element m_provisionalParent;

    /** The editing widget. */
    private I_FormEditWidget m_widget;

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
        m_label = label;
        m_help = help;
        if (m_help == null) {
            closeHelpBubble(null);
            m_help = "";
        }
        generateLabel();
        m_helpBubbleText.setInnerHTML(m_help);
        addStyleName(I_LayoutBundle.INSTANCE.form().emptyValue());
        m_compacteModeStyle = new StyleVariable(this);
        initHighlightingHandler();
        initButtons(label);
    }

    /**
     * Adds a new choice  choice selection menu.<p> 
     * 
     * @param widgetService the widget service to use for labels  
     * @param menuEntry the menu entry bean for the choice 
     */
    public void addChoice(I_WidgetService widgetService, final ChoiceMenuEntryBean menuEntry) {

        AsyncCallback<ChoiceMenuEntryBean> selectHandler = new AsyncCallback<ChoiceMenuEntryBean>() {

            public void onFailure(Throwable caught) {

                // will not be called 

            }

            public void onSuccess(ChoiceMenuEntryBean selectedEntry) {

                m_attributeChoice.hide();
                selectChoice(selectedEntry.getPath());
            }
        };

        m_attributeChoice.addChoice(widgetService, menuEntry, selectHandler);
        m_isChoice = true;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler(com.google.gwt.event.dom.client.MouseDownHandler)
     */
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {

        return addDomHandler(handler, MouseDownEvent.getType());
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
        // using the widget element as the drag helper also to avoid cloning issues on input fields
        m_dragHelper = getElement();
        Element parentElement = getElement().getParentElement();
        if (parentElement == null) {
            parentElement = target.getElement();
        }
        int elementTop = getElement().getAbsoluteTop();
        int parentTop = parentElement.getAbsoluteTop();
        Style style = m_dragHelper.getStyle();
        style.setWidth(m_dragHelper.getOffsetWidth(), Unit.PX);
        // the dragging class will set position absolute
        style.setTop(elementTop - parentTop, Unit.PX);
        m_dragHelper.addClassName(I_LayoutBundle.INSTANCE.form().dragHelper());
        style.setZIndex(com.alkacon.geranium.client.ui.css.I_LayoutBundle.INSTANCE.constants().css().zIndexDND());
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

        m_placeHolder = DomUtil.clone(getElement());
        removeDragHelperStyles(m_placeHolder);
        m_placeHolder.addClassName(I_LayoutBundle.INSTANCE.form().dragPlaceholder());
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
     * Returns the editing widget.<p>
     * 
     * @return the editing widget or <code>null</code> if not available
     */
    public I_EditWidget getValueWidget() {

        return m_widget;
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

        // nothing to do
    }

    /**
     * Removes any present error message.<p>
     */
    public void removeValidationMessage() {

        if (m_hasError) {
            m_messageText.setInnerText("");
            removeStyleName(I_LayoutBundle.INSTANCE.form().hasError());
            removeStyleName(I_LayoutBundle.INSTANCE.form().hasWarning());
            m_hasError = false;
        }

    }

    /**
     * Removes the value.<p>
     */
    public void removeValue() {

        m_hasValue = false;
        m_widgetHolder.clear();
        addStyleName(I_LayoutBundle.INSTANCE.form().emptyValue());
        generateLabel();
        removeValidationMessage();
    }

    /**
     * Sets the compact view mode.<p>
     * 
     * @param mode the mode to set
     */
    public void setCompactMode(int mode) {

        switch (mode) {
            case COMPACT_MODE_FIRST_COLUMN:
                m_compacteModeStyle.setValue(I_LayoutBundle.INSTANCE.form().firstColumn());
                break;
            case COMPACT_MODE_SECOND_COLUMN:
                m_compacteModeStyle.setValue(I_LayoutBundle.INSTANCE.form().secondColumn());
                break;
            case COMPACT_MODE_NESTED:
                m_compacteModeStyle.setValue(I_LayoutBundle.INSTANCE.form().compactView());
                break;
            default:
                m_compacteModeStyle.setValue(null);
        }
        updateWidth();
    }

    /**
     * Shows a validation error message.<p>
     * 
     * @param message the error message
     */
    public void setErrorMessage(String message) {

        m_messageText.setInnerHTML(message);
        addStyleName(I_LayoutBundle.INSTANCE.form().hasError());
        m_hasError = true;
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
        m_widgetHolder.add(entityPanel);
        renderer.renderForm(value, entityPanel, m_handler, getValueIndex());
        removeStyleName(I_LayoutBundle.INSTANCE.form().emptyValue());
    }

    /**
     * Sets the value widget.<p>
     * 
     * @param widget the widget
     * @param value the value
     * @param active <code>true</code> if the widget should be activated
     */
    public void setValueWidget(I_FormEditWidget widget, String value, boolean active) {

        if (m_hasValue) {
            throw new RuntimeException("Value has already been set");
        }
        m_hasValue = true;
        m_isSimpleValue = true;
        m_widget = widget;
        if (AttributeHandler.hasResizeHandler() && (m_widget instanceof HasResizeHandlers)) {
            ((HasResizeHandlers)m_widget).addResizeHandler(AttributeHandler.getResizeHandler());
        }
        m_widgetHolder.clear();
        m_widget.setWidgetInfo(m_label, m_help);
        if (active) {
            m_widget.setValue(value, false);
        } else {
            m_widget.setValue("", false);
        }
        m_widget.asWidget().addStyleName(I_LayoutBundle.INSTANCE.form().widget());
        m_widgetHolder.add(m_widget);
        m_widget.setName(getHandler().getAttributeName());
        m_widget.addValueChangeHandler(new ChangeHandler());
        m_widget.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                HighlightingHandler.getInstance().setFocusHighlighted(AttributeValueView.this);
            }
        });
        m_widget.setActive(active);
        if (!active) {
            m_defaultValue = value;
            addActivationHandler();
        } else {
            removeStyleName(I_LayoutBundle.INSTANCE.form().emptyValue());
        }
    }

    /**
     * Shows a validation warning message.<p>
     * 
     * @param message the warning message
     */
    public void setWarningMessage(String message) {

        m_messageText.setInnerText(message);
        addStyleName(I_LayoutBundle.INSTANCE.form().hasWarning());
        m_hasError = true;
    }

    /**
     * Toggles the permanent highlighting.<p>
     * 
     * @param highlightingOn <code>true</code> to turn the highlighting on
     */
    public void toggleFocusHighlighting(boolean highlightingOn) {

        if (highlightingOn) {
            addStyleName(I_LayoutBundle.INSTANCE.form().focused());
            if (shouldDisplayTooltipAbove()) {
                addStyleName(I_LayoutBundle.INSTANCE.form().displayAbove());
            } else {
                removeStyleName(I_LayoutBundle.INSTANCE.form().displayAbove());
            }
        } else {
            removeStyleName(I_LayoutBundle.INSTANCE.form().focused());
        }
    }

    /**
     * Updates the visibility of the add, remove, up and down buttons.<p>
     * 
     * @param hasAddButton <code>true</code> if the add button should be visible
     * @param hasRemoveButton <code>true</code> if the remove button should be visible
     * @param hasSortButtons <code>true</code> if the sort buttons should be visible
     */
    public void updateButtonVisibility(boolean hasAddButton, boolean hasRemoveButton, boolean hasSortButtons) {

        if (hasAddButton && m_isChoice) {
            m_attributeChoice.getElement().getStyle().clearDisplay();
        } else {
            m_attributeChoice.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasAddButton && !m_isChoice) {
            m_addButton.getElement().getStyle().clearDisplay();
        } else {
            m_addButton.getElement().getStyle().setDisplay(Display.NONE);
        }

        if (hasRemoveButton) {
            m_removeButton.getElement().getStyle().clearDisplay();
        } else {
            m_removeButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasSortButtons && (getValueIndex() != 0)) {
            m_upButton.getElement().getStyle().clearDisplay();
        } else {
            m_upButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasSortButtons && (getElement().getNextSibling() != null)) {
            m_downButton.getElement().getStyle().clearDisplay();
        } else {
            m_downButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasSortButtons) {
            m_moveButton.getElement().getStyle().clearDisplay();
        } else {
            m_moveButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (!hasAddButton && !hasRemoveButton && !hasSortButtons) {
            // hide the button bar if no button is visible
            m_buttonBar.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            // show the button bar
            m_buttonBar.getElement().getStyle().clearDisplay();
        }
    }

    /**
     * Handles the click event to add a new attribute value.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_addButton")
    protected void addNewAttributeValue(ClickEvent event) {

        if ((m_widget != null) && !m_widget.isActive()) {
            activateWidget();
        } else {
            m_handler.addNewAttributeValue(this);
        }
        onResize();
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
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                updateWidth();
            }
        });
    }

    /**
     * Call when content changes.<p>
     */
    protected void onResize() {

        Widget parent = getParent();
        while (parent != null) {
            if (parent instanceof RequiresResize) {
                ((RequiresResize)parent).onResize();
                break;
            }
            parent = parent.getParent();
        }
    }

    /**
     * Handles the click event to remove the attribute value.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_removeButton")
    protected void removeAttributeValue(ClickEvent event) {

        m_handler.removeAttributeValue(this);
        onResize();
    }

    /**
     * Selects the attribute choice.<p>
     * 
     * @param choicePath the choice attribute path 
     */
    protected void selectChoice(List<String> choicePath) {

        m_handler.addNewChoiceAttributeValue(this, choicePath);
    }

    /**
     * Activates the value widget if prNamet.<p>
     */
    void activateWidget() {

        if (m_activationHandlerRegistration != null) {
            m_activationHandlerRegistration.removeHandler();
            m_activationHandlerRegistration = null;
        }
        if ((m_widget != null) && !m_widget.isActive()) {
            m_widget.setActive(true);
            if (m_defaultValue != null) {
                m_widget.setValue(m_defaultValue, true);
            }
            m_handler.updateButtonVisisbility();
            removeStyleName(I_LayoutBundle.INSTANCE.form().emptyValue());
        }
    }

    /**
     * Updates the widget width according to the compact mode setting.<p>
     */
    void updateWidth() {

        if (I_LayoutBundle.INSTANCE.form().firstColumn().equals(m_compacteModeStyle.getValue())) {
            int width = getElement().getParentElement().getOffsetWidth()
                - I_LayoutBundle.INSTANCE.form().SECOND_COLUMN_WIDTH();
            // if width could not be evaluated, fall back to a 'save' value
            if (width < 0) {
                width = 400;
            }
            getElement().getStyle().setWidth(width, Unit.PX);
        } else {
            getElement().getStyle().clearWidth();
        }
    }

    /**
     * Adds a mouse down handler to activate the editing widget.<p>
     */
    private void addActivationHandler() {

        if (m_activationHandlerRegistration == null) {
            m_activationHandlerRegistration = addMouseDownHandler(new MouseDownHandler() {

                public void onMouseDown(MouseDownEvent event) {

                    // only act on click if not inside the button bar
                    if (!DomUtil.checkPositionInside(m_buttonBar.getElement(), event.getClientX(), event.getClientY())) {
                        activateWidget();

                    }
                }
            });
        }
    }

    /**
     * Called when a drag operation for this widget is stopped.<p>
     */
    private void clearDrag() {

        if (m_dragHelper != null) {
            removeDragHelperStyles(m_dragHelper);
            // m_dragHelper.removeFromParent();
            m_dragHelper = null;
        }
        if (m_provisionalParent != null) {
            m_provisionalParent.removeFromParent();
            m_provisionalParent = null;
        }
    }

    /**
     * Generates the attribute label.<p>
     */
    private void generateLabel() {

        HTML labelWidget = new HTML("<div title=\""
            + SafeHtmlUtils.htmlEscape(stripHtml(m_help))
            + "\" class=\""
            + I_LayoutBundle.INSTANCE.form().label()
            + "\">"
            + m_label
            + "</div>");
        m_widgetHolder.add(labelWidget);
    }

    /**
     * Initializes the button styling.<p>
     * 
     * @param label the attribute label 
     */
    private void initButtons(String label) {

        m_addButton.setImageClass(I_ImageBundle.INSTANCE.style().addIcon());
        m_addButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_removeButton.setImageClass(I_ImageBundle.INSTANCE.style().removeIcon());
        m_removeButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_upButton.setImageClass(I_ImageBundle.INSTANCE.style().arrowUpIcon());
        m_upButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_downButton.setImageClass(I_ImageBundle.INSTANCE.style().arrowDownIcon());
        m_downButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_helpBubbleClose.setImageClass(I_ImageBundle.INSTANCE.style().closeIcon());
        m_helpBubbleClose.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        if (EditorBase.getDictionary() != null) {
            m_addButton.setTitle(EditorBase.getDictionary().get(EditorBase.GUI_VIEW_ADD_0));
            m_removeButton.setTitle(EditorBase.getDictionary().get(EditorBase.GUI_VIEW_DELETE_0));
            m_helpBubbleClose.setTitle(EditorBase.getDictionary().get(EditorBase.GUI_VIEW_CLOSE_0));
            m_upButton.setTitle(EditorBase.getDictionary().get(EditorBase.GUI_VIEW_MOVE_UP_0));
            m_downButton.setTitle(EditorBase.getDictionary().get(EditorBase.GUI_VIEW_MOVE_DOWN_0));
        }
    }

    /**
     * Initializes the highlighting handler.<p>
     */
    private void initHighlightingHandler() {

        addMouseOverHandler(HighlightingHandler.getInstance());
        addMouseOutHandler(HighlightingHandler.getInstance());
        addMouseDownHandler(HighlightingHandler.getInstance());
        m_buttonBar.addMouseOverHandler(HighlightingHandler.getInstance());
        m_buttonBar.addMouseOutHandler(HighlightingHandler.getInstance());
    }

    /**
     * Removes the drag helper styles from the given element.<p>
     * 
     * @param helper the helper element
     */
    private void removeDragHelperStyles(Element helper) {

        Style style = helper.getStyle();
        style.clearTop();
        style.clearLeft();
        style.clearPosition();
        style.clearWidth();
        style.clearZIndex();
        helper.removeClassName(I_LayoutBundle.INSTANCE.form().dragHelper());
    }

    /**
     * Returns if the help bubble should be displayed above the value field.<p>
     * 
     * @return <code>true</code> if the help bubble should be displayed above
     */
    private boolean shouldDisplayTooltipAbove() {

        return !isSimpleValue();
    }

    /**
     * Strips all HTML tags.<p>
     * @param html the string that should be striped
     * @return the striped HTML string
     */
    private String stripHtml(String html) {

        return html.replaceAll("\\<.*?\\>", "");
    }
}
