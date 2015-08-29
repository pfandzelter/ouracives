/**
*
* @author: Tobias Pfandzelter (@pfandzelter)
* @version: 0.3
*
*/

import java.util.GregorianCalendar;

public class OuracivesNYTimesWord
{
    //the actual word
    private String word;
    //article it has last been mentioned in
    private OuracivesNYTimesArticle lastArticle;
    //article it is now mentioned in
    private OuracivesNYTimesArticle currentArticle;

    /**
     * returns the actual word as a String
     * @return word
     */
    public String getWord()
    {
        return word;
    }

    /**
     * returns article the word has last been mentioned in as OuracivesNYTimesArticle
     * @return last article the word has been mentioned in
     */
    public OuracivesNYTimesArticle getLastArticle()
    {
        return lastArticle;
    }

    /**
     * returns article the word is currently mentioned in as OuracivesNYTimesArticle
     * @return current article
     */
    public OuracivesNYTimesArticle getCurrentArticle()
    {
        return currentArticle;
    }

    /**
     *
     *  Class constructor.
     *
     */
    public OuracivesNYTimesWord(String word, OuracivesNYTimesArticle lastArticle, OuracivesNYTimesArticle currentArticle)
    {
        this.word = word;
        this.lastArticle = lastArticle;
        this.currentArticle = currentArticle;
    }
}
