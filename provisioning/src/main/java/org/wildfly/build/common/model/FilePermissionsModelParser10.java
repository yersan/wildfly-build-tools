/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.build.common.model;

import org.wildfly.build.util.BuildPropertyReplacer;
import org.wildfly.build.util.xml.ParsingUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Eduardo Martins
 */
public class FilePermissionsModelParser10 {

    public static final String ELEMENT_LOCAL_NAME = "file-permissions";

    enum Element {

        // default unknown element
        UNKNOWN(null),
        PERMISSION("permission"),
        FILTER(FileFilterModelParser10.ELEMENT_LOCAL_NAME),
        ;

        private static final Map<String, Element> elements;

        static {
            Map<String, Element> elementsMap = new HashMap<>();
            elementsMap.put(Element.PERMISSION.getLocalName(), Element.PERMISSION);
            elementsMap.put(Element.FILTER.getLocalName(), Element.FILTER);
            elements = elementsMap;
        }

        static Element of(QName qName) {
            final Element element = elements.get(qName.getLocalPart());
            return element == null ? UNKNOWN : element;
        }

        private final String name;

        Element(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        public String getLocalName() {
            return name;
        }
    }

    enum Attribute {

        // default unknown attribute
        UNKNOWN(null),
        VALUE("value"),
        ;

        private static final Map<String, Attribute> attributes;

        static {
            Map<String, Attribute> attributesMap = new HashMap<>();
            attributesMap.put(VALUE.getLocalName(), VALUE);
            attributes = attributesMap;
        }

        static Attribute of(QName qName) {
            final Attribute attribute = attributes.get(qName.getLocalPart());
            return attribute == null ? UNKNOWN : attribute;
        }

        private final String name;

        Attribute(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        public String getLocalName() {
            return name;
        }
    }

    private final BuildPropertyReplacer propertyReplacer;
    private final FileFilterModelParser10 fileFilterModelParser;

    public FilePermissionsModelParser10(BuildPropertyReplacer propertyReplacer) {
        this(propertyReplacer, new FileFilterModelParser10(propertyReplacer));
    }

    public FilePermissionsModelParser10(BuildPropertyReplacer propertyReplacer, FileFilterModelParser10 fileFilterModelParser) {
        this.propertyReplacer = propertyReplacer;
        this.fileFilterModelParser = fileFilterModelParser;
    }

    public void parseFilePermissions(final XMLStreamReader reader, final List<FilePermission> result) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PERMISSION:
                            parsePermission(reader, result);
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    protected void parsePermission(XMLStreamReader reader, List<FilePermission> result) throws XMLStreamException {
        String permission = null;
        final Set<Attribute> required = EnumSet.of(Attribute.VALUE);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            required.remove(attribute);
            switch (attribute) {
                case VALUE:
                    permission = propertyReplacer.replaceProperties(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (!required.isEmpty()) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), required);
        }

        FilePermission filePermission = new FilePermission(permission);
        result.add(filePermission);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case FILTER:
                            fileFilterModelParser.parseFilter(reader, filePermission.getFilters());
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

}
