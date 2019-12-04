/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package net.openvpn.ovpn3;

public class ClientAPI_ConnectionInfo {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ClientAPI_ConnectionInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ClientAPI_ConnectionInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        ovpncliJNI.delete_ClientAPI_ConnectionInfo(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setDefined(boolean value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_defined_set(swigCPtr, this, value);
  }

  public boolean getDefined() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_defined_get(swigCPtr, this);
  }

  public void setUser(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_user_set(swigCPtr, this, value);
  }

  public String getUser() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_user_get(swigCPtr, this);
  }

  public void setServerHost(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_serverHost_set(swigCPtr, this, value);
  }

  public String getServerHost() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_serverHost_get(swigCPtr, this);
  }

  public void setServerPort(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_serverPort_set(swigCPtr, this, value);
  }

  public String getServerPort() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_serverPort_get(swigCPtr, this);
  }

  public void setServerProto(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_serverProto_set(swigCPtr, this, value);
  }

  public String getServerProto() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_serverProto_get(swigCPtr, this);
  }

  public void setServerIp(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_serverIp_set(swigCPtr, this, value);
  }

  public String getServerIp() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_serverIp_get(swigCPtr, this);
  }

  public void setVpnIp4(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_vpnIp4_set(swigCPtr, this, value);
  }

  public String getVpnIp4() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_vpnIp4_get(swigCPtr, this);
  }

  public void setVpnIp6(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_vpnIp6_set(swigCPtr, this, value);
  }

  public String getVpnIp6() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_vpnIp6_get(swigCPtr, this);
  }

  public void setGw4(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_gw4_set(swigCPtr, this, value);
  }

  public String getGw4() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_gw4_get(swigCPtr, this);
  }

  public void setGw6(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_gw6_set(swigCPtr, this, value);
  }

  public String getGw6() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_gw6_get(swigCPtr, this);
  }

  public void setClientIp(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_clientIp_set(swigCPtr, this, value);
  }

  public String getClientIp() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_clientIp_get(swigCPtr, this);
  }

  public void setTunName(String value) {
    ovpncliJNI.ClientAPI_ConnectionInfo_tunName_set(swigCPtr, this, value);
  }

  public String getTunName() {
    return ovpncliJNI.ClientAPI_ConnectionInfo_tunName_get(swigCPtr, this);
  }

  public ClientAPI_ConnectionInfo() {
    this(ovpncliJNI.new_ClientAPI_ConnectionInfo(), true);
  }

}
