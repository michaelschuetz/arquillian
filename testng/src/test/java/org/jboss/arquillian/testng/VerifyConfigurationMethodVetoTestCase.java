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
package org.jboss.arquillian.testng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IInvokedMethodListener2;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * VerifyConfigurationMethodVetoTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class VerifyConfigurationMethodVetoTestCase
{

   @org.junit.Test
   public void shouldVetoConfigurationMethods() throws Exception
   {
      XmlSuite suite = new XmlSuite();
      suite.setName("Arquillian");
      suite.setAnnotations(AnnotationTypeEnum.JDK.getName());

      XmlTest test = new XmlTest(suite);
      test.setName("Arquillian");
      List<XmlClass> testClasses = new ArrayList<XmlClass>();
      XmlClass testClass = new XmlClass(TestClass.class);
      testClasses.add(testClass);
      test.setXmlClasses(testClasses);

      
      TestNG runner = new TestNG(true);
      runner.setVerbose(0);
      runner.setJUnit(false);
      runner.setXmlSuites(
            Arrays.asList(suite));
      runner.run();
   }
   
   public static class ConfigurableListener implements IHookable, IConfigurable, IInvokedMethodListener, ISuiteListener, ITestListener
   {
      public ConfigurableListener()
      {
         System.out.println("Created Arquillian");
      }
      
      
      /* (non-Javadoc)
       * @see org.testng.IHookable#run(org.testng.IHookCallBack, org.testng.ITestResult)
       */
      public void run(IHookCallBack callBack, ITestResult testResult)
      {
         System.out.println("arquillian.test");
         callBack.runTestMethod(testResult);
      }

      /* (non-Javadoc)
       * @see org.testng.IConfigurable#run(org.testng.IConfigureCallBack, org.testng.ITestResult)
       */
      public void run(IConfigureCallBack paramIConfigureCallBack, ITestResult paramITestResult)
      {
         //System.out.println(paramIConfigureCallBack + " " + paramITestResult);
         //System.out.println("Configurable" + " " + paramITestResult.getMethod().getMethodName());
         paramIConfigureCallBack.runConfigurationMethod(paramITestResult);
      }
      
      /* (non-Javadoc)
       * @see org.testng.IMethodInterceptor#intercept(java.util.List, org.testng.ITestContext)
       */
      public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context)
      {
         return methods; 
      }
      
      /* (non-Javadoc)
       * @see org.testng.IInvokedMethodListener#beforeInvocation(org.testng.IInvokedMethod, org.testng.ITestResult)
       */
      public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
      {
         //System.out.println("beforeInvocation " + method.getTestMethod());
      }

      /* (non-Javadoc)
       * @see org.testng.IInvokedMethodListener#afterInvocation(org.testng.IInvokedMethod, org.testng.ITestResult)
       */
      public void afterInvocation(IInvokedMethod method, ITestResult testResult)
      {
         //System.out.println("afterInvocation " + method);
      }
   
      /* (non-Javadoc)
       * @see org.testng.ISuiteListener#onStart(org.testng.ISuite)
       */
      public void onStart(ISuite suite)
      {
         System.out.println("arquillian.beforeSuite");
      }
      
      /* (non-Javadoc)
       * @see org.testng.ISuiteListener#onFinish(org.testng.ISuite)
       */
      public void onFinish(ISuite suite)
      {
         System.out.println("arquillian.afterSuite");
      }
      
      /* (non-Javadoc)
       * @see org.testng.ITestListener#onStart(org.testng.ITestContext)
       */
      public void onStart(ITestContext context)
      {
         System.out.println("arquillian.beforeClass");
      }
      
      /* (non-Javadoc)
       * @see org.testng.ITestListener#onFinish(org.testng.ITestContext)
       */
      public void onFinish(ITestContext context)
      {
         System.out.println("arquillian.afterClass");         
      }
      
      /* (non-Javadoc)
       * @see org.testng.ITestListener#onTestStart(org.testng.ITestResult)
       */
      public void onTestStart(ITestResult result)
      {
      }
      
      /* (non-Javadoc)
       * @see org.testng.ITestListener#onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
       */
      public void onTestFailedButWithinSuccessPercentage(ITestResult result)
      {
      }
      
      /* (non-Javadoc)
       * @see org.testng.ITestListener#onTestFailure(org.testng.ITestResult)
       */
      public void onTestFailure(ITestResult result)
      {
      }
      
      /* (non-Javadoc)
       * @see org.testng.ITestListener#onTestSkipped(org.testng.ITestResult)
       */
      public void onTestSkipped(ITestResult result)
      {
      }
      
      /* (non-Javadoc)
       * @see org.testng.ITestListener#onTestSuccess(org.testng.ITestResult)
       */
      public void onTestSuccess(ITestResult result)
      {
      }
   }
   
   
   
   @Listeners({ConfigurableListener.class})
   public static class TestClass //extends ConfigurableListener 
   {
      @BeforeSuite
      public void beforeSuite() 
      {
         System.out.println("beforeSuite");
      }

      @BeforeSuite
      public void beforeSuite2() 
      {
         System.out.println("beforeSuite2");
      }

//      @AfterSuite
//      public void afterSuite() 
//      {
//         System.out.println("afterSuite");
//      }
//      
//      @BeforeClass
//      public void beforeClass() 
//      {
//         System.out.println("beforeClass");
//      }
//
//      @AfterClass
//      public void afterClass() 
//      {
//         System.out.println("afterClass");
//      }
//
//      @BeforeGroups
//      public void beforeGroups() 
//      {
//         System.out.println("beforeGroups");
//      }
//
//      @AfterGroups
//      public void afterGroups() 
//      {
//         System.out.println("afterGroups");
//      }
//
//      @BeforeMethod
//      public void beforeMethod() 
//      {
//         System.out.println("beforeMethod");
//      }
//
//      @AfterMethod
//      public void afterMethod() 
//      {
//         System.out.println("afterMethod");
//      }

      @Test
      public void test()
      {
         System.out.println("test");
      }
   }
}
