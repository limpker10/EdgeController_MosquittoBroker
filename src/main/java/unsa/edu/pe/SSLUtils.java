package unsa.edu.pe;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLUtils {

    public static SSLSocketFactory getSingleSocketFactory(final String caCrtFile) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        X509Certificate caCert = null;
        InputStream caCertInputStream = MqttSslClient.class.getClassLoader().getResourceAsStream(caCrtFile);
        assert caCertInputStream != null;
        BufferedInputStream bis = new BufferedInputStream(caCertInputStream);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
        }
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("cert-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }

    public static SSLSocketFactory getAWSIotSocketFactory(String caCrtFile, String crtFile, String keyFile){
        try {
            String password = "";
            Security.addProvider(new BouncyCastleProvider());

            // Load CA certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInputStream = MqttSslClient.class.getClassLoader().getResourceAsStream(caCrtFile);
            BufferedInputStream bis = new BufferedInputStream(caInputStream);
            X509Certificate caCert = null;
            while (bis.available() > 0) {
                caCert = (X509Certificate) cf.generateCertificate(bis);
            }

            // Load client certificate
            InputStream clientCrtInputStream = MqttSslClient.class.getClassLoader().getResourceAsStream(crtFile);
            bis = new BufferedInputStream(clientCrtInputStream);
            X509Certificate cert = null;
            while (bis.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(bis);
            }

            // Load client private key
            PEMParser pemParser = new PEMParser(new InputStreamReader(MqttSslClient.class.getClassLoader().getResourceAsStream(keyFile)));
            PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
            KeyPair key = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
            pemParser.close();

            // CA certificate is used to authenticate server
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);

            // Client key and certificates are sent to server  it can authenticate us
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", cert);
            ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new Certificate[]{cert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());

            // Finally, create SSL socket factory
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}