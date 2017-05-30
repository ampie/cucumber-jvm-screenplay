package com.sbg.bdd.screenplay.core;


public class DownstreamExpectation {
    private DownstreamStub stub;
    private DownstreamVerification verification;

    public DownstreamExpectation(DownstreamStub stub, DownstreamVerification verification) {
        this.stub = stub;
        this.verification=verification;
    }

    public DownstreamStub getStub() {
        return stub;
    }

    public DownstreamVerification getVerification() {
        return verification;
    }

}
