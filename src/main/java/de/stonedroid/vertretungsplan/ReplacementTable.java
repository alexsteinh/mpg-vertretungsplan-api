package de.stonedroid.vertretungsplan;

import java.io.Serializable;
import java.util.*;

/**
 * Holds Messages and Replacements for the chosen grade
 */
public class ReplacementTable implements Serializable
{
    // URL used to scrape off replacements and messages
    private static final String DOWNLOAD_URL = "http://mpg-vertretungsplan.de/w/%s/w000%s.htm";

    // Contain their generic's collection
    private ArrayList<Replacement> replacements;
    private ArrayList<Message> messages;

    // Bonus data
    private Grade grade;
    // A school week has 5 days
    private String[] dates;
    private String[] days;

    private Calendar downloadDate;

    // Intern "constructor" for junit testing
    static ReplacementTable parseFromHtml(String html)
    {
        try
        {
            Object[] results = parseHtml(html, null);
            return new ReplacementTable(results, null, null);
        }
        catch (WebException e)
        {
            return null;
        }
    }

    /**
     * Downloads the ReplacementTable for the chosen grade for the current week asynchronously.
     * After the download is finished, the table is passed to the listener and is ready to be used.
     *
     * @param grade The grade decides which table is going to be downloaded
     * @param listener Listener which notifies user when download is complete
     */
    public static void downloadTableAsync(Grade grade, OnDownloadFinishedListener listener)
    {
        downloadTableAsync(grade, 0, listener);
    }

    /**
     * Downloads the ReplacementTable (with week offset) for the chosen grade asynchronously.
     * After the download is finished, the table is passed to the listener and is ready to be used.
     *
     * @param grade The grade decides which table is going to be downloaded
     * @param plusWeeks Week offset (default is 0)
     * @param listener Listener which notifies user when download is complete
     */
    public static void downloadTableAsync(Grade grade, int plusWeeks, OnDownloadFinishedListener listener)
    {
        new Thread(() ->
        {
            try
            {
                // Download replacement table and pass it to the listener
                ReplacementTable table = downloadTable(grade, plusWeeks);
                listener.onFinished(table);
            }
            catch (WebException e)
            {
                listener.onFailed("Couldn't download replacement table");
            }
        }).start();
    }

    /**
     * Downloads the ReplacementTable for the chosen grade for the current week.
     *
     * @param grade The grade decides which table is going to be downloaded
     * @return ReplacementTable with information for the grade
     * @throws WebException Failed to download ReplacementTable
     */
    public static ReplacementTable downloadTable(Grade grade) throws WebException
    {
        // Download the ReplacementTable with a default value of 0 for plusWeeks
        return downloadTable(grade, 0);
    }

    /**
     * Downloads the ReplacementTable (with week offset) for the chosen grade.
     *
     * @param grade The grade decides which table is going to be downloaded
     * @param plusWeeks Week offset (default is 0)
     * @return ReplacementTable with information for the grade
     * @throws WebException Failed to download ReplacementTable
     */
    public static ReplacementTable downloadTable(Grade grade, int plusWeeks) throws WebException
    {
        String html = downloadHtml(grade, plusWeeks);
        Object[] result = parseHtml(html, grade);
        return new ReplacementTable(result, grade, Calendar.getInstance());
    }

    // Downloads html based on parameters
    private static String downloadHtml(Grade grade, int plusWeeks) throws WebException
    {
        WebClient client = new WebClient();
        // Preparing arguments to fill the '%s's in DOWNLOAD_URL
        // Get week from calendar
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, plusWeeks);
        String week = String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
        // If week has length = 1 -> prefix a '0' otherwise our WebClient will return 404
        if (week.length() == 1)
        {
            week = "0" + week;
        }

