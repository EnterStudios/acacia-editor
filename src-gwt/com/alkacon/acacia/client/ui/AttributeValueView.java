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
import com.alkacon.acacia.client.ButtonBarHandler;
import com.alkacon.acacia.client.ChoiceMenuEntryBean;
import com.alkacon.acacia.client.EditorBase;
import com.alkacon.acacia.client.I_EntityRenderer;
import com.alkacon.acacia.client.I_WidgetService;
import com.alkacon.acacia.client.ValueFocusHandler;
import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.I_FormEditWidget;
import com.alkacon.geranium.client.I_DescendantResizeHandler;
import com.alkacon.geranium.client.dnd.I_DragHandle;
import com.alkacon.geranium.client.dnd.I_Draggable;
import com.alkacon.geranium.client.dnd.I_DropTarget;
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

            setImageClass(I_ImageBundle.INSTANCE.style().bullsEyeIcon());
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

    /** Handler for controlling the visibility of button bars. */
    private static ButtonBarHandler hoverHandler = ButtonBarHandler.INSTANCE;

    /** The first column compact view mode. */
    public static final int COMPACT_MODE_FIRST_COLUMN = 1;

    /** The nested compact view mode. */
    public static final int COMPACT_MODE_NESTED = 3;

    /** The second column compact view mode. */
    public static final int COMPACT_MODE_SECOND_COLUMN = 2;

    /** The single line compact view mode. */
    public static final int COMPACT_MODE_SINGLE_LINE = 4;

    /** The wide compact view mode. */
    public static final int COMPACT_MODE_WIDE = 0;

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
    protected HTMLPanel m_buttonBar;

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

    /** Flag indicating if drag and drop is enabled for this attribute. */
    private boolean m_dragEnabled;

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

    /** The editing widget. */
    private I_FormEditWidget m_widget;

    /** Style variable to enable/disable 'collapsed' style. */
    private StyleVariable m_collapsedStyle = new StyleVariable(this);

    /**
     * Constructor.<p>
     * 
     * @param handler the attribute handler
     * @param label the attribute label
     * @param help the attribute help information
     */
    public AttributeValueView(AttributeHandler handler, String label, String help) {

        // important: provide the move button before binding the widget UI
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
        addStyleName(formCss().emptyValue());
        m_compacteModeStyle = new StyleVariable(this);
        m_compacteModeStyle.setValue(formCss().defaultView());
        initHighlightingHandler();
        initButtons();

        ButtonBarHandler.EventHandler handler2 = hoverHandler.createEventHandler(this);
        m_buttonBar.addDomHandler(handler2, MouseOverEvent.getType());
        m_buttonBar.addDomHandler(handler2, MouseOutEvent.getType());
        m_collapsedStyle.setValue(formCss().uncollapsed());
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
        m_dragHelper.addClassName(formCss().dragHelper());
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
     * Gets the parent attribute value view, or null if none exists.<p>
     * 
     * @return the parent attribute value view 
     */
    public AttributeValueView getParentView() {

        Widget ancestor = getParent();
        while ((ancestor != null) && !(ancestor instanceof AttributeValueView)) {
            ancestor = ancestor.getParent();
        }
        return (AttributeValueView)ancestor;
    }

    /**
     * @see com.alkacon.geranium.client.dnd.I_Draggable#getPlaceholder(com.alkacon.geranium.client.dnd.I_DropTarget)
     */
    public Element getPlaceholder(I_DropTarget target) {

        m_placeHolder = DomUtil.clone(getElement());
        removeDragHelperStyles(m_placeHolder);
        m_placeHolder.addClassName(formCss().dragPlaceholder());
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
     * Checks whether an element is part of the menu of this attribute value view.<p>
     * 
     * @param elem the element to check 
     * @return true if this element is part of the menu 
     */
    public boolean hasButtonElement(Element elem) {

        return (m_buttonBar != null) && m_buttonBar.getElement().isOrHasChild(elem);
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
     * Hides the button bar.<p>
     */
    public void hideAllButtons() {

        m_buttonBar.getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Returns if drag and drop is enabled for this attribute.<p>
     * 
     * @return <code>true</code> if drag and drop is enabled for this attribute
     */
    public boolean isDragEnabled() {

        return m_dragEnabled;
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
            removeStyleName(formCss().hasError());
            removeStyleName(formCss().hasWarning());
            m_hasError = false;
        }

    }

    /**
     * Removes the value.<p>
     */
    public void removeValue() {

        if (!isSimpleValue()) {
            m_hasValue = false;
            m_widgetHolder.clear();
            generateLabel();
        } else {
            // only deactivate the widget and restore the default value
            m_widget.setActive(false);
            m_widget.setValue("", false);
            addActivationHandler();
        }
        addStyleName(formCss().emptyValue());
        removeValidationMessage();
    }

    /**
     * Shows or hides the button bar.<p>
     * 
     * @param visible true if the button bar should be shown 
     */
    public void setButtonsVisible(boolean visible) {

        String hoverStyle = formCss().hoverButton();
        if (visible) {
            m_buttonBar.addStyleName(hoverStyle);
        } else {
            m_buttonBar.removeStyleName(hoverStyle);
        }
    }

    /**
     * Enables or disables the "collapsed" style, which is used for choice elements to reduce the nesting level visually.<p>
     * 
     * @param collapsed true if the view should be set to 'collapsed' 
     */
    public void setCollapsed(boolean collapsed) {

        m_collapsedStyle.setValue(collapsed ? formCss().collapsed() : formCss().uncollapsed());
    }

    /**
     * Sets the compact view mode.<p>
     * 
     * @param mode the mode to set
     */
    public void setCompactMode(int mode) {

        switch (mode) {
            case COMPACT_MODE_FIRST_COLUMN:
                m_compacteModeStyle.setValue(formCss().firstColumn());
                break;
            case COMPACT_MODE_SECOND_COLUMN:
                m_compacteModeStyle.setValue(formCss().secondColumn());
                break;
            case COMPACT_MODE_NESTED:
                m_compacteModeStyle.setValue(formCss().compactView());
                break;
            case COMPACT_MODE_SINGLE_LINE:
                m_compacteModeStyle.setValue(formCss().singleLine());
                break;
            default:

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
        addStyleName(formCss().hasError());
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
        removeStyleName(formCss().emptyValue());
    }

    /**
     * Sets the value widget.<p>
     * 
     * @param widget the widget
     * @param value the value
     * @param defaultValue the default attribute value
     * @param active <code>true</code> if the widget should be activated
     */
    public void setValueWidget(I_FormEditWidget widget, String value, String defaultValue, boolean active) {

        if (m_hasValue) {
            throw new RuntimeException("Value has already been set");
        }
        m_defaultValue = defaultValue;
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
        m_widgetHolder.add(m_widget);
        m_widget.setName(getHandler().getAttributeName());
        m_widget.addValueChangeHandler(new ChangeHandler());
        m_widget.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                ValueFocusHandler.getInstance().setFocus(AttributeValueView.this);
                activateWidget();
            }
        });
        m_widget.setActive(active);
        if (!active) {
            addActivationHandler();
        } else {
            removeStyleName(formCss().emptyValue());
        }
        addStyleName(formCss().simpleValue());
    }

    /**
     * Shows a validation warning message.<p>
     * 
     * @param message the warning message
     */
    public void setWarningMessage(String message) {

        m_messageText.setInnerText(message);
        addStyleName(formCss().hasWarning());
        m_hasError = true;
    }

    /**
     * Shows the button bar.<p>
     */
    public void showButtons() {

        m_buttonBar.getElement().getStyle().clearDisplay();
    }

    /**
     * Tells the attribute value view to change its display state between focused/unfocused (this doesn't actually change the focus).<p>
     * 
     * @param focusOn <code>true</code> to change the display state to 'focused'
     */
    public void toggleFocus(boolean focusOn) {

        if (focusOn) {
            addStyleName(formCss().focused());
            if (shouldDisplayTooltipAbove()) {
                addStyleName(formCss().displayAbove());
            } else {
                removeStyleName(formCss().displayAbove());
            }
        } else {
            removeStyleName(formCss().focused());
            if (m_widget != null) {
                if (m_handler.hasSingleOptionalValue()) {
                    if (m_handler.getWidgetService().shouldRemoveLastValueAfterUnfocus(m_widget)) {
                        m_handler.removeAttributeValue(this);
                    }

                }
            }
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
        if (hasSortButtons && (EditorBase.getDictionary() != null)) {
            m_moveButton.setTitle(EditorBase.getDictionary().get(EditorBase.GUI_VIEW_MOVE_0));
        } else {
            m_moveButton.setTitle("");
        }
        m_dragEnabled = hasSortButtons;
        if (!hasAddButton && !hasRemoveButton && !hasSortButtons) {
            // hide the button bar if no button is visible
            m_buttonBar.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            // show the button bar
            m_buttonBar.getElement().getStyle().clearDisplay();
            if (hasSortButtons || (hasAddButton && hasRemoveButton)) {
                // set multi button mode
                m_buttonBar.addStyleName(formCss().multiButtonBar());
            } else {
                m_buttonBar.removeStyleName(formCss().multiButtonBar());
            }
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

        addStyleName(formCss().closedBubble());
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
            if (parent instanceof I_DescendantResizeHandler) {
                ((I_DescendantResizeHandler)parent).onResizeDescendant();
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
            if ((m_defaultValue != null) && (m_defaultValue.trim().length() > 0)) {
                m_widget.setValue(m_defaultValue, true);
            }
            m_handler.updateButtonVisisbility();
            removeStyleName(formCss().emptyValue());
        }
    }

    /**
     * Updates the widget width according to the compact mode setting.<p>
     */
    void updateWidth() {

        if (formCss().firstColumn().equals(m_compacteModeStyle.getValue())) {
            int width = getElement().getParentElement().getOffsetWidth() - formCss().SECOND_COLUMN_WIDTH();
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
                    if (!m_buttonBar.getElement().isOrHasChild((Node)event.getNativeEvent().getEventTarget().cast())) {
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
        // preventing issue where mouse out was never triggered after drag and drop
        m_moveButton.getElement().removeFromParent();
        m_buttonBar.getElement().insertFirst(m_moveButton.getElement());
    }

    /**
     * Returns the CSS bundle for the form editor.<p>
     * 
     * @return the form CSS bundle 
     */
    private I_LayoutBundle.I_Style formCss() {

        return I_LayoutBundle.INSTANCE.form();
    }

    /**
     * Generates the attribute label.<p>
     */
    private void generateLabel() {

        HTML labelWidget = new HTML("<div title=\""
            + SafeHtmlUtils.htmlEscape(stripHtml(m_help))
            + "\" class=\""
            + formCss().label()
            + "\">"
            + m_label
            + "</div>");
        m_widgetHolder.add(labelWidget);
    }

    /**
     * Initializes the button styling.<p>
     */
    private void initButtons() {

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

        addMouseOverHandler(ValueFocusHandler.getInstance());
        addMouseOutHandler(ValueFocusHandler.getInstance());
        addMouseDownHandler(ValueFocusHandler.getInstance());
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
        helper.removeClassName(formCss().dragHelper());
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
