package derms.replica.replica2;

import java.io.Serializable;

public enum ResourceName implements Serializable {
  AMBULANCE,
  FIRETRUCK,
  PERSONNEL;

  public static ResourceName parse(String s) {
    switch (s) {
      case "AMBULANCE": return ResourceName.AMBULANCE;
      case "FIRETRUCK": return ResourceName.FIRETRUCK;
      case "PERSONNEL": return ResourceName.PERSONNEL;
    }
    throw new IllegalArgumentException("invalid resource name: "+s);
  }
}
