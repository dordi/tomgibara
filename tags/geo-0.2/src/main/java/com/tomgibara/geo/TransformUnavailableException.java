/*
 * Copyright 2012 Tom Gibara
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

package com.tomgibara.geo;

/**
 * Thrown when transformation of a point is not possible because no suitable
 * transformation is known.
 * 
 * @author Tom Gibara
 */

public class TransformUnavailableException extends RuntimeException {

	private static final long serialVersionUID = -3983054942122984072L;

	public TransformUnavailableException() {
	}

	public TransformUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransformUnavailableException(String message) {
		super(message);
	}

	public TransformUnavailableException(Throwable cause) {
		super(cause);
	}

}
