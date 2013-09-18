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
import com.alkacon.acacia.client.ui.InlineEntityWidget;
import com.alkacon.acacia.client.ui.ValuePanel;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.shared.TabInfo;
import com.alkacon.acacia.shared.Type;
import com.alkacon.geranium.client.ui.FlowPanel;
import com.alkacon.geranium.client.ui.TabbedPanel;
import com.alkacon.geranium.client.ui.TabbedPanel.TabbedPanelStyle;
import com.alkacon.geranium.client.util.PositionBean;
import com.alkacon.vie.client.I_Vie;
import com.alkacon.vie.shared.I_Entity;
import com.alkacon.vie.shared.I_EntityAttribute;
import com.alkacon.vie.shared.I_Type;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Panel;

/**
 * Renders the widgets for an in-line form.<p>
 */
public class Renderer implements I_EntityRenderer {

    /**
     * Handles the size of a tabbed panel.<p>
     */
    protected class TabSizeHandler implements SelectionHandler<Integer>, ValueChangeHandler<I_Entity>, ResizeHandler {

        /** The context panel. */
        private Panel m_context;

        /** The tabbed panel. */
        private TabbedPanel<FlowPanel> m_tabbedPanel;

        /**
         * Constructor.<p>
         * 
         * @param tabbedPanel the tabbed panel
         * @param context the context panel
         */
        public TabSizeHandler(TabbedPanel<FlowPanel> tabbedPanel, Panel context) {

            m_tabbedPanel = tabbedPanel;
            m_context = context;
        }

        /**
         * @see com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google.gwt.event.logical.shared.ResizeEvent)
         */
        public void onResize(ResizeEvent event) {

            triggerHeightAdjustment();
        }

        /**
         * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
         */
        public void onSelection(SelectionEvent<Integer> event) {

            triggerHeightAdjustment();
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<I_Entity> event) {

            triggerHeightAdjustment();
        }

        /**
         * Adjusts the tabbed panel height to the height of the current tab content.<p> 
         */
        protected void adjustContextHeight() {

            int tabIndex = m_tabbedPanel.getSelectedIndex();
            FlowPanel tab = m_tabbedPanel.getWidget(tabIndex);
            int height = PositionBean.getInnerDimensions(tab.getElement()).getHeight();
            m_context.getElement().getStyle().setHeight(50 + height, Unit.PX);
        }

