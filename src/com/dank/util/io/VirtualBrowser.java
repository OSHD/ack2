package com.dank.util.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import com.dank.util.ProgressCallback;

public class VirtualBrowser {

    public byte[] getRaw(URL url, ProgressCallback callback) {
        HttpURLConnection connection = createConnection(url, "GET");
        try {
            return download(connection, callback);
        } catch (IOException e) {
            return null;
        }
    }

    public String get(URL url) {
        return new String(getRaw(url, null));
    }

    public byte[] download(HttpURLConnection connection, ProgressCallback callback) throws IOException {
        int len = connection.getContentLength();
        InputStream is = new BufferedInputStream(connection.getInputStream());

        String compression = connection.getHeaderField("Content-Encoding");
        if (compression != null && compression.toLowerCase().contains("gzip")) {
            is = new GZIPInputStream(is);
        }

        ByteArrayOutputStream bAOut = new ByteArrayOutputStream();
        int c = 0;
        int off = 0;
        try {
            while ((c = is.read()) != -1) {
                bAOut.write(c);
                off++;
                if (callback != null) {
                    callback.update(off, len);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bAOut.toByteArray();
    }

    private HttpURLConnection createConnection(URL url, String requestMethod) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection
                    .setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3");
            connection.setRequestProperty("Accept", "");
            connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
            connection.addRequestProperty("Accept-Charset",
                    "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            connection.addRequestProperty("Keep-Alive", "300");
            connection.addRequestProperty("Connection", "keep-alive");
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] postRaw(URL url, String urlParameters) {
        HttpURLConnection connection = null;
        try {
            // Create connection
            connection = createConnection(url, "POST");
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            return download(connection, null);

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String post(URL url, String urlParameters, boolean encryption) {
        return new String(postRaw(url, urlParameters));
    }
}
