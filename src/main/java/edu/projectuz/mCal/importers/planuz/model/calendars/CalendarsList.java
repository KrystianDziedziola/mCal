package edu.projectuz.mCal.importers.planuz.model.calendars;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class stores all the information about every planUz calendar.
 * It's a root of calendars hierarchy.
 * It keeps this information in a list of {@link Calendar} objects.
 * Instance of this class is returned by {@link CalendarsListImporter}.
 */
@Entity
public class CalendarsList {
    @Id
    @GeneratedValue
    private int id;

    private String description;
    @OneToMany(cascade={CascadeType.ALL})
    private List<Calendar> calendars = new ArrayList<>();

    /**
     * Class constructor that simply sets a value of description variable.
     *
     * @param aDescription - simple description of all calendars.
     *                     For example:
     *                     Lista kalendarzy - semestr letni 2015/2016
     */
    public CalendarsList(final String aDescription) {
        this.description = aDescription;
    }

    /**
     * This method allows you to add single calendar to the list.
     *
     * @param calendar - {@link Calendar} to add.
     */
    public final void addCalendar(final Calendar calendar) {
        calendars.add(calendar);
    }

    /**
     * This methods sets calendars list to this passed by the parameter.
     *
     * @param aCalendars - a list of {@link Calendar} to set.
     */
    public final void setListOfCalendars(final ArrayList<Calendar> aCalendars) {
        this.calendars = aCalendars;
    }

    /**
     * This method gets calendars list.
     *
     * @return Calendars list.
     */
    public final List<Calendar> getListOfCalendars() {
        return calendars;
    }

    /**
     * This function allows you to search a {@link Calendar} by it's name.
     * If calendar is not found it throws an exception.
     *
     * @param name - name of a {@link Calendar} to find.
     * @return Returns {@link Calendar} object that was found.
     */
    public final Calendar getCalendarByName(
            final String name) throws Exception {
        for (Calendar calendar : calendars) {
            if (calendar.getName().equals(name)) {
                return calendar;
            }
        }
        throw new Exception("Calendar with name \'" + name + "\' not found.");
    }

    /**
     * @return Size of calendars list
     */
    public final int size() {
        return calendars.size();
    }

    /**
     * Converts object of this class to String object with all of it's content.
     *
     * @return Returns converted String.
     */
    @Override
    public final String toString() {
        return "CalendarsList{" + "description='"
                + description + '\''
                + ", calendars=" + calendars
                + '}';
    }

    //region Getter/Setter/Constructor(No-Arg)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Calendar> getCalendars() {
        return calendars;
    }

    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }

    public CalendarsList() {
    }
    //endregion
}
