package derms.replica.replica2;

import java.io.Serializable;

public enum ResourceType implements Serializable {
  AMBULANCE,
  FIRETRUCK,
  PERSONNEL;

  public static ResourceType parse(String s) throws IllegalArgumentException {
    switch (s) {
      case "AMBULANCE": return ResourceType.AMBULANCE;
      case "FIRETRUCK": return ResourceType.FIRETRUCK;
      case "PERSONNEL": return ResourceType.PERSONNEL;
    }
    throw new IllegalArgumentException("invalid resource name: "+s);
  }
}
