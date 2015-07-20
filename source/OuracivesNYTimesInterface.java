/**
*
* @author: Tobias Pfandzelter
* @version: 0.1
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

import org.json.*;

public class OuracivesNYTimesInterface
{
    //logger to write logs to the log file
    private OuracivesLogger ouracivesLogger;
    //API key to be used when making calls to api.nytimes.com
    private String apiKey;
    
    /**
     *
     * Parses a given String in JSON format for the first appearance of a given field and returns this fields value
     *
     * @param   input   input in JSON format
     * @param   field   field to be searched for
     * @return          value of the first appearance of field "field"
     *
     */
    private String parseFor(String input, String field)
    {
        ouracivesLogger.log("Looking for " + field);
        
        try
        {
            JSONObject json = new JSONObject(input);
            
            JSONObject response = (JSONObject) json.get("response");
            JSONArray docs = (JSONArray) response.get("docs");
            if(docs.length() == 0) return "";
            JSONObject firstArticle = (JSONObject) docs.get(0);
            ouracivesLogger.log("Found: " + firstArticle.get(field).toString());
            return firstArticle.get(field).toString();
            
        }   catch(Exception e)
            {
                ouracivesLogger.log(e.toString());
                return "";
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
    public OuracivesNYTimesWord getFoundWord(String article)
    {
        //get the article and parse it to a readable thing
        String out = callUrl("http://api.nytimes.com/svc/search/v2/articlesearch.json?callback=svc_search_v2_articlesearch&fq=web_url%3A" + "\"" + article + "\"" + "&sort=newest&fl=headline&api-key=");
        
        //parse the output for the headline
        String headline = parseFor(out, "headline");
        
        //create a set of words used in this article, all lower case
        String[] words = headline.split(" ");
        
        HashSet<String> wordSet = new HashSet<String>();
        
        for(int i = 0; i < words.length; i++)
        {
            words[i] = words[i].replaceAll("[^a-zA-Z]", "");
            words[i] = words[i].toLowerCase();
            wordSet.add(words[i]);
        }
        
        //get the current timestamp so we don't use the current article and convert it to url format
        GregorianCalendar cal = getTimestamp(article);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        dateFormat.setTimeZone(cal.getTimeZone());
        String timestamp = dateFormat.format(cal.getTime());
        timestamp = timestamp.replaceAll(" ", "");
        
        //for each word in this set, we will create a OuracivesNYTimesWord and put it in a new list
        LinkedList<OuracivesNYTimesWord> nyTimesWords = new LinkedList<OuracivesNYTimesWord>();
        
        for(String word : wordSet)
        {
            String informationAboutWord = callUrl("http://api.nytimes.com/svc/search/v2/articlesearch.json?callback=svc_search_v2_articlesearch&q=" + word + "&end_date=" + timestamp + "&sort=newest&fl=web_url&api-key=");
            
            String lastArticle = parseFor(informationAboutWord, "web_url");
            nyTimesWords.add(new OuracivesNYTimesWord(word, getTimestamp(lastArticle), cal, lastArticle, article));
        }
        
        //of all our OuracivesNYTimesWords we will find the least recently used one
        OuracivesNYTimesWord leastRecentlyUsedWord = nyTimesWords.get(0);
        
        for(OuracivesNYTimesWord nytw : nyTimesWords)
        {
            if(nytw.getLastMention().before(leastRecentlyUsedWord.getLastMention())) leastRecentlyUsedWord = nytw;
        }
        
        return leastRecentlyUsedWord;
    }
    
    
    /**
     *
     * Finds the newest available article in the NYTimes.
     *
     * @return  unique URL of the newest article
     *
     */
    public String getCurrentArticle()
    {
        String out = callUrl("http://api.nytimes.com/svc/search/v2/articlesearch.json?callback=svc_search_v2_articlesearch&sort=newest&fl=web_url&api-key=");
        String url = parseFor(out, "web_url");
        return url;
    }
    
    
    /**
     *
     * Finds the timestamp of a given article
     *
     * @param   article the URL of a NYTimes article
     * @return          the timestamp of the article as GregorianCalendar
     *
     */
    private GregorianCalendar getTimestamp(String article)
    {
        //if there is no article, return the earliest possible timestamp
        if(article.equals(""))
        {
            return new GregorianCalendar(0, 0, 0, 0, 0);
        }
        
        //get the article and parse it to a readable thing
        String out = callUrl("http://api.nytimes.com/svc/search/v2/articlesearch.json?callback=svc_search_v2_articlesearch&fq=web_url%3A" + "\"" + article + "\"" + "&sort=newest&fl=pub_date&api-key=");
        
        String timestamp = parseFor(out, "pub_date");
        
        //this timestamp is in the format "YYYY-MM-DDTHH:MM:SSZ" (the actual clock time is often 0 for some reason, which also means that we may be unable to correctly find headlines that have been published earlier the same day), let's convert it to GregorianCalendar by using substrings
        GregorianCalendar time = new GregorianCalendar(Integer.parseInt(timestamp.substring(0,3)),
                                                       Integer.parseInt(timestamp.substring(5,6)),
                                                       Integer.parseInt(timestamp.substring(8,9)),
                                                       Integer.parseInt(timestamp.substring(11,12)),
                                                       Integer.parseInt(timestamp.substring(14,15)),
                                                       Integer.parseInt(timestamp.substring(17,18)));
        
        return time;
    }
    
    
    /**
     *
     * Sends a GET request to a given URL and returns the outstream. Designed for api.nytimes.com
     *
     * @param   url the url to be called
     * @return      true if there is a new article, false if not
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
            
            ouracivesLogger.log(((Integer) con.getResponseCode()).toString());
            
            StringBuffer response = new StringBuffer();
            
            while((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
            
            //System.wait(10);
            
            return response.toString();
            
        } catch (Exception e)
        {
            ouracivesLogger.log(e.toString());
            return "";
        }
    }

    
    /**
     *
     * Class constructor.
     *
     */
    public OuracivesNYTimesInterface(String apiKey, OuracivesLogger ouracivesLogger)
    {
        this.apiKey = apiKey;
        
        this.ouracivesLogger = ouracivesLogger;
    }
}

