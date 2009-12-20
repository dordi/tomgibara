/*
package com.tomgibara.stupp;

public final class StuppProperty {

	final StuppType type;
	final String propertyName;

	public StuppProperty(StuppType type, String propertyName) {
		this.type = type;
		this.propertyName = propertyName;
	}

	// object methods

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StuppProperty)) return false;
		StuppProperty that = (StuppProperty) obj;
		if (!this.propertyName.equals(that.propertyName)) return false;
		if (!this.type.equals(that.type)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return type.hashCode() ^ propertyName.hashCode();
	}

	@Override
	public String toString() {
		return type + ":" + propertyName;
	}

}
*/