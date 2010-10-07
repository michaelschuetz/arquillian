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
package org.jboss.arquillian.container.jclouds.integration;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * JCloudsIntegrationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JCloudsIntegrationTestBase extends Arquillian
{
   @Deployment
   public static JavaArchive createDeployment() 
   {
      return ShrinkWrap.create(JavaArchive.class)
               .addPackage(ServiceManager.class.getPackage())
               .addManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }
   
   @Inject 
   private ServiceManager manager;
   
   @Test
   public void shouldBeAbleToExecuteAndInject() throws Exception
   {
      Assert.assertNotNull(manager);
      
      Assert.assertEquals("Hello", manager.sayHello());
   }

   @Test
   public void shouldBeAbleToExecuteAndInject2() throws Exception
   {
      Assert.assertNotNull(manager);
      
      Assert.assertEquals("Hello", manager.sayHello());
   }

   @Test
   public void shouldBeAbleToExecuteAndInject3() throws Exception
   {
      Assert.assertNotNull(manager);
      
      Assert.assertEquals("Hello", manager.sayHello());
   }

   @Test
   public void shouldBeAbleToExecuteAndInject4() throws Exception
   {
      Assert.assertNotNull(manager);
      
      Assert.assertEquals("Hello", manager.sayHello());
   }

   @Test
   public void shouldBeAbleToExecuteAndInject5() throws Exception
   {
      Assert.assertNotNull(manager);
      
      Assert.assertEquals("Hello", manager.sayHello());
   }
}
