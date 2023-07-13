package com.mobile.bank.processor;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import com.mobile.bank.ussd.model.AESUtil;
import com.mobile.bank.ussd.model.Request;
import com.mobile.bank.ussd.model.Response;
import com.mobile.bank.ussd.model.Transaction;
import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.GeneralUtils;
import com.mobile.bank.util.PropsCache;
import com.fnm.ussd.engine.util.UssdSessionTerminatorInterface;
import com.google.gson.Gson;

public class SendMGRequest extends UssdSessionTerminatorInterface {
	static Logger l = new SendMGRequest().getLogger("MG2.0");

	private static final String KEY = PropsCache.getInstance().getProperty("KEY");
	private static final String IV = PropsCache.getInstance().getProperty("IV");
	private static final String URL = PropsCache.getInstance().getProperty("MG2URLDEMO") + "/process";
	// private static final String URL =
	// PropsCache.getInstance().getProperty("MG2URLDEMO") + "/process";
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
		String pCode = generateRandomString(16);
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
		String pCode = generateRandomString(16);
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

		String pCode = generateRandomString(16);
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

		String pCode = generateRandomString(16);
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

		String pCode = generateRandomString(16);
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

		String pCode = generateRandomString(16);
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

		String pCode = generateRandomString(16);
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

	public String doNoProfileMobileMoneyDebit(String appName, String mobile, String bankCode, String account,
			String targetBankCode, String amount, String pin) {

		String pCode = generateRandomString(16);
		String encryptedPin = AESUtil.AESCBCEncrypt(pin, KEY, pCode);

		Transaction trnx = new Transaction();
		trnx.setToken("NOPROFILEW2B");
		trnx.setMsisdn(mobile);
		trnx.setAppName(appName);
		trnx.setAmount(amount);
		trnx.setSource(mobile);
		trnx.setSourceBankCode(bankCode);
		trnx.setTarget(account);
		trnx.setTargetBankCode(targetBankCode);
		trnx.setPin(encryptedPin);
		trnx.setpCode(pCode);

		Gson gson = new Gson();
		String json = gson.toJson(trnx);

		l.info("NOPROFILE MOMODEBIT REQ:: " + json);

		String encryptedMsg = AESUtil.AESCBCEncrypt(json, KEY, IV);

		Request req = new Request();
		req.setId(mobile);
		req.setMsg(encryptedMsg);

		String reqJson = gson.toJson(req);
		l.info("NOPROFILE MOMODEBIT REQ ENC:: " + reqJson);

		Response resp = DoHttpRequest.postToWS(URL, reqJson);

		l.info("NOPROFILE MOMODEBIT RESPONSE:: " + gson.toJson(resp));

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
			String projectedInput = m.containsKey("PROJECTEDINPUT") ? (String) m.get("PROJECTEDINPUT") : "";

			int id = !whoAreU.isEmpty() ? Integer.parseInt(whoAreU.split("==")[0]) : 0;
			String uSess = whoAreU.split("==")[1];

			l.info("TRANS_TYPE::: " + transType);
			l.info("sending to URL::" + URL);

			String trnxChoice = (String) m.get("TXNCHOICE");
			trnxChoice = trnxChoice == null ? "A" : trnxChoice;

			String pin = (String) m.get("PIN");
			String shortCode = (String) m.get("SHORTCODE");

			if (!confirm.isEmpty()) {
				if (confirm.equals("NOUSERACTION.NETWORKTERMINATED.END")) {
					return "{\"error\":\"06\",\"msg\":\"Session timeout\"}";
				}

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

			String appName = PropsCache.getInstance().getProperty(sc).split(":")[1];

			if (Pattern.compile("[0-9]").matcher(transType).find()) {
				// SAME BANK
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

				resp = doFundsTransferToBank(id, appName, mobile, bankCode, walletNo, amount, pin);
			}

			if ((transType.equals("B")) || (trnxChoice.equals("B"))) {
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
				String wallNo1 = m.containsKey("WALLET_NUMBER1") ? m.get("WALLET_NUMBER1").toString()
						: m.get("DESTACCT").toString();
				String wallNo2 = m.containsKey("WALLET_NUMBER2") ? m.get("WALLET_NUMBER2").toString() : "";
				String walletNo = wallNo1;

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
				resp = doFundsTransferToBank(id, appName, mobile, bankCode, walletNo, amount, pin);
			} else if (transType.equals("TAMMC")) {

				String vastype = m.containsKey("VASTYPE") ? (String) m.get("VASTYPE") : "";
				String amount = (String) m.get("AMOUNT");
				String clientTransactionId = confirm.split("[|]")[1];
				String bankCode = "";
				String wallNo = "";

				if (vastype.equalsIgnoreCase("self")) {
					wallNo = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("**"))[0];
					bankCode = getProviderCode(provider);
				} else {
					bankCode = (String) m.get("MM_NETWORK");
					String wallNo1 = (String) m.get("WALLET_NUMBER1");
					String wallNo2 = m.containsKey("WALLET_NUMBER2") ? m.get("WALLET_NUMBER2").toString() : "";
					wallNo = wallNo1;

					try {
						if (bankCode.split("-").length == 2) {
							bankCode = bankCode.split("-")[0];
						}
					} catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException1) {
					}

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
				String targetbankCode = PropsCache.getInstance().getProperty(sc + "BC");
				String srcbankCode = getProviderCode(provider);
				String accountToCredit = "";
				String clientTransactionId = confirm.split("[|]")[1];

				if (vastype.equalsIgnoreCase("SELF")) {
					accountToCredit = account.split("==")[4].replace(",", "");
				} else {
					accountToCredit = account;
				}

				resp = doMobileMoneyDebit(id, appName, mobile, srcbankCode, accountToCredit, targetbankCode, amount,
						"0000", clientTransactionId);
			} else if (transType.equals("T")) {
				String amount = (String) m.get("AMOUNT");
				String cardNo = (String) m.get("DEST_CARD");
				String bankCode = (String) m.get("MM_NETWORK");

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

				String res = doBalance(id, appName, mobile, pin).split("#")[0];
				if (Integer.parseInt(res) == 0)
					resp = doAddAccount(id, appName, mobile, account, pin, firstName);
				else
					resp = "Your request could not be processed. Please try again later";
			} else if (transType.equals("NOPROFILEW2B")) {
				// GA RURAL W2B NO LOGIN
				String account = m.containsKey("ACCT") ? (String) m.get("ACCT") : "";
				String targetbankCode = (String) m.get("MM_NETWORK");
				String srcbankCode = getProviderCode(provider);
				String amount = (String) m.get("AMOUNT");
				try {
					if (targetbankCode.split("-").length == 2) {
						targetbankCode = targetbankCode.split("-")[1];
					}
				} catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException2) {
				}
				resp = doNoProfileMobileMoneyDebit(appName, mobile, srcbankCode, account, targetbankCode, amount,
						"0000");
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

	private String generateRandomString(int length) {
		Random RANDOM = new SecureRandom();
		String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		StringBuilder returnValue = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}

		return new String(returnValue);
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

	public static void main(String[] args) {
		// String balance =
		// "{\"MAINOPTIONS\":\"\",\"MSISDN\":\"233548933270***105199219\",\"PIN\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-ER-3937147-G37E-863\",\"SHORTCODE\":\"*389*389*21\",\"TRANS_TYPE\":\"B\",\"WHOAREU\":\"17==null==903001D575FB05CB2B55EC\"}";
		String miniBal = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"TRANS_TYPE\":\"B\",\"SHORTCODE\":\"*389*389\", \"MSISDN\":\"233209157113\", \"PIN\":\"0007\",\"PROVIDER\":\"VODAFONEGH\"}";
		String tammc = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104992308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"VASTYPE\":\"SELF\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"TAMMC\", \"PIN\":\"0007\"}";
		String t = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104992308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"DEST_CARD\":\"0068860077370006\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"T\", \"PIN\":\"0007\"}";
		String tam = "";
		String tammdSelf = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104992308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"VASTYPE\":\"SELF\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"TAMMD\", \"PIN\":\"0007\", \"PROVIDER\":\"MTNSDP389\"}";
		String tammdOther = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\",\"MSISDN\":\"233548933270***104956308\",\"SHORTCODE\":\"*389*389#\",\"MM_NETWORK\":\"686\",\"VASTYPE\":\"OTHER\", \"AMOUNT\":\"0.01\", \"TRANS_TYPE\":\"TAMMD\", \"PIN\":\"0007\", \"PROVIDER\":\"MTNSDP389\", \"ACCT\":\"0068860077370006\"}";
		String bill = "{\"WHOAREU\":\"6==null==006886E3D36B52115F697E\", \"MSISDN\":\"233209157113***77277273\",\"AMT\":\"1\", \"BILL ACCT_1\":\"7029042239\", \"BILL ACCT_2\":\"\", \"BILLER\":\"DSTV\", \"TRANS_TYPE\":\"BILL\", \"PIN\":\"0007\", \"PROVIDER\":\"MTNSDP389\", \"SHORTCODE\":\"*389*389#\"}";
		System.out.println(new SendMGRequest().processUssdRequest(miniBal));
	}

}
