package com.example.smtp.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.markenwerk.utils.mail.dkim.DkimSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Service
@Slf4j
public class DkimService {

    @Value("${dkim.signing.domain:example.com}")
    private String signingDomain;

    @Value("${dkim.selector:default}")
    private String selector;

    @Value("${dkim.private.key.path:dkim.key}")
    private String privateKeyPath;

    public void sign(MimeMessage message) {
        try {
            File keyFile = new File(privateKeyPath);
            if (!keyFile.exists()) {
                log.warn("DKIM private key not found at {}. Skipping signing.", privateKeyPath);
                return;
            }

            DkimSigner dkimSigner = new DkimSigner(signingDomain, selector, keyFile);
            dkimSigner.setIdentity(signingDomain);
            dkimSigner.setHeaderCanonicalization(net.markenwerk.utils.mail.dkim.Canonicalization.RELAXED);
            dkimSigner.setBodyCanonicalization(net.markenwerk.utils.mail.dkim.Canonicalization.RELAXED);
            dkimSigner.setLengthParam(true);
            dkimSigner.setSigningAlgorithm(net.markenwerk.utils.mail.dkim.SigningAlgorithm.SHA256_WITH_RSA);

            // DkimMessage usage commented out due to dependency issues
            // if (message instanceof DkimMessage) {
            // ((DkimMessage) message).setDkimSigner(dkimSigner);
            // } else {
            log.warn("DKIM signing temporarily disabled due to dependency issues.");
            // }

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Failed to sign message with DKIM", e);
        }
    }
}
