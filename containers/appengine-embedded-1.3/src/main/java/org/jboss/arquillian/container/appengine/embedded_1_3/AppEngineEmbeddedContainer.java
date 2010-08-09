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
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;

import com.google.appengine.tools.development.ApiProxyLocalFactory;
import com.google.appengine.tools.development.DevAppServer;
import com.google.appengine.tools.development.DevAppServerFactory;
import com.google.appengine.tools.development.LocalEnvironment;
import com.google.apphosting.api.ApiProxy;

/**
 * Start AppEngine Embedded Container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AppEngineEmbeddedContainer implements DeployableContainer
{
   public static final String HTTP_PROTOCOL = "http";

   private AppEngineEmbeddedConfiguration containerConfig;
   private DevAppServer server;

   public void setup(Context context, Configuration configuration)
   {
      containerConfig = configuration.getContainerConfig(AppEngineEmbeddedConfiguration.class);
   }

   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      ExplodedExporter exporter = archive.as(ExplodedExporter.class);
      final File appLocation = exporter.exportExploded(
            AccessController.doPrivileged(new PrivilegedAction<File>()
            {
               public File run()
               {
                  return new File(System.getProperty("java.io.tmpdir"));
               }
            })
      );
      appLocation.deleteOnExit();

      try
      {
         server = AccessController.doPrivileged(new PrivilegedExceptionAction<DevAppServer>()
         {
            public DevAppServer run() throws Exception
            {
               DevAppServerFactory factory = new DevAppServerFactory();
               return factory.createDevAppServer(appLocation, containerConfig.getBindAddress(), containerConfig.getBindHttpPort());
            }
         });
         Map properties = System.getProperties();
         //noinspection unchecked
         server.setServiceProperties(properties);
         server.start();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error starting AppEngine.", e);
      }

      ApiProxy.Environment env = new DelegatingEnvironment(new DummyEnvironment())
      {
         @Override
         public String getAppId()
         {
            return archive.getName();
         }
      };
      ApiProxyLocalFactory aplf = new ApiProxyLocalFactory();
      ApiProxy.Delegate delegate = aplf.create(new DummyLocalServerEnvironment(appLocation, containerConfig));

      ApiProxy.setEnvironmentForCurrentThread(env);
      ApiProxy.setDelegate(delegate);

      try
      {
         return new ServletMethodExecutor(new URL(
               HTTP_PROTOCOL,
               containerConfig.getBindAddress(),
               containerConfig.getBindHttpPort(),
               "/")
         );
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create ContainerMethodExecutor", e);
      }
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      if (server == null)
         return;

      ApiProxy.setDelegate(null);
      ApiProxy.clearEnvironmentForCurrentThread();
      ApiProxy.setEnvironmentForCurrentThread(null);

      try
      {
         server.shutdown();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error shutting down AppEngine", e);
      }
      finally
      {
         server = null;
      }
   }

   public void start(Context context) throws LifecycleException
   {
   }

   public void stop(Context context) throws LifecycleException
   {
   }
}