        // Get grade webCode from grade object
        String webCode = grade.getWebCode();
        // Download html with formatted url (using the two arguments just created)
        return client.downloadString(String.format(DOWNLOAD_URL, week, webCode));
    }

    // Parses html and returns a 4-sized Object array
    // Object[] = {ArrayList<Replacement>, ArrayList<Message>, String[], String[]}
    private static Object[] parseHtml(String html, Grade grade) throws WebException
    {
        // Create collector lists for replacements and messages
        ArrayList<Replacement> replacements = new ArrayList<>();
        ArrayList<Message> messages = new ArrayList<>();
        // Get line separator and split html into lines
        String separator = html.contains("\r\n") ? "\r\n" : "\n";
        String[] lines = html.split(separator);
        // Variable to store current date(s) and day name
        String currentDate = "";
        String currentDay = "";
        ArrayList<String> allDates = new ArrayList<>();
        ArrayList<String> allDays = new ArrayList<>();
        // Boolean which indicates that we are currently building the message text
        boolean inMessage = false;
        // String builder to build message text
        StringBuilder messageTextBuilder = new StringBuilder();

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];

            // -----------------------------------------------
            // --- Check validity of this ReplacementTable ---
            // -----------------------------------------------
            // The server stores all replacements for about 1 year
            // Sometimes this old replacements collide with the new ones, consider this example:
            // ReplacementTable of Grade 11 - Week 1:
            //     Real Grade: 11
            // ReplacementTable of Grade 11 - Week 2:
            //     Real Grade: 12 <- why? | That's because new grades were added and so the whole grade list shifted
            //                              one up.
            // To check the validity of the current ReplacementTable, we just have to check the grade embedded in
            // the HTML code. If it equals our given grade, it's valid. If not then not.
            // Unique keyword for the line containing the grade: "<BR>"

            if (grade != null)
            {
                if (line.contains("<BR>"))
                {
                    String strGrade = removeHtmlTags(line)
                            .replaceAll(" ", "")
                            .replace("Klasse", "");
                    Grade _grade = Grade.parse(strGrade);
                    if (_grade != null)
                    {
                        if (!_grade.equals(grade))
                        {
                            throw new WebException("Corrupted data");
                        }
                    }
                    else
                    {
                        // Interrupt parsing and raise exception
                        throw new WebException("Corrupted data");
                    }
                }
            }

            // -----------------------------
            // --- Retrieve current date ---
            // -----------------------------
            // Messages in html code have no property or attribute which indicates its
            // current date, but luckily the html itself contains lines which are easily parsable.
            // Some lines contain the current day names within <b> tags. The <b> tags also only appear
            // at this places, so they are unique and save to parse.

            if (line.contains("<b>") && !inMessage)
            {
                // In a current date line
                // Perform a substring
                int start = line.indexOf("<b>") + 3; // Add 3 because keyword "<b>" is 3 chars long.
                int end = line.indexOf(" ", start);
                currentDate = line.substring(start, end);
                // Remove last "." from date
                if (currentDate.endsWith("."))
                {
                    currentDate = currentDate.substring(0, currentDate.length() - 1);
                }
                start += currentDate.length() + 1;
                end = line.indexOf("</", start);
                currentDay = line.substring(start, end);
                // Remove whitespaces from day
                currentDay = currentDay.trim();
                // Add new date/day to global dates field
                allDates.add(currentDate);
                allDays.add(currentDay);
                continue;
            }

            // -----------------------------
            // --- Retrieve replacements ---
            // -----------------------------
            // All replacements are stored in table rows (<tr>) with the class "list odd" or "list even".
            // (there are different classes because they need to rendered with different colors)
            // The goal is to read each table data in this table row and pass it to our Replacement.Builder to create
            // a java object based on the html file. Unique keywords:
            // - "list odd"
            // - "list even"

            if (line.contains("list odd") || line.contains("list even"))
            {
                // First remove the first part from '<tr..' until '"center">'
                int start = line.indexOf("\">") + 2; // Add 2 because keyword "">" is 2 chars long
                line = line.substring(start);
                // Get data by splitting line with keyword "</td><td class="list" align="center">"
                line = line
                        .replaceAll("</td><td class=\"list\" align=\"center\">", "</td><td class=\"list\">")
                        .replaceAll("&nbsp;", "---");
                ArrayList<String> data = new ArrayList<>(Arrays.asList(line.split("</td><td class=\"list\">")));
                data.add(1, currentDay);

                // Convert to String Array
                String[] data_arr = new String[data.size()];
                data_arr = data.toArray(data_arr);
                // Remove last "." from date
                if (data_arr[0].endsWith("."))
                {
                    data_arr[0] = data_arr[0].substring(0, data_arr[0].length() - 1);
                }
                // Remove brackets from grade data (not occurring all the time)
                data_arr[2] = data_arr[2].replace("(", "").replace(")", "");
                // Remove " R" at the end of a room
                if (data_arr[5].endsWith(" R"))
                {
                    data_arr[5] = data_arr[5].substring(0, data_arr[5].length() - 2);
                }
                // Remove html tags from data piece
                data_arr[7] = data_arr[7].replace("</td></tr>", "");
                // Add (forgotten?) "fällt aus" if new room and new subject are empty
                if (data_arr[4].equals("---") && data_arr[5].equals("---") && data_arr[7].equals("---"))
                {
                    data_arr[7] = "fällt aus";
                }

                // Build replacement
                Replacement.Builder builder = Replacement.Builder
                        .fromData(data_arr);
                Replacement replacement = builder.create();
                replacements.add(replacement);
                continue;
            }

            // -------------------------
            // --- Retrieve messages ---
            // -------------------------
            // All messages are located in tables with the attribute "rules", which is a parse-safe keyword.
            // Goal is to extract the message from the second table row of the table and pass it to our Message.Builder
            // to create a java object based on the html file. Unique keywords:
            // - "rules" (for the table)

            if (inMessage && line.contains("</table>"))
            {
                // If this line contains the above keyword, the message text ends.
                inMessage = false;
                // Build message and it to our list
                Message.Builder builder = new Message.Builder()
                        .setText(messageTextBuilder.toString())
                        .setDay(currentDay)
                        .setDate(currentDate);
                Message message = builder.create();
                messages.add(message);
                // Reset messageTextBuilder
                messageTextBuilder = new StringBuilder();
                continue;
            }

            if (inMessage)
            {
                // We are here if we previously flagged the boolean inMessage.

                // Clean string
                line = line.replaceAll("\r", "");
                while (line.contains("<br><br>"))
                {
                    line = line.replace("<br><br>", "<br>");
                }

                line = line.replaceAll("<br>", "\n");
                String text = removeHtmlTags(line).trim().replaceAll(" {2}", " "); // Remove double spaces

                // Add string to the whole message
                messageTextBuilder.append(text);
                continue;
            }

            if (line.contains("rules"))
            {
                // Set flag that we are have to parse the message text
                inMessage = true;
                // Skip one line, because isn't interesting for us
                i += 1;
            }
        }

        // Put dates into an array
        String[] dates = new String[allDates.size()];
        dates = allDates.toArray(dates);
        // Put days into an array
        String[] days = new String[allDays.size()];
        days = allDays.toArray(days);

        return new Object[] {replacements, messages, dates, days};
    }

    // Removes all html tags in text which was retrieved using the parser from above
    private static String removeHtmlTags(String text)
    {
        // StringBuilder is used, because it handles not final strings the most efficient.
        StringBuilder builder = new StringBuilder();
        builder.append(text);

        // Html tags are build of "<", "something" and ">".
        while (builder.indexOf("<") != -1 && builder.indexOf(">") != -1)
        {
            // Delete tags and tag name between the tags
            int start = builder.indexOf("<");
            int end = builder.indexOf(">") + 1; // Add 1 because keyword ">" is 1 char long.
            builder.delete(start, end);
        }

        return builder.toString();
    }

    // Private constructor which initializes table with the help of the results object array
    private ReplacementTable(Object[] results, Grade grade, Calendar downloadDate)
    {
        assert results.length == 4;
        replacements = (ArrayList<Replacement>) results[0];
        messages = (ArrayList<Message>) results[1];
        dates = (String[]) results[2];
        days = (String[]) results[3];
        this.grade = grade;
        this.downloadDate = downloadDate;
        // Remove double replacements
        optimize();
    }

    /**
     * Returns all replacements
     *
     * @return All replacements as list
     */
    public List<Replacement> getReplacements()
    {
        return replacements;
    }

    /**
     * Returns all replacements, which meet all criteria of the filter
     *
     * @param filter Filter map used to determine if replacement should be returned
     * @return All replacements after the filter was applied
     */
    public List<Replacement> getReplacements(Map<ReplacementFilter, Collection<String>> filter)
    {
        return getReplacements(filter, null);
    }

    /**
     * Returns all replacements, which meet all criteria of the filter and are unknown due to
     * the knownEntries map
     *
     * @param filter Filter map used to determine if replacement should be returned
     * @return All replacements after the filter was applied
     */
    public List<Replacement> getReplacements(Map<ReplacementFilter, Collection<String>> filter, Map<ReplacementFilter, Collection<String>> knownEntries)
    {
        ArrayList<Replacement> filtered = new ArrayList<>();
        ReplacementFilter[] keys = ReplacementFilter.values();

        for (Replacement replacement : replacements)
        {
            boolean canAddReplacement = true;

            for (ReplacementFilter key : keys)
            {
                String dataChunk = replacement.data[key.ordinal()];

                // Is the replacement data known?
                try
                {
                    Collection<String> entries = knownEntries.get(key);
                    if (entries != null && !entries.contains(dataChunk))
                    {
                        // Data is unknown, so it won't be checked by the filter and go through
                        canAddReplacement = true;
                        break;
                    }
                }
                catch (NullPointerException e) {}

                // Is the replacement valid?
                try
                {
                    Collection<String> filterValues = filter.get(key);
                    if (filterValues != null && !filterValues.contains(dataChunk))
                    {
                        canAddReplacement = false;
                        break;
                    }
                }
                catch (NullPointerException e) {}
            }

            if (canAddReplacement)
            {
                filtered.add(replacement);
            }
        }

        return filtered;
    }

    // Merges two replacements with equal content into one
    private void optimize()
    {
        for (int i = 0; i < replacements.size() - 1; i++)
        {
            Replacement r1 = replacements.get(i);
            Replacement r2 = replacements.get(i + 1);
            // Get replacements data for comparision
            // Set period to ""
            String[] data1 = r1.getData();
            data1[3] = "";
            String[] data2 = r2.getData();
            data2[3] = "";
            if (Arrays.equals(data1, data2))
            {
                // If true: both replacements can be merged together
                int p1 = Integer.parseInt(r1.getPeriod());
                int p2 = Integer.parseInt(r2.getPeriod());
                if (Math.abs(p1 - p2) != 1)
                {
                    continue;
                }

                if (p1 > p2)
                {
                    int tmp = p1;
                    p1 = p2;
                    p2 = tmp;
                }

                String period = String.format("%d - %d", p1, p2);
                // Build new replacement
                Replacement.Builder builder = Replacement.Builder
                        .fromData(data1)
                        .setPeriod(period);
                Replacement replacement = builder.create();
                // Delete old replacements (2 times)
                for (int j = 0; j < 2; j++) replacements.remove(i);
                // Add new replacement
                replacements.add(i, replacement);
            }
        }
    }

    /**
     * Returns all messages
     *
     * @return All messages as list
     */
    public List<Message> getMessages()
    {
        return messages;
    }


    /**
     * Returns the ReplacementTable's grade
     *
     * @return Grade of the ReplacementTable
     */
    public Grade getGrade()
    {
        return grade;
    }

    /**
     * Returns all dates that are covered by the ReplacementTable
     *
     * @return All dates of ReplacementTable
     */
    public String[] getDates()
    {
        return dates.clone();
    }

    /**
     * Retuns all days that are covered by the ReplacementTable
     *
     * @return All days of ReplacementTable
     */
    public String[] getDays()
    {
        return days.clone();
    }

    /**
     * Returns the date on which this ReplacementTable was downloaded
     *
     * @return Download date
     */
    public Calendar getDownloadDate()
    {
        return downloadDate;
    }
}
