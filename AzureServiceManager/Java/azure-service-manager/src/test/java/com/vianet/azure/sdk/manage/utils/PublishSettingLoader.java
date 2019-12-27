package com.vianet.azure.sdk.manage.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.codec.binary.Base64;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;


/**
 * 加载PublishSetting，并生成证书
 */
public class PublishSettingLoader {

    public static URI createCertficateFromPublishSettingsFile(File publishSettingsFile, String subscriptionId, String outputKeyStore) throws IOException {
        String certificate = null;
        URI managementUri = null;

        try {
            DocumentBuilder e = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = e.parse(publishSettingsFile);
            document.getDocumentElement().normalize();
            NodeList publishProfileNodeList = document.getElementsByTagName("PublishProfile");
            Element publishProfileElement = (Element)publishProfileNodeList.item(0);
            if(publishProfileElement.hasAttribute("SchemaVersion") && publishProfileElement.getAttribute("SchemaVersion").equals("2.0")) {
                NodeList var20 = publishProfileElement.getElementsByTagName("Subscription");

                for(int e1 = 0; e1 < var20.getLength(); ++e1) {
                    Element subscription = (Element)var20.item(e1);
                    String id = subscription.getAttribute("Id");
                    if(id.equals(subscriptionId)) {
                        certificate = subscription.getAttribute("ManagementCertificate");
                        String serviceManagementUrl = subscription.getAttribute("ServiceManagementUrl");

                        try {
                            managementUri = new URI(serviceManagementUrl);
                            break;
                        } catch (URISyntaxException var16) {
                            throw new IllegalArgumentException("The syntax of the Url in the publish settings file is incorrect.", var16);
                        }
                    }
                }
            } else {
                certificate = publishProfileElement.getAttribute("ManagementCertificate");
                String url = publishProfileElement.getAttribute("Url");

                try {
                    managementUri = new URI(url);
                } catch (URISyntaxException var15) {
                    throw new IllegalArgumentException("The syntax of the Url in the publish settings file is incorrect.", var15);
                }
            }
        } catch (ParserConfigurationException var17) {
            throw new IllegalArgumentException("could not parse publishsettings file", var17);
        } catch (SAXException var18) {
            throw new IllegalArgumentException("could not parse publishsettings file", var18);
        } catch (NullPointerException var19) {
            throw new IllegalArgumentException("could not parse publishsettings file", var19);
        }

        createKeyStoreFromCertifcate(certificate, outputKeyStore);
        return managementUri;
    }


    public static KeyStore createKeyStoreFromCertifcate(String certificate, String keyStoreFileName) throws IOException {
        KeyStore keyStore = null;

        try {
            if((double)Float.valueOf(System.getProperty("java.specification.version")).floatValue() < 1.7D) {
                keyStore = getBCProviderKeyStore();
            } else {
                keyStore = KeyStore.getInstance("PKCS12");
            }

            keyStore.load((InputStream) null, "".toCharArray());
            ByteArrayInputStream e = new ByteArrayInputStream(Base64.decodeBase64(certificate));
            keyStore.load(e, "".toCharArray());
            File outStoreFile = new File(keyStoreFileName);
            if(!outStoreFile.getParentFile().exists()) {
                outStoreFile.getParentFile().mkdirs();
            }

            FileOutputStream outputStream = new FileOutputStream(keyStoreFileName);
            keyStore.store(outputStream, "".toCharArray());
            outputStream.close();
            return keyStore;
        } catch (KeyStoreException var6) {
            throw new IllegalArgumentException("Cannot create keystore from the publish settings file", var6);
        } catch (CertificateException var7) {
            throw new IllegalArgumentException("Cannot create keystore from the publish settings file", var7);
        } catch (NoSuchAlgorithmException var8) {
            throw new IllegalArgumentException("Cannot create keystore from the publish settings file", var8);
        }
    }

    public static KeyStore getBCProviderKeyStore() {
        KeyStore keyStore = null;

        try {
            Class e = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            Security.addProvider((Provider)e.newInstance());
            Field field = e.getField("PROVIDER_NAME");
            keyStore = KeyStore.getInstance("PKCS12", field.get((Object)null).toString());
            return keyStore;
        } catch (Exception var3) {
            throw new RuntimeException("Could not create keystore from publishsettings file.Make sure java versions less than 1.7 has bouncycastle jar in classpath", var3);
        }
    }

}
