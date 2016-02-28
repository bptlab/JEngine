package de.uni_potsdam.hpi.bpt.bp2014.jcore;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import java.util.Date;

/**
 *
 */
public class TimeCalculator {
    // For now supports dates in form of PT1M30S
    public Date getDateForTimerDefinition(String timeInterval) {
        return new Date();
    }

    // For now supports dates in form of PT1M30S
    public Date getDatePlusInterval(Date start, String timeInterval) {
        //
        Date beginning = new Date(start.getTime());
        try {
            Duration duration = DatatypeFactory.newInstance().newDuration(timeInterval);
            duration.addTo(beginning);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Get fucked");
        }
        return beginning;
    }
}
