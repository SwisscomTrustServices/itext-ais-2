package com.swisscom.ais.itext.client.model;

/**
 * Enumerates all the "normal" cases in which a signature can be stopped. These are caused by a correct functioning of the
 * AIS client and server and with a particular behaviour of the user (timeout, cancel of signature).
 */
public enum SignatureResult {

    /**
     * The signature finished successfully. The signatures are already embedded in the PDF documents (see {@link PdfMetadata}).
     */
    SUCCESS,

    /**
     * The user cancelled the signature.
     */
    USER_CANCEL,

    /**
     * The user did not respond in a timely manner.
     */
    USER_TIMEOUT,

    /**
     * The provided user serial number (part of the StepUp process) does not match the one on the server side.
     */
    SERIAL_NUMBER_MISMATCH,

    /**
     * The user failed to properly authenticate for the signature.
     */
    USER_AUTHENTICATION_FAILED,

    /**
     * The request is missing the required MSISDN parameter. This can happen sometimes in the context of the on-demand flow,
     * depending on the user's server configuration (e.g. the enforceStepUpAuthentication flag is true). As an alternative,
     * the on-demand with step-up flow can be used instead.
     */
    INSUFFICIENT_DATA_WITH_ABSENT_MSISDN

}
