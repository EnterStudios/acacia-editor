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
import com.alkacon.acacia.client.I_InlineFormParent;
import com.alkacon.acacia.client.I_InlineHtmlUpdateHandler;
import com.alkacon.acacia.client.I_WidgetService;
import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.geranium.client.I_DescendantResizeHandler;
import com.alkacon.geranium.client.ui.HighlightingBorder;
import com.alkacon.geranium.client.ui.I_Button.ButtonStyle;
import com.alkacon.geranium.client.ui.Popup;
import com.alkacon.geranium.client.ui.PushButton;
import com.alkacon.geranium.client.ui.css.I_ImageBundle;
import com.alkacon.geranium.client.util.PositionBean;
import com.alkacon.vie.client.Entity;
import com.alkacon.vie.client.Vie;
import com.alkacon.vie.shared.I_Entity;
import com.alkacon.vie.shared.I_Type;

import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Widget allowing form based editing for parts of a content to enhance the in-line editing.<p>
 */
public class InlineEntityWidget extends Composite {

    /**
     * Flow panel with handling descendant resizes to reposition pop-up.<p>
     */
    protected class FormPanel extends FlowPanel implements I_DescendantResizeHandler {

        /**
         * Constructor.<p>
         */
        protected FormPanel() {

        }

        /**
         * @see com.alkacon.geranium.client.I_DescendantResizeHandler#onResizeDescendant()
         */
        public void onResizeDescendant() {

            positionPopup();
        }
    }

    /**
     * Timer to update the HTML.<p>
     */
    protected class UpdateTimer extends Timer {

        /** Indicates if the timer is scheduled. */
        private boolean m_scheduled;

        /**
         * @see com.google.gwt.user.client.Timer#cancel()
         */
        @Override
        public void cancel() {

            m_scheduled = false;
            super.cancel();
        }

        /**
         * Returns if the timer is already scheduled.<p>
         * 
         * @return <code>true</code> if the timer is scheduled
         */
        public boolean isScheduled() {

            return m_scheduled;
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            m_scheduled = false;
            runHtmlUpdate();
        }

        /**
         * @see com.google.gwt.user.client.Timer#schedule(int)
         */
        @Override
        public void schedule(int delayMillis) {

            m_scheduled = true;
            super.schedule(delayMillis);
        }
    }

    /**
     * The UI binder interface.<p>
     */
    interface InlineEntityWidgetUiBinder extends UiBinder<FlowPanel, InlineEntityWidget> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static InlineEntityWidgetUiBinder uiBinder = GWT.create(InlineEntityWidgetUiBinder.class);

    /** The add button. */
    @UiField
    protected AttributeChoiceWidget m_addButton;

    /** The attribute choice button. */
    @UiField
    protected AttributeChoiceWidget m_attributeChoice;

    /** The down button. */
    @UiField
    protected PushButton m_downButton;

    /** The injected button. */
    @UiField
    protected PushButton m_editButton;

    /** The remove button. */
    @UiField
    protected PushButton m_removeButton;

    /** The up button. */
    @UiField
    protected PushButton m_upButton;

    /** The highlighting border widget. */
    HighlightingBorder m_highlighting;

    /** The pop-up panel. */
    Popup m_popup;

    /** The handler of the attribute to edit. */
    private AttributeHandler m_attributeHandler;

    /** The attribute value index. */
    private int m_attributeIndex;

    /** The change handler registration. */
    private HandlerRegistration m_entityChangeHandlerRegistration;

    /** The parent widget. */
    private I_InlineFormParent m_formParent;

    /** Indicates if the content has been changed while the edit pop-up was shown. */
    private boolean m_hasChanges;

    /** Handles HTML updates if required. */
    private I_InlineHtmlUpdateHandler m_htmlUpdateHandler;

    /** A timer to update the overlay position. */
    private Timer m_overlayTimer;

    /** The parent of the entity to edit. */
    private I_Entity m_parentEntity;

    /** Flag indicating the popup has been closed. */
    private boolean m_popupClosed;

    /** The reference DOM element, will be highlighted during editing. */
    private Element m_referenceElement;

    /** Flag indicating it is required to open the edit popup aftera HTML update. */
    private boolean m_requireShowPopup;

    /** Flag indicating an HTML update is running. */
    private boolean m_runningUpdate;

    /** The dialog title. */
    private String m_title;

    /** Schedules the HTML update. */
    private UpdateTimer m_updateTimer;

    /** The widget service. */
    private I_WidgetService m_widgetService;

