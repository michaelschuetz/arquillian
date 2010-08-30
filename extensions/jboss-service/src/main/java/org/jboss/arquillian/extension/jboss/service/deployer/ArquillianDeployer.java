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
package org.jboss.arquillian.extension.jboss.service.deployer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnvironmentRefsGroupMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;

/**
 * ArquillianDeployer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDeployer extends AbstractDeployer
{
   private static String JNDI_NAME = ArquillianDeployer.class.getSimpleName();
   
   private String boundDeployment = "";
   private Map<String, String> registry;
   
   public ArquillianDeployer()
   {
      registry = new HashMap<String, String>();
      addInput(JBossMetaData.class);
      addInput(JBossWebMetaData.class);
      print("Constructed");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.deployer.helpers.AbstractDeployer#undeploy(org.jboss.deployers.structure.spi.DeploymentUnit)
    */
   @Override
   public void undeploy(DeploymentUnit unit)
   {
      try
      {
         unregisterContext(unit.getName());
      }
      catch (Exception e)
      {
         print(e.getMessage());
      }
   }
   
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      print(unit.getName());

      List<EnvironmentEntryMetaData> enc = extractEJBInfo(unit);
      extractServletInfo(unit, enc);
      
      try
      {
         registerContext(unit.getName());
      }
      catch (Exception e)
      {
         print(e.getMessage());
      }
      
   }

   private void extractServletInfo(DeploymentUnit unit, List<EnvironmentEntryMetaData> enc)
   {
      JBossWebMetaData metaData = unit.getAttachment(JBossWebMetaData.class);
      if(metaData != null)
      {
         // add Arquillian Bean JNDI names lookup
         
         JBossEnvironmentRefsGroupMetaData refGroup = (JBossEnvironmentRefsGroupMetaData)metaData.getJndiEnvironmentRefsGroup();
         EnvironmentEntriesMetaData entries =  refGroup.getEnvironmentEntries();
         if(entries == null)
         {
            entries = new EnvironmentEntriesMetaData();
         }
         for(EnvironmentEntryMetaData entry : enc)
         {
            
            entries.add(entry);
         }
         refGroup.setEnvironmentEntries(entries);
         
         if(metaData.getServletMappings() != null)
         {
            for(ServletMappingMetaData servletMappingMetaData : metaData.getServletMappings())
            {
               registry.put(
                     findServletClassForName(
                           servletMappingMetaData.getServletName(), 
                           metaData.getServlets()
                     ), 
                     metaData.getContextRoot() + "/" + servletMappingMetaData.getUrlPatterns());
               registry.put(
                     servletMappingMetaData.getServletName(), 
                     metaData.getContextRoot() + "/");
            }
         }
      }
   }
   
   private String findServletClassForName(String servletName, JBossServletsMetaData servlets)
   {
      for(JBossServletMetaData servletMetaData : servlets)
      {
         if(servletName.equals(servletMetaData.getServletName()))
         {
            return servletMetaData.getServletClass();
         }
      }
      return null;
   }

   private List<EnvironmentEntryMetaData> extractEJBInfo(DeploymentUnit unit)
   {
      List<EnvironmentEntryMetaData> enc = new ArrayList<EnvironmentEntryMetaData>();
      
      JBossMetaData metaData = unit.getAttachment(JBossMetaData.class);
      if(metaData == null)
      {
         return enc;
      }
      JBossEnterpriseBeansMetaData beansMetaData = metaData.getEnterpriseBeans();
      if(beansMetaData == null)
      {
         return enc;
      }
      Iterator<JBossEnterpriseBeanMetaData> beansIterator = beansMetaData.iterator();
      
      while(beansIterator.hasNext())
      {
         JBossEnterpriseBeanMetaData beanMetaData  = beansIterator.next();
         if(JBossSessionBeanMetaData.class.isInstance(beanMetaData)) 
         {
            JBossSessionBeanMetaData sessionMetaData = JBossSessionBeanMetaData.class.cast(beanMetaData);
            if(sessionMetaData.getBusinessLocals() != null)
            {
               for(String localInterface : sessionMetaData.getBusinessLocals())
               {
                  EnvironmentEntryMetaData bean = new EnvironmentEntryMetaData();
                  bean.setEnvEntryName("Arquillian_" + localInterface);
                  bean.setValue(sessionMetaData.getLocalJndiName());
                  bean.setType(java.lang.String.class.getName());
                  enc.add(bean);
               }
            }
            if(sessionMetaData.getBusinessRemotes() != null)
            {
               for(String remoteInterface : sessionMetaData.getBusinessRemotes())
               {
                  EnvironmentEntryMetaData bean = new EnvironmentEntryMetaData();
                  bean.setEnvEntryName("Arquillian_" + remoteInterface);
                  bean.setValue(sessionMetaData.getJndiName());
                  bean.setType(java.lang.String.class.getName());
                  enc.add(bean);
               }
            }
         }
      }
      return enc;
   }
   
   private void print(String line)
   {
      System.out.println(ArquillianDeployer.class.getSimpleName() + " " + line);
   }
   
   /**
    * @param name
    */
   private void registerContext(String name) throws Exception
   {
      new InitialContext().bind(JNDI_NAME, registry);
      boundDeployment = name;
   }

   /**
    * @param name
    */
   private void unregisterContext(String name) throws Exception
   {
      if(boundDeployment.equals(name)) 
      {
         new InitialContext().unbind(JNDI_NAME);
      }
   }

}
