package derms.replica.replica2;

import java.io.Serializable;

public  class Resource implements Serializable {
  public ResourceID id;
  public ResourceType type;
  public int duration;
  public boolean isBorrowed;
  public CoordinatorID borrower;
  public int borrowDuration;

  public Resource(ResourceID id, ResourceType type, int duration, boolean isBorrowed, CoordinatorID borrower, int borrowDuration) {
    this.id = id;
    this.type = type;
    this.duration = duration;
    this.isBorrowed = isBorrowed;
    this.borrower = borrower;
    this.borrowDuration = borrowDuration;
  }

  public Resource(ResourceID id, ResourceType type, int duration) {
    this(id, type, duration, false, new CoordinatorID(), -1);
  }

  public Resource() {
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
