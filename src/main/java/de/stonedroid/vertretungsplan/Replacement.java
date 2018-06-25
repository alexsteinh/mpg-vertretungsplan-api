package de.stonedroid.vertretungsplan;

import java.util.Arrays;

/**
 * Contains all available information about an replacement
 */
public class Replacement
{
    // Intern data array containing all information
    String[] data;

    // Private constructor for Builder.create()
    private Replacement(String[] data)
    {
        // Always clone the array, so we don't end up modifying
        // the original and also the only address copied one.
        this.data = data.clone();
    }

    /**
     * Returns all information of replacement
     *
     * @return All data
     */
    public String[] getData()
    {
        return data.clone();
    }

    /**
     * Returns date of replacement
     *
     * @return date
     */
    public String getDate()
    {
        return data[0];
    }

    /**
     * Returns grade of replacement
     *
     * @return grade
     */
    public String getGrade()
    {
        return data[1];
    }

    /**
     * Returns period of replacement
     *
     * @return period
     */
    public String getPeriod()
    {
        return data[2];
    }

    /**
     * Returns subject of replacement
     *
     * @return subject
     */
    public String getSubject()
    {
        return data[3];
    }

    /**
     * Returns room of replacement
     *
     * @return room
     */
    public String getRoom()
    {
        return data[4];
    }

    /**
     * Returns oldSubject of replacement
     *
     * @return oldSubject
     */
    public String getOldSubject()
    {
        return data[5];
    }

    /**
     * Returns text of replacement
     *
     * @return text
     */
    public String getText()
    {
        return data[6];
    }

    /**
     * Returns a human-readable string containing all information
     *
     * @return Replacement information
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; i++)
        {
            sb.append(data[i]);
            if (i != data.length - 1)
            {
                // String spacer to make it look cleaner
                sb.append(" | ");
            }
        }

        return sb.toString();
    }

    /**
     * Checks whether this obj and the other obj have the same content
     *
     * @param obj Another replacement
     * @return Whether obj equals this obj
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Replacement)
        {
            return Arrays.equals(data, ((Replacement) obj).data);
        }
        else
        {
            // obj isn't even a Replacement object
            return false;
        }
    }

    /**
     * Builder class to easily build an Replacement object
     */
    public static final class Builder
    {
        // Intern data array containing information needed for the replacement
        private String[] data;

        /**
         * Returns a Builder object based on the replacement's data
         *
         * @param replacement Replacement which the Builder should base on
         * @return Builder based on replacement's data
         */
        public static Builder fromReplacement(Replacement replacement)
        {
            return fromData(replacement.data);
        }

        /**
         * Returns a Builder object based on the given data.
         * (USE THIS ONLY WHEN YOU KNOW WHAT YOU ARE DOING)
         *
         * @param data Data which the Builder is based on
         * @return Builder bases on given data
         */
        public static Builder fromData(String[] data)
        {
            return new Builder(data);
        }

        // Constructor used for the fromReplacement() method
        private Builder(String[] data)
        {
            // Always clone the array, so we don't end up modifying
            // the original and also the only address copied one.
            this.data = data.clone();
        }

        /**
         * Creates a new Builder, which is ready for being filled with information
         */
        public Builder()
        {
            // Every Replacement contains exactly 7 strings with information
            data = new String[7];
        }

        /**
         * Sets date of the current builder
         *
         * @param date New date
         * @return Current Builder with new date
         */
        public Builder setDate(String date)
        {
            data[0] = date;
            return this;
        }

        /**
         * Sets grade of the current builder
         *
         * @param grade New grade
         * @return Current Builder with new grade
         */
        public Builder setGrade(String grade)
        {
            data[1] = grade;
            return this;
        }

        /**
         * Sets period of the current builder
         *
         * @param period New period
         * @return Current Builder with new period
         */
        public Builder setPeriod(String period)
        {
            data[2] = period;
            return this;
        }

        /**
         * Sets subject of the current builder
         *
         * @param subject New subject
         * @return Current Builder with new subject
         */
        public Builder setSubject(String subject)
        {
            data[3] = subject;
            return this;
        }

        /**
         * Sets room of the current builder
         *
         * @param room New room
         * @return Current Builder with new room
         */
        public Builder setRoom(String room)
        {
            data[4] = room;
            return this;
        }

        /**
         * Sets oldSubject of the current builder
         *
         * @param oldSubject New oldSubject
         * @return Current Builder with new oldSubject
         */
        public Builder setOldSubject(String oldSubject)
        {
            data[5] = oldSubject;
            return this;
        }

        /**
         * Sets text of the current builder
         *
         * @param text New text
         * @return Current Builder with new text
         */
        public Builder setText(String text)
        {
            data[6] = text;
            return this;
        }

        /**
         * Creates a new Replacement object built from the current builder
         *
         * @return New Replacement from builder
         */
        public Replacement create()
        {
            return new Replacement(data);
        }
    }
}
