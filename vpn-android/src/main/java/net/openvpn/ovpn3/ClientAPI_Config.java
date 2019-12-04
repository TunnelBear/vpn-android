/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package net.openvpn.ovpn3;

public class ClientAPI_Config {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ClientAPI_Config(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ClientAPI_Config obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        ovpncliJNI.delete_ClientAPI_Config(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setContent(String value) {
    ovpncliJNI.ClientAPI_Config_content_set(swigCPtr, this, value);
  }

  public String getContent() {
    return ovpncliJNI.ClientAPI_Config_content_get(swigCPtr, this);
  }

  public void setContentList(SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t value) {
    ovpncliJNI.ClientAPI_Config_contentList_set(swigCPtr, this, SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t.getCPtr(value));
  }

  public SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t getContentList() {
    long cPtr = ovpncliJNI.ClientAPI_Config_contentList_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t(cPtr, false);
  }

  public void setGuiVersion(String value) {
    ovpncliJNI.ClientAPI_Config_guiVersion_set(swigCPtr, this, value);
  }

  public String getGuiVersion() {
    return ovpncliJNI.ClientAPI_Config_guiVersion_get(swigCPtr, this);
  }

  public void setSsoMethods(String value) {
    ovpncliJNI.ClientAPI_Config_ssoMethods_set(swigCPtr, this, value);
  }

  public String getSsoMethods() {
    return ovpncliJNI.ClientAPI_Config_ssoMethods_get(swigCPtr, this);
  }

  public void setHwAddrOverride(String value) {
    ovpncliJNI.ClientAPI_Config_hwAddrOverride_set(swigCPtr, this, value);
  }

  public String getHwAddrOverride() {
    return ovpncliJNI.ClientAPI_Config_hwAddrOverride_get(swigCPtr, this);
  }

  public void setPlatformVersion(String value) {
    ovpncliJNI.ClientAPI_Config_platformVersion_set(swigCPtr, this, value);
  }

  public String getPlatformVersion() {
    return ovpncliJNI.ClientAPI_Config_platformVersion_get(swigCPtr, this);
  }

  public void setServerOverride(String value) {
    ovpncliJNI.ClientAPI_Config_serverOverride_set(swigCPtr, this, value);
  }

  public String getServerOverride() {
    return ovpncliJNI.ClientAPI_Config_serverOverride_get(swigCPtr, this);
  }

  public void setPortOverride(String value) {
    ovpncliJNI.ClientAPI_Config_portOverride_set(swigCPtr, this, value);
  }

  public String getPortOverride() {
    return ovpncliJNI.ClientAPI_Config_portOverride_get(swigCPtr, this);
  }

  public void setProtoOverride(String value) {
    ovpncliJNI.ClientAPI_Config_protoOverride_set(swigCPtr, this, value);
  }

  public String getProtoOverride() {
    return ovpncliJNI.ClientAPI_Config_protoOverride_get(swigCPtr, this);
  }

  public void setIpv6(String value) {
    ovpncliJNI.ClientAPI_Config_ipv6_set(swigCPtr, this, value);
  }

  public String getIpv6() {
    return ovpncliJNI.ClientAPI_Config_ipv6_get(swigCPtr, this);
  }

  public void setConnTimeout(int value) {
    ovpncliJNI.ClientAPI_Config_connTimeout_set(swigCPtr, this, value);
  }

  public int getConnTimeout() {
    return ovpncliJNI.ClientAPI_Config_connTimeout_get(swigCPtr, this);
  }

  public void setTunPersist(boolean value) {
    ovpncliJNI.ClientAPI_Config_tunPersist_set(swigCPtr, this, value);
  }

  public boolean getTunPersist() {
    return ovpncliJNI.ClientAPI_Config_tunPersist_get(swigCPtr, this);
  }

  public void setGoogleDnsFallback(boolean value) {
    ovpncliJNI.ClientAPI_Config_googleDnsFallback_set(swigCPtr, this, value);
  }

  public boolean getGoogleDnsFallback() {
    return ovpncliJNI.ClientAPI_Config_googleDnsFallback_get(swigCPtr, this);
  }

  public void setSynchronousDnsLookup(boolean value) {
    ovpncliJNI.ClientAPI_Config_synchronousDnsLookup_set(swigCPtr, this, value);
  }

  public boolean getSynchronousDnsLookup() {
    return ovpncliJNI.ClientAPI_Config_synchronousDnsLookup_get(swigCPtr, this);
  }

  public void setAutologinSessions(boolean value) {
    ovpncliJNI.ClientAPI_Config_autologinSessions_set(swigCPtr, this, value);
  }

  public boolean getAutologinSessions() {
    return ovpncliJNI.ClientAPI_Config_autologinSessions_get(swigCPtr, this);
  }

  public void setRetryOnAuthFailed(boolean value) {
    ovpncliJNI.ClientAPI_Config_retryOnAuthFailed_set(swigCPtr, this, value);
  }

  public boolean getRetryOnAuthFailed() {
    return ovpncliJNI.ClientAPI_Config_retryOnAuthFailed_get(swigCPtr, this);
  }

  public void setExternalPkiAlias(String value) {
    ovpncliJNI.ClientAPI_Config_externalPkiAlias_set(swigCPtr, this, value);
  }

  public String getExternalPkiAlias() {
    return ovpncliJNI.ClientAPI_Config_externalPkiAlias_get(swigCPtr, this);
  }

  public void setDisableClientCert(boolean value) {
    ovpncliJNI.ClientAPI_Config_disableClientCert_set(swigCPtr, this, value);
  }

  public boolean getDisableClientCert() {
    return ovpncliJNI.ClientAPI_Config_disableClientCert_get(swigCPtr, this);
  }

  public void setSslDebugLevel(int value) {
    ovpncliJNI.ClientAPI_Config_sslDebugLevel_set(swigCPtr, this, value);
  }

  public int getSslDebugLevel() {
    return ovpncliJNI.ClientAPI_Config_sslDebugLevel_get(swigCPtr, this);
  }

  public void setCompressionMode(String value) {
    ovpncliJNI.ClientAPI_Config_compressionMode_set(swigCPtr, this, value);
  }

  public String getCompressionMode() {
    return ovpncliJNI.ClientAPI_Config_compressionMode_get(swigCPtr, this);
  }

  public void setPrivateKeyPassword(String value) {
    ovpncliJNI.ClientAPI_Config_privateKeyPassword_set(swigCPtr, this, value);
  }

  public String getPrivateKeyPassword() {
    return ovpncliJNI.ClientAPI_Config_privateKeyPassword_get(swigCPtr, this);
  }

  public void setDefaultKeyDirection(int value) {
    ovpncliJNI.ClientAPI_Config_defaultKeyDirection_set(swigCPtr, this, value);
  }

  public int getDefaultKeyDirection() {
    return ovpncliJNI.ClientAPI_Config_defaultKeyDirection_get(swigCPtr, this);
  }

  public void setForceAesCbcCiphersuites(boolean value) {
    ovpncliJNI.ClientAPI_Config_forceAesCbcCiphersuites_set(swigCPtr, this, value);
  }

  public boolean getForceAesCbcCiphersuites() {
    return ovpncliJNI.ClientAPI_Config_forceAesCbcCiphersuites_get(swigCPtr, this);
  }

  public void setTlsVersionMinOverride(String value) {
    ovpncliJNI.ClientAPI_Config_tlsVersionMinOverride_set(swigCPtr, this, value);
  }

  public String getTlsVersionMinOverride() {
    return ovpncliJNI.ClientAPI_Config_tlsVersionMinOverride_get(swigCPtr, this);
  }

  public void setTlsCertProfileOverride(String value) {
    ovpncliJNI.ClientAPI_Config_tlsCertProfileOverride_set(swigCPtr, this, value);
  }

  public String getTlsCertProfileOverride() {
    return ovpncliJNI.ClientAPI_Config_tlsCertProfileOverride_get(swigCPtr, this);
  }

  public void setPeerInfo(SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t value) {
    ovpncliJNI.ClientAPI_Config_peerInfo_set(swigCPtr, this, SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t.getCPtr(value));
  }

  public SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t getPeerInfo() {
    long cPtr = ovpncliJNI.ClientAPI_Config_peerInfo_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_std__vectorT_openvpn__ClientAPI__KeyValue_t(cPtr, false);
  }

  public void setProxyHost(String value) {
    ovpncliJNI.ClientAPI_Config_proxyHost_set(swigCPtr, this, value);
  }

  public String getProxyHost() {
    return ovpncliJNI.ClientAPI_Config_proxyHost_get(swigCPtr, this);
  }

  public void setProxyPort(String value) {
    ovpncliJNI.ClientAPI_Config_proxyPort_set(swigCPtr, this, value);
  }

  public String getProxyPort() {
    return ovpncliJNI.ClientAPI_Config_proxyPort_get(swigCPtr, this);
  }

  public void setProxyUsername(String value) {
    ovpncliJNI.ClientAPI_Config_proxyUsername_set(swigCPtr, this, value);
  }

  public String getProxyUsername() {
    return ovpncliJNI.ClientAPI_Config_proxyUsername_get(swigCPtr, this);
  }

  public void setProxyPassword(String value) {
    ovpncliJNI.ClientAPI_Config_proxyPassword_set(swigCPtr, this, value);
  }

  public String getProxyPassword() {
    return ovpncliJNI.ClientAPI_Config_proxyPassword_get(swigCPtr, this);
  }

  public void setProxyAllowCleartextAuth(boolean value) {
    ovpncliJNI.ClientAPI_Config_proxyAllowCleartextAuth_set(swigCPtr, this, value);
  }

  public boolean getProxyAllowCleartextAuth() {
    return ovpncliJNI.ClientAPI_Config_proxyAllowCleartextAuth_get(swigCPtr, this);
  }

  public void setAltProxy(boolean value) {
    ovpncliJNI.ClientAPI_Config_altProxy_set(swigCPtr, this, value);
  }

  public boolean getAltProxy() {
    return ovpncliJNI.ClientAPI_Config_altProxy_get(swigCPtr, this);
  }

  public void setDco(boolean value) {
    ovpncliJNI.ClientAPI_Config_dco_set(swigCPtr, this, value);
  }

  public boolean getDco() {
    return ovpncliJNI.ClientAPI_Config_dco_get(swigCPtr, this);
  }

  public void setEcho(boolean value) {
    ovpncliJNI.ClientAPI_Config_echo_set(swigCPtr, this, value);
  }

  public boolean getEcho() {
    return ovpncliJNI.ClientAPI_Config_echo_get(swigCPtr, this);
  }

  public void setInfo(boolean value) {
    ovpncliJNI.ClientAPI_Config_info_set(swigCPtr, this, value);
  }

  public boolean getInfo() {
    return ovpncliJNI.ClientAPI_Config_info_get(swigCPtr, this);
  }

  public void setAllowLocalLanAccess(boolean value) {
    ovpncliJNI.ClientAPI_Config_allowLocalLanAccess_set(swigCPtr, this, value);
  }

  public boolean getAllowLocalLanAccess() {
    return ovpncliJNI.ClientAPI_Config_allowLocalLanAccess_get(swigCPtr, this);
  }

  public void setClockTickMS(long value) {
    ovpncliJNI.ClientAPI_Config_clockTickMS_set(swigCPtr, this, value);
  }

  public long getClockTickMS() {
    return ovpncliJNI.ClientAPI_Config_clockTickMS_get(swigCPtr, this);
  }

  public void setGremlinConfig(String value) {
    ovpncliJNI.ClientAPI_Config_gremlinConfig_set(swigCPtr, this, value);
  }

  public String getGremlinConfig() {
    return ovpncliJNI.ClientAPI_Config_gremlinConfig_get(swigCPtr, this);
  }

  public void setWintun(boolean value) {
    ovpncliJNI.ClientAPI_Config_wintun_set(swigCPtr, this, value);
  }

  public boolean getWintun() {
    return ovpncliJNI.ClientAPI_Config_wintun_get(swigCPtr, this);
  }

  public ClientAPI_Config() {
    this(ovpncliJNI.new_ClientAPI_Config(), true);
  }

}
