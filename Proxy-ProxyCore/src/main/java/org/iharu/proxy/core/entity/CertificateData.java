/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.entity;

import java.io.File;

/**
 *
 * @author iHaru
 */
public class CertificateData {
    
    private String keyStoredir;
    private String alias;
    private String password;
    private String organization;
    private String commonName;
    private String organizationalUnitName;
    private String certOrganization;
    private String certOrganizationUnitName;
    
    public void clear() {
        setKeyStoredir(null);
        setAlias(null);
        setPassword(null);
        setOrganization(null);
        setCommonName(null);
        setOrganizationalUnitName(null);
        setCertOrganization(null);
        setCertOrganizationUnitName(null);
    }

    /**
     * @return the keyStoredir
     */
    public String getKeyStoredir() {
        return keyStoredir;
    }

    /**
     * @param keyStoredir the keyStoredir to set
     */
    public void setKeyStoredir(String keyStoredir) {
        this.keyStoredir = keyStoredir;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @return the commonName
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * @param commonName the commonName to set
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * @return the organizationalUnitName
     */
    public String getOrganizationalUnitName() {
        return organizationalUnitName;
    }

    /**
     * @param organizationalUnitName the organizationalUnitName to set
     */
    public void setOrganizationalUnitName(String organizationalUnitName) {
        this.organizationalUnitName = organizationalUnitName;
    }

    /**
     * @return the certOrganization
     */
    public String getCertOrganization() {
        return certOrganization;
    }

    /**
     * @param certOrganization the certOrganization to set
     */
    public void setCertOrganization(String certOrganization) {
        this.certOrganization = certOrganization;
    }

    /**
     * @return the certOrganizationUnitName
     */
    public String getCertOrganizationUnitName() {
        return certOrganizationUnitName;
    }

    /**
     * @param certOrganizationUnitName the certOrganizationUnitName to set
     */
    public void setCertOrganizationUnitName(String certOrganizationUnitName) {
        this.certOrganizationUnitName = certOrganizationUnitName;
    }
    
}
