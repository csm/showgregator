package org.showgregator.service.admin

import java.security.Permission

class ReadPermission extends Permission("read") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "read"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class AddUserPermission extends Permission("userPermission") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "addUser"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class AddTransientUserPermission extends Permission("userPermission") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "addTransientUser"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class AddPendingUserPermission extends Permission("userPermission") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "addPendingUser"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class DeleteUserPermission extends Permission("userPermission") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "deleteUser"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class DeleteTransientUserPermission extends Permission("userPermission") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "deleteTransientUser"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class DeletePendingUserPermission extends Permission("userPermission") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "deletePendingUser"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class AddRegisterTokenPermission extends Permission("registerToken") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "addToken"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class DeleteRegisterTokenPermission extends Permission("registerToken") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "deleteToken"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class EditUserPermission extends Permission("userPermission") {
  override def implies(permission: Permission): Boolean = permission.equals(this)
  override def getActions: String = "edit"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class AllAdminPermission extends Permission("allAdminPermission") {
  override def implies(permission: Permission): Boolean = (permission.isInstanceOf[AddUserPermission]
    || permission.isInstanceOf[AddTransientUserPermission]
    || permission.isInstanceOf[AddPendingUserPermission]
    || permission.isInstanceOf[DeleteUserPermission]
    || permission.isInstanceOf[DeleteTransientUserPermission]
    || permission.isInstanceOf[DeletePendingUserPermission]
    || permission.isInstanceOf[AddRegisterTokenPermission]
    || permission.isInstanceOf[DeleteRegisterTokenPermission]
    || permission.isInstanceOf[ReadPermission]
    || permission.isInstanceOf[EditUserPermission])
  override def getActions: String = "*"
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

class NoPermission extends Permission("noPermission") {
  override def implies(permission: Permission): Boolean = false
  override def getActions: String = ""
  override def hashCode(): Int = (getName+getActions).hashCode()
  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[this.type]
}

object Permissions {
  def forName(name:String):Permission = name match {
    case "readPermission" => new ReadPermission
    case "addUserPermission" => new AddUserPermission
    case "addTransientUserPermission" => new AddTransientUserPermission
    case "addPendingUserPermission" => new AddPendingUserPermission
    case "deleteUserPermission" => new DeleteUserPermission
    case "deleteTransientUserPermission" => new DeleteTransientUserPermission
    case "deletePendingUserPermission" => new DeletePendingUserPermission
    case "editUserPermission" => new EditUserPermission
    case "addRegisterTokenPermission" => new AddRegisterTokenPermission
    case "deleteRegisterTokenPermission" => new DeleteRegisterTokenPermission
    case "allAdminPermission" => new AllAdminPermission
    case _ => new NoPermission
  }
}