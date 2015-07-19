/**
*
* @author: Tobias Pfandzelter
* @version: 1.0
* 
*/

import java.util.GregorianCalendar;

public class OuracivesNYTimesWord
{
    //the actual word
    private String word;
    //timestamp of the last time it has been mentioned
    private GregorianCalendar lastMention;
    //timestamp of the current time it is mentioned (currently unused)
    private GregorianCalendar currentMention;
    //url of the article it has last been mentioned in
    private String lastArticle;
    //url of the article it is now mentioned in (currently unused)
    private String currentArticle;
    
    /**
     *
     *  Gives the variable word.
     *
     *  @return word
     *
     */
    public String getWord()
    {
        return word;
    }
    
    /**
     *
     *  Gives the variable lastMention.
     *
     *  @return lastMention
     *
     */
    public GregorianCalendar getLastMention()
    {
        return lastMention;
    }
    
    /**
     *
     *  Gives the variable curretnMention.
     *
     *  @return currentMention
     *
     */
    public GregorianCalendar getCurrentMention()
    {
        return currentMention;
    }
    
    /**
     *
     *  Gives the variable lastArticle.
     *
     *  @return lastArticle
     *
     */
    public String getLastArticle()
    {
        return lastArticle;
    }
    
    /**
     *
     *  Gives the variable currentArticle.
     *
     *  @return word
     *
     */
    public String getCurrentArticle()
    {
        return currentArticle;
    }
    
    /**
     *
     *  Class constructor.
     *
     */
    public OuracivesNYTimesWord(String word, GregorianCalendar lastMention, GregorianCalendar currentMention, String lastArticle, String currentArticle)
    {
        this.word = word;
        this.lastMention = lastMention;
        this.currentMention = currentMention;
        this.lastArticle = lastArticle;
        this.currentArticle = currentArticle;
    }
}

