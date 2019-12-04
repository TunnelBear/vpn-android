/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package net.openvpn.ovpn3;

public class ClientAPI_DynamicChallenge {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ClientAPI_DynamicChallenge(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ClientAPI_DynamicChallenge obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        ovpncliJNI.delete_ClientAPI_DynamicChallenge(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setChallenge(String value) {
    ovpncliJNI.ClientAPI_DynamicChallenge_challenge_set(swigCPtr, this, value);
  }

  public String getChallenge() {
    return ovpncliJNI.ClientAPI_DynamicChallenge_challenge_get(swigCPtr, this);
  }

  public void setEcho(boolean value) {
    ovpncliJNI.ClientAPI_DynamicChallenge_echo_set(swigCPtr, this, value);
  }

  public boolean getEcho() {
    return ovpncliJNI.ClientAPI_DynamicChallenge_echo_get(swigCPtr, this);
  }

  public void setResponseRequired(boolean value) {
    ovpncliJNI.ClientAPI_DynamicChallenge_responseRequired_set(swigCPtr, this, value);
  }

  public boolean getResponseRequired() {
    return ovpncliJNI.ClientAPI_DynamicChallenge_responseRequired_get(swigCPtr, this);
  }

  public void setStateID(String value) {
    ovpncliJNI.ClientAPI_DynamicChallenge_stateID_set(swigCPtr, this, value);
  }

  public String getStateID() {
    return ovpncliJNI.ClientAPI_DynamicChallenge_stateID_get(swigCPtr, this);
  }

  public ClientAPI_DynamicChallenge() {
    this(ovpncliJNI.new_ClientAPI_DynamicChallenge(), true);
  }

}