    /**
     * Constructor.<p>
     * 
     * @param referenceElement the reference DOM element, will be highlighted during editing
     * @param formParent the parent widget
     * @param parentEntity the parent of the entity to edit
     * @param attributeHandler the attribute handler
     * @param attributeIndex the attribute value index
     * @param htmlUpdateHandler handles HTML updates if required
     * @param widgetService the widget service
     */
    private InlineEntityWidget(
        Element referenceElement,
        I_InlineFormParent formParent,
        I_Entity parentEntity,
        AttributeHandler attributeHandler,
        int attributeIndex,
        I_InlineHtmlUpdateHandler htmlUpdateHandler,
        I_WidgetService widgetService) {

        initWidget(uiBinder.createAndBindUi(this));
        m_parentEntity = parentEntity;
        m_attributeHandler = attributeHandler;
        m_attributeIndex = attributeIndex;
        m_referenceElement = referenceElement;
        m_formParent = formParent;
        m_htmlUpdateHandler = htmlUpdateHandler;
        m_widgetService = widgetService;
        m_title = "";
        m_updateTimer = new UpdateTimer();
        m_popupClosed = true;
        initButtons();
        addDomHandler(ButtonBarHandler.INSTANCE, MouseOverEvent.getType());
        addDomHandler(ButtonBarHandler.INSTANCE, MouseOutEvent.getType());
    }

    /**
     * Creates the inline edit widget and injects it next to the context element.<p>
     * 
     * @param element the context element
     * @param formParent the parent widget
     * @param parentEntity the parent entity
     * @param attributeHandler the attribute handler
     * @param attributeIndex the attribute value index
     * @param htmlUpdateHandler handles HTML updates if required
     * @param widgetService the widget service
     * 
     * @return the widget instance
     */
    public static InlineEntityWidget createWidgetForEntity(
        Element element,
        I_InlineFormParent formParent,
        I_Entity parentEntity,
        AttributeHandler attributeHandler,
        int attributeIndex,
        I_InlineHtmlUpdateHandler htmlUpdateHandler,
        I_WidgetService widgetService) {

        InlineEntityWidget widget = new InlineEntityWidget(
            element,
            formParent,
            parentEntity,
            attributeHandler,
            attributeIndex,
            htmlUpdateHandler,
            widgetService);
        InlineEditOverlay.getRootOvelay().addButton(widget, element.getAbsoluteTop());
        attributeHandler.updateButtonVisibilty(widget);
        return widget;
    }

    /**
     * Returns the attribute value index.<p>
     * 
     * @return the attribute value index
     */
    public int getAttributeIndex() {

        return m_attributeIndex;
    }

    /**
     * Sets the visibility of the reference element highlighting border.<p>
     * 
     * @param visible <code>true</code> to show the highlighting
     */
    public void setContentHighlightingVisible(boolean visible) {

        if (visible) {
            if (m_highlighting == null) {
                m_highlighting = new HighlightingBorder(
                    PositionBean.getInnerDimensions(m_referenceElement),
                    HighlightingBorder.BorderColor.red);
                RootPanel.get().add(m_highlighting);
            } else {
                m_highlighting.setPosition(PositionBean.getInnerDimensions(m_referenceElement));
            }
        } else {
            if (m_highlighting != null) {
                m_highlighting.removeFromParent();
                m_highlighting = null;
            }
        }
    }

