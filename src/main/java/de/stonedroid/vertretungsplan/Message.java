package de.stonedroid.vertretungsplan;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The "Nachrichten zum Tag" container
 */
public class Message implements Serializable
{
    // Intern array which holds all information
    String[] data;

    // Private constructor for Builder class
    private Message(String[] data)
    {
        this.data = data.clone();
    }

    /**
     * Returns grade's data
     *
     * @return All data
     */
    public String[] getData()
    {
        return data.clone();
    }

    /**
     * Returns grade's date
     *
     * @return date
     */
    public String getDate()
    {
        return data[0];
    }

    /**
     * Returns grade's day
     *
     * @return day
     */
    public String getDay()
    {
        return data[1];
    }

    /**
     * Returns grade's text
     *
     * @return text
     */
    public String getText()
    {
        return data[2];
    }

    /**
     * Returns a human-readable string containing all information
     *
     * @return Message information
     */
    @Override
    public String toString()
    {
        return data[0].concat(" | ").concat(data[1]).concat(" | ").concat(data[2]);
    }

    /**
     * Returns whether this object equals the other object
     *
     * @param obj Another Object (preferably a Grade obj)
     * @return Whether obj equals this obj
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Message)
        {
            return Arrays.equals(data, ((Message) obj).data);
        }
        else
        {
            // obj isn't even a Message object
            return false;
        }
    }

    /**
     * Builder class to easily build a Message object
     */
    public static final class Builder
    {
        // Intern data array containing information needed for the replacement
        private String[] data;

        /**
         * Returns a Builder object based on the message's data
         *
         * @param message Message which the Builder should base on
         * @return Builder based on message's data
         */
        public static Builder fromMessage(Message message)
        {
            return fromData(message.data);
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

        // Constructor used for the fromMessage() method
        private Builder(String[] data)
        {
            this.data = data.clone();
        }

        /**
         * Creates a new Builder, which is ready for being filled with information
         */
        public Builder()
        {
            data = new String[3];
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
         * Sets day of the current builder
         *
         * @param day New day name
         * @return Current Builder with new day
         */
        public Builder setDay(String day)
        {
            data[1] = day;
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
            data[2] = text;
            return this;
        }

        /**
         * Creates a new Message object built from the current builder
         *
         * @return New Message from builder
         */
        public Message create()
        {
            return new Message(data);
        }
    }
}
