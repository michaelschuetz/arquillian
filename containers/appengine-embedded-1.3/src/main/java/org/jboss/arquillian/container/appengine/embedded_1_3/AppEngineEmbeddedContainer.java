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
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import org.jboss.arquillian.container.appengine.embedded_1_3.hack.AppEngineHack;
import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;

import com.google.appengine.tools.development.AppContext;
import com.google.appengine.tools.development.DevAppServer;
import com.google.appengine.tools.development.DevAppServerFactory;

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
      // add a GAE libs
      AppEngineSetup.prepare(archive);            

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

         setup("start", appLocation, containerConfig.getBindHttpPort(), containerConfig.getBindAddress());
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error starting AppEngine.", e);
      }

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

   /**
    * Hack with ApiProxy.
    *
    * @param methodName the method name
    * @param args the arguments
    * @throws Exception for any error
    */
   private void setup(String methodName, Object... args) throws Exception
   {
      AppContext appContext = server.getAppContext();
      ClassLoader cl = appContext.getClassLoader();
      Class<?> clazz = cl.loadClass(AppEngineHack.class.getName());
      Class[] classes = new Class[0];
      if (args != null && args.length > 0)
      {
         classes = new Class[args.length];
         for (int i = 0; i < args.length; i++)
            classes[i] = args.getClass();
      }
      Method method = clazz.getMethod(methodName, classes);
      Object instance = clazz.newInstance();
      method.invoke(instance, args);
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      if (server == null)
         return;

      try
      {
         setup("stop");
      }
      catch (Exception ignored)
      {
      }

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
