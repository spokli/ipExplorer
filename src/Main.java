import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) {
		try {
			String[] ownIp = InetAddress.getLocalHost().getHostAddress().split("\\.");
			String base = ownIp[0] + "." + ownIp[1] + "." + ownIp[2] + ".";

			searchForFritzBox(base);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		

	}

	public static ArrayList<String> searchForFritzBox(String base) {
//		String[] allHtml = checkAllIps(base, true);
		ArrayList<String> results = new ArrayList<String>();

		for (int i = 0; i < 255; i++) {
			String ip = base + i;
			String html = checkIp(ip, "", true);
			System.out.println("Checking " + ip + "...");
			
			if (html.contains("FRITZ")) {
				String foundIp = html.split(":")[0];
				results.add(foundIp);
				System.out.println("FRITZ!Box found on " + foundIp);
			};
		}
		
		// Gebe Ergebnis aus
		System.out.println("--------------------------------------------");
		System.out.println("------------------RESULTS-------------------");
		for(String result : results){
			System.out.println(result);
		}
		
		return results;
	}

	public static String[] checkAllIps(String base, boolean verbose) {
		String[] result = new String[254];

		for (int i = 0; i < 255; i++) {
			String ip = base + i;
			result[i] = checkIp(ip, "", verbose);
//			System.out.println(ip + ": " + result[i]);
		}
		return result;
	}

	public static String checkIp(String targetURL, String urlParameters, boolean verbose) {
		try {
			String result = executeGet("http://" + targetURL, urlParameters);

			if (verbose) {
				return targetURL + ": " + result;
			} else {
				return targetURL + ": " + "exists";
			}
		} catch (SocketTimeoutException e) {
			return targetURL + ": " + "E: timeout";
		} catch (ConnectException e) {
			return targetURL + ": " + "E: connection refused";
		} catch (MalformedURLException e){
			return targetURL + ": " + "E: malformed url";
		} catch (ProtocolException e){
			return targetURL + ": " + "E: protocol error";
		} catch (IOException e){
			return targetURL + ": " + "E: IO error";
		} catch (Exception e){
			return targetURL + ": " + "E: unknown error";
		}

	}

	public static String executeGet(String targetURL, String urlParameters)
			throws SocketTimeoutException, ConnectException, MalformedURLException, ProtocolException, IOException {
		HttpURLConnection connection = null;
		
		// Create connection
		URL url = new URL(targetURL);
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "text/html");

		connection.setConnectTimeout(100); // 0.1 Sekunden Timeout
		connection.setUseCaches(false);
		connection.setDoOutput(true);

		// Send request

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.close();

		// Get Response
		InputStream is = connection.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		StringBuilder response = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		
		String result = response.toString();
		
		if (connection != null) {
			connection.disconnect();
		}
		
		return result;

	}
}
