/**
*
* @author: Tobias Pfandzelter
* @version: 0.2
*
*/


import java.text.SimpleDateFormat;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

import org.json.*;

public class OuracivesNYTimesInterface
{
    //logger to write logs to the log file
    private OuracivesLogger ouracivesLogger;
    //API key to be used when making calls to api.nytimes.com
    private String apiKey;
    //Set of words that are blacklisted from the New York Times article search
    private HashSet blacklistedWords;

    /**
     *
     * Removes all blacklisted words (in variable blacklistedWords) from a given HashSet
     *
     * @param   WordSet HashSet of Strings from which blacklisted search terms will be removed
     * @return          new, modified HashSet
     *
     */
    private HashSet removeBlacklistedTerms (HashSet wordSet)
    {
        //remove blacklisted terms from given set and return from HashSet
        wordSet.removeAll(blacklistedWords);
        return wordSet;
    }


    /**
     *
     * Finds the timestamp of a given article and returns it as an instance of GregorianCalendar
     *
     * @param   article the URL of a NYTimes article
     * @return          the timestamp of the article as GregorianCalendar
     *
     */
    private GregorianCalendar getTimestamp(String pubTime)
    {
        //this timestamp is in the format "YYYY-MM-DDTHH:MM:SSZ" (the actual clock time is often 0 for some reason, which also means that we may be unable to correctly find headlines that have been published earlier the same day), let's convert it to GregorianCalendar by using substrings
        GregorianCalendar timestamp = new GregorianCalendar(Integer.parseInt(pubTime.substring(0,3)),
                                                            Integer.parseInt(pubTime.substring(5,6)),
                                                            Integer.parseInt(pubTime.substring(8,9)),
                                                            Integer.parseInt(pubTime.substring(11,12)),
                                                            Integer.parseInt(pubTime.substring(14,15)),
                                                            Integer.parseInt(pubTime.substring(17,18)));

        return timestamp;
    }

    /**
     * parses a given JSON String in the return format of api.nytimes.com to a OuracivesNYTimesArticle
     * @param  json  JSON String to parse
     * @param  index index of the object to use (one JSON String may contain several objects)
     * @return       OuracivesNYTimesArticle with the attributes of the wanted article
     */
    private OuracivesNYTimesArticle parseJSONToArticle(String json, int index)
    {
        ouracivesLogger.log("Class: OuracivesNYTimesInterface Method: parseJSONToArticle Parsing article");

        try
        {
            String headline;
            String webUrl;
            GregorianCalendar pubDate;

            //first, convert the horrible JSON string to a JSONObject of the article we want to use
            JSONObject jsonObj = new JSONObject(json);
            JSONObject response = (JSONObject) jsonObj.get("response");
            JSONArray docs = (JSONArray) response.get("docs");
            if(docs.length() == 0) return null;
            JSONObject article = (JSONObject) docs.get(index);

            //read headline from article
            headline = ((JSONObject) article.get("headline")).get("main").toString();
            ouracivesLogger.log("Found headline: " + headline);

            //read web URL from article
            webUrl = article.get("web_url").toString();
            ouracivesLogger.log("Found web_url: " + webUrl);

            //read publication date from article;
            pubDate = getTimestamp(article.get("pub_date").toString());

            //return found attributes as a new OuracivesNYTimesArticle
            return new OuracivesNYTimesArticle(headline, webUrl, pubDate);

        }   catch(Exception e)
            {
                ouracivesLogger.log("Class: OuracivesNYTimesInterface Method: parseJSONToArticle Error: " + e.toString());
                return null;
            }
    }

