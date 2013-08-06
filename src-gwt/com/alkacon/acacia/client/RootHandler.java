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

import com.alkacon.acacia.shared.ContentDefinition;
import com.alkacon.acacia.shared.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The root attribute handler.<p>
 */
public class RootHandler implements I_AttributeHandler {

    /** The sub handlers. */
    private List<Map<String, AttributeHandler>> m_handlers;

    /**
     * Constructor.<p>
     */
    public RootHandler() {

        m_handlers = new ArrayList<Map<String, AttributeHandler>>();
        m_handlers.add(new HashMap<String, AttributeHandler>());
    }

    /**
     * @see com.alkacon.acacia.client.I_AttributeHandler#getChildHandler(java.lang.String, int)
     */
    public AttributeHandler getChildHandler(String attributeName, int index) {

        if (m_handlers.size() > index) {
            return m_handlers.get(index).get(attributeName);
        }
        return null;
    }

    /**
     * Returns the attribute handler for the given path.<p>
     * 
     * @param pathNames the path names
     * 
     * @return the attribute handler
     */
    public AttributeHandler getHandlerByPath(String[] pathNames) {

        I_AttributeHandler handler = this;
        int index = 0;
        for (int i = 0; i < pathNames.length; i++) {
            String attributeName = pathNames[i];
            int nextIndex = ContentDefinition.extractIndex(attributeName);
            attributeName = ContentDefinition.removeIndex(attributeName);
            if ((handler instanceof AttributeHandler) && ((AttributeHandler)handler).getAttributeType().isChoice()) {
                // in case of a choice attribute, skip to the next level
                attributeName = Type.CHOICE_ATTRIBUTE_NAME;
            }
            handler = handler.getChildHandler(attributeName, index);
            index = nextIndex;
        }
        return (AttributeHandler)handler;
    }

    /**
     * @see com.alkacon.acacia.client.I_AttributeHandler#insertHandlers(int)
     */
    public void insertHandlers(int index) {

        if (index <= m_handlers.size()) {
            m_handlers.add(index, new HashMap<String, AttributeHandler>());
        } else {
            throw new IndexOutOfBoundsException("index of " + index + " too big, current size: " + m_handlers.size());
        }
    }

    /**
     * @see com.alkacon.acacia.client.I_AttributeHandler#removeHandlers(int)
     */
    public void removeHandlers(int index) {

        m_handlers.remove(index);
    }

    /**
     * @see com.alkacon.acacia.client.I_AttributeHandler#setHandler(int, java.lang.String, com.alkacon.acacia.client.AttributeHandler)
     */
    public void setHandler(int index, String attributeName, AttributeHandler handler) {

        m_handlers.get(index).put(attributeName, handler);
        handler.setParentHandler(this);
    }

    /**
     * Initializes the sub handlers maps for the given value count.<p>
     * 
     * @param count the value count
     */
    protected void initHandlers(int count) {

        if (count == 0) {
            m_handlers.clear();
        } else {
            while (m_handlers.size() < count) {
                m_handlers.add(new HashMap<String, AttributeHandler>());
            }
        }
    }

}
