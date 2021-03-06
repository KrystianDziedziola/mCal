package edu.projectuz.mCal.web.controller;

import edu.projectuz.mCal.core.models.CalendarEvent;
import edu.projectuz.mCal.exporters.csv.CsvExporterToString;
import edu.projectuz.mCal.exporters.ical.ICalExporter;
import edu.projectuz.mCal.importers.planuz.logic.PlanUzImporter;
import edu.projectuz.mCal.importers.planuz.model.calendars.CalendarsList;
import edu.projectuz.mCal.importers.planuz.model.timetables.Department;
import edu.projectuz.mCal.importers.planuz.model.timetables.DepartmentsList;
import edu.projectuz.mCal.service.CalendarEventService;
import edu.projectuz.mCal.service.PlanUzService;
import edu.projectuz.mCal.web.EventToRemoveInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public final class HomeController {

    private HomeController() {
    }

    @Autowired
    private CalendarEventService calendarService;

    @Autowired
    private PlanUzService planUzService;

    @RequestMapping(value = "/", method = GET)
    public String home(final Model model) {
        model.addAttribute("calendarEvent", new CalendarEvent());
        model.addAttribute("eventToRemoveInfo", new EventToRemoveInfo());
        model.addAttribute("calendarEvents", calendarService.findAllCalendarEvent());
        addDepartmentsList(model);
        return "home";
    }

    @RequestMapping(value = "/addEvent", method = POST)
    public String addEventSubmit(@Valid @ModelAttribute("calendarEvent")
                                 final CalendarEvent calendarEvent,
                                 final Errors errors,
                                 final Model model) {
        model.addAttribute("eventToRemoveInfo", new EventToRemoveInfo());
        if (errors.hasErrors()) {
            model.addAttribute("calendarEvents",
                    calendarService.findAllCalendarEvent());
            return "home";
        } else {
            calendarService.saveCalendarEvent(calendarEvent);
            return "redirect:/";
        }

    }

    @RequestMapping(value = "/clearEvents", method = GET)
    public String clearEvents() {
        calendarService.deleteAll();
        return "redirect:/";
    }

    @RequestMapping(value = "/removeEvent/{id}", method = GET)
    public String removeEvent(@PathVariable int id) {
        calendarService.deleteCalendarEventById(id);
        return "redirect:/";
    }

    @RequestMapping(value = "/generate/csv", method = GET, produces = "text/csv;charset=UTF-8")
    public void generateCsv(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition","attachment;filename=events.csv");

        CsvExporterToString converter = new CsvExporterToString();
        ArrayList<CalendarEvent> calendarEvents =
                (ArrayList<CalendarEvent>) calendarService.findAllCalendarEvent();

        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(converter.generateCsvToString(calendarEvents)
                    .getBytes("UTF-8"));
        }
    }

    @RequestMapping(value = "/generate/ics", method = GET, produces = "text/ics;charset=UTF-8")
    public void generateICal(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition","attachment;filename=events.ics");

        ICalExporter converter = new ICalExporter();
        ArrayList<CalendarEvent> calendarEvents =
                (ArrayList<CalendarEvent>) calendarService.findAllCalendarEvent();

        try (ServletOutputStream out = response.getOutputStream();) {
            out.write(converter.generateICal(calendarEvents).getBytes("UTF-8"));
        }
    }

    private void addDepartmentsList(Model model) {
        DepartmentsList departmentsList = planUzService.getAllTimetables();
        if (departmentsList == null) {
            model.addAttribute("departmentsList", new ArrayList<Department>());
        } else {
            model.addAttribute("departmentsList", planUzService.getAllTimetables().getDepartmentsList());
        }
    }
}
