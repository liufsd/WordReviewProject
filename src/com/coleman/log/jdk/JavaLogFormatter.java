package com.coleman.log.jdk;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JavaLogFormatter extends Formatter
{
    
    Date dat = new Date();
    
    private SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss:SSS");
    
    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    private String lineSeparator = ":";
    
    /**
     * Format the given LogRecord.
     * 
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record)
    {
        StringBuffer sb = new StringBuffer();
        // Minimize memory allocations here.
        dat.setTime(record.getMillis());
        String time = format.format(dat);
        
        sb.append(time);
        sb.append(" ");
        if (record.getSourceClassName() != null)
        {
            sb.append(record.getSourceClassName());
        }
        else
        {
            sb.append(record.getLoggerName());
        }
        if (record.getSourceMethodName() != null
                && !record.getSourceMethodName().equals(""))
        {
            sb.append(" ");
            sb.append(record.getSourceMethodName());
        }
        sb.append("(" + record.getLevel().getLocalizedName() + ")");
        sb.append(lineSeparator);
        String message = formatMessage(record);
        sb.append(message);
        if (record.getThrown() != null)
        {
            try
            {
                sb.append(lineSeparator);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ex)
            {
            }
        }
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
