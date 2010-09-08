/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.junit;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.ProfileBuilder;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.impl.handler.TestEventExecuter;
import org.jboss.arquillian.impl.handler.TestLifecycleMethodExecuter;
import org.jboss.arquillian.spi.ClassContextAppender;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestContextAppender;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;


/**
 * VerifyLifecycleMethodExecutionVetoingTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class VerifyLifecycleMethodExecutionVetoingTestCase
{
   public static Map<String, Integer> containerCallbacks = new HashMap<String, Integer>();
   
   @After
   public void clean()
   {
      containerCallbacks.clear();
   }
   
   @Test
   public void shouldCallBeforeAfterIfHandlersAreActive() throws Exception
   {
      containerCallbacks.put("beforeClass", 0);
      containerCallbacks.put("before", 0);
      containerCallbacks.put("test", 0);
      containerCallbacks.put("after", 0);
      containerCallbacks.put("afterClass", 0);
      
      runArquillianTest(
            new TestContextAppender()
            {
               public void append(Context context)
               {
                  context.register(org.jboss.arquillian.spi.event.suite.Before.class, new TestLifecycleMethodExecuter());
                  context.register(org.jboss.arquillian.spi.event.suite.Test.class, new TestEventExecuter());
                  context.register(org.jboss.arquillian.spi.event.suite.After.class, new TestLifecycleMethodExecuter());
               }
            },
            new ClassContextAppender()
            {
               public void append(Context context)
               {
                  context.register(org.jboss.arquillian.spi.event.suite.BeforeClass.class, new TestLifecycleMethodExecuter());
                  context.register(org.jboss.arquillian.spi.event.suite.AfterClass.class, new TestLifecycleMethodExecuter());
               }
            }
      );

      assertCallback("beforeClass", 1);
      assertCallback("before", 1);
      assertCallback("test", 1);
      assertCallback("after", 1);
      assertCallback("afterClass", 1);
   }
   
   /*
    * Verify that the Statement execution chain is not broken when before/after execution is removed. 
    * Test should still be called.
    */
   @Test
   public void shouldCallTestIfBeforeAfterHandlersAreNotActive() throws Exception
   {
      containerCallbacks.put("beforeClass", 0);
      containerCallbacks.put("before", 0);
      containerCallbacks.put("test", 0);
      containerCallbacks.put("after", 0);
      containerCallbacks.put("afterClass", 0);
      
      runArquillianTest(
            new TestContextAppender()
            {
               public void append(Context context)
               {
                  context.register(org.jboss.arquillian.spi.event.suite.Test.class, new TestEventExecuter());
               }
            },
            new ClassContextAppender()
            {
               public void append(Context context)
               {
               }
            }
      );
      
      assertCallback("beforeClass", 0);
      assertCallback("before", 0);
      assertCallback("test", 1);
      assertCallback("after", 0);
      assertCallback("afterClass", 0);
   }

   protected void runArquillianTest(final TestContextAppender testContextAppender, final ClassContextAppender classContextAppender) throws InitializationError
   {
      RunNotifier notifier = new RunNotifier();
      Result testResult = new Result();
      notifier.addFirstListener(testResult.createListener());
      
      Arquillian arq = new Arquillian(BeforeAfterTestClass.class) {
         @Override
         protected TestRunnerAdaptor createTestRunnerAdaptor()
         {
            Configuration configuration = new Configuration();
            return DeployableTestBuilder.build(new ProfileBuilder()
            {
               public void buildSuiteContext(SuiteContext context) {}

               public void buildClassContext(ClassContext context, Class<?> testClass) 
               { 
                  classContextAppender.append(context);
               }
               
               public void buildTestContext(TestContext context, Object testInstance)
               {
                  testContextAppender.append(context);
               }
            }, 
            configuration);
         }
      };
      arq.run(notifier);
      notifier.fireTestRunFinished(testResult);
   }

   private void assertCallback(String name, Integer count)
   {
      Assert.assertEquals(count, containerCallbacks.get(name));
   }
   
   
   public static class BeforeAfterTestClass 
   {
      @BeforeClass
      public static void beforeClass()
      {
         wasCalled("beforeClass");
      }
      
      @AfterClass
      public static void afterClass()
      {
         wasCalled("afterClass");
      }

      @Before
      public void before() 
      {
         wasCalled("before");
      }
      
      @After
      public void after() 
      {
         wasCalled("after");
      }
      
      @Test
      public void test()
      {
         wasCalled("test");
      }
   }

   public static void wasCalled(String name) 
   {
      System.out.println("was called: " + name);
      if(containerCallbacks.containsKey(name))
      {
         containerCallbacks.put(name, containerCallbacks.get(name) + 1);
      }
      else 
      {
         throw new RuntimeException("Unknown callback: " + name);
      }
   }
}
