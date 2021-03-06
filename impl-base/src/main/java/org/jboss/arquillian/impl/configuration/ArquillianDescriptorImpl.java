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
package org.jboss.arquillian.impl.configuration;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.api.ContainerDef;
import org.jboss.arquillian.impl.configuration.api.DefaultProtocolDef;
import org.jboss.arquillian.impl.configuration.api.GroupDef;
import org.jboss.arquillian.impl.configuration.api.ProtocolDef;
import org.jboss.shrinkwrap.descriptor.api.DescriptorExportException;
import org.jboss.shrinkwrap.descriptor.api.Node;
import org.jboss.shrinkwrap.descriptor.impl.base.NodeProviderImplBase;
import org.jboss.shrinkwrap.descriptor.impl.base.XMLExporter;
import org.jboss.shrinkwrap.descriptor.spi.DescriptorExporter;

/**
 * ArquillianDescriptor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDescriptorImpl extends NodeProviderImplBase implements ArquillianDescriptor
{
   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private Node model;
   
   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public ArquillianDescriptorImpl()
   {
      this(new Node("arquillian"));
   }
   
   public ArquillianDescriptorImpl(Node model)
   {
      this.model = model;
   }

   //-------------------------------------------------------------------------------------||
   // API --------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#defaultProtocol()
    */
   @Override
   public DefaultProtocolDef defaultProtocol(String type)
   {
      return new DefaultProtocolDefImpl(model, model.getOrCreate("protocol")).setType(type);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#getDefaultProtocol()
    */
   @Override
   public DefaultProtocolDef getDefaultProtocol()
   {
      if(model.getSingle("protocol") != null)
      {
         return new DefaultProtocolDefImpl(model, model.getSingle("protocol"));
      }
      return null;
   }
   
   public ContainerDef container(String name) 
   {
      return new ContainerDefImpl(model, model.create("container")).setContainerName(name);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#group(java.lang.String)
    */
   @Override
   public GroupDef group(String name)
   {
      return new GroupDefImpl(model, model.create("group")).setGroupName(name);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#getContainers()
    */
   @Override
   public List<ContainerDef> getContainers()
   {
      List<ContainerDef> containers = new ArrayList<ContainerDef>();
      for(Node container : model.get("container"))
      {
         containers.add(new ContainerDefImpl(model, container));
      }
      return containers;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#getGroups()
    */
   @Override
   public List<GroupDef> getGroups()
   {
      List<GroupDef> groups = new ArrayList<GroupDef>();
      for(Node group : model.get("group"))
      {
         groups.add(new GroupDefImpl(model, group));
      }
      return groups;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - SchemaDescriptorProvider --------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.shrinkwrap.descriptor.spi.NodeProvider#getRootNode()
    */
   @Override
   public Node getRootNode()
   {
      return model;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.shrinkwrap.descriptor.impl.base.NodeProviderImplBase#getExporter()
    */
   @Override
   protected DescriptorExporter getExporter()
   {
      return new XMLExporter();
   }
}
