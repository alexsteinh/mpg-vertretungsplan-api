package de.stonedroid.vertretungsplan;

import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("" +
                "--------------\n" +
                "--- Test 1 ---\n" +
                "--------------\n");
        Test1();
        System.out.println("" +
                "\n---------------------------\n" +
                "--- Test 2 (with async) ---\n" +
                "---------------------------\n");
        Test2();
    }

    // Basic usage example
    public static void Test1()
    {
        ReplacementTable table = null;
        try
        {
            // Replace Grade.parse(x) with whatever grade you wish
            table = ReplacementTable.downloadTable(Grade.parse("11"), 0);
        }
        catch (WebException e)
        {
            e.printStackTrace();
        }

        printReplacementTable(table);
    }

    // Basic usage example (using async method)
    public static void Test2()
    {
        ReplacementTable.downloadTableAsync(Grade.parse("11"), new OnDownloadFinishedListener()
        {
            @Override
            public void onFinished(ReplacementTable table)
            {
                printReplacementTable(table);
            }

            @Override
            public void onFailed(String message)
            {
                System.err.println(message);
            }
        });
    }

    private static void printReplacementTable(ReplacementTable table)
    {
        List<Replacement> replacements = table.getReplacements();
        List<Message> messages = table.getMessages();
        System.out.println("Replacements:");

        for (Replacement replacement : replacements)
        {
            System.out.println(replacement);
        }

        System.out.println("\nMessages:");

        for (Message message : messages)
        {
            System.out.println(message);
        }
    }
}
