package derms.replica.replica2;

import java.io.Serializable;

public  class Resource implements Serializable {
  public ResourceID id;
  public ResourceName name;
  public int duration;
  public boolean isBorrowed;
  public CoordinatorID borrower;
  public int borrowDuration;

  public Resource(ResourceID id, ResourceName name, int duration, boolean isBorrowed, CoordinatorID borrower, int borrowDuration) {
    this.id = id;
    this.name = name;
    this.duration = duration;
    this.isBorrowed = isBorrowed;
    this.borrower = borrower;
    this.borrowDuration = borrowDuration;
  }

  public Resource(ResourceID id, ResourceName name, int duration) {
    this(id, name, duration, false, new CoordinatorID(), -1);
  }

  public Resource() {
    this(new ResourceID(), ResourceName.AMBULANCE, 0);
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
