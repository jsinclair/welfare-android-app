package za.co.aws.welfare.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class NetworkUtils {

    /**
     * Create a URL with the given parameters.
     * @param startingURL The starting URL to which the parameters will be added. The parameter values
     *                    will be encoded to be URL safe.
     * @param params The parameters to add to the URL.
     * @return The URL with URL safe parameters added.
     */
    public static String createURL (String startingURL, Map<String, String> params) {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(startingURL);

        if (params.size() > 0) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet())
            {
                if (!first) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(entry.getKey()).append("=").append(encode(entry.getValue(), "utf-8"));
                first = false;
            }
        }
        return urlBuilder.toString();
    }

    /**
     * Encode the given string to be URL safe.
     * @param toEncode The string to encode.
     * @param format The format to use.
     * @return The encoded string, or the given string if an error occurred.
     */
    private static String encode(String toEncode, String format) {
        try {
            return URLEncoder.encode(toEncode, format);
        } catch (UnsupportedEncodingException e) {
            return toEncode;
        }
    }

}
