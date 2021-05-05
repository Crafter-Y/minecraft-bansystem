package de.craftery.bansystem;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class offlineUUID {
    public static UUID getUUID (String PlayerName) {
        HttpURLConnection connection;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        UUID uuid = null;
        JSONParser jsonParser = new JSONParser();
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + PlayerName);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();

                Object obj = jsonParser.parse(responseContent.toString());
                JSONObject obj2 = (JSONObject)obj;
                String unformattedUUID = obj2.get("id").toString();

                uuid = UUID.fromString(unformattedUUID.substring(0, 8) + "-" + unformattedUUID.substring(8, 12) + "-"
                        + unformattedUUID.substring(12, 16) + "-" + unformattedUUID.substring(16, 20) + "-" + unformattedUUID.substring(20, 32));

            }
            return uuid;
        } catch (MalformedURLException e) {

        } catch (IOException e) {

        } catch (Exception e) {

        }
        return uuid;
    }
}
