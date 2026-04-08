package com.ms4systems.devs.analytics;

import java.util.*;

import com.ms4systems.devs.core.util.Pair;

public class RelationIterator implements Iterator {
	private Relation r;
	private ArrayList keys;
	private Object curKey = null;
	private Set curSet;
	private ArrayList values;
	private boolean change = true;

	public RelationIterator(Relation R) {
		r = R;
		Set keyset = R.keySet();
		keys = new ArrayList(keyset);
	}

	private Object Next() {
		if (keys.isEmpty())
			return null;
		if (change) {
			change = false;
			curKey = keys.get(0);
			curSet = r.getSet(curKey);
			values = new ArrayList(curSet);
		}
		if (values.isEmpty())
			return null;
		return new Pair(curKey, values.get(0));
	}

	private void removeNext() {
		values.remove(0);
		if (values.isEmpty()) {
			keys.remove(0);
			change = true;
		}
	}

	public boolean hasNext() {
		return Next() != null;
	}

	public Object next() {
		Object ret = Next();
		removeNext();
		return ret;
	}

	public void remove() {
	}

}