    /**
     * Updates the visibility of the add, remove, up and down buttons.<p>
     * 
     * @param hasEditButton <code>true</code> if the edit button should be visible
     * @param hasAddButton <code>true</code> if the add button should be visible
     * @param hasRemoveButton <code>true</code> if the remove button should be visible
     * @param hasSortButtons <code>true</code> if the sort buttons should be visible
     */
    public void updateButtonVisibility(
        boolean hasEditButton,
        boolean hasAddButton,
        boolean hasRemoveButton,
        boolean hasSortButtons) {

        if (hasEditButton) {
            m_editButton.getElement().getStyle().clearDisplay();
        } else {
            m_editButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        //        if (hasAddButton && m_isChoice) {
        //            m_attributeChoice.getElement().getStyle().clearDisplay();
        //        } else {
        m_attributeChoice.getElement().getStyle().setDisplay(Display.NONE);
        //        }
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
        if (hasSortButtons && (m_attributeIndex != 0)) {
            m_upButton.getElement().getStyle().clearDisplay();
        } else {
            m_upButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasSortButtons && (getElement().getNextSibling() != null)) {
            m_downButton.getElement().getStyle().clearDisplay();
        } else {
            m_downButton.getElement().getStyle().setDisplay(Display.NONE);
        }

        if (hasEditButton && (hasAddButton || hasRemoveButton || hasSortButtons)) {
            // set multi button mode
            addStyleName(I_LayoutBundle.INSTANCE.form().multiButtonBar());
            m_editButton.setImageClass(I_ImageBundle.INSTANCE.style().bullsEyeIcon());
        } else {
            removeStyleName(I_LayoutBundle.INSTANCE.form().multiButtonBar());
            m_editButton.setImageClass(I_ImageBundle.INSTANCE.style().editIcon());
        }
    }

    /**
     * Positions the widget button above the reference element.<p>
     */
    protected void positionWidget() {

        InlineEditOverlay.getRootOvelay().setButtonPosition(this, m_referenceElement.getAbsoluteTop());
    }

    /** Adds a new attribute value. */
    void addNewAttributeValue() {

        m_attributeHandler.addNewAttributeValue(m_attributeIndex);
        m_requireShowPopup = true;
        m_attributeIndex += 1;
        runHtmlUpdate();
    }

    /**
     * Repositions the edit overlay after the HTML has been updated.<p>
     */
    void afterHtmlUpdate() {

        if (m_overlayTimer != null) {
            m_overlayTimer.cancel();
            m_overlayTimer = null;
        }
        m_runningUpdate = false;
        List<Element> elements = Vie.getInstance().getAttributeElements(
            m_parentEntity,
            m_attributeHandler.getAttributeName(),
            m_formParent.getElement());
        if (m_popupClosed) {
            // the form popup has already been closed, reinitialize the editing widgets for updated HTML
            InlineEditOverlay.updateCurrentOverlayPosition();
            if (m_requireShowPopup) {
                if (elements.size() > m_attributeIndex) {
                    m_referenceElement = elements.get(m_attributeIndex);
                }
                showEditPopup(null);
                m_hasChanges = true;
            } else {
                InlineEditOverlay.getRootOvelay().clearButtonPanel();
                m_htmlUpdateHandler.reinitWidgets(m_formParent);
            }

        } else {
            if (m_referenceElement != null) {
                InlineEditOverlay.removeLastOverlay();
            }
            if (elements.size() > m_attributeIndex) {
                m_referenceElement = elements.get(m_attributeIndex);
                InlineEditOverlay.addOverlayForElement(m_referenceElement);
            } else {
                m_referenceElement = m_formParent.getElement();
                InlineEditOverlay.addOverlayForElement(m_referenceElement);
            }
        }
        // schedule to update the ovelay position
        m_overlayTimer = new Timer() {

            /** Timer run counter. */
            private int timerRuns = 0;

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                InlineEditOverlay.updateCurrentOverlayPosition();
                if (timerRuns > 3) {
                    cancel();
                }
                timerRuns++;
            }
        };
        m_overlayTimer.scheduleRepeating(100);
    }

    /**
     * Sets the changed flag.<p>
     */
    void onEntityChange() {

        if (m_updateTimer.isScheduled()) {
            m_updateTimer.cancel();
        }
        m_updateTimer.schedule(150);
        m_hasChanges = true;
    }

    /**
     * Cleanup after the edit pop-up was opened.<p>
     */
    void onPopupClose() {

        if (m_referenceElement != null) {
            InlineEditOverlay.removeLastOverlay();
        }
        InlineEditOverlay.updateCurrentOverlayPosition();
        if (m_entityChangeHandlerRegistration != null) {
            m_entityChangeHandlerRegistration.removeHandler();
        }
        AttributeHandler.setResizeHandler(null);
        if (!m_runningUpdate) {
            if (m_hasChanges) {
                InlineEditOverlay.getRootOvelay().clearButtonPanel();
                m_htmlUpdateHandler.reinitWidgets(m_formParent);
            }
        }
        m_popup = null;
    }

    /** Handles the remove attribute click.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_removeButton")
    void onRemoveClick(ClickEvent event) {

        m_attributeHandler.removeAttributeValue(m_attributeIndex);
        setContentHighlightingVisible(false);
        runHtmlUpdate();
    }

    /**
     * Positions the given pop-up relative to the reference element.<p>
     */
    void positionPopup() {

        if (m_referenceElement != null) {
            PositionBean referencePosition = PositionBean.getInnerDimensions(m_referenceElement);
            int currentTop = m_popup.getAbsoluteTop();
            int windowHeight = Window.getClientHeight();
            int scrollTop = Window.getScrollTop();
            int contentHeight = m_popup.getOffsetHeight();
            int top = referencePosition.getTop();
            if (((windowHeight + scrollTop) < (top + referencePosition.getHeight() + contentHeight + 20))
                && ((contentHeight + 40) < top)) {
                top = top - contentHeight - 5;
                if ((currentTop < top) && ((top - currentTop) < 200)) {
                    // keep the current position
                    top = currentTop;
                }
            } else {
                top = top + referencePosition.getHeight() + 5;
                if ((currentTop > top) && ((currentTop - top) < 200)) {
                    // keep the current position
                    top = currentTop;
                }
            }
            m_popup.center();
            m_popup.setPopupPosition(m_popup.getPopupLeft(), top);
            if (((contentHeight + top) - scrollTop) > windowHeight) {
                Window.scrollTo(Window.getScrollLeft(), ((contentHeight + top) - windowHeight) + 20);
            }
        } else {
            m_popup.center();
        }
    }

