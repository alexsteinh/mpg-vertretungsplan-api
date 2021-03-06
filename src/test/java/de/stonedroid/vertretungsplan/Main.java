package de.stonedroid.vertretungsplan;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Test3();
    }

    // Basic usage example
    public static void Test1()
    {
        ReplacementTable table = null;
        try
        {
            // Replace Grade.parse(x) with whatever grade you wish
            table = ReplacementTable.downloadTable(Grade.parse("11"), 1);
        }
        catch (WebException e)
        {
            e.printStackTrace();
        }

        printReplacementTable(table);
    }

    public static void Test3()
    {
        List<String> names = Grade.getGradeNames();
        ArrayList<Grade> grades = new ArrayList<>();
        names.forEach(s -> grades.add(Grade.parse(s)));

        for (Grade grade : grades)
        {
            try
            {
                ReplacementTable table = ReplacementTable.downloadTable(grade, 1);
            }
            catch (WebException e)
            {
                System.err.println("Struggling with " + grade);
            }
        }
    }

    // Basic usage example (using async method)
    public static void Test2()
    {
        ReplacementTable.downloadTableAsync(Grade.parse("12"), new OnDownloadFinishedListener()
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
        // Print dates
        System.out.println("Grade: " + table.getGrade());
        System.out.println("Dates:");
        String[] dates = table.getDates();

        for (int i = 0; i < dates.length; i++)
        {
            if (i < dates.length - 1)
            {
                System.out.print(dates[i] + " ");
            }
            else
            {
                System.out.println(dates[i] + "\n");
            }
        }

        // Print days
        System.out.println("Days:");
        String[] days = table.getDays();

        for (int i = 0; i < days.length; i++)
        {
            if (i < days.length - 1)
            {
                System.out.print(days[i] + " ");
            }
            else
            {
                System.out.println(days[i] + "\n");
            }
        }

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
