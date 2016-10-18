/**
 *
 */
package ua.nure.cs.cs133.insight.server;

/**
 * @author gavr
 *
 */
public class Monument {
	private Long id;
	private String name;
	private String desc;
	private String pic;
	private Integer type;
	private String builder;

	private Double latitude;
	private Double longitude;

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	private Double distance;

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double locLat) {
		this.latitude = locLat;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double locLon) {
		this.longitude = locLon;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getBuilder() {
		return builder;
	}

	public void setBuilder(String builder) {
		this.builder = builder;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
