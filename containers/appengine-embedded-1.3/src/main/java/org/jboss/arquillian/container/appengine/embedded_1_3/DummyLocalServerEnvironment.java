/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.arquillian.container.appengine.embedded_1_3;

import java.io.File;

import com.google.appengine.tools.development.LocalServerEnvironment;

/**
 * Dummy local server env.
 *
 * @author John Patterson
 * @author Ales Justin
 */
class DummyLocalServerEnvironment implements LocalServerEnvironment
{
   private AppEngineEmbeddedConfiguration config;
   private final File warPath;

   public DummyLocalServerEnvironment(File warPath, AppEngineEmbeddedConfiguration config)
   {
      this.warPath = warPath;
      this.config = config;
   }

   public void waitForServerToStart() throws InterruptedException
   {
   }

   public int getPort()
   {
      return config.getBindHttpPort();
   }

   public File getAppDir()
   {
      return warPath;   
   }

   public String getAddress()
   {
      return config.getBindAddress();
   }
}

