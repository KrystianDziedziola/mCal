package edu.projectuz.mCal.importers.ical.logic;

import edu.projectuz.mCal.core.models.CalendarEvent;
import edu.projectuz.mCal.importers.base.BaseEventImporter;
import edu.projectuz.mCal.importers.base.ImporterSourceType;
import edu.projectuz.mCal.importers.ical.model.ICalModel;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParserImpl;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * This class is main class for importer all data from ICal.
 */
public class ICalImporter extends BaseEventImporter {

    /**
     * @param sourcePath specifies the path of the file.
     * @param sourceType specifies the type of resource {@link ImporterSourceType}.
     */

    protected ICalImporter(String sourcePath, ImporterSourceType sourceType) {
        super(sourcePath, sourceType);
    }
    public ICalModel iCalModel = new ICalModel();
    private CalendarParserImpl Parser = new CalendarParserImpl();

    /**
     * This method return name importer.
     */
    @Override
    public String getName() {
        return "ICalImporter";
    }

    @Override
    public void importData() {

    }

/**
 * This is a main function of this class.
 * It is used to import all data from ICal.
 *
 * @return Returned list of events.
 * */
    public ArrayList<CalendarEvent> getCalendarEventList() throws Exception {
        InputStream is = new ByteArrayInputStream(getSourceContent().getBytes());
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = null;
        try {
            calendar = builder.build(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }
        ArrayList<VEvent> vevents = calendar.getComponents(Component.VEVENT);
        ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
        try {
            for (VEvent ev : vevents) {
                CalendarEvent event = new CalendarEvent();
                //event.setStartDate() // tutaj data
                //event.setEndDate() // tutaj tysz
                event.setTitle(ev.getName());//not sure
                event.setDescription(ev.getDescription().getValue());
                //event.setTimeZone()//;c
                events.add(event);
            }
        }catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
        return events;
    }
}