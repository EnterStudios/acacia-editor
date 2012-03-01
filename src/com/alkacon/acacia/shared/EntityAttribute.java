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

package com.alkacon.acacia.shared;

import com.alkacon.vie.shared.I_Entity;
import com.alkacon.vie.shared.I_EntityAttribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serializable entity attribute implementation.<p>
 */
public class EntityAttribute implements I_EntityAttribute, Serializable {

    /** Serial version id. */
    private static final long serialVersionUID = 8283921354261037725L;

    /** The complex type values. */
    private List<Entity> m_entityValues;

    /** The attribute name. */
    private String m_name;

    /** The simple type values. */
    private List<String> m_simpleValues;

    /**
     * Constructor. For serialization only.<p>
     */
    protected EntityAttribute() {

    }

    /**
     * Creates a entity type attribute.<p>
     * 
     * @param name the attribute name
     * @param values the attribute values
     * 
     * @return the newly created attribute
     */
    public static EntityAttribute createEntityAttribute(String name, List<Entity> values) {

        EntityAttribute result = new EntityAttribute();
        result.m_name = name;
        result.m_entityValues = Collections.unmodifiableList(values);
        return result;
    }

    /**
     * Creates a simple type attribute.<p>
     * 
     * @param name the attribute name
     * @param values the attribute values
     * 
     * @return the newly created attribute
     */
    public static EntityAttribute createSimpleAttribute(String name, List<String> values) {

        EntityAttribute result = new EntityAttribute();
        result.m_name = name;
        result.m_simpleValues = Collections.unmodifiableList(values);
        return result;
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#getAttributeName()
     */
    public String getAttributeName() {

        return m_name;
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#getComplexValue()
     */
    public I_Entity getComplexValue() {

        return m_entityValues.get(0);
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#getComplexValues()
     */
    public List<I_Entity> getComplexValues() {

        List<I_Entity> result = new ArrayList<I_Entity>();
        result.addAll(m_entityValues);
        return Collections.unmodifiableList(result);
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#getSimpleValue()
     */
    public String getSimpleValue() {

        return m_simpleValues.get(0);
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#getSimpleValues()
     */
    public List<String> getSimpleValues() {

        return Collections.unmodifiableList(m_simpleValues);
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#getValueCount()
     */
    public int getValueCount() {

        if (isComplexValue()) {
            return m_entityValues.size();
        }
        return m_simpleValues.size();
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#isComplexValue()
     */
    public boolean isComplexValue() {

        return m_entityValues != null;
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#isSimpleValue()
     */
    public boolean isSimpleValue() {

        return m_simpleValues != null;
    }

    /**
     * @see com.alkacon.vie.shared.I_EntityAttribute#isSingleValue()
     */
    public boolean isSingleValue() {

        if (isComplexValue()) {
            return m_entityValues.size() == 1;
        }
        return m_simpleValues.size() == 1;
    }
}
