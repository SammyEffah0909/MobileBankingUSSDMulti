package com.mobile.bank.processor;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mobile.bank.ussd.model.AESUtil;
import com.mobile.bank.ussd.model.Request;
import com.mobile.bank.ussd.model.Response;
import com.mobile.bank.ussd.model.Transaction;
import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.GeneralUtils;
import com.mobile.bank.util.PropsCache;
import com.etz.http.etc.Card;
import com.etz.http.etc.HttpHost;
import com.etz.http.etc.TransCode;
import com.etz.http.etc.XProcessor;
import com.etz.http.etc.XRequest;
import com.etz.http.etc.XResponse;
import com.etz.security.util.Cryptographer;
import com.fnm.ussd.engine.util.UssdSessionTerminatorInterface;
import com.google.gson.Gson;

public class SendNIBMGRequest extends UssdSessionTerminatorInterface {
	static Logger l = new SendNIBMGRequest().getLogger("MG2.0");

	private static final String KEY = PropsCache.getInstance().getProperty("KEY");
	private static final String IV = PropsCache.getInstance().getProperty("IV");
	private static final String URL = PropsCache.getInstance().getProperty("MG_URL") + "/process";

	private static final String AUTOSWITCH_IP = PropsCache.getInstance().getProperty("AUTOSWITCH_IP");
	private static final String AUTOSWITCH_PORT = PropsCache.getInstance().getProperty("AUTOSWITCH_PORT");

