/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.testenricher.seam2;

import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Enricher that provide Seam2 class and method argument injection.
 *
 * @author <a href="mailto:michaelschuetz83@gmail.com">Michael Schuetz</a>
 * @version $Revision: $
 */
public class Seam2InjectionEnricher implements TestEnricher {
    private static final String ANNOTATION_NAME = "org.jboss.seam.annotations.In";

    private static final Logger log = Logger.getLogger(Seam2InjectionEnricher.class.getName());

    /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestEnricher#enrich(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
    public void enrich(Object testCase) {
        if (SecurityActions.isClassPresent(ANNOTATION_NAME)) {
            injectClass(testCase);
        }
    }

    /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestEnricher#resolve(org.jboss.arquillian.spi.Context, java.lang.reflect.Method)
    */
    public Object[] resolve(Method method) {
        return new Object[method.getParameterTypes().length];
    }

    protected void injectClass(Object testCase) {

        initializeSeam();

        // TODO handle return value
        Component.getInstance(testCase.getClass());
    }

    protected void initializeSeam() {
        // TODO use mock context as in AbstractSeamTest
        if (Contexts.getApplicationContext() == null) {
            Lifecycle.beginCall();
        }
    }
}

