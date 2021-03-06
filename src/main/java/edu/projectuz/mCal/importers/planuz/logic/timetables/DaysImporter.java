package edu.projectuz.mCal.importers.planuz.logic.timetables;

import edu.projectuz.mCal.importers.planuz.model.timetables.GroupTimetable;
import edu.projectuz.mCal.importers.planuz.model.calendars.DaysList;
import edu.projectuz.mCal.importers.planuz.logic.HtmlComponentName;
import edu.projectuz.mCal.importers.planuz.model.timetables.TimetableDay;
import edu.projectuz.mCal.importers.planuz.model.timetables.TimetableEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is used to import {@link DaysList} object.
 * It is used by {@link GroupsImporter}
 */
class DaysImporter {

    private String url;
    private Document htmlContent;
    private ArrayList<TimetableDay> daysList = new ArrayList<>();

    /**
     * Class constructor which simply sets a value of url variable.
     *
     * @param anUrl - stores information about url address of
     *              HTML content with every {@link TimetableDay} of
     *              particular {@link GroupTimetable}.
     */
    DaysImporter(final String anUrl) {
        this.url = anUrl;
    }

    /**
     * Main function of this class.
     *
     * @return Returns a list of {@link TimetableDay}
     * objects from particular {@link GroupTimetable}.
     */
    ArrayList<TimetableDay> importDays() {
        importHtmlContent();
        importTable();
        return daysList;
    }

    private void importHtmlContent() {
        try {
            htmlContent = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importTable() {
        if (isTimetableEmpty()) {
            daysList = null;
        } else {
            Element table = htmlContent.select(HtmlComponentName.TABLE).
                    get(getTableIndex());
            Elements rows = table.select(HtmlComponentName.ROW);
            rows.remove(TimetableComponentIndex.ROW_WITH_HEADERS);
            importDaysListFromRows(rows);
        }
    }

    private int getTableIndex() {
        return htmlContent.select(HtmlComponentName.TABLE).size() - 1;
    }

    private void importDaysListFromRows(final Elements rows) {
        TimetableDay timetableDay = null;
        for (Element row : rows) {
            if (isRowWithDayName(row)) {
                daysList.add(timetableDay);
                String dayName = row.text();
                timetableDay = new TimetableDay(dayName);
            } else {
                assert timetableDay != null;
                timetableDay.addEvent(convertRowIntoEvent(row));
            }
        }

        daysList.add(timetableDay);
        if (!daysList.isEmpty()) {
            daysList.remove(TimetableComponentIndex.NULL_ELEMENT);
        }

        if (daysList.isEmpty()) {
            daysList = null;
        }

        if (daysList != null) {
            addDayNamesToEvents(daysList);
        }
    }

    private void addDayNamesToEvents(final ArrayList<TimetableDay> aDaysList) {
        for (TimetableDay timetableDay : aDaysList) {
            String name = timetableDay.getName();

            for (TimetableEvent timetableEvent : timetableDay.getEventsList()) {
                timetableEvent.setDayName(name);
            }
        }
    }

    private TimetableEvent convertRowIntoEvent(final Element row) {
        Elements columns = row.select(HtmlComponentName.COLUMN);

        String subgroup = getFormattedSubgroup(
                columns.get(TimetableComponentIndex.SUBGROUP).text());
        String startTime = columns.get(
                TimetableComponentIndex.START_TIME).text();
        String endTime = columns.get(
                TimetableComponentIndex.END_TIME).text();
        String className = columns.get(
                TimetableComponentIndex.CLASS_NAME).text();
        String classType = columns.get(
                TimetableComponentIndex.CLASS_TYPE).text();
        String teacherName = columns.get(
                TimetableComponentIndex.TEACHER_NAME).text();
        String room = columns.get(
                TimetableComponentIndex.ROOM).text();
        String days = getFormattedDays(columns.get(
                TimetableComponentIndex.DAYS).text());

        return new TimetableEvent(subgroup,
                startTime, endTime, className,
                classType, teacherName, room, days);
    }

    private String getFormattedSubgroup(final String text) {
        return text.equals(" ") ? "Everyone" : text;
    }

    private String getFormattedDays(final String days) {
        final int DAYS_PART_INDEX = 0;
        String[] daysParts = days.split("/");
        return daysParts[DAYS_PART_INDEX];
    }

    private boolean isRowWithDayName(final Element row) {
        int NUMBER_OF_COLUMNS_IN_ROW_WITH_DAY_NAME = 1;
        int numberOfColumns = row.select(HtmlComponentName.COLUMN).size();
        return numberOfColumns == NUMBER_OF_COLUMNS_IN_ROW_WITH_DAY_NAME;
    }

    /**
     * This function is kind of tricky to understand.
     * It is this way because of odd structure of planUz.
     * In very few GroupsTimetable there is additional table that
     * contains only simple text.
     * Usually table with timetable is on the second position,
     * but when there's this additional table with text,
     * the table with timetable is moved on third position.
     * @return true or false.
     */
    private boolean isTimetableEmpty() {
        final int NORMAL_TIMETABLE_POSITION = 2;
        final int WITH_ADDITIONAL_TABLE = 3;

        int numberOfTables = htmlContent.select(HtmlComponentName.TABLE).size();
        int numberOfTableWithTimetable = NORMAL_TIMETABLE_POSITION;

        //change of position when there's additional table
        if (numberOfTables == WITH_ADDITIONAL_TABLE) {
            numberOfTableWithTimetable = WITH_ADDITIONAL_TABLE;
        }

        return numberOfTables < numberOfTableWithTimetable;
    }
}