    /**
     *
     * Finds the word in the newest article that has been used the least recently in any NYTimes article.
     *
     * @param   article the article to be searched
     * @return          this word
     *
     */
    public OuracivesNYTimesWord getFoundWord(OuracivesNYTimesArticle currentArticle)
    {
        //create a set of words used in this article, all lower case
        String[] words = currentArticle.getHeadline().split(" ");

        HashSet<String> wordSet = new HashSet<String>();

        for(int i = 0; i < words.length; i++)
        {
            words[i] = words[i].replaceAll("[^a-zA-Z]", "");
            words[i] = words[i].toLowerCase();
            wordSet.add(words[i]);
        }

        //we don't care about the words that are blacklisted from the NYTimes search
        wordSet = removeBlacklistedTerms(wordSet);

        //get the current timestamp so we don't use the current article and convert it to url format
        GregorianCalendar cal = currentArticle.getPubDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        dateFormat.setTimeZone(cal.getTimeZone());
        String timestamp = dateFormat.format(cal.getTime());
        timestamp = timestamp.replaceAll(" ", "");

        //for each word in this set, we will create a OuracivesNYTimesWord and put it in a new list
        LinkedList<OuracivesNYTimesWord> nyTimesWords = new LinkedList<OuracivesNYTimesWord>();

        for(String word : wordSet)
        {
            //getting the newest URL will just return the same article we're currently using again, therefore we want the second URL
            OuracivesNYTimesArticle lastArticle = parseJSONToArticle(callUrl("http://api.nytimes.com/svc/search/v2/articlesearch.json?callback=svc_search_v2_articlesearch&q=" + word + "&end_date=" + timestamp + "&sort=newest&api-key="), 1);
            nyTimesWords.add(new OuracivesNYTimesWord(word, lastArticle, currentArticle));
        }

        //of all our OuracivesNYTimesWords we will find the least recently used one
        OuracivesNYTimesWord leastRecentlyUsedWord = nyTimesWords.get(0);

        for(OuracivesNYTimesWord nytw : nyTimesWords)
        {
            if(nytw.getLastArticle().getPubDate().before(leastRecentlyUsedWord.getLastArticle().getPubDate())) leastRecentlyUsedWord = nytw;
        }

        return leastRecentlyUsedWord;
    }


    /**
     *
     * Finds the newest available article in the NYTimes.
     *
     * @return  newest Article as OuracivesNYTimesArticle
     *
     */
    public OuracivesNYTimesArticle getCurrentArticle()
    {
        //Problem is that returning the newest article will return one that is published in the future instead of one that has already been published
        //to solve this problem, we will only search for articles that have been published prior to the current date (which we get by using GregorianCalendar and SimpleDateFormat as it has to be in the format yyyyMMdd)
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(cal.getTimeZone());
        String out = callUrl("http://api.nytimes.com/svc/search/v2/articlesearch.json?callback=svc_search_v2_articlesearch&end_date=" + dateFormat.format(cal.getTime()) + "&sort=newest&api-key=");
        //we want to get the first article, not second, etc.
        return parseJSONToArticle(out, 0);
    }

    /**
     *
     * Sends a GET request to a given URL and returns the outstream. Designed for api.nytimes.com
     *
     * @param   url the url to be called
     * @return      returned JSON string as String object
     *
     */
    private String callUrl(String url)
    {
        url = url + apiKey;
        try
        {
            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;

            ouracivesLogger.log("Class: OuracivesNYTimesInterface Method: callUrl Response: " + ((Integer) con.getResponseCode()).toString());

            StringBuffer response = new StringBuffer();

            while((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }

            return response.toString();

        } catch (Exception e)
        {
            ouracivesLogger.log("Class: OuracivesNYTimesInterface Method: callUrl Error: " + e.toString());
            return "";
        }
    }

    /**
     *
     * Class constructor.
     *
     */
    public OuracivesNYTimesInterface(String apiKey, String blacklistPath, OuracivesLogger ouracivesLogger)
    {
        this.apiKey = apiKey;
        this.ouracivesLogger = ouracivesLogger;

        //load blacklisted search terms
        try {
                    //open file
                    BufferedReader in = new BufferedReader(new FileReader(blacklistPath));
                    //as blacklist.txt has only one line of text, reading the first line only is not a problems
                    String str = in.readLine();
                    //close file
                    in.close();

                    //create a HashSet using the words (seperated by comma) from blacklist.txt
                    this.blacklistedWords = new HashSet<String>(Arrays.asList(str.split(",")));

                } catch (Exception e) {
                    ouracivesLogger.log("Class: OuracivesNYTimesInterface Method: OuracivesNYTimesInterface Error: " +  e.toString());
                }


    }
}
