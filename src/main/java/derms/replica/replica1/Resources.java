package derms.replica.replica1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class Resources {
	private Map<ResourceName, Map<ResourceID, Resource>> resources;

	public Resources() {
		this.resources = new ConcurrentHashMap<ResourceName, Map<ResourceID, Resource>>();
	}

	public List<Resource> borrowed(CoordinatorID borrower, ResourceName name) {
		List<Resource> borrowed = new ArrayList<Resource>();
		Resource[] namedResources = getByName(name);
		for (Resource r : namedResources) {
			if (r.isBorrowed && r.borrower.equals(borrower)) {
				borrowed.add(r);
			}
		}
		return borrowed;
	}

	public Resource getByID(ResourceID id) throws NoSuchElementException {
		for (Map<ResourceID, Resource> rids : resources.values()) {
			Resource resource = rids.get(id);
			if (resource != null) {
				return resource;
			}
		}
		throw new NoSuchElementException("No such resource "+id);
	}

	public Resource[] getByName(ResourceName name) {
		Map<ResourceID, Resource> rids = resources.get(name);
		if (rids == null) {
			return new Resource[0];
		}
		Resource[] r = new Resource[0];
		return rids.values().toArray(r);
	}

	public void add(Resource r) {
		Map<ResourceID, Resource> rids;
		synchronized (resources) {
			rids = resources.get(r.name);
			if (rids == null) {
				rids = new ConcurrentHashMap<ResourceID, Resource>();
				resources.put(r.name, rids);
			}
		}
		synchronized (rids) {
			Resource existing = rids.get(r.id);
			if (existing != null) {
				existing.duration += r.duration;
			} else {
				rids.put(r.id, r);
			}
		}
	}

	public void removeByID(ResourceID id) throws NoSuchElementException {
		for (Map<ResourceID, Resource> rids : resources.values()) {
			if (rids.containsKey(id)) {
				rids.remove(id);
				return;
			}
		}
		throw new NoSuchElementException("No such resource "+id);
	}
}
