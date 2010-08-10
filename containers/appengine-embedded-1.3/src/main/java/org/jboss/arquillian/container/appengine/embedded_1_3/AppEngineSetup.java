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
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Setup AppEngine libs.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class AppEngineSetup
{
   private static Logger log = Logger.getLogger(AppEngineSetup.class.getName());

   private static final String LOCAL_MAVEN_REPO =
         System.getProperty("user.home") + File.separatorChar +
               ".m2" + File.separatorChar + "repository";

   private static final String GROUP_ID = "com.google.appengine";
   private static final String VERSION = "1.3.5";

   private static final String[] FILES = {
         "appengine-api-1.0-sdk",
         "appengine-api-labs"
   };

   /**
    * Prepare AppEngine libs.
    *
    * @param archive the current archive
    */
   static void prepare(Archive archive)
   {
      File[] files = getFiles();

      WebArchive webArchive = archive.as(WebArchive.class);
      webArchive.addLibraries(files);

      log.info(webArchive.toString(true));
   }

   private static File[] getFiles()
   {
      File[] files = new File[FILES.length];
      for (int i = 0; i < files.length; i++)
      {
         File file = resolve(GROUP_ID, FILES[i], VERSION);
         if (file.exists() == false)
            throw new IllegalArgumentException("Missing AppEngine library: " + file);
         files[i] = file;         
      }
      return files;
   }

   private static File resolve(String groupId, String artifactId, String version)
   {
      return new File(LOCAL_MAVEN_REPO + File.separatorChar +
            groupId.replace(".", File.separator) + File.separatorChar +
            artifactId + File.separatorChar +
            version + File.separatorChar +
            artifactId + "-" + version + ".jar");
   }
}
