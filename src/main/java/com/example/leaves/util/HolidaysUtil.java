package com.example.leaves.util;

import com.example.leaves.model.payload.response.Holiday;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.leaves.constants.GlobalConstants.*;

@Component
public class HolidaysUtil {
    private final Gson gson;
    private List<LocalDate> holidays = new ArrayList<>();
    public HolidaysUtil(Gson gson) {
        this.gson = gson;
    }

    public void setHolidayDates() throws IOException {
        List<LocalDate> holidays = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        holidays.addAll(fetchAllHolidayDatesForYear(currentYear));
        holidays.addAll(fetchAllHolidayDatesForYear(nextYear));
        setHolidays(holidays);
    }
    private List<LocalDate> fetchAllHolidayDatesForYear(int year) throws IOException {
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        URL url = new URL(HOLIDAYS_API_BASE_URL + year + "/BG");
        List<LocalDate> holidayDates = new ArrayList<>();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        //Getting the response code
        int responseCode = conn.getResponseCode();
        conn.disconnect();
        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            List<Holiday> holidays = Arrays.stream(gson
                            .fromJson(inputStreamReader, Holiday[].class))
                    .collect(Collectors.toList());
            //Close the scanner
            inputStreamReader.close();


            // Checks how many of Christmas Holidays are in the weekend
            int daysToAddAfterChristmasHolidays = checkHowManyDaysToAddAfterChristmasHolidays(holidays);
            holidays
                    .stream()
                    .forEach(day -> {
//                      According to Bulgarian Labour Code, Art. 154, para. 2
//                      If holiday is not Easter Holiday and is in Saturday or Sunday,
//                      next or next two work days should be holidays as well
                        if (EASTER_HOLIDAYS.contains(day.getName()) || day.getName().contains(CHRISTMAS_HOLIDAYS_PREFIX)) {
                            holidayDates.add(day.getDate());
                        } else {
                            holidayDates.add(day.getDate());
                            int daysToSkip = 0;
                            if (isSaturday(day.getDate())) {
                                daysToSkip = 2;
                            }
                            if (isSunday(day.getDate())) {
                                daysToSkip = 1;
                            }
                            if (daysToSkip != 0) {
                                LocalDate addedDate = day.getDate().plusDays(daysToSkip);
                                holidayDates.add(addedDate);
                            }
                        }
                    });

            // Adds that many holidays after Christmas
            for (int i = 1; i <= daysToAddAfterChristmasHolidays; i++) {
                LocalDate addedDate = holidays.get(holidays.size() - 1).getDate().plusDays(i);
                holidayDates.add(addedDate);
            }
        }
        return holidayDates;
    }

    public boolean isHoliday(LocalDate date) {
        return getHolidays().contains(date);
    }

    private int checkHowManyDaysToAddAfterChristmasHolidays(List<Holiday> holidays) {
        int counter = 0;
        List<LocalDate> christmasDays = holidays
                .stream()
                .filter(day -> day.getName().contains(CHRISTMAS_HOLIDAYS_PREFIX))
                .map(Holiday::getDate)
                .collect(Collectors.toList());
        for (LocalDate christmasDay : christmasDays) {
            if (isWeekend(christmasDay)) {
                counter++;
            }
        }
        return counter;
    }

    private boolean isWeekend(LocalDate date) {
        return isSaturday(date) || isSunday(date);
    }

    private boolean isSaturday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY;
    }

    private boolean isSunday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public List<LocalDate> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<LocalDate> holidays) {
        this.holidays = holidays;
    }
}
