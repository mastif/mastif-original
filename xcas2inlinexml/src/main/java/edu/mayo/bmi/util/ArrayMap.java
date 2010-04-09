package edu.mayo.bmi.util;

/**
 * Note: copied code from java2s.com
 */

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ArrayMap extends AbstractMap implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	static class Entry implements Map.Entry {
		protected Object key, value;

		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public Object setValue(Object newValue) {
			Object oldValue = value;
			value = newValue;
			return oldValue;
		}

		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry e = (Map.Entry) o;
			return (key == null ? e.getKey() == null : key.equals(e.getKey()))
					&& (value == null ? e.getValue() == null : value.equals(e
							.getValue()));
		}

		public int hashCode() {
			int keyHash = (key == null ? 0 : key.hashCode());
			int valueHash = (value == null ? 0 : value.hashCode());
			return keyHash ^ valueHash; // ^means that if keyHash and valueHash
										// are different.
		}

		public String toString() {
			return key + "=" + value;
		}
	}

	private Set entries = null;

	private ArrayList list;

	public ArrayMap() {
		list = new ArrayList();
	}

	public ArrayMap(Map map) {
		list = new ArrayList();
		putAll(map);
	}

	public ArrayMap(int initialCapacity) {
		list = new ArrayList(initialCapacity);
	}

	public Set entrySet() {
		if (entries == null) {
			entries = new AbstractSet() {
				public void clear() {
					list.clear();
				}

				public Iterator iterator() {
					return list.iterator();
				}

				public int size() {
					return list.size();
				}
			};
		}
		return entries;
	}

	public Object put(Object key, Object value) {
		int size = list.size();
		Entry entry = null;
		int i;
		if (key == null) {
			for (i = 0; i < size; i++) {
				entry = (Entry) (list.get(i));
				if (entry.getKey() == null) {
					break;
				}
			}
		} else {
			for (i = 0; i < size; i++) {
				entry = (Entry) (list.get(i));
				if (key.equals(entry.getKey())) {
					break;
				}
			}
		}
		Object oldValue = null;
		if (i < size) {
			oldValue = entry.getValue();
			entry.setValue(value);
		} else {
			list.add(new Entry(key, value));
		}
		return oldValue;
	}

	public Object clone() {
		return new ArrayMap(this);
	}

	public static void main(String args[]) {
		Map map = new ArrayMap(13);
		map.put("1", "One");
		map.put("2", "Two");
		map.put("3", "Three");
		map.put("4", "Four");
		map.put("5", "Five");
		map.put("6", "Six");
		map.put("7", "Seven");
		map.put("8", "Eight");
		map.put("9", "Nine");
		map.put("10", "Ten");
		map.put("11", "Eleven");
		map.put("12", "Twelve");
		map.put("13", "Thirteen");
		System.out.println(map);
		System.out.println(map.keySet());
		System.out.println(map.values());
	}
}