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
package org.jboss.arquillian.extension.jboss.service;

import java.io.File;

import org.jboss.arquillian.extension.jboss.service.deployer.ArquillianDeployer;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

/**
 * ArquillianDeployerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDeployerTestCase
{
   @Test
   public void deploy() throws Exception 
   {
      JavaArchive deployer = ShrinkWrap.create(JavaArchive.class, "arquillian-deployer.jar")
                                       .addClass(ArquillianDeployer.class)
                                       .addManifestResource(
                                             "META-INF/arquillian-service-beans.xml", 
                                             ArchivePaths.create("jboss-beans.xml"));
      
      deployer.as(ZipExporter.class)
               .exportZip(
                     new File("/home/aslak/dev/servers/jboss-6.0.0.M3/server/default/deploy/" + deployer.getName()), 
                     true);
   }
}