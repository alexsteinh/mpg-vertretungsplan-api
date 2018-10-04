package de.stonedroid.vertretungsplan;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class ReplacementTableTest
{
    private static final int EXAMPLE_COUNT = 3;

    private ReplacementTable[] tables = new ReplacementTable[EXAMPLE_COUNT];

    @Before
    public void init()
    {
        for (int i = 0; i < EXAMPLE_COUNT; i++)
        {
            try
            {
                // Initialize tables using html downloaded by hand
                String resourceName = String.format("/example%d.html", i + 1);
                String html = Utils.readFileToEnd(getClass().getResource(resourceName).getFile());
                tables[i] = ReplacementTable.parseFromHtml(html);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testGetReplacements()
    {
        String[][] expected = new String[][]
                {
                        {
                                "22.6. | Freitag | 11 | 5 - 6 | --- | --- | rev2 | Exkursion",
                                "22.6. | Freitag | 11 | 1 - 2 | E2 | 101 R | E2 | E findet statt",
                                "22.6. | Freitag | 11 | 1 - 2 | --- | --- | E5 | fällt aus"
                        },
                        {
                                "25.6. | Montag | 11 | 5 - 6 | --- | --- | bio2 | fällt aus"
                        },
                        {
                                "25.6. | Montag | 10A | 3 - 4 | D | 122 R | B | Tausch",
                        }
                };

        for (int i = 0; i < expected.length; i++)
        {
            List<Replacement> replacements = tables[i].getReplacements();

            for (int j = 0; j < replacements.size(); j++)
            {
                // Check whether replacement content are the same as expected
                assertEquals(expected[i][j], replacements.get(j).toString());
            }
        }
    }

    @Test
    public void testGetReplacementsWithFilter()
    {
        String[][] expected = new String[][]
                {
                        {
                                "22.6. | Freitag | 11 | 1 - 2 | E2 | 101 R | E2 | E findet statt",
                                "22.6. | Freitag | 11 | 1 - 2 | --- | --- | E5 | fällt aus"
                        },
                        {
                        },
                        {
                                "25.6. | Montag | 10A | 3 - 4 | D | 122 R | B | Tausch"
                        }
                };

        for (int i = 0; i < expected.length; i++)
        {
            // Configure a unique filter foreach replacement table
            HashMap<ReplacementFilter, Collection<String>> filter = new HashMap<>();
            switch (i)
            {
                case 0:
                    filter.put(ReplacementFilter.PERIOD, Arrays.asList("1 - 2"));
                    filter.put(ReplacementFilter.OLD_SUBJECT, Arrays.asList("E"));
                    break;
                case 1:
                    filter.put(ReplacementFilter.TEXT, Arrays.asList("fällt"));
                    filter.put(ReplacementFilter.ROOM, Arrays.asList("418"));
                    break;
                case 2:
                    filter.put(ReplacementFilter.ROOM, Arrays.asList("122"));
                    filter.put(ReplacementFilter.SUBJECT, Arrays.asList("D"));
                    filter.put(ReplacementFilter.PERIOD, Arrays.asList("4"));
            }

            // Check whether there are as many filtered replacements as expected
            List<Replacement> replacements = tables[i].getReplacements(filter);
            assertEquals(expected[i].length, replacements.size());

            for (int j = 0; j < replacements.size(); j++)
            {
                // Check whether replacement content are the same as expected
                assertEquals(expected[i][j], replacements.get(j).toString());
            }
        }
    }
}
