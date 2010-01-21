/*
 * Copyright 2009 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.stupp;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import com.tomgibara.pronto.util.Arguments;
import com.tomgibara.pronto.util.Reflect;
import com.tomgibara.stupp.ann.StuppEquality;
import com.tomgibara.stupp.ann.StuppIndexed;
import com.tomgibara.stupp.ann.StuppNamed;

public class StuppType {

	public static final String PRIMARY_INDEX_NAME = "primary";

	private static final Class<?>[] CONS_PARAMS = new Class<?>[] { InvocationHandler.class };

	//TODO this caching won't work because references to definitions aren't maintained
	private static final WeakHashMap<Definition, StuppType> instances = new WeakHashMap<Definition, StuppType>();
	
	//TODO establish proper rules for valid type names
	private static final Pattern validNamePattern = Pattern.compile("[A-Za-z_]+");
	private static final Pattern invalidCharPattern = Pattern.compile("[^A-Za-z_]");

	public static Definition newDefinition(Class<?> clss) {
		return newDefinition(null, clss);
	}
	
	public static Definition newDefinition(Class<?>... classes) {
		return newDefinition(null, classes);
	}
	
	public static Definition newDefinition(ClassLoader classLoader, Class<?> clss) {
		classLoader = Stupp.nonNullClassLoader(classLoader, clss);
		final Class<?> proxyClass = Proxy.getProxyClass(classLoader, clss);
		return new Definition(proxyClass);
	}
	
	public static Definition newDefinition(ClassLoader classLoader, Class<?>... classes) {
		//TODO unpleasant change of behaviour here
		classLoader = Stupp.nonNullClassLoader(classLoader, StuppType.class);
		final Class<?> proxyClass = Proxy.getProxyClass(classLoader, classes);
		return new Definition(proxyClass);
	}

	//convenience method
	public static StuppType getInstance(Class<?> clss) {
		return newDefinition(clss).getType();
	}
	
	static void checkName(String name) {
		if (!validNamePattern.matcher(name).matches()) throw new IllegalArgumentException("Index name " + name + "does not match pattern: " + validNamePattern.pattern());
	}
	
	private static StuppType getInstance(Definition def) {
		synchronized (instances) {
			StuppType type = instances.get(def);
			if (type == null) {
				def = def.clone();
				type = new StuppType(def);
				instances.put(def, type);
			}
			return type;
		}
	}
	
	private final Class<?> proxyClass;

	final HashSet<String> propertyNames;
	final HashMap<Method, String> methodPropertyNames;
	final HashMap<String, Class<?>> propertyClasses;
	final StuppProperties equalityProperties;
	final HashMap<String, StuppProperties> indexProperties = new HashMap<String, StuppProperties>();
	final HashMap<String, Annotation> indexDefinitions;
	final String name;
	
	private StuppType(Definition def) {
		proxyClass = def.proxyClass;
		methodPropertyNames = def.methodPropertyNames;
		propertyClasses = def.propertyClasses;
		propertyNames = new HashSet<String>(methodPropertyNames.values());
		equalityProperties = properties(def.equalityProperties);
		for (Map.Entry<String, ArrayList<String>> entry : def.indexProperties.entrySet()) {
			final ArrayList<String> propertyNames = entry.getValue();
			indexProperties.put(entry.getKey(), properties((String[]) propertyNames.toArray(new String[propertyNames.size()])));
		}
		indexDefinitions = def.indexDefinitions;
		name = def.name;
	}

	public String getName() {
		return name;
	}
	
	public boolean instanceImplements(Class<?> clss) {
		return clss.isAssignableFrom(proxyClass);
	}
	
	public StuppProperties properties(String... propertyNames) {
		return new StuppProperties(this, propertyNames);
	}

	public StuppProperties getIndexProperties() {
		return indexProperties.get(PRIMARY_INDEX_NAME);
	}

	//TODO include index type in annotation
	public Class<? extends StuppIndex<?>> getIndexClass() {
		return getIndexClass(PRIMARY_INDEX_NAME);
	}
	
	public StuppProperties getIndexProperties(String indexName) {
		return indexProperties.get(indexName);
	}
	
	//TODO include index type in annotation
	public Class<? extends StuppIndex<?>> getIndexClass(String indexName) {
		return StuppUniqueIndex.class;
	}
	
	public Object newInstance() {
		try {
			return proxyClass.getConstructor(CONS_PARAMS).newInstance(new Object[] { new StuppHandler(this) });
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}

	// package methods

	Collection<? extends StuppIndex<?>> createIndices() {
		return StuppIndex.createIndices(indexProperties, indexDefinitions);
	}
	
	// inner classes

	public static class Definition implements Cloneable {
		
		final Class<?> proxyClass;
		final HashMap<Method, String> methodPropertyNames;
		final HashMap<String, Class<?>> propertyClasses;
		String[] equalityProperties = null;
		final HashMap<String, ArrayList<String>> indexProperties = new HashMap<String, ArrayList<String>>();
		final HashMap<String, Annotation> indexDefinitions = new HashMap<String, Annotation>();
		String name;
		
		private Definition(Definition that) {
			this.proxyClass = that.proxyClass;
			this.methodPropertyNames = that.methodPropertyNames;
			this.propertyClasses = that.propertyClasses;
			this.indexProperties.putAll(that.indexProperties);
			this.equalityProperties = that.equalityProperties.clone();
			this.indexDefinitions.putAll(that.indexDefinitions);
			this.name = that.name;
		}
		
		private Definition(Class<?> proxyClass) {
			//generate method property name map and type map
			HashMap<Method, String> methodPropertyNames = new HashMap<Method, String>();
			HashMap<String, Class<?>> propertyClasses = new HashMap<String, Class<?>>();
			final Class<?>[] interfaces = proxyClass.getInterfaces();
			StringBuilder sb = new StringBuilder();
			for (Class<?> iface : interfaces) {
				if (sb.length() > 0) sb.append('_');
				final String name = iface.getSimpleName();
				final int i = name.lastIndexOf('.');
				final String baseName = i < 0 ? name : name.substring(i+1);
				sb.append(invalidCharPattern.matcher(baseName).replaceAll(""));
				for (Method method : iface.getMethods()) {
					final boolean setter = Reflect.isSetter(method);
					final boolean getter = Reflect.isGetter(method);
					if (setter || getter) {
						final String propertyName = Reflect.propertyName(method.getName());
						methodPropertyNames.put(method, propertyName);
						final Class<?> c = setter ? method.getParameterTypes()[0] : method.getReturnType();
						final Class<?> k = propertyClasses.get(propertyName);
						if (c != k) {
							if (k == null) {
								propertyClasses.put(propertyName, c);
							} else {
								boolean cek = k.isAssignableFrom(c);
								boolean kec = c.isAssignableFrom(k);
								if (!cek && !kec) {
									throw new IllegalArgumentException("Incompatible setter/getter types: " + propertyName);
								} else if (getter && cek || setter && kec) {
									throw new IllegalArgumentException("Incompatible setter type too general: " + propertyName);
								} else if (getter) {
									propertyClasses.put(propertyName, c);
								}
							}
						}
					}
				}
			}
			
			//assign values
			this.proxyClass = proxyClass;
			this.methodPropertyNames = methodPropertyNames;
			this.propertyClasses = propertyClasses;
			this.name = sb.length() == 0 ? "_" : sb.toString();
			
			//default other state based on annotations
			processAnnotations();
		}
		
		public Definition setName(String name) {
			Arguments.notNull(name, "name");
			checkName(name);
			this.name = name;
			return this;
		}
		
		public Definition addIndex(String indexName, String... keyProperties) {
			if (keyProperties == null) throw new IllegalArgumentException("null indexProperties");
			StuppIndex.checkName(indexName);
			if (this.indexProperties.containsKey(indexName)) throw new IllegalArgumentException("duplicate index name: " + indexName);
			
			//check key properties
			final int length = keyProperties.length;
			final HashSet<String> set = new HashSet<String>();
			//TODO doesn't actually check if there is a corresponding setter - does that matter?
			for (int i = 0; i < length; i++) {
				final String property = keyProperties[i];
				if (property == null) throw new IllegalArgumentException("Null key property at index " + i);
				Class<?> clss = propertyClasses.get(property);
				if (clss == null) throw new IllegalArgumentException("Unknown property " + property + " at index " + i);
				if (!set.add(property)) throw new IllegalArgumentException("Duplicate key property " + property + " at index " + i);
			}

			this.indexProperties.put(indexName, new ArrayList<String>(Arrays.asList(keyProperties)));
			return this;
		}
		
		public Definition removeIndex(String indexName) {
			indexProperties.remove(indexName);
			return this;
		}

		public Definition setIndexDefinition(Annotation annotation) {
			final String indexName = StuppIndex.checkForIndexAnnotation(annotation);
			if (indexName == null) throw new IllegalArgumentException("Supplied annotation is not an index definition annotation");
			indexDefinitions.put(indexName, annotation);
			return this;
		}
		
		public Definition clearIndexDefinition(String indexName) {
			indexDefinitions.remove(indexName);
			return this;
		}
		
		public Definition setEqualityProperties(String... equalityProperties) {
			if (equalityProperties == null) throw new IllegalArgumentException("null equalityProperties");
			
			final int length = equalityProperties.length;
			final LinkedHashSet<String> set = new LinkedHashSet<String>();
			//TODO doesn't actually check if there is a corresponding setter - does that matter?
			for (int i = 0; i < length; i++) {
				final String property = equalityProperties[i];
				if (property == null) throw new IllegalArgumentException("Null equality property at index " + i);
				if (!propertyClasses.containsKey(property)) throw new IllegalArgumentException("Unknown property " + property + " at index " + i);
				if (!set.add(property)) throw new IllegalArgumentException("Duplicate equality property " + property + " at index " + i);
				
			}

			this.equalityProperties = (String[]) set.toArray(new String[set.size()]);
			return this;
		}

		//TODO consider removing this method
		private void checkComplete() {
		}

		public StuppType getType() {
			checkComplete();
			return getInstance(this);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Definition)) return false;
			final Definition that = (Definition) obj;
			if (this.proxyClass != that.proxyClass) return false;
			if (!Arrays.equals(equalityProperties, that.equalityProperties)) return false;
			if (!this.indexProperties.equals(that.indexProperties)) return false;
			if (!this.indexDefinitions.equals(that.indexDefinitions)) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			return proxyClass.hashCode() ^ indexProperties.hashCode() ^ indexDefinitions.hashCode() ^ Arrays.hashCode(equalityProperties);
		}
		
		@Override
		protected Definition clone() {
			return new Definition(this);
		}
		
		private void processAnnotations() {
			
			final HashMap<String, ArrayList<Method>> indexMethods = new HashMap<String, ArrayList<Method>>();
			final ArrayList<Method> equalityMethods = new ArrayList<Method>();
			final HashMap<String, Annotation> indexDefinitions = new HashMap<String, Annotation>();
			final Class<?>[] interfaces = proxyClass.getInterfaces();
			StringBuilder sb = null;
			for (Class<?> i : interfaces) {
				StuppNamed named = i.getAnnotation(StuppNamed.class);
				if (named != null) {
					if (sb == null) {
						sb = new StringBuilder();
					} else {
						sb.append('_');
					}
					sb.append(named.value());
				}
				Annotation[] annotations = i.getAnnotations();
				for (Annotation annotation : annotations) {
					final String indexName = StuppIndex.checkForIndexAnnotation(annotation);
					//TODO in the future allow repeated consistent definitions of index annotation (requires equality on annotation implementations). 
					if (indexName != null) {
						Annotation old = indexDefinitions.put(indexName, annotation);
						if (old != null) throw new IllegalArgumentException("Two index definition annotations for index: " + indexName);
					}
				}
				for (Method method : i.getMethods()) {
					if (!Reflect.isSetter(method)) continue;
					final StuppIndexed indexed = method.getAnnotation(StuppIndexed.class);
					final StuppEquality equality = method.getAnnotation(StuppEquality.class);
					if (indexed != null) {
						final String indexName = indexed.name();
						ArrayList<Method> methods = indexMethods.get(indexName);
						if (methods == null) {
							StuppIndex.checkName(indexName);
							methods = new ArrayList<Method>();
							indexMethods.put(indexName, methods);
						}
						int position = indexed.position();
						final int size = methods.size();
						if (position < 0 || position == size) {
							methods.add(method);
						} else if (position < size) {
							methods.set(position, method);
						} else {
							while (position > size) {
								methods.add(null);
								position --;
							}
							methods.add(method);
						}
					}
					//TODO weaken this
					if (equality != null || indexed != null) {
						equalityMethods.add(method);
					}
				}
			}
			//create name
			if (sb != null) {
				name = sb.toString();
				checkName(name);
			}
			//create key arrays
			for (Map.Entry<String, ArrayList<Method>> entry : indexMethods.entrySet()) {
				final String indexName = entry.getKey();
				final ArrayList<Method> methods = entry.getValue();
				//ensure key properties have no gaps
				while (methods.remove(null));
				{
					final int length = methods.size();
					final ArrayList<String> properties = new ArrayList<String>(length);
					for (int i = 0; i < length; i++) {
						final Method method = methods.get(i);
						final String propertyName = Reflect.propertyName(method.getName());
						properties.add(propertyName);
					}
					this.indexProperties.put(indexName, properties);
				}
			}
			//create equality array
			{
				final int length = equalityMethods.size();
				final String[] equalityProperties = new String[length];
				for (int i = 0; i < length; i++) {
					Method method = equalityMethods.get(i);
					equalityProperties[i] = Reflect.propertyName(method.getName());
				}
				this.equalityProperties = equalityProperties;
			}
			//record index annotations permanently
			this.indexDefinitions.putAll(indexDefinitions);
		}
	}
	
}
