package com.example.leaves.util;

import com.example.leaves.constants.GlobalConstants;
import com.example.leaves.model.payload.response.Holiday;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.leaves.constants.GlobalConstants.CHRISTMAS_HOLIDAYS_PREFIX;
import static com.example.leaves.constants.GlobalConstants.EASTER_HOLIDAYS;

@Component
public class HolidaysUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HolidaysUtil.class);

    @Value("${holidays.api.base.url}")
    private String holidayApiBaseUrl;
    private List<LocalDate> holidays = new ArrayList<>();

    @Scheduled(cron = "${cron-jobs.update.holidays:0 0 0 1 6 *}", zone = GlobalConstants.EUROPE_SOFIA)
    public void setHolidayDates() {
        List<LocalDate> holidayDates = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        holidayDates.addAll(fetchAllHolidayDatesForYear(currentYear));
        holidayDates.addAll(fetchAllHolidayDatesForYear(nextYear));
        setHolidays(holidayDates);
    }

    private List<LocalDate> fetchAllHolidayDatesForYear(int year) {
        String urlString = holidayApiBaseUrl + year + "/BG";
        List<LocalDate> holidayDates = new ArrayList<>();

        WebClient.Builder builder = WebClient.builder();

        List<Holiday> holidayList = builder.build()
                .get()
                .uri(urlString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Holiday>>() {
                })
                .block();

        if (holidayList == null) {
            LOGGER.error("error fetching holidays from api");
            return holidayDates;
        }
        // Checks how many of Christmas Holidays are in the weekend
        int daysToAddAfterChristmasHolidays = checkHowManyDaysToAddAfterChristmasHolidays(holidayList);

//                      According to Bulgarian Labour Code, Art. 154, para. 2
//                      If holiday is not Easter Holiday and is in the weekend,
//                      next one or two work days should be holidays as well
        holidayList
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
            LocalDate addedDate = holidayList.get(holidayList.size() - 1).getDate().plusDays(i);
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
