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
package org.jboss.arquillian.container.jbossas.remote_4_2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.arquillian.protocol.servlet_2_5.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * JBossASRemoteContainer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASRemoteContainer implements DeployableContainer
{
   private HttpServer httpFileServer;
   
   private JBossASConfiguration configuration;

   private RMIAdaptor rmiAdaptor;
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#setup(org.jboss.arquillian.spi.Context, org.jboss.arquillian.spi.Configuration)
    */
   public void setup(Context context, Configuration configuration)
   {
      this.configuration = configuration.getContainerConfig(JBossASConfiguration.class);
   }

   public void start(Context context) throws LifecycleException
   {
      try 
      {
         // TODO: configure http bind address
         httpFileServer = HttpServer.create();
         httpFileServer.bind(
               new InetSocketAddress(
                     InetAddress.getByName(configuration.getLocalDeploymentBindAddress()), 
                     configuration.getLocalDeploymentBindPort()), 
               -1);
         httpFileServer.start();
         
         rmiAdaptor = (RMIAdaptor)new InitialContext().lookup("jmx/invoker/RMIAdaptor");
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not connect to container", e);
      }
   }

   public ContainerMethodExecutor deploy(final Context context, final Archive<?> archive) throws DeploymentException
   {
      if(archive == null) 
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      String deploymentName = archive.getName();
      try
      {
         httpFileServer.createContext("/" + deploymentName, new HttpHandler()
         {
            public void handle(HttpExchange exchange) throws IOException
            {
               InputStream zip = archive.as(ZipExporter.class).exportZip();
               ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
               JBossASRemoteContainer.copy(zip, zipStream);
               zip.close();

               byte[] zipArray = zipStream.toByteArray();
               exchange.sendResponseHeaders(200, zipArray.length);

               OutputStream out = exchange.getResponseBody();
               out.write(zipArray);
               out.close();

            }
         });
         URL fileServerUrl = createFileServerURL(deploymentName);

         context.add(URL.class, fileServerUrl);
         ObjectName mainDeployer = new ObjectName("jboss.system:service=MainDeployer");
         rmiAdaptor.invoke(
               mainDeployer, 
               "deploy", 
               new Object[]{ fileServerUrl }, 
               new String[]{ URL.class.getName() }
         );
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not deploy " + deploymentName, e);
      }

      try 
      {
         return new ServletMethodExecutor(
               new URL(
                     "http",
                     configuration.getRemoteServerAddress(),
                     configuration.getRemoteServerHttpPort(), 
                     "/")
               );
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create ContianerMethodExecutor", e);
      }
   }

   public void undeploy(final Context context, final Archive<?> archive) throws DeploymentException
   {
      if(archive == null) 
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      try
      {
         URL fileServerUrl = context.get(URL.class);
         ObjectName mainDeployer = new ObjectName("jboss.system:service=MainDeployer");
         rmiAdaptor.invoke(
               mainDeployer, 
               "undeploy", 
               new Object[]{ fileServerUrl }, 
               new String[]{ URL.class.getName() }
         );
         
         httpFileServer.removeContext("/" + archive.getName());
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not undeploy " + archive.getName(), e);
      }
   }

   public void stop(Context context) throws LifecycleException
   {
      try 
      {
         httpFileServer.stop(0);
         rmiAdaptor = null;
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not clean up", e);
      }
   }
   
   private URL createFileServerURL(String archiveName) 
   {
      try 
      {
         InetSocketAddress address = httpFileServer.getAddress();
         return new URL(
               "http", 
               address.getHostName(), 
               address.getPort(), 
               "/" + archiveName);
      }
      catch (MalformedURLException e) 
      {
         throw new RuntimeException("Could not create fileserver url", e);
      }
   }

   private static void copy(InputStream source, OutputStream destination) throws IOException
   {
      if (source == null)
      {
         throw new IllegalArgumentException("source must be specified");
      }
      if (destination == null)
      {
         throw new IllegalArgumentException("destination must be specified");
      }
      byte[] readBuffer = new byte[2156]; 
      int bytesIn = 0; 
      while((bytesIn = source.read(readBuffer)) != -1) 
      { 
         destination.write(readBuffer, 0, bytesIn); 
      }
   }
}