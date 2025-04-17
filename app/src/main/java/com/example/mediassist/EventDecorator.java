package com.example.mediassist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDecorator extends View {
    private Paint dotPaint;
    private Map<String, List<Integer>> eventDates; // Date -> Liste de couleurs
    private CalendarView calendarView;
    private int currentMonth;
    private int currentYear;

    public EventDecorator(Context context) {
        super(context);
        init();
    }

    public EventDecorator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        eventDates = new HashMap<>();

        // Initialiser avec le mois et l'année actuels
        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1; // Les mois commencent à 0 dans Calendar
        currentYear = cal.get(Calendar.YEAR);
    }

    public void setCalendarView(CalendarView calendarView) {
        this.calendarView = calendarView;
    }

    public void setCurrentMonthAndYear(int month, int year) {
        this.currentMonth = month;
        this.currentYear = year;
        invalidate();
    }

    public void addEvent(String date, int colorRes) {
        if (!eventDates.containsKey(date)) {
            eventDates.put(date, new ArrayList<>());
        }

        List<Integer> colors = eventDates.get(date);
        if (!colors.contains(colorRes)) {
            colors.add(colorRes);
            invalidate();
        }
    }

    public void clearEvents() {
        eventDates.clear();
        invalidate();
    }

    public boolean hasEvents(String date) {
        return eventDates.containsKey(date) && !eventDates.get(date).isEmpty();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Cette méthode serait utilisée pour dessiner des indicateurs sur le calendrier
        // Malheureusement, le CalendarView standard d'Android ne permet pas facilement
        // de personnaliser l'apparence des jours

        // Pour une implémentation complète, il faudrait utiliser une bibliothèque tierce
        // comme MaterialCalendarView ou créer un calendrier personnalisé
    }
}