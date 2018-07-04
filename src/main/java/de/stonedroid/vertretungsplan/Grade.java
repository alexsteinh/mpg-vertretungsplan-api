package de.stonedroid.vertretungsplan;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Grade class for using within the ReplacementTable downloader
 */
public class Grade implements Serializable
{
    // All grade values that are possible
    private static final List<String> grades = Arrays.asList(
            "5a", "5b", "5c", "5d",
            "6a", "6b", "6c", "6d",
            "7a", "7b", "7c", "7d",
            "8a", "8b", "8c", "8d",
            "9a", "9b", "9c", "9d", "9e",
            "10a", "10b", "10c", "10d", "10e",
            "11", "12",
            "xy"
    );

    // This points to the grade value of the grades list above
    private int index;

    // Intern constructor, so that a programmer is forced to use the parse utility
    private Grade(int index)
    {
        this.index = index;
    }

    /**
     * Tries to return a valid grade object with help
     * of the given string.
     *
     * @param grade grade name as string
     * @return if successfully parsed: grade object. If not: null
     */
    public static Grade parse(String grade)
    {
        // Making our grade string parsing-ready
        grade = grade.trim().toLowerCase();
        // Index
        //         = -1 -> if grade was not found in list
        //         => 0 -> if grade was found in list
        int i = grades.indexOf(grade);
        if (i != -1)
        {
            // String grade is a valid grade value
            return new Grade(i);
        }
        else
        {
            // String grade was not found in the grade list
            return null;
        }
    }

    /**
     * Returns all possible values of grade
     *
     * @return All grade names
     */
    public static List<String> getGradeNames()
    {
        return grades;
    }

    /**
     * Returns ready for site-scraping number to place into the scraping url.
     *
     * @return ready for site-scraping number to place into the scraping url
     */
    public String getWebCode()
    {
        // "5a"s webcode is "01", not "00", that's why we add 1 to index.
        String strIndex = String.valueOf(index + 1);
        if (strIndex.length() == 1)
        {
            // Index is only one char long when casted to a string
            // so we need to add a prefix "0", because the web code
            // is always 2 chars long
            strIndex = "0" + strIndex;
        }

        return strIndex;
    }

    /**
     * Returns grade value
     *
     * @return grade value
     */
    @Override
    public String toString()
    {
        return grades.get(index);
    }

    /**
     * Returns whether obj equals this obj.
     *
     * @param obj another (Grade)obj, which should equal this grade object
     * @return whether obj equals this obj
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Grade)
        {
            // If both indexes are the same, both objects are the same
            return this.index == ((Grade) obj).index;
        }
        else
        {
            // obj isn't even a Grade object
            return false;
        }
    }
}
