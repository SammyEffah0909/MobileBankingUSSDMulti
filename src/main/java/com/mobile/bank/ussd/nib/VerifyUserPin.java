package com.mobile.bank.ussd.nib;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.mobile.bank.util.PropsCache;
import com.etz.http.etc.Card;
import com.etz.http.etc.HttpHost;
import com.etz.http.etc.TransCode;
import com.etz.http.etc.XProcessor;
import com.etz.http.etc.XRequest;
import com.etz.http.etc.XResponse;
import com.fnm.ussd.engine.util.UssdActionClassInterface;
import com.google.gson.Gson;
import com.test.gcb.util.DataPipe;

public class VerifyUserPin extends UssdActionClassInterface {
	static Logger l = Logger.getLogger(VerifyUserPin.class);

	private static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiDgqyysIeKwR+xu+NWcQO3vhArH4y33mKnIAiuCg+wni9rACRUugiH/Yb2ZmtIoVVXSVLxjJcNte0aWdYIKMiQ6Cj3+8zLXze+3+d6jBwRMnOs5j82S4CZwSQJJmQ/MLtrL6jgN/bpS5X735y2HJzF8dHkyCKTdc+QnWPDC+nAkNNF9NhNCGa0Ft6hMIcZaYIbv/8Fe9ocSACOoTG2Hhqyc56n2wpA7N8fT0tRYsbZfr4bUzjUSMwS9w8CNcSj7UyLS09kJoXUvX3vwRzTuHAFPM9+ApbTpwSG9hwk0yoNysCtt1PGmK+zOagp0WthdlhhUnnOmx7Jc1OslE1CRP6wIDAQAB";
	private static final String AUTOSWITCH_IP = PropsCache.getInstance().getProperty("AUTOSWITCH_IP");
	private static final String AUTOSWITCH_PORT = PropsCache.getInstance().getProperty("AUTOSWITCH_PORT");

	public TreeMap<String, String> processIntermediateAction(String jsonData) {
		TreeMap<String, String> f = new TreeMap();
		try {
			Gson j = new Gson();
			Map<String, String> m = (Map) j.fromJson(jsonData, Map.class);
			String mn = ((String) m.get("MSISDN")).split(java.util.regex.Pattern.quote("***"))[0];
			// String alias = (String) m.get("WHOAREU").split("==")[0];
			String pin = m.containsKey("PROJECTEDINPUT") ? m.get("PROJECTEDINPUT") : "";
			String alias = m.get("LOGIN").split("\\|")[1];

			l.info("json received:: " + jsonData);

			String accountNum = getSelfAccountNo(mn, alias);

			boolean pinVerified = verifyPin(accountNum, pin);

			if (pinVerified) {
				l.info("okay can you give me my transactions flow now?");
				f.put("_FIDJ_2", "");
			} else {
				f.put("TMSG", "Profile verification failed. Please try again later");
				f.put("", "");
			}

		} catch (Exception e) {
			l.error(e, e);
		}
		return f;
	}

	public static void main(String[] args) {
		String s = "{\"LOGIN\":\"A|NIB-140-----29501\",\"MSISDN\":\"233542023469***104643109\",\"PROJECTEDINPUT\":\"1111\",\"PROVIDER\":\"USSDBRIDGE\",\"REFERENCE\":\"ussdx-VN-4983480-A83I-208\",\"SHORTCODE\":\"*389*389*710\",\"WHOAREU\":\"NIB-140-----29501\\u003d\\u003dDENNIS AKOMEAH\\u003d\\u003d005001058D91E68A7E7202\"}";

		System.out.println(new VerifyUserPin().processIntermediateAction(s));
	}

	public static String getEncryptedPin(String pin) {
		String encryptedPin = null;

		try {
			encryptedPin = DatatypeConverter.printBase64Binary(RSAUtil.encrypt(pin, publicKey));
			System.out.println(encryptedPin);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return encryptedPin;
	}

	public static String getSelfAccountNo(String mobile, String alias) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		String accts = "";

		String selectSQL = "select device_name, substring_index(device_name, '-', -1) account_number from mobiledb.m_mobile_devices where dest_mobile = ? and device_name like ? limit 1";
		try {
			DataSource ds = DataPipe.setupDataSource("ABII");
			dbConnection = ds.getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, mobile);
			preparedStatement.setString(2, "NIB%");

			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				accts += rs.getString("account_number");
			}
			l.info("account_number : " + accts);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
			if (rs != null) {
				rs.close();
			}
		}

		return accts;
	}

	public static boolean verifyPin(String account, String pin) {
		boolean success = false;
		String returnValue = "";
		String ipAddress = AUTOSWITCH_IP;
		String key = "123456";
		String expiryDate = "022020";
		int port = Integer.parseInt(AUTOSWITCH_PORT);
		XProcessor processor = new XProcessor();

		HttpHost host = new HttpHost();
		host.setServerAddress(ipAddress);
		host.setPort(port);
		host.setSecureKey(key);

		Card card = new Card();
		card.setCardNumber(String.format("005%s", account));
		card.setCardExpiration(expiryDate);

		XRequest request = new XRequest();
		request.setXmlString(String.format("<CBARequest>PAUTH:%s</CBARequest>", getEncryptedPin(pin)));
		request.setCard(card);
		request.setTransCode(TransCode.BANKSERVICE);

		XResponse response = null;

		try {
			response = processor.process(host, request);

			System.out.println("Response: " + response.getResponse());
			System.out.println("Message: " + response.getMessage());
			System.out.println("Custom XML: " + response.getCustomXml());

			l.info("Response: " + response.getResponse());
			l.info("Message: " + response.getMessage());
			l.info("Custom XML: " + response.getCustomXml());

			if (response.getResponse() == 0) {
				returnValue = response.getCustomXml();
				if (returnValue.equalsIgnoreCase("true"))
					success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}

}
