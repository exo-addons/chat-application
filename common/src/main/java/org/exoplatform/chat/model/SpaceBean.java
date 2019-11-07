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

package org.exoplatform.chat.model;

public class SpaceBean implements java.io.Serializable
{
  String id;
  String room;
  String displayName;
  String groupId;
  String shortName;
  String prettyName;
  long timestamp = -1;

  public String getPrettyName() {
    return prettyName;
  }

  public void setPrettyName(String prettyName) {
    this.prettyName = prettyName;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override public boolean equals(Object aThat) {
    if ( this == aThat ) return true;
    if ( !(aThat instanceof SpaceBean) ) return false;
    SpaceBean that = (SpaceBean)aThat;

    return  areEqual(this.id, that.id) &&
              areEqual(this.room, that.room) &&
              areEqual(this.displayName, that.displayName) &&
              areEqual(this.groupId, that.groupId) &&
              areEqual(this.shortName, that.shortName) &&
            areEqual(this.prettyName, that.prettyName);
  }

  @Override public int hashCode() {
    return this.id.hashCode()
            ^ this.room.hashCode()
            ^ this.displayName.hashCode()
            ^ this.groupId.hashCode()
            ^ this.shortName.hashCode()
            ^ this.prettyName.hashCode();
  }

  static public boolean areEqual(Object aThis, Object aThat){
    return aThis == null ? aThat == null : aThis.equals(aThat);
  }

  static public boolean areEqual(long aThis, long aThat){
    return aThis == aThat;
  }
}
