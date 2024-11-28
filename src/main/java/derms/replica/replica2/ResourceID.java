package derms.replica.replica2;

import java.io.Serializable;

class ResourceID implements Serializable {
  String city;
  short num;

  ResourceID (String city, short num) {
    this.city = city;
    this.num = num;
  }

  ResourceID() {
    this("XXX", (short) 1111);
  }

  static ResourceID parse(String s) throws IllegalArgumentException {
    if (s.length() != City.codeLen+ID.nDigits) {
      throw new IllegalArgumentException("invalid resource ID: "+s);
    }
    try {
      String cityCode = s.substring(0, City.codeLen);
      short num = Short.parseShort(s.substring(City.codeLen));
      return new ResourceID(cityCode, num);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("invalid resource ID: "+e.getMessage());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    ResourceID other = (ResourceID) obj;
    return (this.city.equals(other.city)) && (this.num == other.num);
  }

  @Override
  public int hashCode() {
    return city.hashCode() * num;
  }

  @Override
  public String toString() {
    return city+num;
  }
}