        /**
         * Triggers the tab panel height adjustment scheduled after the browsers event loop.
         */
        private void triggerHeightAdjustment() {

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    adjustContextHeight();
                }
            });
        }
    }

    /**
     * The widget value change handler.<p>
     */
    protected class WidgetChangeHandler implements ValueChangeHandler<String> {

        /** The attribute handler. */
        private AttributeHandler m_attributeHandler;

        /** The value index. */
        private int m_valueIndex;

        /**
         * Constructor.<p>
         * 
         * @param attributeHandler the attribute handler
         * @param valueIndex the value index, only relevant for in-line rendering
         */
        protected WidgetChangeHandler(AttributeHandler attributeHandler, int valueIndex) {

            m_attributeHandler = attributeHandler;
            m_valueIndex = valueIndex;
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            m_attributeHandler.handleValueChange(m_valueIndex, event.getValue());
        }
    }

    /** The entity CSS class. */
    public static final String ENTITY_CLASS = I_LayoutBundle.INSTANCE.form().entity();

    /** The attribute label CSS class. */
    public static final String LABEL_CLASS = I_LayoutBundle.INSTANCE.form().label();

    /** The renderer name. */
    public static final String RENDERER_NAME = "default";

    /** The widget holder CSS class. */
    public static final String WIDGET_HOLDER_CLASS = I_LayoutBundle.INSTANCE.form().widgetHolder();

    /** The VIE instance. */
    I_Vie m_vie;

    /** The widget service. */
    I_WidgetService m_widgetService;

    /**
     * Constructor.<p>
     * 
     * @param vie the VIE instance
     * @param widgetService the widget service
     */
    public Renderer(I_Vie vie, I_WidgetService widgetService) {

        m_vie = vie;
        m_widgetService = widgetService;
    }

    /**
     * Gets the paths of nested choice attributes starting from a given type.<p>
     *  
     * @param attributeType the type from which to start 
     * @param startingAtChoiceAttribute true if the attribute is a synthetic CHOICE_ATTRIBUTE
     * 
     * @return the list of nested choice attribute name paths  
     */
    public static List<ChoiceMenuEntryBean> getChoiceEntries(I_Type attributeType, boolean startingAtChoiceAttribute) {

        ChoiceMenuEntryBean rootEntry = new ChoiceMenuEntryBean(null);
        collectChoiceEntries(attributeType, startingAtChoiceAttribute, rootEntry);
        return rootEntry.getChildren();
    }

    /**
     * Sets the attribute choices if present.<p>
     * 
     * @param widgetService the widget service to use 
     * @param valueWidget the value widget
     * @param attributeType the attribute type
     */
    public static void setAttributeChoice(
        I_WidgetService widgetService,
        AttributeValueView valueWidget,
        I_Type attributeType) {

        if (attributeType.isChoice()) {
            List<ChoiceMenuEntryBean> menuEntries = getChoiceEntries(attributeType, false);
            for (ChoiceMenuEntryBean menuEntry : menuEntries) {
                valueWidget.addChoice(widgetService, menuEntry);
            }
        }
    }

    /**
     * Recursive helper method to create a tree structure of choice menu entries for a choice type.<p>
     * 
     * @param startType the type from which to start
     * @param startingAtChoiceAttribute true if the recursion starts at a synthetic choice attribute
     * @param currentEntry the current menu entry bean  
     */
    private static void collectChoiceEntries(
        I_Type startType,
        boolean startingAtChoiceAttribute,
        ChoiceMenuEntryBean currentEntry) {

        if (startingAtChoiceAttribute || startType.isChoice()) {
            I_Type choiceType = startingAtChoiceAttribute
            ? startType
            : startType.getAttributeType(Type.CHOICE_ATTRIBUTE_NAME);
            for (String choiceName : choiceType.getAttributeNames()) {
                ChoiceMenuEntryBean subEntry = currentEntry.addChild(choiceName);
                I_Type includedType = choiceType.getAttributeType(choiceName);
                collectChoiceEntries(includedType, false, subEntry);
            }
        }
    }

    /**
     * @see com.alkacon.acacia.client.I_EntityRenderer#configure(java.lang.String)
     */
    public I_EntityRenderer configure(String configuration) {

        return this;
    }

    /**
     * @see com.alkacon.acacia.client.I_EntityRenderer#getName()
     */
    public String getName() {

        return RENDERER_NAME;
    }

    /**
     * @see com.alkacon.acacia.client.I_EntityRenderer#renderAttributeValue(com.alkacon.vie.shared.I_Entity, com.alkacon.acacia.client.AttributeHandler, int, com.google.gwt.user.client.ui.Panel)
     */
    public void renderAttributeValue(
        I_Entity parentEntity,
        AttributeHandler attributeHandler,
        int attributeIndex,
        Panel context) {

        I_Type entityType = m_vie.getType(parentEntity.getTypeName());
        I_Type attributeType = attributeHandler.getAttributeType();
        String attributeName = attributeHandler.getAttributeName();
        int minOccurrence = entityType.getAttributeMinOccurrence(attributeName);
        I_EntityAttribute attribute = parentEntity.getAttribute(attributeName);
        if ((attribute == null) && (minOccurrence > 0)) {
            attribute = createEmptyAttribute(parentEntity, attributeName, minOccurrence);
        }

        ValuePanel attributeElement = new ValuePanel();
        context.add(attributeElement);
        context.addStyleName(ENTITY_CLASS);
        RootHandler parentHandler = new RootHandler();
        parentHandler.setHandler(attributeIndex, attributeName, attributeHandler);
        attributeHandler.setSingleValueIndex(attributeIndex);
        String label = m_widgetService.getAttributeLabel(attributeName);
        String help = m_widgetService.getAttributeHelp(attributeName);
        if (attribute != null) {
            I_EntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeName, attributeType);
            AttributeValueView valueWidget = new AttributeValueView(attributeHandler, label, help);
            if (attributeType.isChoice() && (entityType.getAttributeMaxOccurrence(attributeName) == 1)) {
                valueWidget.setCollapsed(true);
            }
            attributeElement.add(valueWidget);
            if (attribute.isSimpleValue()) {
                valueWidget.setValueWidget(
                    m_widgetService.getAttributeFormWidget(attributeName),
                    attribute.getSimpleValues().get(attributeIndex),
                    m_widgetService.getDefaultAttributeValue(attributeName),
                    true);
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    // widget should be displayed in compact view, using only 50% of the available width
                    valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_FIRST_COLUMN);
                } else {
                    if (m_widgetService.isDisplaySingleLine(attributeName)) {
                        valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_SINGLE_LINE);
                    }
                }
            } else {
                valueWidget.setValueEntity(renderer, attribute.getComplexValues().get(attributeIndex));
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_NESTED);
                }
            }
            setAttributeChoice(valueWidget, attributeType);
        }
        attributeHandler.updateButtonVisisbility();
    }

    /**
     * @see com.alkacon.acacia.client.I_EntityRenderer#renderForm(com.alkacon.vie.shared.I_Entity, java.util.List, com.google.gwt.user.client.ui.Panel, com.alkacon.acacia.client.I_AttributeHandler, int)
     */
    @SuppressWarnings("unchecked")
    public TabbedPanel<FlowPanel> renderForm(
        I_Entity entity,
        List<TabInfo> tabInfos,
        Panel context,
        I_AttributeHandler parentHandler,
        int attributeIndex) {

        if ((tabInfos == null) || (tabInfos.size() < 2)) {
            renderForm(entity, context, parentHandler, attributeIndex);
            return null;
        } else {

            context.getElement().getStyle().setHeight(600, Unit.PX);
            context.getElement().setAttribute("typeof", entity.getTypeName());
            context.getElement().setAttribute("about", entity.getId());
            context.getElement().getStyle().setPadding(0, Unit.PX);
            TabbedPanel<FlowPanel> tabbedPanel = new TabbedPanel<FlowPanel>(TabbedPanelStyle.classicTabs);
            final TabSizeHandler tabSizeHandler = new TabSizeHandler(tabbedPanel, context);
            tabbedPanel.addSelectionHandler(tabSizeHandler);
            if (entity instanceof HasValueChangeHandlers) {
                ((HasValueChangeHandlers<I_Entity>)entity).addValueChangeHandler(tabSizeHandler);
            }
            // adjust the tab panel height after a delay as some widgets may need time to initialize
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                private int counter = 0;

                /**
                 * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
                 */
                public boolean execute() {

                    tabSizeHandler.adjustContextHeight();
                    counter++;
                    return counter < 6;
                }
            }, 200);
            AttributeHandler.setResizeHandler(tabSizeHandler);
            tabbedPanel.getElement().getStyle().setBorderWidth(0, Unit.PX);
            Iterator<TabInfo> tabIt = tabInfos.iterator();
            TabInfo currentTab = tabIt.next();
            TabInfo nextTab = tabIt.next();
            FlowPanel tabPanel = createTab();
            tabbedPanel.addNamed(tabPanel, currentTab.getTabName(), currentTab.getTabId());
            I_Type entityType = m_vie.getType(entity.getTypeName());
            List<String> attributeNames = entityType.getAttributeNames();
            AttributeValueView lastCompactView = null;
            boolean collapsed = currentTab.isCollapsed()
                && ((nextTab != null) && attributeNames.get(1).endsWith("/" + nextTab.getStartName()));
            for (final String attributeName : attributeNames) {
                if ((nextTab != null) && attributeName.endsWith("/" + nextTab.getStartName())) {
                    currentTab = nextTab;
                    nextTab = tabIt.hasNext() ? tabIt.next() : null;
                    tabPanel = createTab();
                    tabbedPanel.addNamed(tabPanel, currentTab.getTabName(), currentTab.getTabId());
                    // check if the tab content may be collapsed
                    if (currentTab.isCollapsed()) {
                        int currentIndex = attributeNames.indexOf(attributeName);
                        collapsed = ((currentIndex + 1) == attributeNames.size())
                            || ((nextTab != null) && attributeNames.get(currentIndex + 1).endsWith(
                                "/" + nextTab.getStartName()));
                    }
                    if (lastCompactView != null) {
                        // previous widget was set to first column mode,
                        // revert that as no following widget will occupy the second column
                        lastCompactView.setCompactMode(AttributeValueView.COMPACT_MODE_WIDE);
                    }
                }
                AttributeHandler handler = new AttributeHandler(m_vie, entity, attributeName, m_widgetService);
                parentHandler.setHandler(attributeIndex, attributeName, handler);
                I_Type attributeType = entityType.getAttributeType(attributeName);
                int minOccurrence = entityType.getAttributeMinOccurrence(attributeName);
                I_EntityAttribute attribute = entity.getAttribute(attributeName);
                // only single complex values may be collapsed
                if (collapsed
                    && (attribute != null)
                    && !attributeType.isSimpleType()
                    && (minOccurrence == 1)
                    && (entityType.getAttributeMaxOccurrence(attributeName) == 1)) {
                    I_EntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeName, attributeType);
                    renderer.renderForm(attribute.getComplexValue(), tabPanel, handler, 0);
                } else {
                    ValuePanel attributeElement = new ValuePanel();
                    tabPanel.add(attributeElement);
                    if ((attribute == null) && (minOccurrence > 0)) {
                        attribute = createEmptyAttribute(entity, attributeName, minOccurrence);
                    }
                    lastCompactView = renderAttribute(
                        entityType,
                        attributeType,
                        attribute,
                        handler,
                        attributeElement,
                        attributeName,
                        lastCompactView);
                }
                handler.updateButtonVisisbility();
            }
            if (lastCompactView != null) {
                // previous widget was set to first column mode,
                // revert that as no following widget will occupy the second column
                lastCompactView.setCompactMode(AttributeValueView.COMPACT_MODE_WIDE);
            }
            context.add(tabbedPanel);
            return tabbedPanel;
        }
    }

    /**
     * @see com.alkacon.acacia.client.I_EntityRenderer#renderForm(com.alkacon.vie.shared.I_Entity, com.google.gwt.user.client.ui.Panel, com.alkacon.acacia.client.I_AttributeHandler, int)
     */
    public void renderForm(I_Entity entity, Panel context, I_AttributeHandler parentHandler, int attributeIndex) {

        context.addStyleName(ENTITY_CLASS);
        context.getElement().setAttribute("typeof", entity.getTypeName());
        context.getElement().setAttribute("about", entity.getId());
        I_Type entityType = m_vie.getType(entity.getTypeName());
        AttributeValueView lastCompactView = null;
        if (entityType.isChoice()) {
            I_EntityAttribute attribute = entity.getAttribute(Type.CHOICE_ATTRIBUTE_NAME);
            assert (attribute != null) && attribute.isComplexValue() : "a choice type must have a choice attribute";
            AttributeHandler handler = new AttributeHandler(m_vie, entity, Type.CHOICE_ATTRIBUTE_NAME, m_widgetService);
            parentHandler.setHandler(attributeIndex, Type.CHOICE_ATTRIBUTE_NAME, handler);
            ValuePanel attributeElement = new ValuePanel();
            for (I_Entity choiceEntity : attribute.getComplexValues()) {
                I_Type choiceType = m_vie.getType(choiceEntity.getTypeName());
                List<I_EntityAttribute> choiceAttributes = choiceEntity.getAttributes();
                assert (choiceAttributes.size() == 1) && choiceAttributes.get(0).isSingleValue() : "each choice entity may only have a single attribute with a single value";
                I_EntityAttribute choiceAttribute = choiceAttributes.get(0);
                I_Type attributeType = choiceType.getAttributeType(choiceAttribute.getAttributeName());
                I_EntityRenderer renderer = m_widgetService.getRendererForAttribute(
                    choiceAttribute.getAttributeName(),
                    attributeType);
                String label = m_widgetService.getAttributeLabel(choiceAttribute.getAttributeName());
                String help = m_widgetService.getAttributeHelp(choiceAttribute.getAttributeName());
                context.add(attributeElement);
                AttributeValueView valueWidget = new AttributeValueView(handler, label, help);
                attributeElement.add(valueWidget);
                if (choiceAttribute.isSimpleValue()) {
                    valueWidget.setValueWidget(
                        m_widgetService.getAttributeFormWidget(choiceAttribute.getAttributeName()),
                        choiceAttribute.getSimpleValue(),
                        m_widgetService.getDefaultAttributeValue(choiceAttribute.getAttributeName()),
                        true);
                    if (m_widgetService.isDisplaySingleLine(choiceAttribute.getAttributeName())) {
                        valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_SINGLE_LINE);
                    }
                } else {
                    valueWidget.setValueEntity(renderer, choiceAttribute.getComplexValue());
                    if (m_widgetService.isDisplayCompact(choiceAttribute.getAttributeName())) {
                        valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_NESTED);
                    }
                }
                setAttributeChoice(valueWidget, entityType);
            }
            handler.updateButtonVisisbility();
        } else {
            List<String> attributeNames = entityType.getAttributeNames();
            for (String attributeName : attributeNames) {
                int minOccurrence = entityType.getAttributeMinOccurrence(attributeName);
                I_EntityAttribute attribute = entity.getAttribute(attributeName);
                if ((attribute == null) && (minOccurrence > 0)) {
                    attribute = createEmptyAttribute(entity, attributeName, minOccurrence);
                }
                I_Type attributeType = entityType.getAttributeType(attributeName);
                ValuePanel attributeElement = new ValuePanel();
                context.add(attributeElement);
                AttributeHandler handler = new AttributeHandler(m_vie, entity, attributeName, m_widgetService);
                parentHandler.setHandler(attributeIndex, attributeName, handler);
                lastCompactView = renderAttribute(
                    entityType,
                    attributeType,
                    attribute,
                    handler,
                    attributeElement,
                    attributeName,
                    lastCompactView);
            }
        }
        if (lastCompactView != null) {
            // previous widget was set to first column mode,
            // revert that as no following widget will occupy the second column
            lastCompactView.setCompactMode(AttributeValueView.COMPACT_MODE_WIDE);
        }
    }

    /**
     * @see com.alkacon.acacia.client.I_EntityRenderer#renderInline(com.alkacon.vie.shared.I_Entity, com.alkacon.acacia.client.I_InlineFormParent, com.alkacon.acacia.client.I_InlineHtmlUpdateHandler)
     */
    public void renderInline(I_Entity entity, I_InlineFormParent formParent, I_InlineHtmlUpdateHandler updateHandler) {

        I_Type entityType = m_vie.getType(entity.getTypeName());
        List<String> attributeNames = entityType.getAttributeNames();
        for (String attributeName : attributeNames) {
            I_Type attributeType = entityType.getAttributeType(attributeName);
            I_EntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeName, attributeType);
            renderer.renderInline(
                entity,
                attributeName,
                formParent,
                updateHandler,
                entityType.getAttributeMinOccurrence(attributeName),
                entityType.getAttributeMaxOccurrence(attributeName));
        }
    }

    /**
     * @see com.alkacon.acacia.client.I_EntityRenderer#renderInline(com.alkacon.vie.shared.I_Entity, java.lang.String, com.alkacon.acacia.client.I_InlineFormParent, com.alkacon.acacia.client.I_InlineHtmlUpdateHandler, int, int)
     */
    public void renderInline(
        I_Entity parentEntity,
        String attributeName,
        I_InlineFormParent formParent,
        I_InlineHtmlUpdateHandler updateHandler,
        int minOccurrence,
        int maxOccurrence) {

        I_EntityAttribute attribute = parentEntity.getAttribute(attributeName);
        if (attribute != null) {
            List<Element> elements = m_vie.getAttributeElements(parentEntity, attributeName, formParent.getElement());
            if (!elements.isEmpty()) {
                AttributeHandler handler = new AttributeHandler(m_vie, parentEntity, attributeName, m_widgetService);
                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);
                    if (attribute.isSimpleValue()) {
                        I_EditWidget widget = m_widgetService.getAttributeInlineWidget(
                            attributeName,
                            (com.google.gwt.user.client.Element)element);
                        widget.addValueChangeHandler(new WidgetChangeHandler(handler, i));
                        formParent.adoptWidget(widget);
                    } else {
                        InlineEntityWidget.createWidgetForEntity(
                            element,
                            formParent,
                            parentEntity,
                            handler,
                            i,
                            updateHandler,
                            m_widgetService);
                    }
                }
            } else if (attribute.isComplexValue()) {
                for (I_Entity entity : attribute.getComplexValues()) {
                    renderInline(entity, formParent, updateHandler);
                }
            }
        } else {
            List<Element> elements = m_vie.getAttributeElements(parentEntity, attributeName, formParent.getElement());
            if (!elements.isEmpty() && (elements.size() == 1)) {
                AttributeHandler handler = new AttributeHandler(m_vie, parentEntity, attributeName, m_widgetService);
                InlineEntityWidget.createWidgetForEntity(
                    elements.get(0),
                    formParent,
                    parentEntity,
                    handler,
                    -1,
                    updateHandler,
                    m_widgetService);
            }
        }

    }

    /**
     * Creates an empty attribute.<p>
     * 
     * @param parentEntity the parent entity
     * @param attributeName the attribute name
     * @param minOccurrence the minimum occurrence of the attribute
     * 
     * @return the entity attribute
     */
    protected I_EntityAttribute createEmptyAttribute(I_Entity parentEntity, String attributeName, int minOccurrence) {

        I_EntityAttribute result = null;
        I_Type attributeType = m_vie.getType(parentEntity.getTypeName()).getAttributeType(attributeName);
        if (attributeType.isSimpleType()) {
            for (int i = 0; i < minOccurrence; i++) {
                parentEntity.addAttributeValue(attributeName, m_widgetService.getDefaultAttributeValue(attributeName));
            }
            result = parentEntity.getAttribute(attributeName);
        } else {
            for (int i = 0; i < minOccurrence; i++) {
                parentEntity.addAttributeValue(attributeName, m_vie.createEntity(null, attributeType.getId()));
            }
            result = parentEntity.getAttribute(attributeName);
        }
        return result;
    }

    /** 
     * Creates a tab.<p>
     * 
     * @return the created tab 
     */
    private FlowPanel createTab() {

        FlowPanel tabPanel;
        tabPanel = new FlowPanel();
        tabPanel.addStyleName(ENTITY_CLASS);
        tabPanel.addStyleName(I_LayoutBundle.INSTANCE.form().formParent());
        tabPanel.getElement().getStyle().setMargin(0, Unit.PX);
        return tabPanel;
    }

    /**
     * Renders a single attribute.<p>
     * 
     * @param entityType the type of the entity containing the attribute 
     * @param attributeType the attribute type
     * @param attribute the attribute, or null if not set
     * @param handler the attribute handler
     * @param attributeElement the attribute parent element
     * @param attributeName the attribute name
     * @param lastCompactView the previous attribute view that was rendered in compact mode if present
     *  
     * @return the last attribute view that was rendered in compact mode if present
     */
    private AttributeValueView renderAttribute(
        I_Type entityType,
        I_Type attributeType,
        I_EntityAttribute attribute,
        AttributeHandler handler,
        ValuePanel attributeElement,
        String attributeName,
        AttributeValueView lastCompactView) {

        String label = m_widgetService.getAttributeLabel(attributeName);
        String help = m_widgetService.getAttributeHelp(attributeName);
        if (attribute != null) {
            I_EntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeName, attributeType);
            for (int i = 0; i < attribute.getValueCount(); i++) {
                AttributeValueView valueWidget = new AttributeValueView(handler, label, help);
                if (attributeType.isChoice() && (entityType.getAttributeMaxOccurrence(attributeName) == 1)) {
                    valueWidget.setCollapsed(true);
                }
                attributeElement.add(valueWidget);
                if (attribute.isSimpleValue()) {
                    valueWidget.setValueWidget(
                        m_widgetService.getAttributeFormWidget(attributeName),
                        attribute.getSimpleValues().get(i),
                        m_widgetService.getDefaultAttributeValue(attributeName),
                        true);
                    // check for compact view setting
                    if (m_widgetService.isDisplayCompact(attributeName)) {
                        // widget should be displayed in compact view, using only 50% of the available width
                        if (lastCompactView == null) {
                            // set mode to first column
                            valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_FIRST_COLUMN);
                            lastCompactView = valueWidget;
                        } else {
                            // previous widget is displayed as first column, set second column mode
                            valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_SECOND_COLUMN);
                            lastCompactView = null;
                        }
                    } else {
                        if (lastCompactView != null) {
                            // previous widget was set to first column mode,
                            // revert that as the current widget will be displayed in a new line
                            lastCompactView.setCompactMode(AttributeValueView.COMPACT_MODE_WIDE);
                            lastCompactView = null;
                        }
                        if (m_widgetService.isDisplaySingleLine(attributeName)) {
                            valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_SINGLE_LINE);
                        }
                    }
                } else {
                    valueWidget.setValueEntity(renderer, attribute.getComplexValues().get(i));
                    if (lastCompactView != null) {
                        // previous widget was set to first column mode,
                        // revert that as the current widget will be displayed in a new line
                        lastCompactView.setCompactMode(AttributeValueView.COMPACT_MODE_WIDE);
                        lastCompactView = null;
                    }
                    if (m_widgetService.isDisplayCompact(attributeName)) {
                        valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_NESTED);
                    }
                }
                setAttributeChoice(valueWidget, attributeType);
            }
        } else {
            AttributeValueView valueWidget = new AttributeValueView(handler, label, help);
            attributeElement.add(valueWidget);
            if (attributeType.isSimpleType()) {
                // create a deactivated widget, to add the attribute on click
                valueWidget.setValueWidget(
                    m_widgetService.getAttributeFormWidget(attributeName),
                    "",
                    m_widgetService.getDefaultAttributeValue(attributeName),
                    false);
                // check for compact view setting
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    // widget should be displayed in compact view, using only 50% of the available width
                    if (lastCompactView == null) {
                        // set mode to first column
                        valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_FIRST_COLUMN);
                        lastCompactView = valueWidget;
                    } else {
                        // previous widget is displayed as first column, set second column mode
                        valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_SECOND_COLUMN);
                        lastCompactView = null;
                    }
                } else {
                    if (lastCompactView != null) {
                        // previous widget was set to first column mode,
                        // revert that as the current widget will be displayed in a new line
                        lastCompactView.setCompactMode(AttributeValueView.COMPACT_MODE_WIDE);
                        lastCompactView = null;
                    }
                    if (m_widgetService.isDisplaySingleLine(attributeName)) {
                        valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_SINGLE_LINE);
                    }
                }
            } else {
                if (lastCompactView != null) {
                    // previous widget was set to first column mode,
                    // revert that as the current widget will be displayed in a new line
                    lastCompactView.setCompactMode(AttributeValueView.COMPACT_MODE_WIDE);
                    lastCompactView = null;
                }
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    valueWidget.setCompactMode(AttributeValueView.COMPACT_MODE_NESTED);
                }
            }
            setAttributeChoice(valueWidget, attributeType);
        }
        handler.updateButtonVisisbility();
        return lastCompactView;
    }

    /**
     * Sets the attribute choices if present.<p>
     * 
     * @param valueWidget the value widget
     * @param attributeType the attribute type
     */
    private void setAttributeChoice(AttributeValueView valueWidget, I_Type attributeType) {

        setAttributeChoice(m_widgetService, valueWidget, attributeType);
    }
}
