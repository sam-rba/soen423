package derms.replica2;

import java.io.Serializable;

class CoordinatorID implements Serializable {
  String city;
  short num;

  CoordinatorID(String city, short num) {
    this.city = city;
    this.num = num;
  }

  CoordinatorID() {
    this("XXX", (short) 0);
  }

  static CoordinatorID parse(String str) throws IllegalArgumentException {
    if (str.length() != City.codeLen+ID.nDigits)
      throw new IllegalArgumentException("illegal coordinator ID: " + str);

    try {
      String city = str.substring(0, City.codeLen);
      short num = Short.parseShort(str.substring(City.codeLen));
      return new CoordinatorID(city, num);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("illegal coordinator ID '" + str + "': " + e.getMessage());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj.getClass() != this.getClass())
      return false;
    CoordinatorID other = (CoordinatorID) obj;
    return other.city.equals(this.city) && other.num == this.num;
  }

  @Override
  public String toString() {
    return city+"C"+num;
  }
}
