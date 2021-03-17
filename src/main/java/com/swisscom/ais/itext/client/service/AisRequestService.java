package com.swisscom.ais.itext.client.service;

import com.swisscom.ais.itext.client.impl.PdfDocumentHandler;
import com.swisscom.ais.itext.client.model.RevocationInformation;
import com.swisscom.ais.itext.client.model.SignatureMode;
import com.swisscom.ais.itext.client.model.SignatureStandard;
import com.swisscom.ais.itext.client.model.SignatureType;
import com.swisscom.ais.itext.client.model.UserData;
import com.swisscom.ais.itext.client.rest.model.AdditionalProfile;
import com.swisscom.ais.itext.client.rest.model.signreq.*;
import com.swisscom.ais.itext.client.utils.IdGenerator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AisRequestService {

    private static final String SWISSCOM_BASIC_PROFILE = "http://ais.swisscom.ch/1.1";
    private static final String CLAIMED_IDENTITY_DELIMITER = ":";

    public AISSignRequest buildAisSignRequest(List<PdfDocumentHandler> documents, SignatureMode signatureMode, SignatureType signatureType,
                                              UserData userData, List<AdditionalProfile> additionalProfiles, boolean withStepUp,
                                              boolean withCertificateRequest) {
        List<DocumentHash> documentsHashes = documents.stream()
            .map(doc -> new DocumentHash()
                .withId(doc.getId())
                .withDsigDigestMethod(new DsigDigestMethod().withAlgorithm(doc.getDigestAlgorithm().getDigestUri()))
                .withDsigDigestValue(doc.getEncodedDocumentHash()))
            .collect(Collectors.toList());

        OptionalInputs optionalInputs = buildRequestOptionalInputs(signatureMode, signatureType, userData, additionalProfiles, withStepUp,
                                                                   withCertificateRequest);

        SignRequest request = new SignRequest()
            .withRequestID(IdGenerator.generateRequestId())
            .withProfile(SWISSCOM_BASIC_PROFILE)
            .withInputDocuments(new InputDocuments().withDocumentHash(documentsHashes))
            .withOptionalInputs(optionalInputs);

        return new AISSignRequest().withSignRequest(request);
    }

    private OptionalInputs buildRequestOptionalInputs(SignatureMode signatureMode, SignatureType signatureType, UserData userData,
                                                      List<AdditionalProfile> additionalProfiles, boolean withStepUp,
                                                      boolean withCertificateRequest) {
        AddTimestamp addTimestamp = null;
        if (userData.isAddTimestamp()) {
            addTimestamp = new AddTimestamp().withType(SignatureType.TIMESTAMP.getUri());
        }

        ClaimedIdentity claimedIdentity = new ClaimedIdentity();
        StringJoiner claimedIdentityJoiner = new StringJoiner(CLAIMED_IDENTITY_DELIMITER);
        claimedIdentityJoiner.setEmptyValue(userData.getClaimedIdentityName());
        if (!signatureMode.equals(SignatureMode.TIMESTAMP) && StringUtils.isNotBlank(userData.getClaimedIdentityKey())) {
            claimedIdentityJoiner.add(userData.getClaimedIdentityKey());
        }
        claimedIdentity.setName(claimedIdentityJoiner.toString());

        ScCertificateRequest certificateRequest = null;
        if (withCertificateRequest) {
            certificateRequest = new ScCertificateRequest().withScDistinguishedName(userData.getDistinguishedName());
        }

        if (withStepUp) {
            if (Objects.isNull(certificateRequest)) {
                certificateRequest = new ScCertificateRequest();
            }
            ScPhone phone = new ScPhone()
                .withScLanguage(userData.getStepUpLanguage())
                .withScMSISDN(userData.getStepUpMsisdn())
                .withScMessage(userData.getStepUpMessage())
                .withScSerialNumber(userData.getStepUpSerialNumber());
            ScStepUpAuthorisation stepUpAuthorisation = new ScStepUpAuthorisation().withScPhone(phone);
            certificateRequest.setScStepUpAuthorisation(stepUpAuthorisation);
        }

        ScAddRevocationInformation addRevocationInformation = new ScAddRevocationInformation();
        if (userData.getAddRevocationInformation().equals(RevocationInformation.DEFAULT)) {
            if (signatureMode.equals(SignatureMode.TIMESTAMP)) {
                addRevocationInformation.setType(RevocationInformation.BOTH.getValue());
            } else {
                addRevocationInformation.setType(null);
            }
        } else {
            addRevocationInformation.setType(userData.getAddRevocationInformation().getValue());
        }

        OptionalInputs optionalInputs = new OptionalInputs()
            .withAddTimestamp(addTimestamp)
            .withAdditionalProfile(additionalProfiles.stream().map(AdditionalProfile::getUri).collect(Collectors.toList()))
            .withClaimedIdentity(claimedIdentity)
            .withSignatureType(signatureType.getUri())
            .withScCertificateRequest(certificateRequest)
            .withScAddRevocationInformation(addRevocationInformation);
        if (!signatureMode.equals(SignatureMode.TIMESTAMP) && !userData.getSignatureStandard().equals(SignatureStandard.DEFAULT)) {
            optionalInputs.setScSignatureStandard(userData.getSignatureStandard().getValue());
        }
        return optionalInputs;
    }
}
