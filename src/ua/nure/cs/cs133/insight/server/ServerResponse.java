/**
 *
 */
package ua.nure.cs.cs133.insight.server;

import java.util.List;

/**
 * @author usertt
 *
 */
public class ServerResponse {
	private int success;
	private String message;
	private List<Monument> places;
	public int getSuccess() {
		return success;
	}
	public void setSuccess(int success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<Monument> getPlaces() {
		return places;
	}
	public void setPlaces(List<Monument> places) {
		this.places = places;
	}


}
