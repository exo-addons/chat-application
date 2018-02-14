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

package org.exoplatform.chat.services;

import java.util.List;
import java.util.Map;

import org.exoplatform.chat.model.UserBean;

public interface TokenService
{
  String ANONIM_USER = "__anonim_";

  String getToken(String user);

  boolean hasUserWithToken(String user, String token);

  boolean hasUserWithToken(String user, String token, String dbName);

  void addUser(String user, String token, String dbName);

  void removeUserToken(String user, String token, String dbName);

  Map<String, UserBean> getActiveUsersFilterBy(String user, List<String> limitUsers, String dbName, boolean withUsers, boolean withPublic, boolean isAdmin, int limit);

  boolean isDemoUser(String user);

}