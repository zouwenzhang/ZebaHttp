package com.zeba.http;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HttpsTrustManager implements X509TrustManager {

    private List<X509Certificate> certificates=new LinkedList<>();

    public void loadCert(InputStream inputStream)throws Exception{
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(inputStream);
        inputStream.close();
        certificates.add(certificate);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//        for(X509Certificate certificate:certificates){
//            if(certificate.getIssuerDN().getName().equals(chain[0].getIssuerDN().getName())){
////                if(certificate.getSerialNumber().equals(chain[0].getSerialNumber())){
////                    if(Arrays.equals(certificate.getSignature(),chain[0].getSignature())){
////                        return;
////                    }
////                }
//                return;
//            }
//        }
//        try {
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
//            tmf.init((KeyStore) null);
//            for (TrustManager trustManager : tmf.getTrustManagers()) {
//                ((X509TrustManager) trustManager).checkServerTrusted(chain, authType);
//            }
//        } catch (Exception e) {
//            throw new CertificateException(e);
//        }
        //2、判断服务器证书 发布方的标识名  和 本地证书 发布方的标识名 是否一致
        //3、判断服务器证书 主体的标识名  和 本地证书 主体的标识名 是否一致
        //getIssuerDN()  获取证书的 issuer（发布方的标识名）值。
        //getSubjectDN()  获取证书的 subject（主体的标识名）值。
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    // //受信任证书链
    //      CertificateFactory factory = CertificateFactory.getInstance("X509");
    //      //构建用户证书和CertPath
    //      ByteArrayInputStream ins = new ByteArrayInputStream(message);
    //      java.security.cert.X509Certificate usercert = (java.security.cert.X509Certificate) factory.generateCertificate(ins);
    //
    //      List<Certificate> certList = new ArrayList<Certificate>();
    //      certList.add(usercert);
    //      CertPath certPath = factory.generateCertPath(certList);
    //      CertPathValidator validator = CertPathValidator.getInstance("PKIX");
    //
    //       //验证证书链
    //       String certStorePath = "your证书库.p7c";
    //      Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
    //       InputStream input = new FileInputStream(certStorePath);
    //      Collection<Certificate> certs = (Collection<Certificate>)factory.generateCertificates(input);
    //       for (Iterator i = certs.iterator(); i.hasNext();){
    //         java.security.cert.X509Certificate certIt =(java.security.cert.X509Certificate)i.next();
    //         trustAnchors.add(new TrustAnchor(certIt, null));
    //       }
    //      //构建证书验证设置
    //      PKIXParameters parameters = new PKIXParameters(trustAnchors);
    //      parameters.setRevocationEnabled(false);
    //
    //       try {
    //         PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validator.validate(certPath, parameters);
    //          TrustAnchor trustAnchor = result.getTrustAnchor();
    //         java.security.cert.X509Certificate issuer = trustAnchor.getTrustedCert();
    //         if(issuer==null){
    //             issuer = usercert;
    //          }
    //         //证书类型转换
    //         writeLog("查询crl列表");
    //         cn.com.sslvpn.lib.x509.Certificate libCertificate = new cn.com.sslvpn.lib.x509.Certificate();
    //         libCertificate = cn.com.sslvpn.lib.x509.Certificate.readFromBuffer(usercert.getEncoded());
    //         cn.com.sslvpn.lib.x509.Certificate libissuer = new cn.com.sslvpn.lib.x509.Certificate();
    //          libissuer = cn.com.sslvpn.lib.x509.Certificate.readFromBuffer(issuer.getEncoded());
    //
    //          if (crlCache.getCheck() != CRLCache.CHECK_NO) {
    //             boolean ok = crlCache.check(libissuer, libCertificate);
    //            if(ok){
    //               writeLog("证书通过crl检测");
    //             }
    //             else{
    //               writeLog("证书无效或者已被吊销");
    //                os.write(new byte[]{(byte)MessageType.error_message});
    //               os.write(Convert.shortToByte((short)(1)));
    //                os.write(new byte[]{MessageError.ClientCerttificateError});
    //               os.flush();
    //                throw new Exception("证书无效或者已被吊销");
    //             }
    //          }
    //         writeLog("证书验证成功");
    //       } catch (CertPathValidatorException e) {
    //         writeLog("证书验证失败");
    //         e.printStackTrace();
    //          os.write(new byte[]{(byte)MessageType.error_message});
    //         os.write(Convert.shortToByte((short)(1)));
    //          os.write(new byte[]{MessageError.ClientCerttificateError});
    //         os.flush();
    //          throw e;
    //       } catch (InvalidAlgorithmParameterException e) {
    //         writeLog("证书验证失败");
    //         e.printStackTrace();
    //          os.write(new byte[]{(byte)MessageType.error_message});
    //         os.write(Convert.shortToByte((short)(1)));
    //          os.write(new byte[]{MessageError.ClientCerttificateError});
    //         os.flush();
    //          throw e;
    //       } catch(Exception e){
    //         writeLog("证书验证异常");
    //          os.write(new byte[]{(byte)MessageType.error_message});
    //         os.write(Convert.shortToByte((short)(1)));
    //          os.write(new byte[]{MessageError.ClientCerttificateError});
    //         os.flush();
    //          throw e;
    //       }
}
