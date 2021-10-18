package com.banistmo.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages.List;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

public class GmailAPI {

  private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String user = "me";
  private static final String REFRESH_TOKEN = System.getProperty("refresh.token");
  private static final String CLIENT_ID = System.getProperty("client.id");
  private static final String CLIENT_SECRET = System.getProperty("client.secret");
  private static final String CREDENTIALS_PATH = "/credentials.json";
  static GoogleClientSecrets clientSecrets = null;
  static Gmail service = null;

  public static Message getMailByBody(String searchString) throws IOException {

    String messageId;
    Message message = null;
    List request = service.users().messages().list(user).setQ(searchString);
    ListMessagesResponse messagesResponse = request.execute();
    request.setPageToken(messagesResponse.getNextPageToken());

    if (messagesResponse.getMessages() != null) {
      messageId = messagesResponse.getMessages().get(0).getId();
      message = service.users().messages().get(user, messageId).execute();
    }
    return message;
  }

  public static Gmail getGmailService() throws IOException, GeneralSecurityException {

    InputStream in = GmailAPI.class.getResourceAsStream(CREDENTIALS_PATH);
    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    clientSecrets.getDetails().setClientId(CLIENT_ID);
    clientSecrets.getDetails().setClientSecret(CLIENT_SECRET);

    Credential authorize =
        new GoogleCredential.Builder()
            .setTransport(GoogleNetHttpTransport.newTrustedTransport())
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret())
            .build()
            .setAccessToken(getAccessToken())
            .setRefreshToken(REFRESH_TOKEN);

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    service =
        new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
            .setApplicationName(GmailAPI.APPLICATION_NAME)
            .build();

    return service;
  }

  private static String getAccessToken() {

    try {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("grant_type", "refresh_token");
      params.put("client_id", clientSecrets.getDetails().getClientId());
      params.put("client_secret", clientSecrets.getDetails().getClientSecret());
      params.put("refresh_token", REFRESH_TOKEN);

      StringBuilder postData = new StringBuilder();
      for (Map.Entry<String, Object> param : params.entrySet()) {
        if (postData.length() != 0) {
          postData.append('&');
        }
        postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
        postData.append('=');
        postData.append(
            URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8));
      }
      byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

      URL url = new URL("https://accounts.google.com/o/oauth2/token");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setUseCaches(false);
      con.setRequestMethod("POST");
      con.getOutputStream().write(postDataBytes);

      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      StringBuilder buffer = new StringBuilder();
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        buffer.append(line);
      }

      JSONObject json = new JSONObject(buffer.toString());
      return json.getString("access_token");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}
