package com.banistmo.examples;

import static com.banistmo.utils.GmailAPI.getGmailService;
import static com.banistmo.utils.GmailAPI.getMailByBody;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.model.Message;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ConsultarCorreoGmail {

  public static void main(String[] args) throws IOException, GeneralSecurityException {

    String emailBody;
    String emailDate;
    Message message;

    System.setProperty(
        "refresh.token",
        "1//05jfntVIsswlLCgYIARAAGAUSNwF-L9Ir-T86uHWdkqqipkcOEFX7_WAvKU6Xt-miwFOVj_TVu5Lawy8ASJ7IJ3DPESCdF8Bxf6g");
    System.setProperty(
        "client.id", "1010124264903-2ak7asm0ca838q9v1eh0t050hf0jgt09.apps.googleusercontent.com");
    System.setProperty("client.secret", "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");

    getGmailService();

    // Ejemplo de implementación a partir de obtener el mensaje
    // Con el método getMailByBody se lee la bandeja de entrada de los correos indicados y se procede
    message = getMailByBody("Recuperar Contraseña");

    emailDate = message.getPayload().getHeaders().get(1).getValue();
    if (message.getPayload().getParts().get(0).getBody().getData() != null) {
      emailBody =
          StringUtils.newStringUtf8(
              Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));
    } else if (message.getPayload().getParts().get(0).getParts().get(0).getBody().getData()
        != null) {
      emailBody =
          StringUtils.newStringUtf8(
              Base64.decodeBase64(
                  message.getPayload().getParts().get(0).getParts().get(0).getBody().getData()));
    } else {
      emailBody =
          StringUtils.newStringUtf8(Base64.decodeBase64(message.getPayload().getBody().getData()));
    }

    System.out.println("Email Date: " + emailDate);
    System.out.println("Email body: \n" + emailBody);
  }
}

