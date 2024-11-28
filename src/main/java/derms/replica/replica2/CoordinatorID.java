package derms.replica.replica2;

import java.io.Serializable;

public class CoordinatorID implements Serializable {
  public String city;
  public short num;

  public CoordinatorID(String city, short num) {
    this.city = city;
    this.num = num;
  }

  public CoordinatorID(String city, int num) {
    this(city, (short) num);
  }

  public CoordinatorID() {
    this("XXX", 0);
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
