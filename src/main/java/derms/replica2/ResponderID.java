package derms.replica2;

import derms.City;

class ResponderID {
	City city;
	short num;

	ResponderID(City city, short num) {
		this.city = city;
		this.num = num;
	}

	@Override
	public String toString() {
		return ""+city+"R"+num;
	}
}
