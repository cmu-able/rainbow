package org.sa.rainbow.util;

import java.util.HashSet;
import java.util.Set;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ReflectionsExtension extends Reflections {
	public ReflectionsExtension(Object[] params) {
		super(params);
	}
	
	

	public ReflectionsExtension(ConfigurationBuilder filterInputsBy) {
		super(filterInputsBy);
	}



	@Override
	public void expandSuperTypes() {
		if (store.keySet().contains(SubTypesScanner.class.getSimpleName())) {
			Multimap<String, String> mmap = store.get(SubTypesScanner.class.getSimpleName());
			Set<String> keys = new HashSet<>(mmap.keySet());
			keys.removeAll(mmap.values());
			Multimap<String,String> expand = HashMultimap.create();
			for (String key : keys) {
				final Class<?> type = ReflectionUtils.forName(key);
				if (type != null) {
					lexpandSupertypes(expand, key, type);
				}
			}
			mmap.putAll(expand);
			
		}
	}

	private void lexpandSupertypes(Multimap<String, String> mmap, String key, Class<?> type) {
		for (Class<?> supertype : ReflectionUtils.getSuperTypes(type)) {
	        if (mmap.put(supertype.getName(), key)) {
	            //if (log != null) log.debug("expanded subtype {} -> {}", supertype.getName(), key);
	            lexpandSupertypes(mmap, supertype.getName(), supertype);
	        }
	    }
	}
}