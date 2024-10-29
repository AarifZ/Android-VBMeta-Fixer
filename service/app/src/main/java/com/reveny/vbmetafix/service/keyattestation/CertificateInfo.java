package com.reveny.vbmetafix.service.keyattestation;

import android.util.Base64;
import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CertificateInfo {
    public static final int KEY_FAILED = -1;
    public static final int KEY_UNKNOWN = 0;
    public static final int KEY_AOSP = 1;
    public static final int KEY_GOOGLE = 2;
    public static final int KEY_KNOX = 3;
    public static final int KEY_OEM = 4;

    public static final int CERT_UNKNOWN = 0;
    public static final int CERT_SIGN = 1;
    public static final int CERT_REVOKED = 2;
    public static final int CERT_EXPIRED = 3;
    public static final int CERT_NORMAL = 4;

    private static final String GOOGLE_ROOT_PUBLIC_KEY = "" +
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAr7bHgiuxpwHsK7Qui8xU" +
        "FmOr75gvMsd/dTEDDJdSSxtf6An7xyqpRR90PL2abxM1dEqlXnf2tqw1Ne4Xwl5j" +
        "lRfdnJLmN0pTy/4lj4/7tv0Sk3iiKkypnEUtR6WfMgH0QZfKHM1+di+y9TFRtv6y" +
        "//0rb+T+W8a9nsNL/ggjnar86461qO0rOs2cXjp3kOG1FEJ5MVmFmBGtnrKpa73X" +
        "pXyTqRxB/M0n1n/W9nGqC4FSYa04T6N5RIZGBN2z2MT5IKGbFlbC8UrW0DxW7AYI" +
        "mQQcHtGl/m00QLVWutHQoVJYnFPlXTcHYvASLu+RhhsbDmxMgJJ0mcDpvsC4PjvB" +
        "+TxywElgS70vE0XmLD+OJtvsBslHZvPBKCOdT0MS+tgSOIfga+z1Z1g7+DVagf7q" +
        "uvmag8jfPioyKvxnK/EgsTUVi2ghzq8wm27ud/mIM7AY2qEORR8Go3TVB4HzWQgp" +
        "Zrt3i5MIlCaY504LzSRiigHCzAPlHws+W0rB5N+er5/2pJKnfBSDiCiFAVtCLOZ7" +
        "gLiMm0jhO2B6tUXHI/+MRPjy02i59lINMRRev56GKtcd9qO/0kUJWdZTdA2XoS82" +
        "ixPvZtXQpUpuL12ab+9EaDK8Z4RHJYYfCT3Q5vNAXaiWQ+8PTWm2QgBR/bkwSWc+" +
        "NpUFgNPN9PvQi8WEg5UmAGMCAwEAAQ==";

    private static final String AOSP_ROOT_EC_PUBLIC_KEY = "" +
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7l1ex+HA220Dpn7mthvsTWpdamgu" +
        "D/9/SQ59dx9EIm29sa/6FsvHrcV30lacqrewLVQBXT5DKyqO107sSHVBpA==";

    private static final String AOSP_ROOT_RSA_PUBLIC_KEY = "" +
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCia63rbi5EYe/VDoLmt5TRdSMf" +
        "d5tjkWP/96r/C3JHTsAsQ+wzfNes7UA+jCigZtX3hwszl94OuE4TQKuvpSe/lWmg" +
        "MdsGUmX4RFlXYfC78hdLt0GAZMAoDo9Sd47b0ke2RekZyOmLw9vCkT/X11DEHTVm" +
        "+Vfkl5YLCazOkjWFmwIDAQAB";

    private static final String KNOX_SAKV2_ROOT_PUBLIC_KEY = "" +
        "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBhbGuLrpql5I2WJmrE5kEVZOo+dgA" +
        "46mKrVJf/sgzfzs2u7M9c1Y9ZkCEiiYkhTFE9vPbasmUfXybwgZ2EM30A1ABPd12" +
        "4n3JbEDfsB/wnMH1AcgsJyJFPbETZiy42Fhwi+2BCA5bcHe7SrdkRIYSsdBRaKBo" +
        "ZsapxB0gAOs0jSPRX5M=";

    private static final byte[] googleKey = Base64.decode(GOOGLE_ROOT_PUBLIC_KEY, 0);
    private static final byte[] aospEcKey = Base64.decode(AOSP_ROOT_EC_PUBLIC_KEY, 0);
    private static final byte[] aospRsaKey = Base64.decode(AOSP_ROOT_RSA_PUBLIC_KEY, 0);
    private static final byte[] knoxSakv2Key = Base64.decode(KNOX_SAKV2_ROOT_PUBLIC_KEY, 0);

    private final X509Certificate cert;
    private int issuer = KEY_UNKNOWN;
    private int status = CERT_UNKNOWN;
    private GeneralSecurityException securityException;
    private Attestation attestation;
    private CertificateParsingException certException;

    private Integer certsIssued;

    private CertificateInfo(X509Certificate cert) {
        this.cert = cert;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public int getIssuer() {
        return issuer;
    }

    public int getStatus() {
        return status;
    }

    public GeneralSecurityException getSecurityException() {
        return securityException;
    }

    public Attestation getAttestation() {
        return attestation;
    }

    public CertificateParsingException getCertException() {
        return certException;
    }

    public Integer getCertsIssued() {
        return certsIssued;
    }

    private void checkIssuer() {
        var publicKey = cert.getPublicKey().getEncoded();

        if (Arrays.equals(publicKey, googleKey)) {
            issuer = KEY_GOOGLE;
        } else if (Arrays.equals(publicKey, aospEcKey)) {
            issuer = KEY_AOSP;
        } else if (Arrays.equals(publicKey, aospRsaKey)) {
            issuer = KEY_AOSP;
        } else if (Arrays.equals(publicKey, knoxSakv2Key)) {
            issuer = KEY_KNOX;
        }
    }

    private void checkStatus(PublicKey parentKey) {
        try {
            status = CERT_SIGN;
            cert.verify(parentKey);

            // Check serial code removed

            status = CERT_EXPIRED;
            cert.checkValidity();
            status = CERT_NORMAL;
        } catch (GeneralSecurityException e) {
            Log.e("Attestation", "Error verifying certificate", e);
            securityException = e;
        }
    }

    private boolean checkAttestation() {
        boolean terminate;
        try {
            attestation = Attestation.loadFromCertificate(cert);
            // If key purpose included KeyPurpose::SIGN,
            // then it could be used to sign arbitrary data, including any tbsCertificate,
            // and so an attestation produced by the key would have no security properties.
            // If the parent certificate can attest that the key purpose is only KeyPurpose::ATTEST_KEY,
            // then the child certificate can be trusted.
            var purposes = attestation.getTeeEnforced().getPurposes();
            terminate = purposes == null || !purposes.contains(AuthorizationList.KM_PURPOSE_ATTEST_KEY);
        } catch (CertificateParsingException e) {
            // Log.e("Attestation", "Error parsing certificate", e);
            certException = e;
            terminate = false;
        }
        return terminate;
    }

    public static AttestationResult parseCertificateChain(List<X509Certificate> certs) throws Exception {
        var infoList = new ArrayList<CertificateInfo>();

        var parent = certs.get(certs.size() - 1);
        for (int i = certs.size() - 1; i >= 0; i--) {
            var parentKey = parent.getPublicKey();
            var info = new CertificateInfo(certs.get(i));
            infoList.add(info);
            info.checkStatus(parentKey);
            if (parent == info.cert) {
                info.checkIssuer();
            } else {
                parent = info.cert;
            }
            if (info.checkAttestation()) {
                break;
            }
        }

        return AttestationResult.form(infoList);
    }

    private static List<X509Certificate> sortCerts(List<X509Certificate> certs) {
        if (certs.size() < 2) {
            return certs;
        }

        var issuer = certs.get(0).getIssuerX500Principal();
        boolean okay = true;
        for (var cert : certs) {
            var subject = cert.getSubjectX500Principal();
            if (issuer.equals(subject)) {
                issuer = subject;
            } else {
                okay = false;
                break;
            }
        }
        if (okay) {
            return certs;
        }

        var newList = new ArrayList<X509Certificate>(certs.size());
        for (var cert : certs) {
            boolean found = false;
            var subject = cert.getSubjectX500Principal();
            for (var c : certs) {
                if (c == cert) continue;
                if (c.getIssuerX500Principal().equals(subject)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newList.add(cert);
            }
        }
        if (newList.size() != 1) {
            return certs;
        }

        var oldList = new LinkedList<>(certs);
        oldList.remove(newList.get(0));
        for (int i = 0; i < newList.size(); i++) {
            issuer = newList.get(i).getIssuerX500Principal();
            for (var it = oldList.iterator(); it.hasNext(); ) {
                var cert = it.next();
                if (cert.getSubjectX500Principal().equals(issuer)) {
                    newList.add(cert);
                    it.remove();
                    break;
                }
            }
        }
        if (!oldList.isEmpty()) {
            return certs;
        }
        return newList;
    }

    public static AttestationResult parseCertificateChain(CertPath certPath) throws Exception {
        // noinspection unchecked
        var certs = (List<X509Certificate>) certPath.getCertificates();
        if (certs.isEmpty()) {
            throw new CertificateParsingException("No certificate found");
        }
        return parseCertificateChain(sortCerts(certs));
    }
}
