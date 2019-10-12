package org.littleshoot.proxy.mitm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import static org.iharu.constant.ConstantValue.FILESEPARATOR;
import org.iharu.proxy.core.entity.CertificateData;

/**
 * Parameter object holding personal informations given to a SSLEngineSource.
 * 
 * XXX consider to inline within the interface SslEngineSource, if MITM is core
 */
public class Authority {

    private final File keyStoreDir;

    private final String alias;

    private final char[] password;

    private final String commonName;

    private final String organization;

    private final String organizationalUnitName;

    private final String certOrganization;

    private final String certOrganizationalUnitName;

    /**
     * Create a parameter object with example certificate and certificate
     * authority informations
     * @param certificateData
     */
    public Authority(CertificateData certificateData) {
        boolean pass = checkCertData(certificateData);
//        boolean exist =  new File(certificateData.getKeyStoredir(), certificateData.getAlias()).exists();
        if(pass) {
            keyStoreDir = new File(certificateData.getKeyStoredir());
            alias = certificateData.getAlias();
            password = certificateData.getPassword().toCharArray();
            commonName = certificateData.getCommonName();
            organization = certificateData.getOrganization();
            organizationalUnitName = certificateData.getOrganizationalUnitName();
            certOrganization = certificateData.getCertOrganization();
            certOrganizationalUnitName = certificateData.getCertOrganizationUnitName();
        } else {
            File dir;
            try {
                dir = Files.createTempDirectory("iharu-proxy").toFile();
            } catch (IOException ex) {
                dir = new File(System.getProperty("java.io.tmpdir") + "iharu-proxy");
            }
            keyStoreDir = dir;
            alias = "mitm-cert";
            password = String.valueOf(new SecureRandom().nextLong()).toCharArray();
            commonName = certificateData.getCommonName() == null? "iHaruProxy Certificate":certificateData.getCommonName();
            organization = "iHaru Organization";
            organizationalUnitName = "iHaru Certificate Authority";
            certOrganization = organization;
            certOrganizationalUnitName = organization
                + ", describe proxy purpose here, since Man-In-The-Middle is bad normally.";
        }
    }

    /**
     * Create a parameter object with the given certificate and certificate
     * authority informations
     */
    public Authority(File keyStoreDir, String alias, char[] password,
            String commonName, String organization,
            String organizationalUnitName, String certOrganization,
            String certOrganizationalUnitName) {
        super();
        this.keyStoreDir = keyStoreDir;
        this.alias = alias;
        this.password = password;
        this.commonName = commonName;
        this.organization = organization;
        this.organizationalUnitName = organizationalUnitName;
        this.certOrganization = certOrganization;
        this.certOrganizationalUnitName = certOrganizationalUnitName;
    }
    
    private boolean checkCertData(CertificateData certificateData) {
        if(certificateData == null)
            return false;
        return StringUtils.isNoneBlank(
                certificateData.getKeyStoredir(),
                certificateData.getAlias(),
                certificateData.getPassword(),
                certificateData.getCommonName(),
                certificateData.getOrganization(),
                certificateData.getOrganizationalUnitName(),
                certificateData.getCertOrganization(),
                certificateData.getCertOrganizationUnitName()
        );
    }

    public File aliasFile(String fileExtension) {
        return new File(keyStoreDir, alias + fileExtension);
    }

    public String alias() {
        return alias;
    }

    public char[] password() {
        return password;
    }

    public String commonName() {
        return commonName;
    }

    public String organization() {
        return organization;
    }

    public String organizationalUnitName() {
        return organizationalUnitName;
    }

    public String certOrganisation() {
        return certOrganization;
    }

    public String certOrganizationalUnitName() {
        return certOrganizationalUnitName;
    }

}
