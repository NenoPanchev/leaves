package com.example.leaves.util;

import com.example.leaves.model.payload.response.Holiday;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
    @Value("${holidays.api.base.url}")
    private String HOLIDAY_API_BASE_URL;
    private List<LocalDate> holidays = new ArrayList<>();
    public HolidaysUtil(Gson gson) {
        this.gson = gson;
    }

    @Scheduled(cron = "1 0 0 1 6 * ", zone = "EET")
    public void setHolidayDates() throws IOException {
        List<LocalDate> holidays = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        holidays.addAll(fetchAllHolidayDatesForYear(currentYear));
        holidays.addAll(fetchAllHolidayDatesForYear(nextYear));
        setHolidays(holidays);
    }
    private List<LocalDate> fetchAllHolidayDatesForYear(int year) throws IOException {
        String urlString = HOLIDAY_API_BASE_URL + year + "/BG";
        List<LocalDate> holidayDates = new ArrayList<>();

        WebClient.Builder builder = WebClient.builder();

        List<Holiday> holidays = builder.build()
                .get()
                .uri(urlString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Holiday>>() {})
                .block();


            // Checks how many of Christmas Holidays are in the weekend
            int daysToAddAfterChristmasHolidays = checkHowManyDaysToAddAfterChristmasHolidays(holidays);

//                      According to Bulgarian Labour Code, Art. 154, para. 2
//                      If holiday is not Easter Holiday and is in the weekend,
//                      next one or two work days should be holidays as well
            holidays
                    .stream()
                    .forEach(day -> {
                        if (EASTER_HOLIDAYS.contains(day.getName())
                                || day.getName().contains(CHRISTMAS_HOLIDAYS_PREFIX)) {
                            holidayDates.add(day.getDate());
                        } else {
                            holidayDates.add(day.getDate());
                            int daysToSkip = 0;
                            if (isSunday(day.getDate())) {
                                daysToSkip = 1;
                            }
                            if (isSaturday(day.getDate())) {
                                daysToSkip = 2;
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
