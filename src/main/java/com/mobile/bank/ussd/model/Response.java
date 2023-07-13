package com.mobile.bank.ussd.model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class Response implements Serializable{

    private static final long serialVersionUID = 5624521949261147850L;
    private int error;
    private String message;
    private String mobile;
    private String reference;
    private String token;
    private List<MobileProfile> mProfileList;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<MobileProfile> getmProfileList() {
        return mProfileList;
    }

    public void setmProfileList(List<MobileProfile> mProfileList) {
        this.mProfileList = mProfileList;
    }
    
}
