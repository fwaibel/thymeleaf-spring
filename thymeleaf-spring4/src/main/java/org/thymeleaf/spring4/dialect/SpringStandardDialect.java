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
package org.thymeleaf.spring4.dialect;

import java.util.LinkedHashSet;
import java.util.Set;

import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.spring4.expression.SPELVariableExpressionEvaluator;
import org.thymeleaf.spring4.expression.SpringStandardConversionService;
import org.thymeleaf.spring4.expression.SpringStandardExpressionObjectFactory;
import org.thymeleaf.spring4.processor.SpringActionTagProcessor;
import org.thymeleaf.spring4.processor.SpringErrorClassTagProcessor;
import org.thymeleaf.spring4.processor.SpringErrorsTagProcessor;
import org.thymeleaf.spring4.processor.SpringHrefTagProcessor;
import org.thymeleaf.spring4.processor.SpringInputCheckboxFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringInputFileFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringInputGeneralFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringInputPasswordFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringInputRadioFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringMethodTagProcessor;
import org.thymeleaf.spring4.processor.SpringObjectTagProcessor;
import org.thymeleaf.spring4.processor.SpringOptionFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringOptionInSelectFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringSelectFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringSrcTagProcessor;
import org.thymeleaf.spring4.processor.SpringTextareaFieldTagProcessor;
import org.thymeleaf.spring4.processor.SpringTranslationDocTypeProcessor;
import org.thymeleaf.spring4.processor.SpringValueTagProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardConversionService;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.processor.StandardActionTagProcessor;
import org.thymeleaf.standard.processor.StandardHrefTagProcessor;
import org.thymeleaf.standard.processor.StandardMethodTagProcessor;
import org.thymeleaf.standard.processor.StandardObjectTagProcessor;
import org.thymeleaf.standard.processor.StandardSrcTagProcessor;
import org.thymeleaf.standard.processor.StandardValueTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * <p>
 *   SpringStandard Dialect. This is the class containing the implementation of Thymeleaf Standard Dialect, including all
 *   <tt>th:*</tt> processors, expression objects, etc. for Spring-enabled environments.
 * </p>
 * <p>
 *   Note this dialect uses <strong>SpringEL</strong> as an expression language and adds some Spring-specific
 *   features on top of {@link StandardDialect}, like <tt>th:field</tt> or Spring-related expression objects.
 * </p>
 * <p>
 *   The usual and recommended way of using this dialect is by instancing {@link org.thymeleaf.spring4.SpringTemplateEngine}
 *   instead of {@link org.thymeleaf.TemplateEngine}. The former will automatically add this dialect and perform
 *   some specific configuration like e.g. Spring-integrated message resolution.
 * </p>
 * <p>
 *   Note a class with this name existed since 1.0, but it was completely reimplemented
 *   in Thymeleaf 3.0
 * </p>
 *
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 3.0.0
 *
 */
public class SpringStandardDialect extends StandardDialect {

    public static final String NAME = "SpringStandard";
    public static final String PREFIX = "th";
    public static final int PROCESSOR_PRECEDENCE = 1000;



    private final IExpressionObjectFactory SPRING_STANDARD_EXPRESSION_OBJECTS_FACTORY = new SpringStandardExpressionObjectFactory();
    private final IStandardConversionService SPRING_STANDARD_CONVERSION_SERVICE = new SpringStandardConversionService();
    
    
    
    
    public SpringStandardDialect() {
        super(NAME, PREFIX, PROCESSOR_PRECEDENCE);
    }




    @Override
    public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
        return SPELVariableExpressionEvaluator.INSTANCE;
    }



    @Override
    public IStandardConversionService getConversionService() {
        return SPRING_STANDARD_CONVERSION_SERVICE;
    }


    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return SPRING_STANDARD_EXPRESSION_OBJECTS_FACTORY;
    }




    @Override
    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        return createSpringStandardProcessorsSet(this, dialectPrefix);
    }




    /**
     * <p>
     *   Create a the set of SpringStandard processors, all of them freshly instanced.
     * </p>
     *
     * @param dialect the dialect these processors will be created for
     * @param dialectPrefix the prefix established for the Standard Dialect, needed for initialization
     * @return the set of SpringStandard processors.
     */
    public static Set<IProcessor> createSpringStandardProcessorsSet(final IProcessorDialect dialect, final String dialectPrefix) {
        /*
         * It is important that we create new instances here because, if there are
         * several dialects in the TemplateEngine that extend StandardDialect, they should
         * not be returning the exact same instances for their processors in order
         * to allow specific instances to be directly linked with their owner dialect.
         */

        final Set<IProcessor> standardProcessors = StandardDialect.createStandardProcessorsSet(dialect, dialectPrefix);

        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>(40);


        /*
         * REMOVE STANDARD PROCESSORS THAT WE WILL REPLACE
         */
        for (final IProcessor standardProcessor : standardProcessors) {
            // There are several processors we need to remove from the Standard Dialect set
            if (!(standardProcessor instanceof StandardObjectTagProcessor) &&
                    !(standardProcessor instanceof StandardActionTagProcessor) &&
                    !(standardProcessor instanceof StandardHrefTagProcessor) &&
                    !(standardProcessor instanceof StandardMethodTagProcessor) &&
                    !(standardProcessor instanceof StandardSrcTagProcessor) &&
                    !(standardProcessor instanceof StandardValueTagProcessor)) {

                processors.add(standardProcessor);

            } else if (standardProcessor.getTemplateMode() != TemplateMode.HTML) {
                // We only want to remove from the StandardDialect the HTML versions of the attribute processors
                processors.add(standardProcessor);
            }
        }


        /*
         * ATTRIBUTE TAG PROCESSORS
         */
        processors.add(new SpringActionTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringHrefTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringMethodTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringSrcTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringValueTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringObjectTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringErrorsTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringInputGeneralFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringInputPasswordFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringInputCheckboxFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringInputRadioFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringInputFileFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringSelectFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringOptionInSelectFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringOptionFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringTextareaFieldTagProcessor(dialect, dialectPrefix));
        processors.add(new SpringErrorClassTagProcessor(dialect, dialectPrefix));

        /*
         * DOCTYPE PROCESSORS
         */
        processors.add(new SpringTranslationDocTypeProcessor(dialect));

        return processors;

    }


}