	public String doBalance(int id, String appName, String mobile, String pin) {
		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("B");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("BALANCE REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);

		l.info("BALANCE REQ ENC:: " + reqJson);

		long a = System.currentTimeMillis();
		Response resp = DoHttpRequest.postToWS(URL, reqJson);
		long b = System.currentTimeMillis();

		l.info("BALANCE RESP:: " + gson.toJson(resp));
		l.info("BAL TAT:: " + (b - a));

		return String.format("%s#%s", resp.getError(), resp.getMessage());
	}

	public String doChangePin(int id, String appName, String mobile, String oldPin, String newPin) {
		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedOldPin = AESUtil.AESCBCEncrypt(oldPin, KEY, pCode);
		String encryptedNewPin = AESUtil.AESCBCEncrypt(newPin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("CP");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setPin(encryptedOldPin);
		trnx.setNewPin(encryptedNewPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("CHANGE PIN REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("CHANGE PIN REQ ENC:: " + reqJson);

		long a = System.currentTimeMillis();
		Response resp = DoHttpRequest.postToWS(URL, reqJson);
		long b = System.currentTimeMillis();

		l.info("CHANGE PIN RESP:: " + gson.toJson(resp));
		l.info("CP TAT:: " + (b - a));

		return resp.getMessage();
	}

	public String doAddAccount(int id, String appName, String mobile, String account, String pin, String firstName) {
		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("ACCAD");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setPin(encryptedPin);
		trnx.setNewUserAccount(account);
		trnx.setpCode(pCode);
		trnx.setuSess(firstName);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("ADD ACCOUNT REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("ADD ACCOUNT REQ ENC:: " + reqJson);

		Response resp = DoHttpRequest.postToWS(URL, reqJson);

		l.info("ADD ACCOUNT RESPONSE:: " + gson.toJson(resp));

		return resp.getMessage();
	}

	public String doMiniStatement(int id, String appName, String mobile, String pin) {
		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("L");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("MINI STATEMENT REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("MINI STATEMENT REQ ENC:: " + reqJson);

		long a = System.currentTimeMillis();
		Response resp = DoHttpRequest.postToWS(URL, reqJson);
		long b = System.currentTimeMillis();

		l.info("MINI STATEMENT RESPONSE:: " + gson.toJson(resp));
		l.info("Mini Stmt TAT::" + (b - a));
		return resp.getMessage().replace("\\n", "~");
	}

	private String doVirtualTopup(int id, String appName, String mobile, String network, String account, String amount,
			String pin) {

		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("VL");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setVasType(network);
		trnx.setVasAccount(account);
		trnx.setAmount(amount);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("VTU REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("VTU REQ ENC:: " + reqJson);

		long a = System.currentTimeMillis();
		Response resp = DoHttpRequest.postToWS(URL, reqJson);
		long b = System.currentTimeMillis();

		l.info("VTU RESP:: " + gson.toJson(resp));
		l.info("VTU TAT::" + (b - a));

		return resp.getMessage();
	}

	public String doBillPayment(int id, String appName, String mobile, String biller, String account, String amount,
			String pin) {

		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("BILL");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setVasType(biller);
		trnx.setVasAccount(account);
		trnx.setAmount(amount);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("BILL PAYMENT REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("BILL PAYMENT REQ ENC:: " + reqJson);

		long a = System.currentTimeMillis();
		Response resp = DoHttpRequest.postToWS(URL, reqJson);
		long b = System.currentTimeMillis();

		l.info("BILL PAYMENT RESP:: " + gson.toJson(resp));
		l.info("BILL PAYMENT TAT:: " + (b - a));

		return resp.getMessage();
	}

	public String doFundsTransferToCard(int id, String appName, String mobile, String cardNumber, String amount,
			String pin) {

		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("T");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setAmount(amount);
		trnx.setTarget(cardNumber);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("FT TO CARD REQUEST PLAIN:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("FT TO CARD REQUEST ENCRYPTED:: " + reqJson);

		long a = System.currentTimeMillis();
		Response resp = DoHttpRequest.postToWS(URL, reqJson);
		long b = System.currentTimeMillis();

		l.info("FT TO CARD RESPONSE:: " + gson.toJson(resp));
		l.info("FT TO CARD TAT::" + (b - a));
		return resp.getMessage();
	}

	public String doFundsTransferToBank(int id, String appName, String mobile, String bankCode, String account,
			String amount, String pin) {

		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("TAM");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setAmount(amount);
		trnx.setTarget(account);
		trnx.setTargetBankCode(bankCode);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("FT TO BANK REQUEST PLAIN:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("FT TO BANK REQUEST ENCRYPTED:: " + reqJson);

		Response resp = DoHttpRequest.postToWS(URL, reqJson);

		l.info("FT TO BANK RESPONSE:: " + gson.toJson(resp));

		return resp.getMessage();
	}

	public String doFundsTransferToBank_MMC(int id, String appName, String mobile, String targetBankCode, String amount,
			String account, String pin, String uSess, String clientTransactionId) {

		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("TAMMC");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setAmount(amount);
		trnx.setTarget(account);
		trnx.setTargetBankCode(targetBankCode);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);
		trnx.setuSess(uSess);
		trnx.setClientTransactionID(clientTransactionId);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("FT TO BANK REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("FT TO BANK REQ ENC:: " + reqJson);

		Response resp = DoHttpRequest.postToWS(URL, reqJson);

		l.info("FT TO BANK RESPONSE:: " + gson.toJson(resp));

		return resp.getMessage();
	}

	public String doMobileMoneyDebit(int id, String appName, String mobile, String bankCode, String account,
			String targetBankCode, String amount, String pin, String clientTransactionId) {

		String pCode = GeneralUtils.generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setId(id);
		trnx.setToken("TAMMD");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setAmount(amount);
		trnx.setSource(mobile);
		trnx.setSourceBankCode(bankCode);
		trnx.setTarget(account);
		trnx.setTargetBankCode(targetBankCode);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);
		trnx.setClientTransactionID(clientTransactionId);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("MOMODEBIT REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("MOMODEBIT REQ ENC:: " + reqJson);

		Response resp = DoHttpRequest.postToWS(URL, reqJson);

		l.info("MOMODEBIT RESPONSE:: " + gson.toJson(resp));

		return resp.getMessage();
	}

	public String processUssdRequest(String jsonReq) {
		String resp = "";
		try {
			l.info("Json Received from USSDEngine: " + jsonReq);
			Gson j = new Gson();
			Map m = (Map) j.fromJson(jsonReq, Map.class);
			String mobile = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("**"))[0];
			String transType = (String) m.get("TRANS_TYPE");
			String provider = (String) m.get("PROVIDER");
			String whoAreU = m.containsKey("WHOAREU") ? m.get("WHOAREU").toString() : "";
			String confirm = m.containsKey("CONFIRM") ? m.get("CONFIRM").toString() : "";
			String reference = m.get("REFERENCE").toString().replace("ussdx-", "02NIB");
			String shortCode = (String) m.get("SHORTCODE");
			String projectedInput = m.containsKey("PROJECTEDINPUT") ? (String) m.get("PROJECTEDINPUT") : "";
			int id = Integer.parseInt(whoAreU.split("==")[0]);
			String uSess = whoAreU.split("==")[1];
			l.info("TRANS_TYPE::: " + transType);

			if (!confirm.isEmpty()) {
				if (confirm.contains("No")) {
					return "{\"error\":\"06\",\"msg\":\"You have opted to cancel the current request\"}";
				}

				if (confirm.contains("Exit")) {
					return "{\"error\":\"06\",\"msg\":\"Operation Ended\"}";
				}
			}

			if (!projectedInput.isEmpty() && projectedInput.equals("NOUSERACTION.NETWORKTERMINATED.END")) {
				return "{\"error\":\"06\",\"msg\":\"Session timeout\"}";
			}

			int howMany = shortCode.split("\\*").length - 1;
			String sc = "";

			switch (howMany) {
			case 1:
				sc = shortCode.replaceAll("#", "").replaceAll("\\*", "");
				break;
			case 2:
				sc = shortCode.replaceAll("#", "").replaceAll("\\*", "").substring(3);
				break;
			case 3:
				sc = shortCode.replaceAll("#", "").replaceAll("\\*", "").substring(6);
				break;
			}

			// String pin = (String) m.get("PIN");
			String pin = (String) m.get("PROJECTEDINPUT");

			// String appName = GeneralUtils.getAppName(sc).split(":")[1];
			String appName = PropsCache.getInstance().getProperty(sc).split(":")[1];

			if (transType == null) {
				String option = whoAreU.split("==")[5];
				if (option.equals("CHANGEPIN")) {
					String new1Pin = (String) m.get("NEW1PIN");
					String new2Pin = (String) m.get("NEW2PIN");
					String oldPin = (String) m.get("OLDPIN");
					if (!new1Pin.equals(new2Pin)) {
						return "{\"error\":\"06\",\"msg\":\"Sorry! new Pin details entered do not match\"}";
					}
					resp = doChangePin(id, appName, mobile, oldPin, new1Pin);
				}
			}

			if (Pattern.compile("[0-9]").matcher(transType).find()) {
				// SAME BANK
				String mmNetwork = (String) m.get("MM_NETWORK");
				String amount = (String) m.get("AMOUNT");
				String wallNo1 = (String) m.get("WALLET_NUMBER1");
				String wallNo2 = m.containsKey("WALLET_NUMBER2") ? m.get("WALLET_NUMBER2").toString() : "";
				String walletNo = wallNo1;

				if (!wallNo2.isEmpty()) {
					if (!wallNo1.equals(wallNo2)) {
						return "{\"error\":\"06\",\"msg\":\"Sorry! Account No. entered do not match\"}";
					}
				}
				String bankCode = transType;

				try {
					if (bankCode.split("-").length == 2) {
						bankCode = bankCode.split("-")[0];
					}
				} catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException) {
				}
				if ((bankCode.equals("686")) || (bankCode.equals("844")) || (bankCode.equals("863"))
						|| (bankCode.equals("247"))) {
					walletNo = "233" + wallNo1.substring(1);
				}

				if (mmNetwork.equalsIgnoreCase("self")) {
					String customerAcc = (String) m.get("CUSTACCS");
					String accountToCredit = customerAcc.split("==")[4].replace(",", "");
					// if (customerAcc.equals(whoAreU.split("==")[0].split("-")[1])) {
					// return "{\"error\":\"06\",\"msg\":\"You cannot transfer money to same
					// account. Please try again with a different account\"}";
					// }
					// String otherAcc = customerAcc.split("==")[2];
					resp = doFundsTransferToBank(id, appName, mobile, bankCode, accountToCredit, amount, pin);
				} else {
					// if (walletNo.equals(whoAreU.split("==")[0].split("-")[1])) {
					// return "{\"error\":\"06\",\"msg\":\"You cannot transfer money to same
					// account. Please try again with a different account\"}";
					// }
					resp = doFundsTransferToBank(id, appName, mobile, bankCode, walletNo, amount, pin);
				}
			}

			if ((transType.equals("B"))) {
				resp = doBalance(id, appName, mobile, pin).split("#")[1];
			} else if (transType.equals("VL")) {
				String network = m.containsKey("NETWORK") ? m.get("NETWORK").toString() : "";
				String amount = (String) m.get("AMOUNT");
				String vtuOption = m.containsKey("VTUOPTION") ? (String) m.get("VTUOPTION") : "";

				String mob = m.containsKey("MOBILE") ? (String) m.get("MOBILE") : "";
				String topupMobile;
				if (!vtuOption.isEmpty()) {
					if (vtuOption.equalsIgnoreCase("self")) {
						topupMobile = formatMobile(mobile);
						network = getProvider(provider);
					} else {
						topupMobile = formatMobile(mob);
					}
				} else {
					topupMobile = formatMobile(mob);
				}

				resp = doVirtualTopup(id, appName, mobile, network, topupMobile, amount, pin);
			} else if (transType.equals("TAM")) {
				String amount = (String) m.get("AMOUNT");
				String narration = (String) m.get("USER_REFERENCE");
				String wallNo1 = m.containsKey("WALLET_NUMBER1") ? m.get("WALLET_NUMBER1").toString()
						: m.get("DESTACCT").toString();
				String wallNo2 = m.containsKey("WALLET_NUMBER2") ? m.get("WALLET_NUMBER2").toString() : "";
				String walletNo = wallNo1;
				String accountToDebit = whoAreU.split("==")[4].replace(",", "");

				if (!wallNo2.isEmpty()) {
					if (!wallNo1.equals(wallNo2)) {
						return "{\"error\":\"06\",\"msg\":\"Sorry! Account No. entered do not match\"}";
					}
				}
				String bankCode = !m.containsKey("MM_NETWORK") ? m.get("VASTYPE").toString()
						: m.get("MM_NETWORK").toString();

				try {
					if (bankCode.split("-").length == 2) {
						bankCode = bankCode.split("-")[0];
					}
				} catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException) {
				}
				if ((bankCode.equals("686")) || (bankCode.equals("844")) || (bankCode.equals("863"))
						|| (bankCode.equals("247"))) {
					walletNo = "233" + wallNo1.substring(1);
				}
				// resp = doFundsTransferToBank(id, appName, mobile, bankCode, walletNo, amount,
				// pin);
				if (bankCode.startsWith("999"))
					bankCode = bankCode.substring(3);

				resp = doBalance(id, appName, mobile, pin).split("#")[0];
				if (Integer.parseInt(resp) == 0)
					resp = sendGIPRequest(shortCode, accountToDebit, amount, bankCode, walletNo, reference, narration);
				else
					resp = "Your request could not be processed. Please try again later";
			} else if (transType.equals("TAMMC")) {

				String vastype = m.containsKey("VASTYPE") ? (String) m.get("VASTYPE") : "";
				String amount = (String) m.get("AMOUNT");
				String bankCode = m.containsKey("MM_NETWORK") ? (String) m.get("MM_NETWORK") : getMMNOCode(provider);
				String wallNo = "";
				// String clientTransactionId = confirm.split("[|]")[1];
				String clientTransactionId = "";

				try {
					if (bankCode.split("-").length == 2) {
						bankCode = bankCode.split("-")[0];
					}
				} catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException1) {
				}

				if (vastype.equalsIgnoreCase("self")) {
					wallNo = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("**"))[0];
				} else {
					String wallNo1 = (String) m.get("WALLET_NUMBER1");
					String wallNo2 = m.containsKey("WALLET_NUMBER2") ? m.get("WALLET_NUMBER2").toString() : "";
					wallNo = wallNo1;

					if (!wallNo2.isEmpty()) {
						if (!wallNo1.equals(wallNo2)) {
							return "{\"error\":\"06\",\"msg\":\"Sorry! Account No. entered do not match\"}";
						}
					}

					if ((bankCode.equals("686")) || (bankCode.equals("844")) || (bankCode.equals("863"))
							|| (bankCode.equals("247"))) {
						wallNo = "233" + wallNo1.substring(1);
					}
				}
				resp = doFundsTransferToBank_MMC(id, appName, mobile, bankCode, amount, wallNo, pin, uSess,
						clientTransactionId);
			} else if (transType.equalsIgnoreCase("TAMMD")) {
				l.info(":::: Initiating Transfer to Account ::::");
				String vastype = m.containsKey("VASTYPE") ? (String) m.get("VASTYPE") : "";
				String amount = (String) m.get("AMOUNT");
				String account = m.containsKey("ACCT") ? (String) m.get("ACCT") : "";
				String targetbankCode = (String) m.get("MM_NETWORK");
				String srcbankCode = getProviderCode(provider);
				// String momoNetwork = "";
				String accountToCredit = account.split("==")[4].replace(",", "");
				// String clientTransactionId = confirm.split("[|]")[1];
				String clientTransactionId = "";

				try {
					if (targetbankCode.split("-").length == 2) {
						// momoNetwork = targetbankCode.split("-")[0];
						targetbankCode = targetbankCode.split("-")[1];
					}
				} catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException2) {
				}

				resp = doMobileMoneyDebit(id, appName, mobile, srcbankCode, accountToCredit, targetbankCode, amount,
						"0000", clientTransactionId);
			} else if (transType.equals("T")) {
				String amount = (String) m.get("AMOUNT");
				String cardNo = (String) m.get("DEST_CARD");
				// String bankCode = (String) m.get("MM_NETWORK");

				resp = doFundsTransferToCard(id, appName, mobile, cardNo, amount, pin);
			} else if (transType.equals("BILL")) {
				String biller = (String) m.get("BILLER");
				String billAcct1 = (String) m.get("BILL ACCT_1");
				String billAcct2 = m.containsKey("BILL ACCT_2") ? m.get("BILL ACCT_2").toString() : "";
				String amount = (String) m.get("AMT");

				if (!billAcct2.isEmpty()) {
					if (!billAcct1.equals(billAcct2)) {
						return "{\"error\":\"06\",\"msg\":\"Sorry! Account details entered do not match\"}";
					}
				}
				resp = doBillPayment(id, appName, mobile, biller, billAcct1, amount, pin);
			} else if (transType.equals("BUNDLE")) {
				String destAcct = "";
				String amount = "";
				String biller = (String) m.get("VASTYPE");

				if (biller.equalsIgnoreCase("SURF")) {
					String verifyData = m.get("VERIFYDATA").toString();
					amount = verifyData.split(":")[1];
					String bundleCode = verifyData.split(":")[2];
					destAcct = String.format("%s|%s", m.get("DESTACCT").toString(), bundleCode);
				} else if (biller.equalsIgnoreCase("BUSY")) {
					String verifyData = m.get("AMOUNT").toString();
					amount = verifyData.split(":")[0];
					String bundleCode = verifyData.split("\\|")[1];
					destAcct = String.format("%s|%s", m.get("DESTACCT").toString(), bundleCode);
				} else if (biller.equals("TELESOL")) {
					String verifyData = m.get("AMOUNT").toString();
					amount = verifyData.split("\\|")[0];
					String offerId = verifyData.split("\\|")[1];
					destAcct = m.get("DESTACCT").toString();

					destAcct = String.format("%s#%s", destAcct, offerId);
				} else if (biller.equals("GLODATA")) {
					String[] amountParts = m.get("AMOUNT").toString().split("\\|");
					amount = amountParts[0];
					String dataPlan = amountParts[1];
					destAcct = m.get("DESTACCT").toString();
					destAcct = String.format("%s-%s", destAcct, dataPlan);
				} else if (biller.equals("MTNDATA")) {
					// 0.5|MTNDLY20MB
					String verifyData = m.get("AMOUNT").toString();
					amount = verifyData.split("\\|")[0];
					String bundleData = verifyData.split("\\|")[1];
					destAcct = m.get("DESTACCT").toString();

					destAcct = String.format("%s#%s", destAcct, bundleData);
				} else if (biller.equals("VFDATA")) {
					amount = m.get("AMOUNT").toString().split("\\|")[0];
					destAcct = m.get("DESTACCT").toString();
				} else {
					amount = m.get("AMOUNT").toString();
					destAcct = m.get("DESTACCT").toString();
				}
				resp = doBillPayment(id, appName, mobile, biller, destAcct, amount, pin);
			} else if (transType.equals("AIRLINE")) {
				String amount = (String) m.get("AMOUNT");
				String billerAcct = (String) m.get("DESTACCT");
				String biller = (String) m.get("VASTYPE");

				resp = doBillPayment(id, appName, mobile, biller, billerAcct, amount, pin);
			} else if (transType.equals("CP")) {
				String new1Pin = (String) m.get("NEW1PIN");
				String new2Pin = (String) m.get("NEW2PIN");
				String oldPin = (String) m.get("OLDPIN");
				if (!new1Pin.equals(new2Pin)) {
					return "{\"error\":\"06\",\"msg\":\"Sorry! new Pin details entered do not match\"}";
				}
				resp = doChangePin(id, appName, mobile, oldPin, new1Pin);
			} else if (transType.equals("MINI")) {
				l.info(":::: Initiating Mini Statement ::::");
				resp = doMiniStatement(id, appName, mobile, pin);
			} else if (transType.equals("ADACC")) {
				l.info("::: Add account implementation");
				String account = (String) m.get("NEWUSERACCT");
				String match = (String) m.get("MATCH");
				String firstName = match.split("::")[5];

				resp = doBalance(id, appName, mobile, pin).split("#")[0];
				if (Integer.parseInt(resp) == 0)
					resp = doAddAccount(id, appName, mobile, account, pin, firstName);
				else
					resp = "Your request could not be processed. Please try again later";
			} else if (transType.equals("GHQRE")) {
				String terminalId = (String) m.get("TERMINAL");
				String amount = (String) m.get("AMOUNT");
				String narration = (String) m.get("USER_REFERENCE");
				// String cardNumber = whoAreU.split("==")[2];
				String accountToDebit = whoAreU.split("==")[4].replace(",", "");
				String verify = (String) m.get("VERIFY");
				String merchantName = verify.split("#")[0];

				resp = doBalance(id, appName, mobile, pin).split("#")[0];
				if (Integer.parseInt(resp) == 0)
					resp = payMerchant(shortCode, merchantName, accountToDebit, terminalId, amount, reference,
							narration);
				else
					resp = "Your request could not be processed. Please try again later";
			}
			l.info("Response received from :: " + resp);
		} catch (Exception e) {
			l.error("processUssdRequest()", e);
		}
		if (resp.isEmpty()) {
			resp = "Transaction cannot be processed at the moment. Please try again later";
		}
		return "{\"error\":\"00\",\"msg\":\"" + resp + "\"}";
	}

	public String processUssdResponse(String formatter, String msg2format) {
		String w = "";
		try {
			Gson gson = new Gson();
			Map m = (Map) gson.fromJson(msg2format, Map.class);
			w = (String) m.get("msg");
		} catch (Exception e) {
			l.error(e, e);
		}
		return w;
	}

	private String formatMobile(String mobile) {
		if (mobile.startsWith("233")) {
			return mobile;
		}
		if (mobile.startsWith("0")) {
			mobile = "233" + mobile.substring(1);
		} else if (mobile.startsWith("+")) {
			mobile = mobile.substring(1);
		}
		return mobile;
	}

	private String getProvider(String provider) {
		String network = "";

		switch (provider) {
		case "MTNSDP389":
			network = "MTN";
			break;
		case "VODAFONEGH":
			network = "VODAFONE";
			break;
		case "TIGO":
			network = "TIGO";
			break;
		case "GLO":
			network = "GLO";
			break;
		default:
			network = "MTN";
		}

		return network;
	}

	private String getProviderCode(String provider) {
		String network = "";

		switch (provider) {
		case "MTNSDP389":
			network = "686";
			break;
		case "VODAFONEGH":
			network = "863";
			break;
		case "TIGO":
			network = "844";
			break;
		default:
			network = "686";
		}

		return network;
	}

	private String getMMNOCode(String provider) {
		String network = "";

		switch (provider) {
		case "MTNSDP389":
			network = "686";
			break;
		case "VODAFONEGH":
			network = "863";
			break;
		case "TIGO":
			network = "844";
			break;
		default:
			network = "686";
		}

		return network;
	}

	private String getAliasPrefix(String code) {
		String c = "";

		try {
			if (code.length() == 7) {
				c = code.substring(code.length() - 2, code.length()).replace("#", "");
			} else if (code.length() > 5 && code.length() <= 9) {
				c = code.substring(5).replace("#", "");
			} else if (code.length() > 9) {
				c = code.substring(9, code.length()).replace("#", "");
			} else {
				c = code.substring(1).replace("#", "");
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			return PropsCache.getInstance().getProperty("389");
		}
		return PropsCache.getInstance().getProperty(c + "PREFIX");
	}

	/*
	 * public static String payMerchant(String merchantName, String account, String
	 * terminalId, String amount, String reference, String pin) { String returnValue
	 * = ""; String ipAddress = "172.16.30.6"; String key = "123456"; String
	 * expiryDate = "022020"; int port = 8080; XProcessor processor = new
	 * XProcessor();
	 * 
	 * HttpHost host = new HttpHost(); host.setServerAddress(ipAddress);
	 * host.setPort(port); host.setSecureKey(key);
	 * 
	 * Card card = new Card(); card.setCardNumber(account);
	 * card.setCardExpiration(expiryDate); card.setCardPin(pin);
	 * 
	 * XRequest request = new XRequest();
	 * request.setTransAmount(Double.parseDouble(amount)); //
	 * request.setMerchantCode("999" + terminalId);
	 * request.setMerchantCode("0067510000010000"); request.setCard(card);
	 * request.setTransCode(TransCode.PAYMENT); request.setChannelId("02");
	 * request.setCurrency("936"); request.setDescription("QRC#" + terminalId);
	 * request.setReference(reference); request.setOtherReference(reference);
	 * 
	 * XResponse response = null;
	 * 
	 * try { response = processor.process(host, request);
	 * 
	 * l.info("Executing QRCode request..."); l.info("Response: " +
	 * response.getResponse()); l.info("Message: " + response.getMessage());
	 * l.info("Custom XML: " + response.getCustomXml());
	 * 
	 * if (response.getResponse() == 0) { returnValue = String.
	 * format("You have sent GHS%s to %s.~Terminal ID: %s~Transaction ID:%s~Thank You"
	 * , amount, merchantName, terminalId, reference); } else { returnValue =
	 * "Your request could not be processed. Please try again later"; } } catch
	 * (Exception e) { e.printStackTrace(); }
	 * 
	 * return returnValue; }
	 */

	public static String cryptPan(String pan, int encType) {
		String cryptedPan = "";
		Cryptographer crypt = new Cryptographer();
		byte[] epinblock = null;
		String mmk = "01010101010101010101010101010101";
		if (encType == 1) {
			String padValue = "FFFFFF" + pan.substring(6);
			try {
				crypt.getClass();
				epinblock = crypt.doCryto(padValue, mmk, 1);
				cryptedPan = pan.substring(0, 6) + Cryptographer.byte2hex(epinblock);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				crypt.getClass();
				epinblock = crypt.doCryto(pan.substring(6), mmk, 2);
				String decPan = Cryptographer.byte2hex(epinblock).substring(6);
				if (decPan.startsWith("FFFFFF")) {
					decPan = decPan.substring(6);
				}
				cryptedPan = pan.substring(0, 6) + decPan;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return cryptedPan;
	}

	public static String sendGIPRequest(String shortcode, String account, String amount, String destBankCode,
			String destAccount, String reference, String narration) {
		String returnValue = "failed";
		String ipAddress = AUTOSWITCH_IP;
		String key = "123456";
		String expiryDate = "022020";
		int port = Integer.parseInt(AUTOSWITCH_PORT);

		int howMany = shortcode.split("\\*").length - 1;
		String sc = "";

		switch (howMany) {
		case 1:
			sc = shortcode.replaceAll("#", "").replaceAll("\\*", "");
			break;
		case 2:
			sc = shortcode.replaceAll("#", "").replaceAll("\\*", "").substring(3);
			break;
		case 3:
			sc = shortcode.replaceAll("#", "").replaceAll("\\*", "").substring(6);
			break;
		}

		String bankCode = PropsCache.getInstance().getProperty(sc + "BC");
		XProcessor processor = new XProcessor();

		HttpHost host = new HttpHost();
		host.setServerAddress(ipAddress);
		host.setPort(port);
		host.setSecureKey(key);

		Card card = new Card();
		// System.out.println(String.format("%s%s", bankCode, account));
		card.setCardNumber(String.format("%s%s", bankCode, account));
		card.setCardExpiration(expiryDate);

		XRequest request = new XRequest();
		request.setXmlString(String.format("<CBARequest>GIP:%s:%s:%s:%s:%s</CBARequest>", destAccount, destBankCode,
				amount, reference, narration));
		request.setCard(card);
		request.setTransCode(TransCode.BANKSERVICE);

		XResponse response = null;

		try {
			response = processor.process(host, request);

			l.info("Executing GIP Request...");
			l.info("Response: " + response.getResponse());
			l.info("Message: " + response.getMessage());
			l.info("Custom XML: " + response.getCustomXml());

			if (response.getResponse() == 0) {
				returnValue = String.format("You have sent GHS%s to %s.~Transaction ID:%s~Thank You", amount,
						destAccount, reference);
			} else {
				returnValue = "Your request could not be processed. Please try again later.~Ref:" + reference;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnValue;
	}

	public static String payMerchant(String shortcode, String merchantName, String account, String terminalId,
			String amount, String reference, String narration) {
		String returnValue = "failed";
		String ipAddress = AUTOSWITCH_IP;
		String key = "123456";
		String expiryDate = "022020";
		int port = Integer.parseInt(AUTOSWITCH_PORT);

		int howMany = shortcode.split("\\*").length - 1;
		String sc = "";

		switch (howMany) {
		case 1:
			sc = shortcode.replaceAll("#", "").replaceAll("\\*", "");
			break;
		case 2:
			sc = shortcode.replaceAll("#", "").replaceAll("\\*", "").substring(3);
			break;
		case 3:
			sc = shortcode.replaceAll("#", "").replaceAll("\\*", "").substring(6);
			break;
		}

		String bankCode = PropsCache.getInstance().getProperty(sc + "BC");

		XProcessor processor = new XProcessor();

		HttpHost host = new HttpHost();
		host.setServerAddress(ipAddress);
		host.setPort(port);
		host.setSecureKey(key);

		Card card = new Card();
		card.setCardNumber(String.format("%s%s", bankCode, account));
		card.setCardExpiration(expiryDate);

		XRequest request = new XRequest();
		request.setXmlString(
				String.format("<CBARequest>QRC:%s:%s:%s:%s</CBARequest>", terminalId, amount, reference, narration));
		request.setCard(card);
		request.setTransCode(TransCode.BANKSERVICE);

		XResponse response = null;

		try {
			response = processor.process(host, request);

			l.info("Executing QRCode request...");
			l.info("Response: " + response.getResponse());
			l.info("Message: " + response.getMessage());
			l.info("Custom XML: " + response.getCustomXml());

			if (response.getResponse() == 0) {
				returnValue = String.format("You have sent GHS%s to %s.~Terminal ID: %s~Transaction ID:%s~Thank You",
						amount, merchantName, terminalId, reference);
			} else {
				returnValue = "Your request could not be processed. Please try again later.~Ref:" + reference;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnValue;
	}

	public static void main(String[] args) {
		String miniBal = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"TRANS_TYPE\":\"B\",\"SHORTCODE\":\"*389*389\", \"MSISDN\":\"233209157113\", \"PIN\":\"0007\",\"PROVIDER\":\"VODAFONEGH\"}";
		String tammc = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104992308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"VASTYPE\":\"SELF\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"TAMMC\", \"PIN\":\"0007\"}";
		String t = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104992308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"DEST_CARD\":\"0068860077370006\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"T\", \"PIN\":\"0007\"}";
		String tam = "";
		String tammdSelf = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104992308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"VASTYPE\":\"SELF\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"TAMMD\", \"PIN\":\"0007\", \"PROVIDER\":\"MTNSDP389\"}";
		String tammdOther = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104956308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"VASTYPE\":\"OTHER\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"TAMMD\", \"PIN\":\"0007\", \"PROVIDER\":\"MTNSDP389\", \"ACCT\":\"0068860077370006\"}";
		String bill = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\", \"MSISDN\":\"233209157113***77277273\",\"AMT\":\"1\", \"BILL ACCT_1\":\"7029042239\", \"BILL ACCT_2\":\"\", \"BILLER\":\"DSTV\", \"TRANS_TYPE\":\"BILL\", \"PIN\":\"0007\", \"PROVIDER\":\"MTNSDP389\", \"SHORTCODE\":\"*389*389#\"}";
		String gip = "{\"AMOUNT\":\"1\",\"BANKCAT\":\"\",\"CONFIRM\":\"A|Yes\",\"DESTACCT\":\"00117679802552\",\"LOGIN\":\"A|57\",\"MM_NETWORK\":\"006\",\"MSISDN\":\"233548933270***105808281\",\"PIN\":\"1111\",\"PINVERIF\":\"\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"MTNSDP389\",\"REFERENCE\":\"ussdx-XO-0234048-D54E-223\",\"SHORTCODE\":\"*389*710\",\"TRANS_TYPE\":\"TAM\",\"VERIFY\":\"ARYEH NII AYITE\",\"WHOAREU\":\"57\\u003d\\u003dDENNIS AKOMEAH\\u003d\\u003d00500171314D19B76FE8DB\\u003d\\u003d240*****501\\u003d\\u003d2402038329501,\"}";
		String qr = "{\"AMOUNT\":\"1\",\"CONFIRM\":\"A|Yes\",\"LOGIN\":\"A|57\",\"MSISDN\":\"233548933270***105832997\",\"PIN\":\"1111\",\"PINVERIF\":\"\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"MTNSDP389\",\"REFERENCE\":\"ussdx-YM-3642414-E42B-dd4\",\"SHORTCODE\":\"*389*710\",\"TERMINAL\":\"3800270914\",\"TRANS_TYPE\":\"GHQRE\",\"VERIFY\":\"DSTV#0.0#00020101021132790018GH.NET.GHIPSS.GHQR01161064024100064025020601000003030010416E20210913T1200005204341253039365802GH5904DSTV6005ACCRA6108000000016233031500000000006518807103800270914630412A1\",\"WHOAREU\":\"57\\u003d\\u003dDENNIS AKOMEAH\\u003d\\u003d00500171314D19B76FE8DB\\u003d\\u003d240*****501\\u003d\\u003d2402038329501,\"}";
		// System.out.println(new SendNIBMGRequest().processUssdRequest(qr));
		// System.out.println("ussdx-293930-3949".replace("ussdx-", "02"));
	}

}
