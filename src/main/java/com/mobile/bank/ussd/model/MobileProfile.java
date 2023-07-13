package com.mobile.bank.ussd.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class MobileProfile implements Serializable {
    private static final long serialVersionUID = 622334554430866L;
    private int id;
    private String authBy;
    private String mobileNo;
    private int appId;
    private String appName;
    private String bankCode;
    private String cardNumber;
    private String maskedAccount;
    private String customerAcc;
    private String alias;
    private String agentId;
    private String uSess;
    private Date lastTransaction;
    private String cardScheme;
    private boolean active;
    private boolean pinChanged;
    private String chana;
    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private String param5;
    private Date created;
    private Date modified;
    private Date numModifiedCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthBy() {
        return authBy;
    }

    public void setAuthBy(String authBy) {
        this.authBy = authBy;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getuSess() {
        return uSess;
    }

    public void setuSess(String uSess) {
        this.uSess = uSess;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getChana() {
        return chana;
    }

    public void setChana(String chana) {
    	this.chana = chana;
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getLastTransaction() {
        return lastTransaction;
    }

    public void setLastTransaction(Date lastTransaction) {
        this.lastTransaction = lastTransaction;
    }

    public Date getNumModifiedCount() {
        return numModifiedCount;
    }

    public void setNumModifiedCount(Date numModifiedCount) {
        this.numModifiedCount = numModifiedCount;
    }  

	public String getMaskedAccount() {
		return maskedAccount;
	}

	public void setMaskedAccount(String maskedAccount) {
		this.maskedAccount = maskedAccount;
	}

	public String getCustomerAcc() {
		return customerAcc;
	}

	public void setCustomerAcc(String customerAcc) {
		this.customerAcc = customerAcc;
	}

	public boolean isPinChanged() {
		return pinChanged;
	}

	public void setPinChanged(boolean pinChanged) {
		this.pinChanged = pinChanged;
	}

	@Override
	public String toString() {
		return "MobileProfile [id=" + id + ", authBy=" + authBy + ", mobileNo=" + mobileNo + ", appId=" + appId
				+ ", appName=" + appName + ", bankCode=" + bankCode + ", cardNumber=" + cardNumber + ", maskedAccount="
				+ maskedAccount + ", customerAcc=" + customerAcc + ", alias=" + alias + ", agentId=" + agentId
				+ ", uSess=" + uSess + ", lastTransaction=" + lastTransaction + ", cardScheme=" + cardScheme
				+ ", active=" + active + ", pinChanged=" + pinChanged + ", chana=" + chana + ", param1=" + param1
				+ ", param2=" + param2 + ", param3=" + param3 + ", param4=" + param4 + ", param5=" + param5
				+ ", created=" + created + ", modified=" + modified + ", numModifiedCount=" + numModifiedCount + "]";
	}
}
