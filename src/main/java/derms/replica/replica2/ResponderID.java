package derms.replica.replica2;

public class ResponderID {
	public City city;
	public short num;

	public ResponderID(City city, short num) {
		this.city = city;
		this.num = num;
	}

	@Override
	public String toString() {
		return ""+city+"R"+num;
	}
}
