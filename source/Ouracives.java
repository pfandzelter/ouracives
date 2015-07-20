/**
*
* @author: Tobias Pfandzelter
* @version: 0.1
* 
*/

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.GregorianCalendar;

import java.text.SimpleDateFormat;

public class Ouracives
{
    /**
     *
     * Main function that is called on start
     *
     */
    public void start(String apiKeyNYTimes, String consumerKeyStr, String consumerSecretStr, String accessTokenStr, String accessTokenSecretStr)
    {
        //initialize the logger
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(cal.getTimeZone());
        String timestamp = dateFormat.format(cal.getTime());
        
        OuracivesLogger ouracivesLogger = new OuracivesLogger(timestamp + "log");
        
        //initialize Twitter interface with our set of API keys
        OuracivesTwitterInterface ouracivesTwitterInterface = new OuracivesTwitterInterface(consumerKeyStr, consumerSecretStr, accessTokenStr, accessTokenSecretStr, ouracivesLogger);
        
        //initialize NYTimes interface with out API key
        OuracivesNYTimesInterface ouracivesNYTimesInterface = new OuracivesNYTimesInterface(apiKeyNYTimes, ouracivesLogger);
        
        //at first, get the newest article the NYTimes has to offer
        String newestArticle = ouracivesNYTimesInterface.getCurrentArticle();
        
        //look if there is a new article
        //if there is none, skip to the wait process; otherwise execute algorithm
        while(true)
        {
            if(!ouracivesNYTimesInterface.getCurrentArticle().equals(newestArticle))
            {
                newestArticle = ouracivesNYTimesInterface.getCurrentArticle();
                //find the word in the new article that has least recently been used
                OuracivesNYTimesWord foundWord = ouracivesNYTimesInterface.getFoundWord(newestArticle);
                //tweet out the found word
                ouracivesTwitterInterface.tweetWord(foundWord);
            }
            //wait 10 minutes before calling the API again
            try
            {
                Thread.sleep(600000);
            }   catch(Exception e)
                {
                    ouracivesLogger.log(e.toString());
                }
        }
    }
    
    /**
     *
     * Main method that is called with "java Ouracives". Creates an instance of Ouracives and starts the application.
     *
     * @param   args    command line arguments (ignored)
     *
     */
    public static void main(String[] args)
    {
        //reads the config.properties file and writes the appropiate API keys
        //http://www.mkyong.com/java/java-properties-file-examples/
        Properties prop = new Properties();
        InputStream input = null;
        
        String apiKeyNYTimes = "";
        String consumerKeyStr = "";
        String consumerSecretStr = "";
        String accessTokenStr = "";
        String accessTokenSecretStr = "";
        
        try {
            
            input = new FileInputStream("config.properties");
            
            // load a properties file
            prop.load(input);
            
            apiKeyNYTimes = prop.getProperty("apiKeyNYTimes");
            consumerKeyStr = prop.getProperty("consumerKeyStr");
            consumerSecretStr = prop.getProperty("consumerSecretStr");
            accessTokenStr = prop.getProperty("accessTokenStr");
            accessTokenSecretStr = prop.getProperty("accessTokenSecretStr");
            
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //create an instance of Ouracives
        Ouracives ou = new Ouracives();
        //start the algorithm with the set of API keys
        ou.start(apiKeyNYTimes, consumerKeyStr, consumerSecretStr, accessTokenStr, accessTokenSecretStr);
    }

}
