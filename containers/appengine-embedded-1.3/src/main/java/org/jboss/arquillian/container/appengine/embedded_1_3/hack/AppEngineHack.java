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

package org.jboss.arquillian.container.appengine.embedded_1_3.hack;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.apphosting.api.ApiProxy;

/**
 * AppEngine ApiProxy hack.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AppEngineHack
{
   public void start()
   {
      ApiProxy.Delegate delegate = new ApiProxy.Delegate()
      {
         public byte[] makeSyncCall(ApiProxy.Environment environment, String s, String s1, byte[] bytes) throws ApiProxy.ApiProxyException
         {
            return bytes;
         }

         public Future<byte[]> makeAsyncCall(ApiProxy.Environment environment, String s, String s1, final byte[] bytes, ApiProxy.ApiConfig apiConfig)
         {
            return new Future<byte[]>()
            {
               public boolean cancel(boolean mayInterruptIfRunning)
               {
                  return false;
               }

               public boolean isCancelled()
               {
                  return false;
               }

               public boolean isDone()
               {
                  return true;
               }

               public byte[] get() throws InterruptedException, ExecutionException
               {
                  return bytes;
               }

               public byte[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
               {
                  return bytes;
               }
            };
         }

         public void log(ApiProxy.Environment environment, ApiProxy.LogRecord logRecord)
         {
            System.out.println(logRecord.getMessage());
         }
      };
      ApiProxy.setDelegate(delegate);
   }

   public void stop()
   {
      ApiProxy.setDelegate(null);
   }
}
