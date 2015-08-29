/**
*
* @author: Tobias Pfandzelter
* @version: 0.2
*
*/

import java.net.URLEncoder;

import java.util.GregorianCalendar;

import java.text.SimpleDateFormat;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;


public class OuracivesTwitterInterface
{
    //API keys to access twitter API
    private String consumerKeyStr;
    private String consumerSecretStr;
    private String accessTokenStr;
    private String accessTokenSecretStr;
    //logger to write logs to log file
    private OuracivesLogger ouracivesLogger;

    /**
     *
     * Makes a Twitter API call to a given url.
     * Blatantly copied from http://javapapers.com/core-java/post-to-twitter-using-java/
     *
     * @param   url     URL that will be called
     *
     */
    private void callUrl(String url)
    {
        ouracivesLogger.log("Class: OuracivesTwitterInterface Method: callUrl Calling: " + url);

        try
        {
            OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr,
                                                                       consumerSecretStr);
            oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);

            HttpPost httpPost = new HttpPost(url);

            oAuthConsumer.sign(httpPost);

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpPost);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            ouracivesLogger.log("Class: OuracivesTwitterInterface Method: callUrl Status code: " + statusCode + ':'
                                + httpResponse.getStatusLine().getReasonPhrase());
            ouracivesLogger.log("Class: OuracivesTwitterInterface Method: callUrl Content: " + IOUtils.toString(httpResponse.getEntity().getContent()));
        } catch (Exception e)
            {
                ouracivesLogger.log(e.toString());
            }

    }

    /**
     *
     * Creates a beautiful tweet from a given OuracivesNYTimesWord
     *
     * @param   word    Word that will be used for the tweet
     * @return          newly created tweet
     *
     */
    private String convertNYTimesWordToTweet(OuracivesNYTimesWord word)
    {
        //this variable will contain our final tweet
        String tweet = "";

        //add the actual word
        tweet += word.getWord();

        tweet += ". ";
        tweet += "Last seen: ";

        //convert the field lastMention to an usable timestamp format
        if(word.getLastArticle() == null)
        {
            tweet += "never";

        } else
            {
                GregorianCalendar cal = word.getLastArticle().getPubDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(cal.getTimeZone());
                String timestamp = dateFormat.format(cal.getTime());

                //add timestamp
                tweet += timestamp;

                tweet += " ";
                tweet += "in: ";

                //add the url of the field lastArticle (hopefully it is shortened in the actual tweet)
                tweet += word.getLastArticle().getWebUrl();
            }

        return tweet;
    }

    /**
     *
     * Converts a String (text) to a format that can be appended to a Twitter API call.
     *
     * @param   tweet   Text that will be converted
     * @return          converted String
     *
     */
    private String convertTweetToUrlFormat(String tweet)
    {
        try
        {
            tweet = URLEncoder.encode(tweet, "UTF-8");
        }   catch(Exception e)
            {
                ouracivesLogger.log("Class: OuracivesTwitterInterface Method: convertTweetToUrlFormat Error: " + e.toString());
            }

        return tweet;
    }


    /**
     *
     * This method tweets a given NYTimesWord to the @ouracives twitter account
     *
     * @param   word    word that will be tweeted in OuracivesNYTimesWord format
     *
     */
    public void tweetWord(OuracivesNYTimesWord word)
    {
        //make a tweet out of the word
        String wordsToTweet = convertNYTimesWordToTweet(word);

        //tweet it
        tweetText(wordsToTweet);
    }

    /**
     *
     * This method tweets a given String to the @ouracives twitter account
     *
     * @param   tweet    text that will be tweeted in OuracivesNYTimesWord format
     *
     */
    private void tweetText(String tweet)
    {
        ouracivesLogger.log("Class: OuracivesTwitterInterface Method: tweetText Tweeting: " + tweet);
        //convert it to UTF-8 so it can be appended to the url
        tweet = convertTweetToUrlFormat(tweet);

        //tweet the tweet!
        callUrl("https://api.twitter.com/1.1/statuses/update.json?status=" + tweet);
    }

    /**
     *
     * Class constructor.
     *
     */
    public OuracivesTwitterInterface(String consumerKeyStr, String consumerSecretStr, String accessTokenStr, String accessTokenSecretStr, OuracivesLogger ouracivesLogger)
    {
        this.consumerKeyStr = consumerKeyStr;
        this.consumerSecretStr = consumerSecretStr;
        this.accessTokenStr = accessTokenStr;
        this.accessTokenSecretStr = accessTokenSecretStr;
        this.ouracivesLogger = ouracivesLogger;
    }
}
