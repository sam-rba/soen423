package derms.replica.replica2;

import java.io.Serializable;

enum ResourceType implements Serializable {
  AMBULANCE,
  FIRETRUCK,
  PERSONNEL;

  static ResourceType parse(String s) throws IllegalArgumentException {
    switch (s) {
      case "AMBULANCE": return ResourceType.AMBULANCE;
      case "FIRETRUCK": return ResourceType.FIRETRUCK;
      case "PERSONNEL": return ResourceType.PERSONNEL;
    }
    throw new IllegalArgumentException("invalid resource name: "+s);
  }
}
