package com.mobile.bank.ussd.model;

import java.io.Serializable;

/**
 *
 * @author Seth Sebeh-Kusi
 */ // appName, msidn, newUserAccount, pin, usess(name)
public class Transaction implements Serializable {
	private static final long serialVersionUID = 7033665693284615180L;
	private int id; // mProfile record primary id
	private String msisdn;
	private String vasType;
	private String targetBankCode;
	private String sourceBankCode;
	private String token;
	private String pCode;
	private String amount;
	private String pin; // encrypted pin
	private String appName;
	private String clientRef;
	private String reference;
	private String target; // account to receive funds
	private String source; // account to debit funds
	private String vasAccount; // account to receive vas value(airtime,bills etc)
	private String description;
	private String channel;
	private String merchantAlias;
	private String newPin;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private String param5;
	private String encryptedMsg;
	private String newUserAccount;
	private String clientTransactionID;
	private String uSess;
	private boolean isActive;
	private String pinChangeIds;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getTargetBankCode() {
		return targetBankCode;
	}

	public void setTargetBankCode(String targetBankCode) {
		this.targetBankCode = targetBankCode;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getClientRef() {
		return clientRef;
	}

	public void setClientRef(String clientRef) {
		this.clientRef = clientRef;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getMerchantAlias() {
		return merchantAlias;
	}

	public void setMerchantAlias(String merchantAlias) {
		this.merchantAlias = merchantAlias;
	}

	public String getNewPin() {
		return newPin;
	}

	public void setNewPin(String newPin) {
		this.newPin = newPin;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getParam3() {
		return param3;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public String getParam4() {
		return param4;
	}

	public void setParam4(String param4) {
		this.param4 = param4;
	}

	public String getParam5() {
		return param5;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public String getEncryptedMsg() {
		return encryptedMsg;
	}

	public void setEncryptedMsg(String encryptedMsg) {
		this.encryptedMsg = encryptedMsg;
	}

	public String getNetwork() {
		return vasType;
	}

	public void setNetwork(String network) {
		this.vasType = network;
	}

	public String getpCode() {
		return pCode;
	}

	public void setpCode(String pCode) {
		this.pCode = pCode;
	}

	public String getVasType() {
		return vasType;
	}

	public void setVasType(String vasType) {
		this.vasType = vasType;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getVasAccount() {
		return vasAccount;
	}

	public void setVasAccount(String vasAccount) {
		this.vasAccount = vasAccount;
	}

	public String getSourceBankCode() {
		return sourceBankCode;
	}

	public void setSourceBankCode(String sourceBankCode) {
		this.sourceBankCode = sourceBankCode;
	}

	public String getNewUserAccount() {
		return newUserAccount;
	}

	public void setNewUserAccount(String newUserAccount) {
		this.newUserAccount = newUserAccount;
	}

	public String getuSess() {
		return uSess;
	}

	public void setuSess(String uSess) {
		this.uSess = uSess;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getPinChangeIds() {
		return pinChangeIds;
	}

	public void setPinChangeIds(String pinChangeIds) {
		this.pinChangeIds = pinChangeIds;
	}

	public String getClientTransactionID() {
		return clientTransactionID;
	}

	public void setClientTransactionID(String clientTransactionID) {
		this.clientTransactionID = clientTransactionID;
	}

}
