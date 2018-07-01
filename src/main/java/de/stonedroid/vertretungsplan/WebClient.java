package de.stonedroid.vertretungsplan;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Used to download html content from web sites
 */
class WebClient
{
    // A userAgent is used to identify this program as a "real" browser by adding properties like:
    // - Browser name
    // - OS name
    // - Browser/OS version
    // - Protocol version
    private String userAgent = null;

    /**
     * Downloads and returns web site html code.
     *
     * @param url Website url which should be used to download data from
     * @return A String containing the website's html
     * @throws WebException
     */
    public String downloadString(String url) throws WebException
    {
        try
        {
            // Opens a connection and retrieves the input stream
            URLConnection connection = new URL(url).openConnection();
            // Set userAgent if set by user
            if (userAgent != null)
            {
                connection.setRequestProperty("User-Agent", userAgent);
            }

            InputStream stream = connection.getInputStream();
            // Get converted input stream.
            String html = Utils.inputStreamToString(stream);
            // Close connection
            stream.close();
            return html;
        }
        catch (IOException e)
        {
            throw new WebException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Gets userAgent
     *
     * @return userAgent
     */
    public String getUserAgent()
    {
        return userAgent;
    }

    /**
     * Sets userAgent
     *
     * @param userAgent new userAgent
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }
}
