package de.stonedroid.vertretungsplan;

import java.util.*;

/**
 * Holds Messages and Replacements for the chosen grade
 */
public class ReplacementTable
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

    // Intern "constructor" for junit testing
    static ReplacementTable parseFromHtml(String html)
    {
        Object[] results = parseHtml(html);
        return new ReplacementTable(results, null);
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
        Object[] result = parseHtml(html);
        return new ReplacementTable(result, grade);
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

    // Parses html and returns a 3-sized Object array
    // Object[] = {ArrayList<Replacement>, ArrayList<Message>, String[]}
    private static Object[] parseHtml(String html)
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
        // Boolean which indicates that we are currently building the message text
        boolean inMessage = false;
        // String builder to build message text
        StringBuilder messageTextBuilder = new StringBuilder();

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];

            // -----------------------------
            // --- Retrieve current date ---
            // -----------------------------
            // Messages in the html code have no property or attribute which indicates it's
            // current date, but luckily the html itself contains lines which are easily parsable.
            // Some lines contain the current day names within <b> tags. The <b> tags also only appear
            // at this places, so they are unique and save to parse.

            if (line.contains("<b>"))
            {
                // In a current date line
                // Perform a substring
                int start = line.indexOf("<b>") + 3; // Add 3 because keyword "<b>" is 3 chars long.
                int end = line.indexOf(" ", start);
                currentDate = line.substring(start, end);
                start += currentDate.length() + 1;
                end = line.indexOf("</", start);
                currentDay = line.substring(start, end);
                // Add new date to global dates field
                allDates.add(currentDate);
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
                line = line.substring(start, line.length());
                // Get data by splitting line with keyword "</td><td class="list" align="center">"
                ArrayList<String> data = new ArrayList<>(Arrays.asList(line.split("</td><td class=\"list\" align=\"center\">")));
                data.add(1, currentDay);
                // Remove html tags from data piece
                data.set(7, data.get(7).replace("</td></tr>", ""));
                // Convert to String Array
                String[] data_arr = new String[data.size()];
                data_arr = data.toArray(data_arr);
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
                line = line.replaceAll("<br>", " ").replaceAll("\r", "");
                String text = removeHtmlTags(line).trim().replaceAll(" {2}", " "); // Remove double spaces
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

        // Put dates into a string
        String[] dates = new String[allDates.size()];
        dates = allDates.toArray(dates);
        return new Object[] {replacements, messages, dates};
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
    private ReplacementTable(Object[] results, Grade grade)
    {
        assert results.length == 3;
        replacements = (ArrayList<Replacement>) results[0];
        messages = (ArrayList<Message>) results[1];
        dates = (String[]) results[2];
        this.grade = grade;
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
    public List<Replacement> getReplacements(Map<ReplacementFilter, String[]> filter)
    {
        // New List where filtered replacements will be added
        ArrayList<Replacement> filtered = new ArrayList<>();

        for (Replacement replacement : replacements)
        {
            String[] data = replacement.data;
            Set<ReplacementFilter> keys = filter.keySet();
            // Indicates that the replacement shouldn't be added in the list
            boolean breakOut = false;

            for (ReplacementFilter key : keys)
            {
                String[] values = filter.get(key);
                // If the replacement doesn't contain any filter value it shouldn't be added
                // into the filtered list.
                boolean hasValue = false;

                for (String value : values)
                {
                    if (data[key.ordinal()].contains(value))
                    {
                        hasValue = true;
                        break;
                    }
                }

                if (!hasValue)
                {
                    breakOut = true;
                    break;
                }
            }

            if (!breakOut)
            {
                filtered.add(replacement);
            }
        }

        return filtered;
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
}
