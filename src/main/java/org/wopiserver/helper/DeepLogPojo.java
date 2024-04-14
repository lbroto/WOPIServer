package org.wopiserver.helper;

import java.lang.reflect.Field;

/**
 * Through reflection, this class analyze and print every POJO properties
 */
public class DeepLogPojo {
	public static void printAllProperties(Object o) {
		Class<?> c=o.getClass();
		System.err.println("Class "+c.getName());
		for(Field f: c.getDeclaredFields()) {
			System.err.print(f.getName()+" => ");
			try {
				f.setAccessible(true);			// avoid any issue due to "private" fields
				System.err.println(f.get(o));
			} catch(Exception e) {
				System.err.println("unreachable");
			}				
		}
	}
}
