package com.treasure_data.td_logger.android;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class DefaultApiClient implements ApiClient {
    private static final String TAG = DefaultApiClient.class.getSimpleName();
    private String apikey;
    private String host;
    private int port;

    public static class ApiError extends Exception {
        private static final long serialVersionUID = 1L;

        public ApiError(String message) {
            super(message);
        }
    }

    /* (non-Javadoc)
     * @see com.treasure_data.td_logger.android.ApiClient#init(java.lang.String, java.lang.String, int)
     */
    @Override
    public void init(String apikey, String host, int port) {
        this.apikey = apikey;
        this.host = host;
        this.port = port;
    }

    private void setupClient(HttpURLConnection conn) {
        conn.setRequestProperty("Authorization", "TD1 " + apikey);
        conn.setDoOutput(true);
        conn.setUseCaches (false);
    }

    /* (non-Javadoc)
     * @see com.treasure_data.td_logger.android.ApiClient#createTable(java.lang.String, java.lang.String)
     */
    @Override
    public String createTable(String database, String table) throws IOException, ApiError {
        HttpURLConnection conn = null;
        try {
            String path = String.format("/v3/table/create/%s/%s/log", database, table);
            URL url = new URL("http", host, port, path);
            Log.d(TAG, "createTable: url=" + url);
            conn = (HttpURLConnection) url.openConnection();
            setupClient(conn);

            conn.setRequestMethod("POST");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ApiError("status code = " + responseCode + " " + conn.getResponseMessage());
            }
            return IOUtils.toString(conn.getInputStream());
        }
        finally {
            IOUtils.closeQuietly(conn.getInputStream());
        }
    }

    /* (non-Javadoc)
     * @see com.treasure_data.td_logger.android.ApiClient#importTable(java.lang.String, java.lang.String, byte[])
     */
    @Override
    public String importTable(String database, String table, byte [] data) throws IOException, ApiError {
        HttpURLConnection conn = null;
        try {
            String path = String.format("/v3/table/import/%s/%s/msgpack.gz", database, table);
            URL url = new URL("http", host, port, path);
            Log.d(TAG, "importTable: url=" + url + ", data.len=" + data.length);

            conn = (HttpURLConnection) url.openConnection();
            setupClient(conn);

            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));

            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream(conn.getOutputStream());
                out.write(data);
                out.flush();
            }
            finally {
                IOUtils.closeQuietly(out);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ApiError(conn.getResponseMessage());
            }
            return IOUtils.toString(conn.getInputStream());
        }
        finally {
            IOUtils.closeQuietly(conn.getInputStream());
        }
    }
}