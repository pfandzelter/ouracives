/**
 *
 * @author Tobias Pfandzelter
 * @version 0.2
 *
 */

import java.util.GregorianCalendar;

public class OuracivesNYTimesArticle
{
    //contains headline of the article
    private String headline;
    //contains web URL of the article
    private String webUrl;
    //contains publication date of the article as GregorianCalendar (easier formatting and calculating)
    private GregorianCalendar pubDate;

    /**
     * returns headline of the article
     * @return String headline of the article
     */
    public String getHeadline()
    {
        return headline;
    }

    /**
     * returns web URL of the article
     * @return String web URL
     */
    public String getWebUrl()
    {
        return webUrl;
    }

    /**
     * returns publication date of the article
     * @return GregorianCalendar publication date
     */
    public GregorianCalendar getPubDate()
    {
        return pubDate;
    }

    /**
     * Class constructor
     * @param   String  headline of the article
     * @param   String  web URL of the article
     * @param   GregorianCalendar publication date of the article
     * @return  void
     */
    public OuracivesNYTimesArticle(String headline, String webUrl, GregorianCalendar pubDate)
    {
        this.headline = headline;
        this.webUrl = webUrl;
        this.pubDate = pubDate;
    }
}
