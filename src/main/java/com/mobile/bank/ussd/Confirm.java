package com.mobile.bank.ussd;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mobile.bank.util.DoHttpRequest;
import com.mobile.bank.util.PropsCache;
import com.etz.http.etc.Card;
import com.etz.http.etc.HttpHost;
import com.etz.http.etc.XProcessor;
import com.etz.http.etc.XRequest;
import com.etz.http.etc.XResponse;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;

public class Confirm extends UssdActionClassInterface {

	static Logger l = new Confirm().getLogger("Confirmation");
	static String AUTOSWITCH_IP = "";
	static int AUTOSWITCH_PORT = 8080;

	static {
		try {
			AUTOSWITCH_IP = PropsCache.getInstance().getProperty("AUTOSWITCH_IP");
			AUTOSWITCH_PORT = Integer.parseInt(PropsCache.getInstance().getProperty("AUTOSWITCH_PORT"));
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	@Override
	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();

		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			// get inputholders from USSD Menu Here
			String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
			// String vastype = m.get("VASTYPE");
			String amount = "";
			String whoAreU = m.containsKey("WHOAREU") ? m.get("WHOAREU") : "";
			String transType = m.get("TRANS_TYPE");
			String reference = m.get("REFERENCE").toString().replace("ussdx-", "02RF");
			String provider = m.get("PROVIDER");
			String shortcode = m.get("SHORTCODE").replaceAll("#", "");
			String msg = "";
			String ref = "";

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

			String[] elevy;

			String senderBankCode = PropsCache.getInstance().getProperty(sc + "BC");

			// if (transType.equals("TAM")) {
			// amount = (String) m.get("AMOUNT");
			// String wallNo1 = m.containsKey("WALLET_NUMBER1") ?
			// m.get("WALLET_NUMBER1").toString()
			// : m.get("DESTACCT").toString();
			//
			// }

			if (sc.equals("021") || sc.equals("840")) {
				if (transType.equals("TAM")) {
					String mmNetwork = m.get("MM_NETWORK");
					String cardNumToDebit = "C:" + whoAreU.split("==")[2];
					if (mmNetwork.equals("021")) {
						amount = m.get("AMOUNT");
						String walletNum = m.get("WALLET_NUMBER1");

						elevy = fetchFees(Double.parseDouble(amount), senderBankCode, cardNumToDebit, mn, mmNetwork,
								walletNum, "TAM").split("[|]");

						if (elevy[0].equals("00") || elevy[0].equals("01")) {
							msg = String.format("Transfer GHS%s to %s", amount, walletNum);
							msg += " Elevy: " + elevy[1];
							ref = elevy[2];
							if (elevy[0].equals("01")) {
								msg += "~Overcharged elevy fees will be reversed";
							}
						} else {
							f.put("TMSG", "Sorry, something went wrong. Please try again.~~");
							f.put("1|Exit", "Exit");
							return f;
						}
					}
				}
			}

			if (transType.equals("TAMMC")) {
				// String accountToDebit = whoAreU.split("==")[4].replace(",", "");
				String cardNumToDebit = "C:" + whoAreU.split("==")[2];
				String vastype = m.containsKey("VASTYPE") ? (String) m.get("VASTYPE") : "";
				amount = m.get("AMOUNT");
				String bankCode = m.containsKey("MM_NETWORK") ? (String) m.get("MM_NETWORK") : getMMNOCode(provider);
				String walletNum = "";

				try {
					if (bankCode.split("-").length == 2) {
						bankCode = bankCode.split("-")[0];
					}
				} catch (ArrayIndexOutOfBoundsException ex) {
				}

				if (vastype.equalsIgnoreCase("self")) {
					walletNum = mn;
					elevy = fetchFees(Double.parseDouble(amount), senderBankCode, cardNumToDebit, mn, bankCode,
							walletNum, "TAMMC").split("[|]");

					if (elevy[0].equals("00") || elevy[0].equals("01")) {
						msg = String.format("Transfer GHS%s to %s", amount, walletNum);
						ref = elevy[2];
					} else {
						f.put("TMSG", "Sorry, something went wrong. Please try again.~~");
						f.put("1|Exit", "Exit");
						return f;
					}
				} else {
					String verify = m.get("VERIFY");
					walletNum = m.get("WALLET_NUMBER1");
					elevy = fetchFees(Double.parseDouble(amount), senderBankCode, cardNumToDebit, mn, bankCode,
							walletNum, "TAMMC").split("[|]");

					if (elevy[0].equals("00") || elevy[0].equals("01")) {
						msg = String.format("Transfer GHS%s to %s - %s", amount, verify, walletNum);
						ref = elevy[2];
					} else {
						f.put("TMSG", "Sorry, something went wrong. Please try again.~~");
						f.put("1|Exit", "Exit");
						return f;
					}
				}
				if (elevy[0].equals("00") || elevy[0].equals("01")) {
					msg += " Elevy: " + elevy[1];
					ref = elevy[2];
					if (elevy[0].equals("01")) {
						msg += "~Overcharged elevy fees will be reversed";
					}
				} else {
					f.put("TMSG", "Sorry, something went wrong. Please try again.~~");
					f.put("1|Exit", "Exit");
					return f;
				}
			} else if (transType.equals("TAMMD")) {
				String vastype = m.containsKey("VASTYPE") ? (String) m.get("VASTYPE") : "";
				amount = (String) m.get("AMOUNT");
				String account = m.containsKey("ACCT") ? (String) m.get("ACCT") : "";
				String targetbankCode = m.containsKey("MM_NETWORK") ? (String) m.get("MM_NETWORK")
						: getMMNOCode(provider);
				String srcbankCode = "";
				// String momoNetwork = "";
				// String accountToCredit = account.split("==")[4].replace(",", "");

				if (account.contains("==")) {
					account = account.split("==")[4].replace(",", "");
				}

				try {
					if (targetbankCode.contains("-")) {
						if (targetbankCode.split("-").length == 2) {
							srcbankCode = targetbankCode.split("-")[0];
							targetbankCode = targetbankCode.split("-")[1];
						}
					} else {
						srcbankCode = getMMNOCode(provider);
						targetbankCode = senderBankCode;
					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					l.error(ex.getMessage());
				}

				if (vastype.equalsIgnoreCase("self")) {

					elevy = fetchFees(Double.parseDouble(amount), srcbankCode, mn, mn, targetbankCode, account,
							transType).split("[|]");
					if (elevy[0].equals("00") || elevy[0].equals("01")) {
						msg = String.format("Transfer GHS%s to %s", amount, account);
						ref = elevy[2];
						msg += " Elevy: " + elevy[1];
						if (elevy[0].equals("01")) {
							msg += "~Overcharged elevy fees will be reversed";
						}
					} else {
						f.put("TMSG", "Sorry, something went wrong. Please try again.~~");
						f.put("1|Exit", "Exit");
						return f;
					}
				} else {
					elevy = fetchFees(Double.parseDouble(amount), srcbankCode, mn, mn, targetbankCode, account,
							transType).split("[|]");
					if (elevy[0].equals("00") || elevy[0].equals("01")) {
						msg = String.format("Transfer GHS%s to %s", amount, account);
						msg += " Elevy: " + elevy[1];
						ref = elevy[2];
						if (elevy[0].equals("01")) {
							msg += "~Overcharged elevy fees will be reversed";
						}
					} else {
						f.put("TMSG", "Sorry, something went wrong. Please try again.~~");
						f.put("1|Exit", "Exit");
						return f;
					}
				}
			}
			/*
			 * else if (transType.equals("T")) {
			 * 
			 * } else if (transType.equals("GHQRE")) {
			 * 
			 * }
			 */

			l.info("json received:: " + jsonData);

			f.put("TMSG", msg);
			f.put(String.format("1|%s|Yes", ref), "Confirm");
			f.put("2|No", "Cancel");
		} catch (Exception e) {
			l.error(e, e);
		}

		return f;
	}

	private String fetchFees(double amount, String senderBankCode, String senderAccountNumber, String senderPhoneNumber,
			String receiverBankCode, String receiverAccountNumber, String transType) {
		String returnString = "";

		String url = PropsCache.getInstance().getProperty("ELEVY_BASE_URL");

		JSONObject req = new JSONObject();
		req.put("transferAmount", amount);
		req.put("senderBankCode", senderBankCode);
		req.put("senderAccountNumber", senderAccountNumber);
		req.put("senderPhoneNumber", senderPhoneNumber);
		req.put("receiverBankCode", receiverBankCode);
		req.put("receiverAccountNumber", receiverAccountNumber);
		req.put("service", transType);
		req.put("serviceProvider", "");
		req.put("vasAccount", "");
		req.put("channel", "02");
		// req.put("clientTransactionID", reference);

		l.info("elevy request:: " + req.toString());

		String resp = DoHttpRequest.sendJSONPost(url, req.toString());
		l.info("elevy response:: " + resp);

		JSONObject res = new JSONObject(resp);
		String error = res.getString("error");
		System.out.println(error);

		if (error.equals("00")) {
			double taxableAmount = res.getDouble("taxableAmount");
			double elevy = res.getDouble("elevy");
			String elevyId = res.getString("elevyID");
			String clientTransactionId = res.getString("clientTransactionID");
			returnString = String.format("%s|%s|%s", error, elevy, clientTransactionId);
		} else if (error.equals("01")) {
			double elevy = res.getDouble("elevy");
			String clientTransactionId = res.getString("clientTransactionID");
			String message = res.getString("message");
			returnString = String.format("%s|%s|%s|%s", error, elevy, clientTransactionId, message);
		}

		return returnString;
	}

	public static double getFee(String amount, String feeCategory) {
		double fee = 0.0;
		String ipAddress = AUTOSWITCH_IP;
		String key = "123456";
		String expiryDate = "0000";
		int port = AUTOSWITCH_PORT;

		XProcessor processor = new XProcessor();

		HttpHost host = new HttpHost();
		host.setServerAddress(ipAddress);
		host.setPort(port);
		host.setSecureKey(key);

		Card card = new Card();
		card.setCardNumber("0069900806246954");
		card.setCardExpiration(expiryDate);
		card.setCardPin("0000");

		XRequest request = new XRequest();
		request.setCard(card);
		request.setTransCode("K");
		request.setFeeCategory(feeCategory);
		request.setTransAmount(Double.parseDouble(amount));

		XResponse response = null;
		try {
			response = processor.process(host, request);
			fee = Double.parseDouble(String.format("%.2f", response.getBalance()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fee;
	}

	public static HttpHost getHttpHost() {
		HttpHost httpHost = null;
		try {
			final String hostIp = AUTOSWITCH_IP;
			final int port = AUTOSWITCH_PORT;
			httpHost = new HttpHost();
			httpHost.setServerAddress(hostIp);
			httpHost.setPort(port);
			final String key = "123456";
			httpHost.setSecureKey(key);
		} catch (Exception ex) {
			l.info((Object) ("could not connected " + ex.getMessage()));
			l.error((Object) "getHttpHost()", (Throwable) ex);
		}
		return httpHost;
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

	public static void main(String[] args) {
		// String elevy = new Confirm().fetchFees("02SP-1234-OP00-1965", 1.0, "006",
		// "0241624178931", "233548933270",
		// "006", "0153014478843501", "TAM");
		//
		// System.out.println(elevy);

		String json = "{\"AMOUNT\":\"2\",\"CONFIRM\":\"NOUSERACTION.NETWORKTERMINATED.END\",\"MSISDN\":\"233548933270***104953931\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-AR-3697606-C97A-329\",\"SHORTCODE\":\"*389*389*710\",\"TRANS_TYPE\":\"TAMMC\",\"VASTYPE\":\"SELF\",\"WHOAREU\":\"29154\\u003d\\u003dE-TRANZACT USSD TEST\\u003d\\u003d0050012AE6E646668EF20A\\u003d\\u003d203*****96201\\u003d\\u003d2035079796201\"}";
		System.out.println(new Confirm().processIntermediateAction(json));
	}

}
