/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.spring4.processor;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring4.requestdata.RequestDataValueProcessorUtils;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.unbescape.html.HtmlEscape;


/**
 * 
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.0.0
 *
 */
public final class SpringValueTagProcessor extends AbstractStandardExpressionAttributeTagProcessor {


    // This is 1010 in order to make sure it is executed after "name" and "type"
    public static final int ATTR_PRECEDENCE = 1010;
    public static final String ATTR_NAME = "value";



    public SpringValueTagProcessor(final IProcessorDialect dialect, final String dialectPrefix) {
        super(dialect, TemplateMode.HTML, dialectPrefix, ATTR_NAME, ATTR_PRECEDENCE, false);
    }



    @Override
    protected final void doProcess(
            final ITemplateContext context,
            final IProcessableElementTag tag,
            final AttributeName attributeName, final String attributeValue,
            final String attributeTemplateName, final int attributeLine, final int attributeCol,
            final Object expressionResult,
            final IElementTagStructureHandler structureHandler) {

        String newAttributeValue = HtmlEscape.escapeHtml4Xml(expressionResult == null ? "" : expressionResult.toString());

        // Let RequestDataValueProcessor modify the attribute value if needed, but only in the case we don't also have
        // a 'th:field' - in such case, we will let th:field do its job
        if (!tag.getAttributes().hasAttribute(attributeName.getPrefix(), AbstractSpringFieldTagProcessor.ATTR_NAME)) {

            // We will need to know the 'name' and 'type' attribute values in order to (potentially) modify the 'value'
            final String nameValue = tag.getAttributes().getValue("name");
            final String typeValue = tag.getAttributes().getValue("type");

            newAttributeValue =
                    RequestDataValueProcessorUtils.processFormFieldValue(context, nameValue, newAttributeValue, typeValue);

        }

        // Set the 'value' attribute
        tag.getAttributes().replaceAttribute(attributeName, ATTR_NAME, (newAttributeValue == null? "" : newAttributeValue));

    }

    
}
