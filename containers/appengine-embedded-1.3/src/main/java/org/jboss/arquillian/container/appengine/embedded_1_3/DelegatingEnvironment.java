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

import com.google.apphosting.api.ApiProxy;

/**
 * Delegating env.
 *
 * @author John Patterson
 * @author Ales Justin
 */
abstract class DelegatingEnvironment implements ApiProxy.Environment
{
   private final ApiProxy.Environment delegate;

   public DelegatingEnvironment(ApiProxy.Environment delegate)
   {
      this.delegate = delegate;
   }

   public String getAppId()
   {
      return delegate.getAppId();
   }

   public Map<String, Object> getAttributes()
   {
      return delegate.getAttributes();
   }

   public String getAuthDomain()
   {
      return delegate.getAuthDomain();
   }

   public String getEmail()
   {
      return delegate.getEmail();
   }

   public String getRequestNamespace()
   {
      return delegate.getRequestNamespace();
   }

   public String getVersionId()
   {
      return delegate.getVersionId();
   }

   public boolean isAdmin()
   {
      return delegate.isAdmin();
   }

   public boolean isLoggedIn()
   {
      return delegate.isLoggedIn();
   }
}
