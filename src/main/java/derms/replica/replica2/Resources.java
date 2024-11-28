package derms.replica.replica2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

class Resources {
	private Map<ResourceType, Map<ResourceID, Resource>> resources;

	Resources() {
		this.resources = new ConcurrentHashMap<ResourceType, Map<ResourceID, Resource>>();
	}

	List<Resource> borrowed(CoordinatorID borrower, ResourceType name) {
		List<Resource> borrowed = new ArrayList<Resource>();
		Resource[] namedResources = getByName(name);
		for (Resource r : namedResources) {
			if (r.isBorrowed && r.borrower.equals(borrower)) {
				borrowed.add(r);
			}
		}
		return borrowed;
	}

	Resource getByID(ResourceID id) throws NoSuchElementException {
		for (Map<ResourceID, Resource> rids : resources.values()) {
			Resource resource = rids.get(id);
			if (resource != null) {
				return resource;
			}
		}
		throw new NoSuchElementException("No such resource "+id);
	}

	Resource[] getByName(ResourceType name) {
		Map<ResourceID, Resource> rids = resources.get(name);
		if (rids == null) {
			return new Resource[0];
		}
		Resource[] r = new Resource[0];
		return rids.values().toArray(r);
	}

	void add(Resource r) {
		Map<ResourceID, Resource> rids;
		synchronized (resources) {
			rids = resources.get(r.type);
			if (rids == null) {
				rids = new ConcurrentHashMap<ResourceID, Resource>();
				resources.put(r.type, rids);
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

	void removeByID(ResourceID id) throws NoSuchElementException {
		for (Map<ResourceID, Resource> rids : resources.values()) {
			if (rids.containsKey(id)) {
				rids.remove(id);
				return;
			}
		}
		throw new NoSuchElementException("No such resource "+id);
	}
}
