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

public class Stupp {

	static final Class<?>[] NO_PARAMS = new Class<?>[0];

	public static void setProperty(Object object, String property, Object value) {
		getHandler(object).setProperty(object, property, value, true);
	}

	public static Object getProperty(Object object, String property) {
		return getHandler(object).getProperty(property);
	}

	public static StuppType getType(Object object) {
		return getHandler(object).getType();
	}
	
	public static StuppScope getScope(Object object) {
		return getHandler(object).getScope();
	}
	
	static StuppHandler getHandler(Object object) {
		if (!Proxy.isProxyClass(object.getClass())) throw new IllegalArgumentException();
		InvocationHandler handler = Proxy.getInvocationHandler(object);
		if (!(handler instanceof StuppHandler)) throw new IllegalArgumentException();
		return (StuppHandler) handler;
		
	}
	
	static StuppHandler getHandlerOrNull(Object object) {
		if (object == null) return null;
		if (!Proxy.isProxyClass(object.getClass())) return null;
		InvocationHandler handler = Proxy.getInvocationHandler(object);
		if (!(handler instanceof StuppHandler)) return null;
		return (StuppHandler) handler;
	}
	
	//assumes the object is a valid stupp managed instance
	//TODO check where we can use this
	static StuppHandler getHandlerFast(Object object) {
		return (StuppHandler) Proxy.getInvocationHandler(object);
	}

	static String nameOfAnnotation(Annotation annotation) {
		final Method method;
		try {
			method = annotation.getClass().getMethod("name", NO_PARAMS);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Annotation registered without name method: " + annotation.getClass(), e);
		}
		try {
			return (String) method.invoke(annotation);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Annotation registered with non-String name method: " + annotation.getClass(), e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	static ClassLoader nonNullClassLoader(ClassLoader classLoader, Class<?> clss) {
		if (classLoader != null) return classLoader;
		classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader != null) return classLoader;
		if (classLoader == null) classLoader = clss.getClassLoader();
		return classLoader;
	}
	

}
