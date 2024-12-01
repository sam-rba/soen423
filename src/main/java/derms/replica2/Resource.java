package derms.replica2;

import java.io.Serializable;

class Resource implements Serializable {
  ResourceID id;
  ResourceType type;
  int duration;
  boolean isBorrowed;
  CoordinatorID borrower;
  int borrowDuration;

  Resource(ResourceID id, ResourceType type, int duration, boolean isBorrowed, CoordinatorID borrower, int borrowDuration) {
    this.id = id;
    this.type = type;
    this.duration = duration;
    this.isBorrowed = isBorrowed;
    this.borrower = borrower;
    this.borrowDuration = borrowDuration;
  }

  Resource(ResourceID id, ResourceType type, int duration) {
    this(id, type, duration, false, new CoordinatorID(), -1);
  }

  Resource() {
    this(new ResourceID(), ResourceType.AMBULANCE, 0);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return id+" "+duration;
  }
}
