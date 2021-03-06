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

import junit.framework.Test;
import junit.framework.TestSuite;

public class StuppTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.tomgibara.stupp");
		//$JUnit-BEGIN$
		suite.addTestSuite(StuppTest.class);
		suite.addTestSuite(HandlerTest.class);
		suite.addTestSuite(TypeTest.class);
		suite.addTestSuite(ScopeTest.class);
		suite.addTestSuite(FactoryTest.class);
		suite.addTestSuite(LockTest.class);
		suite.addTestSuite(PropertyIndexTest.class);
		suite.addTestSuite(UniqueIndexTest.class);
		//$JUnit-END$
		return suite;
	}

}
