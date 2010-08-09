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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.apphosting.api.ApiProxy;

/**
 * Dummy env.
 *
 * @author Ales Justin
 */
class DummyEnvironment implements ApiProxy.Environment
{
   public String getAppId()
   {
      return "dummy";
   }

   public String getVersionId()
   {
      return "1.0";
   }

   public String getEmail()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isLoggedIn()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isAdmin()
   {
      throw new UnsupportedOperationException();
   }

   public String getAuthDomain()
   {
      throw new UnsupportedOperationException();
   }

   public String getRequestNamespace()
   {
      return "";
   }

   public Map<String, Object> getAttributes()
   {
      return new ConcurrentHashMap<String, Object>();
   }
}

