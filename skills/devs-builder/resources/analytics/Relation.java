package  com.ms4systems.devs.analytics;

import java.util.*;

public class Relation {
	protected Hashtable<Object, HashSet<Object>> h;
	protected int size;
	protected String name;

	public Iterator iterator() {
		return new RelationIterator(this);
	}

	// This variable is just here so we can use @SuppressWarnings("unused")
	private final int unusedIntVariableForWarnings = 0;

	public Relation() {
		this("Relation");
	}

	public Relation(String name) {
		this.name = name;
		h = new Hashtable();
		size = 0;
	}

	public Relation(boolean b) {
		h = new Hashtable();
		size = 0;
	}

	public Relation(Hashtable<Object, HashSet<Object>> hs) {
		h = hs;
		size = hs.size();
	}

	public Hashtable<Object, HashSet<Object>> getHashtable() {
		return h;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public HashSet getSet(Object key) {
		if (h.get(key) == null) {
			return new HashSet();
		} else {
			return (HashSet) h.get(key);
		}
	}

	public synchronized Object put(Object key, Object value) {
		HashSet s = getSet(key);
		Iterator it = s.iterator();
		Object old = it.hasNext() ? it.next() : null;

		// System.out.println("Before "+size);
		if (s.add(value)) {
			size++;
		}
		// System.out.println("After "+size);
		h.remove(key);
		h.put(key, s);
		return old;
	}

	public synchronized Object remove(Object key, Object value) {
		HashSet s = getSet(key);
		Iterator it = s.iterator();
		Object old = it.hasNext() ? it.next() : null;
		if (s.remove(value)) {
			size--;
		}
		h.remove(key);
		if (s.size() > 0) {
			h.put(key, s);
		}
		return old;
	}

	public synchronized void removeAll(Object key) {
		Set s = getSet(key);
		size -= s.size();
		h.remove(key);
	}

	public Object get(Object key) {
		Set s = getSet(key);
		if (s.isEmpty()) {
			return null;
		} else {
			Iterator it = s.iterator();
			return it.next();
		}
	}

	public boolean contains(Object key, Object value) {
		return getSet(key).contains(value);
	}

	public HashSet keySet() {
		return new HashSet(h.keySet());
	}

	public HashSet valueSet() {
		return new HashSet(h.values());
	}

	public String toString() {
		return h.toString();
	}

	public void print() {
		System.out.println(toString());
	}

	public synchronized boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		Class cl = getClass();
		if (!cl.isInstance(o)) {
			return false;
		}
		Relation t = (Relation) o;
		if (t.size() != size()) {
			return false;
		}

		Set keyset = keySet();
		Set tset = t.keySet();
		if (!keyset.equals(tset)) {
			return false;
		}

		Iterator it = keyset.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Set valueset = getSet(key);
			Set tvalueset = t.getSet(key);
			if (!valueset.equals(tvalueset)) {
				return false;
			}
		}
		return true;
	}

	public Relation getConverse() {
		Relation r = new Relation(true);
		for (Object k : this.keySet()) {
			for (Object v : this.getSet(k)) {
				r.put(v, k);
			}
		}
		return r;
	}

	// ENDID

	// End custom function definitions
	public static void main(String[] args) {

	}

}
