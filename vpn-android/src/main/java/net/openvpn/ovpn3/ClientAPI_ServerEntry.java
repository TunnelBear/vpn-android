/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package net.openvpn.ovpn3;

public class ClientAPI_ServerEntry {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ClientAPI_ServerEntry(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ClientAPI_ServerEntry obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        ovpncliJNI.delete_ClientAPI_ServerEntry(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setServer(String value) {
    ovpncliJNI.ClientAPI_ServerEntry_server_set(swigCPtr, this, value);
  }

  public String getServer() {
    return ovpncliJNI.ClientAPI_ServerEntry_server_get(swigCPtr, this);
  }

  public void setFriendlyName(String value) {
    ovpncliJNI.ClientAPI_ServerEntry_friendlyName_set(swigCPtr, this, value);
  }

  public String getFriendlyName() {
    return ovpncliJNI.ClientAPI_ServerEntry_friendlyName_get(swigCPtr, this);
  }

  public ClientAPI_ServerEntry() {
    this(ovpncliJNI.new_ClientAPI_ServerEntry(), true);
  }

}