    /**
     * Updates the HTML according to the entity data.<p>
     */
    void runHtmlUpdate() {

        if (m_runningUpdate) {
            m_updateTimer.schedule(50);
        } else {
            m_runningUpdate = true;
            m_htmlUpdateHandler.updateHtml(m_formParent, new Command() {

                public void execute() {

                    afterHtmlUpdate();
                }
            });
        }
    }

    /**
     * Opens the form popup.<p>
     * 
     * @param clickEvent the click event
     */
    @UiHandler("m_editButton")
    void showEditPopup(ClickEvent clickEvent) {

        m_editButton.clearHoverState();
        m_popup = new Popup(m_title, -1);
        m_popup.setModal(true);
        m_popup.setAutoHideEnabled(true);
        m_popup.removePadding();
        m_popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                onPopupClose();
            }
        });
        m_hasChanges = false;
        m_requireShowPopup = false;
        m_entityChangeHandlerRegistration = ((Entity)m_parentEntity).addValueChangeHandler(new ValueChangeHandler<Entity>() {

            public void onValueChange(ValueChangeEvent<Entity> event) {

                onEntityChange();
            }
        });
        I_Type type = Vie.getInstance().getType(m_parentEntity.getTypeName());
        FlowPanel formPanel = new FormPanel();
        formPanel.setStyleName(I_LayoutBundle.INSTANCE.form().formParent());
        formPanel.getElement().getStyle().setMargin(0, Unit.PX);
        formPanel.getElement().getStyle().setBorderWidth(0, Unit.PX);
        formPanel.getElement().getStyle().setPropertyPx("minHeight", 30);
        m_popup.add(formPanel);
        m_popup.addDialogClose(null);
        I_LayoutBundle.INSTANCE.dialogCss().ensureInjected();
        m_popup.show();
        AttributeHandler.setScrollElement(formPanel.getElement());
        AttributeHandler.setResizeHandler(new ResizeHandler() {

            public void onResize(ResizeEvent event) {

                positionPopup();
            }
        });
        m_widgetService.getRendererForType(type).renderAttributeValue(
            m_parentEntity,
            m_attributeHandler,
            m_attributeIndex,
            formPanel);
        InlineEditOverlay.addOverlayForElement(m_referenceElement);
        positionPopup();
        m_popup.getElement().getStyle().setZIndex(I_LayoutBundle.INSTANCE.constants().css().zIndexPopup());
        m_popupClosed = false;
    }

    /**
     * Initializes the button styling.<p>
     */
    private void initButtons() {

        m_addButton.addChoice(
            m_attributeHandler.getWidgetService(),
            new ChoiceMenuEntryBean(m_attributeHandler.getAttributeName()),
            new AsyncCallback<ChoiceMenuEntryBean>() {

                public void onFailure(Throwable caught) {

                    // will not be called 

                }

                public void onSuccess(ChoiceMenuEntryBean selectedEntry) {

                    // nothing to do
                }
            });
        m_addButton.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_addButton.hide();
                addNewAttributeValue();
                event.preventDefault();
                event.stopPropagation();

            }
        }, ClickEvent.getType());

        m_editButton.setImageClass(I_ImageBundle.INSTANCE.style().editIcon());
        m_editButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_removeButton.setImageClass(I_ImageBundle.INSTANCE.style().removeIcon());
        m_removeButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_upButton.setImageClass(I_ImageBundle.INSTANCE.style().arrowUpIcon());
        m_upButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        m_downButton.setImageClass(I_ImageBundle.INSTANCE.style().arrowDownIcon());
        m_downButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);

        if (EditorBase.hasDictionary()) {
            String label = m_widgetService.getAttributeLabel(m_attributeHandler.getAttributeName());
            m_addButton.setTitle(EditorBase.getMessageForKey(EditorBase.GUI_VIEW_ADD_1, label));
            m_removeButton.setTitle(EditorBase.getMessageForKey(EditorBase.GUI_VIEW_DELETE_1, label));
            m_upButton.setTitle(EditorBase.getMessageForKey(EditorBase.GUI_VIEW_MOVE_UP_0));
            m_downButton.setTitle(EditorBase.getMessageForKey(EditorBase.GUI_VIEW_MOVE_DOWN_0));
            m_title = EditorBase.getMessageForKey(EditorBase.GUI_VIEW_EDIT_1, label);
            m_editButton.setTitle(m_title);
        }
    }
}
