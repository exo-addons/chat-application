/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.chat.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

public class PropertyManager {
  private static final Logger LOG = Logger.getLogger(PropertyManager.class.getName());

  private static Properties properties;

  public static final String PROPERTIES_PATH;
  static {
    String chatConfDir = System.getProperty("exo.chat.conf");
    if (StringUtils.isEmpty(chatConfDir)) {
      chatConfDir = System.getProperty("catalina.base")+"/conf/chat.properties";
    }
    PROPERTIES_PATH = chatConfDir;
  }

  public static final String PROPERTY_SYSTEM_PREFIX = "chat.";
  public static final String PROPERTY_SERVICES_IMPLEMENTATION = "servicesImplementation";
  public static final String PROPERTY_SERVER_TYPE = "dbServerType";
  public static final String PROPERTY_SERVER_HOST = "dbServerHost";
  public static final String PROPERTY_SERVER_PORT = "dbServerPort";
  public static final String PROPERTY_DB_NAME = "dbName";
  public static final String PROPERTY_DB_AUTHENTICATION = "dbAuthentication";
  public static final String PROPERTY_DB_USER = "dbUser";
  public static final String PROPERTY_DB_PASSWORD = "dbPassword";
  public static final String PROPERTY_CHAT_SERVER_BASE = "chatServerBase";
  public static final String PROPERTY_CHAT_SERVER_URL = "chatServerUrl";
  public static final String PROPERTY_CHAT_PORTAL_PAGE = "chatPortalPage";
  public static final String PROPERTY_INTERVAL_CHAT = "chatIntervalChat";
  public static final String PROPERTY_INTERVAL_SESSION = "chatIntervalSession";
  public static final String PROPERTY_INTERVAL_STATUS = "chatIntervalStatus";
  public static final String PROPERTY_INTERVAL_NOTIF = "chatIntervalNotif";
  public static final String PROPERTY_INTERVAL_USERS = "chatIntervalUsers";
  public static final String PROPERTY_PASSPHRASE = "chatPassPhrase";
  public static final String PROPERTY_CRON_NOTIF_CLEANUP = "chatCronNotifCleanup";
  public static final String PROPERTY_PUBLIC_MODE = "publicMode";
  public static final String PROPERTY_PUBLIC_ADMIN_GROUP = "publicAdminGroup";
  public static final String PROPERTY_TEAM_ADMIN_GROUP = "teamAdminGroup";
  public static final String PROPERTY_TOKEN_VALIDITY = "chatTokenValidity";
  public static final String PROPERTY_READ_DAYS = "chatReadDays";
  public static final String PROPERTY_READ_TOTAL_JSON = "chatReadTotalJson";
  public static final String PROPERTY_READ_TOTAL_TXT = "chatReadTotalTxt";

  public static final String PROPERTY_SERVER_TYPE_EMBED = "embed";
  public static final String PROPERTY_SERVER_TYPE_MONGO = "mongo";

  public static final String PROPERTY_SERVICE_IMPL_MONGO = "mongo";
  public static final String PROPERTY_SERVICE_IMPL_JCR = "jcr";

  public static final String PROPERTY_MAIL_HOST = "mailHost";
  public static final String PROPERTY_MAIL_PORT = "mailPort";
  public static final String PROPERTY_MAIL_USER = "mailUser";
  public static final String PROPERTY_MAIL_PASSWORD = "mailPassword";

  public static String getProperty(String key)
  {
    String value = (String)properties().get(key);
    return value;
  }

  private synchronized static Properties properties()
  {
    if (properties==null)
    {
      properties = new Properties();
      InputStream stream = null;
      try
      {
        stream = new FileInputStream(PROPERTIES_PATH);
        properties.load(stream);
        stream.close();
      }
      catch (Exception e)
      {
        LOG.warning(e.getMessage());
      }

      overridePropertyIfNotSet(PROPERTY_SERVICES_IMPLEMENTATION, PROPERTY_SERVICE_IMPL_MONGO);
      overridePropertyIfNotSet(PROPERTY_SERVER_TYPE, "mongo");
      overridePropertyIfNotSet(PROPERTY_SERVER_HOST, "localhost");
      overridePropertyIfNotSet(PROPERTY_SERVER_PORT, "27017");
      overridePropertyIfNotSet(PROPERTY_DB_NAME, "chat");
      overridePropertyIfNotSet(PROPERTY_DB_AUTHENTICATION, "false");
      overridePropertyIfNotSet(PROPERTY_DB_USER, "");
      overridePropertyIfNotSet(PROPERTY_DB_PASSWORD, "");
      overridePropertyIfNotSet(PROPERTY_CHAT_SERVER_BASE, "");
      overridePropertyIfNotSet(PROPERTY_CHAT_SERVER_URL, "/chatServer");
      overridePropertyIfNotSet(PROPERTY_CHAT_PORTAL_PAGE, "/portal/intranet/chat");
      overridePropertyIfNotSet(PROPERTY_INTERVAL_CHAT, "3000");
      overridePropertyIfNotSet(PROPERTY_INTERVAL_SESSION, "60000");
      overridePropertyIfNotSet(PROPERTY_INTERVAL_STATUS, "15000");
      overridePropertyIfNotSet(PROPERTY_INTERVAL_NOTIF, "3000");
      overridePropertyIfNotSet(PROPERTY_INTERVAL_USERS, "5000");
      overridePropertyIfNotSet(PROPERTY_PASSPHRASE, "chat");
      overridePropertyIfNotSet(PROPERTY_CRON_NOTIF_CLEANUP, "0 0/60 * * * ?");
      overridePropertyIfNotSet(PROPERTY_PUBLIC_MODE, "false");
      overridePropertyIfNotSet(PROPERTY_PUBLIC_ADMIN_GROUP, "/platform/administrators");
      overridePropertyIfNotSet(PROPERTY_TEAM_ADMIN_GROUP, "/platform/users");
      overridePropertyIfNotSet(PROPERTY_TOKEN_VALIDITY, "25000");
      overridePropertyIfNotSet(PROPERTY_READ_DAYS, "30");
      overridePropertyIfNotSet(PROPERTY_READ_TOTAL_JSON, "200");
      overridePropertyIfNotSet(PROPERTY_READ_TOTAL_TXT, "2000");

      overridePropertyIfNotSet(PROPERTY_MAIL_HOST, "smtp.gmail.com");
      overridePropertyIfNotSet(PROPERTY_MAIL_PORT, "587");
      overridePropertyIfNotSet(PROPERTY_MAIL_USER, "exo");
      overridePropertyIfNotSet(PROPERTY_MAIL_PASSWORD, "");
    }
    return properties;
  }

  private static void overridePropertyIfNotSet(String key, String value) {
    if (properties().getProperty(key)==null)
    {
      properties().setProperty(key, value);
    }
    if (System.getProperty(PROPERTY_SYSTEM_PREFIX+key)!=null) {
      properties().setProperty(key, System.getProperty(PROPERTY_SYSTEM_PREFIX+key));
    }

  }

  public static void overrideProperty(String key, String value) {
    properties().setProperty(key, value);
  }
}
