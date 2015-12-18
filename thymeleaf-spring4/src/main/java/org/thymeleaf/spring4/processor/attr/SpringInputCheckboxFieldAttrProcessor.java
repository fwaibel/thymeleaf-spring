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
package org.thymeleaf.spring4.processor.attr;

import java.util.Map;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.support.BindStatus;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.NestableNode;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.spring4.SelectedValueComparator;
import org.thymeleaf.spring4.requestdata.RequestDataValueProcessorUtils;


/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 1.0
 *
 */
public final class SpringInputCheckboxFieldAttrProcessor 
        extends AbstractSpringFieldAttrProcessor {

    
    public static final String CHECKBOX_INPUT_TYPE_ATTR_VALUE = "checkbox";
    

    
    public SpringInputCheckboxFieldAttrProcessor() {
        super(ATTR_NAME,
              INPUT_TAG_NAME,
              INPUT_TYPE_ATTR_NAME,
              CHECKBOX_INPUT_TYPE_ATTR_VALUE);
    }




    @Override
    protected ProcessorResult doProcess(final Arguments arguments,
            final Element element, final String attributeName, final String attributeValue, 
            final BindStatus bindStatus, final Map<String, Object> localVariables) {

        String name = bindStatus.getExpression();
        name = (name == null? "" : name);
        
        final String id = computeId(arguments, element, name, true);
        
        String value = null;
        boolean checked = false;

        Object boundValue = bindStatus.getValue();
        final Class<?> valueType = bindStatus.getValueType();


        if (Boolean.class.equals(valueType) || boolean.class.equals(valueType)) {
            
            if (boundValue instanceof String) {
                boundValue = Boolean.valueOf((String) boundValue);
            }
            final Boolean booleanValue = (boundValue != null ? (Boolean) boundValue : Boolean.FALSE);
            value = "true";
            checked = booleanValue.booleanValue();
            
        } else {
            
            value = element.getAttributeValue("value");
            if (value == null) {
                throw new TemplateProcessingException(
                        "Attribute \"value\" is required in \"input(checkbox)\" tags " +
                        "when binding to non-boolean values");
            }
            
            checked = SelectedValueComparator.isSelected(bindStatus, value);
            
        }
        
        final NestableNode parent = element.getParent();
        
        final Element inputElement = (Element) element.cloneNode(parent, false);
        inputElement.removeAttribute(attributeName);
        
        inputElement.setAttribute("id", id);
        inputElement.setAttribute("name", name);
        inputElement.setAttribute(
                "value",
                RequestDataValueProcessorUtils.processFormFieldValue(
                        arguments.getConfiguration(), arguments, name, value, "checkbox"));
        if (checked) {
            inputElement.setAttribute("checked", "checked");
        } else {
            inputElement.removeAttribute("checked");
        }
        inputElement.setAllNodeLocalVariables(localVariables);

        parent.insertBefore(element, inputElement);


        if (!isDisabled(inputElement)) {

            final String hiddenName = WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + name;
            final String hiddenValue = "on";
            final Element hiddenElement = new Element("input");
            hiddenElement.setAttribute("type", "hidden");
            hiddenElement.setAttribute("name", hiddenName);
            hiddenElement.setAttribute(
                    "value",
                    RequestDataValueProcessorUtils.processFormFieldValue(
                            arguments.getConfiguration(), arguments, hiddenName, hiddenValue, "hidden"));
            hiddenElement.setAllNodeLocalVariables(localVariables);

            parent.insertBefore(element, hiddenElement);

        }


        parent.removeChild(element);
        
        return ProcessorResult.OK;         
        
    }


    private static final boolean isDisabled(final Element inputElement) {
        // Disabled = attribute "disabled" exists
        return inputElement.hasNormalizedAttribute("disabled");
    }

    

}
