/**
*
* @author: Tobias Pfandzelter
* @version: 0.1
*
*/

import java.util.GregorianCalendar;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;


public class OuracivesLogger
{
    //filename to write the log into
    private String filename;

    /**
     *
     * Logs a given line to the log file. That's it.
     *
     * @param   line    word that will be logged
     *
     */
    public void log(String line)
    {
        //create a timestamp to be inserted in front of every logged line
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        dateFormat.setTimeZone(cal.getTimeZone());
        String timestamp = dateFormat.format(cal.getTime());

        //append the line to the log file
        try
        {
            Writer out = new BufferedWriter(new FileWriter(filename, true));
            out.append(timestamp);
            out.append("\n");
            out.append(line);
            out.append("\n");
            out.append("\n");
            out.close();
        }   catch (Exception e)
            {
                //here it may be a good idea to log this exception... but this could result in an infinite loop, right?
                //for now, let us print it to the terminal
                System.out.println(e);
            }
    }

    /**
     *
     * Class constructor.
     *
     */
	public OuracivesLogger(String filename)
    {

        this.filename = filename + ".txt";
        try
        {
            //create the file (override if need be)
            PrintWriter writer = new PrintWriter(this.filename, "UTF-8");
            writer.close();
        }   catch (Exception e)
            {
                System.out.println(e);
            }

    }
}
