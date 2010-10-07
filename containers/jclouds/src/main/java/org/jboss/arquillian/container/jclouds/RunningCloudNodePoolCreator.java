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
package org.jboss.arquillian.container.jclouds;

import java.util.Iterator;

import org.jboss.arquillian.container.jclouds.pool.Creator;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;

import com.google.common.base.Predicate;

/**
 * A {@link Creator} that can match up against 
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class RunningCloudNodePoolCreator extends ConnectedNodeCreator
{
   private String tag;
   
   private Iterator<? extends ComputeMetadata> foundNodes;
   
   public RunningCloudNodePoolCreator(ComputeServiceContext context, String tag)
   {
      super(context);
      this.tag = tag;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.jclouds.ConnectedNodeCreator#createNodeMetadata()
    */
   @Override
   public NodeMetadata createNodeMetadata()
   {
      synchronized (this)
      {
         if(foundNodes == null)
         {
            foundNodes = getComputeContext().getComputeService().listNodesDetailsMatching(
                  new Predicate<ComputeMetadata>()
                  {
                     public boolean apply(ComputeMetadata input)
                     {
                        if (input instanceof NodeMetadata)
                        {
                           NodeMetadata nodeMetadata = (NodeMetadata) input;
                           if(tag.equals(nodeMetadata.getTag()) && nodeMetadata.getState() == NodeState.RUNNING)
                           {
                              return true;
                           }
                        }
                        return false;
                     } 
                  }).iterator();
         }
         if(foundNodes.hasNext())
         {
            return (NodeMetadata)foundNodes.next();
         }
         else
         {
            throw new RuntimeException("Requested more nodes in pool then found in cloud");
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.jclouds.ConnectedNodeCreator#destroyNodeMetadata(org.jclouds.compute.domain.NodeMetadata)
    */
   @Override
   public void destroyNodeMetadata(NodeMetadata nodeMetadata)
   {
   }
}
