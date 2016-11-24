package de.hpi.bpt.chimera.janalytics;

import de.hpi.bpt.chimera.database.ConnectionWrapper;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * This class provides an Algorithm to calculate the Runtime for a ScenarioInstance.
 */
public class ScenarioInstanceRuntime implements AnalyticsService {
	private static Logger log = Logger.getLogger(ScenarioInstanceRuntime.class);
	private Date startDate;

	@Override
	/**
	 * This Method calculates the runtime for a scenario instance
	 *
	 * @param args used to specify the ScenarioID for which the Instance-Runtime is calculated
	 * @return a JSONObject containing the scenarioInstanceID and the Instance-Runtime
	 */ public JSONObject calculateResult(String[] args) {
		int scenarioInstanceId = Integer.parseInt(args[0]);
		long duration;
		int millisecondsPerSecond = 1000;
		int secondsPerMinute = 60;
		int minutesPerHour = 60;
		int hoursPerDay = 24;
		int daysPerYear = 365;
		getStartTimestamp(scenarioInstanceId);
		duration = getDuration();

		// converting duration from milliseconds to the format days:hours:minutes:seconds
		long second = (duration / millisecondsPerSecond) % secondsPerMinute;
		long minute = (duration / (millisecondsPerSecond * secondsPerMinute)) % minutesPerHour;
		long hour = (duration / (millisecondsPerSecond * secondsPerMinute * minutesPerHour)) % hoursPerDay;
		long day = (duration / (millisecondsPerSecond * secondsPerMinute * minutesPerHour * hoursPerDay)) % daysPerYear;

		String time = String.format("%02d:%02d:%02d:%02d", day, hour, minute, second);
		String json = "{\"scenarioId\":" + scenarioInstanceId + ",\"ScenarioInstanceRuntime\":\"" + time + "\"}";
		return new JSONObject(json);
	}

	/**
	 * @return the duration since start in milliseconds.
	 */
	public long getDuration() {
		if (startDate == null) {
			return (-1);
		}
		return ((System.currentTimeMillis()) - startDate.getTime());
	}

	/**
	 * @param scenarioInstanceId This is the database od of a scenario instance.
	 */
	public void getStartTimestamp(int scenarioInstanceId) {

		String sql = "SELECT MIN(timestamp) AS start_timestamp " + "FROM `historydataobjectinstance` as h, " + "scenarioinstance as s " + "WHERE h.scenarioinstance_id = " + scenarioInstanceId + " AND h.scenarioinstance_id = s.id";
		try (Connection conn = ConnectionWrapper.getInstance().connect(); Statement stmt = conn.createStatement()) {

			ResultSet results = stmt.executeQuery(sql);
			if (results.next()) {
				startDate = results.getTimestamp("start_timestamp");
			}
			results.close();
		} catch (SQLException e) {
			log.error("SQL Error!: ", e);
		}
	}
}
