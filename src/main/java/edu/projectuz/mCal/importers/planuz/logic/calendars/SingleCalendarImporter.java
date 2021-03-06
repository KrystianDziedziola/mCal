package edu.projectuz.mCal.importers.planuz.logic.calendars;

import edu.projectuz.mCal.importers.planuz.logic.HtmlComponentName;
import edu.projectuz.mCal.importers.planuz.model.calendars.Calendar;
import edu.projectuz.mCal.importers.planuz.model.calendars.Day;
import edu.projectuz.mCal.importers.planuz.model.calendars.DaysList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class imports single {@link Calendar} from planUz calendars list.
 * It is a part of {@link CalendarsListImporter}.
 */
class SingleCalendarImporter {

    private Document htmlContent;

    private String name;
    private String description;
    private String url;
    private final int magicNumber = 5;

    private ArrayList<String> tableNames = new ArrayList<>();
    private ArrayList<DaysList> daysLists = new ArrayList<>();

    /**
     * Class constructor. Sets up calendar properties passed as parameters.
     * Values of parameters are imported by {@link CalendarsListImporter}
     * from planUz calendars page.
     *
     * @param aName        Name of importing calendar for example "D".
     * @param aDescription Description of importing calendar
     *                     for example "Studia stacjonarne".
     * @param anUrl        Url address of importing calendar.
     *                     It links to HTML content of calendar.
     */
    SingleCalendarImporter(final String aName,
                           final String aDescription,
                           final String anUrl) {
        this.name = aName;
        this.description = aDescription;
        this.url = anUrl;
    }

    /**
     * This is a main function of this class
     * which is used to import calendar from planUz.
     *
     * @return Returns a {@link Calendar} object that
     * can be added to {@link CalendarsListImporter}.
     */
    Calendar importCalendar() {
        Calendar calendar = new Calendar(name, description);
        importContent();
        calendar.setDaysLists(daysLists);
        return calendar;
    }

    private void importHtmlContent() {
        try {
            htmlContent = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importContent() {
        importHtmlContent();
        importAllTablesNames();
        importTables();
    }

    private void importAllTablesNames() {
        String firstTableName = getFirstTableNameFromHeader();
        tableNames.add(formatName(firstTableName));

        Elements boldTexts = htmlContent.select(HtmlComponentName.BOLD_TEXT);
        int NUMBER_OF_BOLD_TEXTS_TO_SKIP_FOR_NEXT_TABLE_NAME = magicNumber;
        for (int textIndex = CalendarComponentIndex.SECOND_TABLE_NAME;
             textIndex < boldTexts.size();
             textIndex += NUMBER_OF_BOLD_TEXTS_TO_SKIP_FOR_NEXT_TABLE_NAME) {
            tableNames.add(formatName(boldTexts.get(textIndex).text()));
        }
    }

    private String formatName(final String tableName) {
        final int NAME_PART_INDEX = 0;
        String[] nameParts = tableName.split(" - ");
        return nameParts[NAME_PART_INDEX];
    }

    /**
     * This method is necessary because of a mistake made in creating planUz.
     * First table name is placed into "h3" tags with page header.
     * So this two things have to be separated.
     * Every other table name is in "b" tags.
     *
     * @return String value of first table name parsed from header.
     */
    private String getFirstTableNameFromHeader() {
        String header = htmlContent.select(HtmlComponentName.HEADER).text();
        final int FIRST_TABLE_NAME_BEGINNING_INDEX = 14;
        return header.substring(FIRST_TABLE_NAME_BEGINNING_INDEX);
    }

    private void importTables() {
        Elements tables = htmlContent.select(HtmlComponentName.TABLE);
        tables.remove(CalendarComponentIndex.TABLE_WITHOUT_DAYS);

        for (int tableIndex = CalendarComponentIndex.FIRST_TABLE_WITH_DAYS;
             tableIndex < tables.size(); tableIndex++) {
            DaysList daysList = new DaysList(tableNames.get(tableIndex));
            Elements rows = tables.get(tableIndex).
                    select(HtmlComponentName.ROW);

            for (int rowIndex = CalendarComponentIndex.FIRST_ROW_WITH_DAYS;
                 rowIndex < rows.size(); rowIndex++) {
                Elements columns = rows.get(rowIndex).
                        select(HtmlComponentName.COLUMN);
                daysList.addDay(convertColumnsIntoDay(columns));
            }
            daysLists.add(daysList);
        }

    }

    private Day convertColumnsIntoDay(final Elements columns) {
        int number = Integer.parseInt(columns.get(
                CalendarComponentIndex.DAY_NUMBER_COLUMN).text());
        String date = columns.get(
                CalendarComponentIndex.DAY_DATE_COLUMN).text();
        String dayAccordingToTimetable = columns.get(
                CalendarComponentIndex.TIMETABLE_DAY_COLUMN).text();
        String dayAccordingToCalendar = columns.get(
                CalendarComponentIndex.CALENDAR_DAY_COLUMN).text();
        return new Day(number, date,
                dayAccordingToTimetable, dayAccordingToCalendar);
    }
}
