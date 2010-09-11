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
package org.jboss.arquillian.testng;

import java.lang.reflect.Method;

import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.impl.XmlConfigurationBuilder;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.jboss.arquillian.spi.util.TestEnrichers;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

/**
 * Arquillian TestNG integration entry point. Register this as a listener on the TestClass using the @Listener
 * annotation for in the suite xml file. Will call into Arquillian in the appropriate lifecycle methods.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class Arquillian implements IHookable, IConfigurable
{
   public static final String ARQUILLIAN_DATA_PROVIDER = "ARQUILLIAN_DATA_PROVIDER";
   
   private static ThreadLocal<TestRunnerAdaptor> deployableTest = new ThreadLocal<TestRunnerAdaptor>();

   /**
    * Based on the Configuration Annotation used on the current method, we call back Arquillian
    * in the correct lifecycle. Not all TestNG configuration methods are currently supported. 
    */
   /* (non-Javadoc)
    * @see org.testng.IConfigurable#run(org.testng.IConfigureCallBack, org.testng.ITestResult)
    */
   public void run(IConfigureCallBack callBack, ITestResult testResult)
   {
      try
      {
         ITestNGMethod currentMethod = testResult.getMethod();
         if(currentMethod.isBeforeSuiteConfiguration())
         {
            arquillianBeforeSuite(callBack, testResult);
         }
         else if(currentMethod.isAfterSuiteConfiguration())
         {
            arquillianAfterSuite(callBack, testResult);
         }
         else if(currentMethod.isBeforeClassConfiguration())
         {
            arquillianBeforeClass(callBack, testResult);
         }
         else if(currentMethod.isAfterClassConfiguration())
         {
            arquillianAfterClass(callBack, testResult);
         }
         else if(currentMethod.isBeforeMethodConfiguration())
         {
            arquillianBeforeTest(callBack, testResult);
         }
         else if(currentMethod.isAfterMethodConfiguration())
         {
            arquillianAfterTest(callBack, testResult);
         }
         else
         {
            throw new RuntimeException(
                  "Unknown Configuration annotation used, Arquillian does not know how to handle " +
                  "found annotations " + testResult.getMethod().getMethod().getAnnotations() + 
                  " on " + testResult.getMethod().getMethod() + ". "  +
                  "Supported annotations are " + getSupportedConfigurationMethods() + ".");
         }
      } 
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   }
   
   protected void arquillianBeforeSuite(IConfigureCallBack callBack, ITestResult testResult) throws Exception
   {
      if(deployableTest.get() == null)
      {
         Configuration configuration = new XmlConfigurationBuilder().build();
         TestRunnerAdaptor adaptor = DeployableTestBuilder.build(configuration);
         adaptor.beforeSuite(); 
         deployableTest.set(adaptor); // don't set TestRunnerAdaptor if beforeSuite fails
      }
   }

   protected void arquillianAfterSuite(IConfigureCallBack callBack, ITestResult testResult) throws Exception
   {
      if (deployableTest.get() == null) 
      {
         return; // beforeSuite failed
      }
      deployableTest.get().afterSuite();
      deployableTest.set(null);
   }

   protected void arquillianBeforeClass(IConfigureCallBack callBack, ITestResult testResult) throws Exception
   {
      deployableTest.get().beforeClass(
            testResult.getTestClass().getRealClass(), 
            new ConfigurationLifecycleMethodExecuter(callBack, testResult));
   }

   protected void arquillianAfterClass(IConfigureCallBack callBack, ITestResult testResult) throws Exception
   {
      deployableTest.get().afterClass(
            testResult.getTestClass().getRealClass(), 
            new ConfigurationLifecycleMethodExecuter(callBack, testResult));
   }
   
   protected void arquillianBeforeTest(IConfigureCallBack callBack, ITestResult testResult) throws Exception 
   {
      deployableTest.get().before(
            testResult.getInstance(), 
            testResult.getMethod().getMethod(),  // not the TestMethod
            new ConfigurationLifecycleMethodExecuter(callBack, testResult));
   }

   protected void arquillianAfterTest(IConfigureCallBack callBack, ITestResult testResult) throws Exception 
   {
      deployableTest.get().after(            
            testResult.getInstance(), 
            testResult.getMethod().getMethod(), // not the TestMethod
            new ConfigurationLifecycleMethodExecuter(callBack, testResult));

   }

   public void run(final IHookCallBack callback, final ITestResult testResult)
   {
      TestResult result;
      try
      {
         result = deployableTest.get().test(new TestMethodExecuter(callback, testResult));
               
         if(result.getThrowable() != null)
         {
            testResult.setThrowable(result.getThrowable());
         }
         // calculate test end time. this is overwritten in the testng invoker.. 
         testResult.setEndMillis( (result.getStart() - result.getEnd()) + testResult.getStartMillis());
      } 
      catch (Exception e)
      {
         testResult.setThrowable(e);
      }
   }
   
   @DataProvider(name = Arquillian.ARQUILLIAN_DATA_PROVIDER)
   public Object[][] arquillianArgumentProvider(Method method) 
   {
      Object[][] values = new Object[1][method.getParameterTypes().length];
      
      if (deployableTest.get() == null)
      {
         return values;
      }

      Object[] parameterValues = TestEnrichers.enrich(deployableTest.get().getActiveContext(), method);
      values[0] = parameterValues; 
      
      return values;
   }
   
   /**
    * Helper to write out the supported Configuration methods for TestNG.
    * 
    * @return All Supported Configuration annotations.
    */
   private String getSupportedConfigurationMethods() 
   {
      return "@" + BeforeSuite.class.getSimpleName() + " " + 
             "@" + AfterSuite.class.getSimpleName() + " " +
             "@" + BeforeClass.class.getSimpleName() + " " +
             "@" + AfterClass.class.getSimpleName() + " " +
             "@" + BeforeMethod.class.getSimpleName() + " " +
             "@" + AfterMethod.class.getSimpleName();
   }
   
   private static class TestMethodExecuter implements TestMethodExecutor
   {
      private IHookCallBack callBack;
      private ITestResult testResult;

      public TestMethodExecuter(IHookCallBack callBack, ITestResult testResult)
      {
         this.callBack = callBack;
         this.testResult = testResult;
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.TestMethodExecutor#getInstance()
       */
      public Object getInstance()
      {
         return testResult.getInstance();
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.TestMethodExecutor#getMethod()
       */
      public Method getMethod()
      {
         return testResult.getMethod().getMethod();
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.LifecycleMethodExecutor#invoke()
       */
      public void invoke() throws Throwable
      {
         callBack.runTestMethod(testResult);
         clearParameters(testResult);
      }

      private void clearParameters(final ITestResult testResult)
      {
         // clear parameters. they can be contextual and might fail TestNG during the report writing.
         Object[] parameters = testResult.getParameters();
         for(int i = 0; parameters != null && i < parameters.length; i++)
         {
            Object parameter = parameters[i];
            if(parameter != null)
            {
               parameters[i] = parameter.getClass().getName();
            }
            else
            {
               parameters[i] = "null";
            }
         }
      }
   }
   
   private static class ConfigurationLifecycleMethodExecuter implements LifecycleMethodExecutor
   {
      private IConfigureCallBack callBack;
      private ITestResult testResult;
      
      public ConfigurationLifecycleMethodExecuter(IConfigureCallBack callBack, ITestResult testResult)
      {
         this.callBack = callBack;
         this.testResult = testResult;
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.LifecycleMethodExecutor#invoke()
       */
      public void invoke() throws Throwable
      {
         callBack.runConfigurationMethod(testResult);
      }
   }
}
