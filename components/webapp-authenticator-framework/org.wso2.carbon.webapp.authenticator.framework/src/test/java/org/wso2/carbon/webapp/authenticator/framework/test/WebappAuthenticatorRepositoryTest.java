/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.webapp.authenticator.framework.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticatorRepository;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import org.wso2.carbon.webapp.authenticator.framework.test.util.MalformedAuthenticator;
import org.wso2.carbon.webapp.authenticator.framework.test.util.TestWebappAuthenticator;

public class WebappAuthenticatorRepositoryTest {

    @Test
    public void testAddAuthenticator() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();

        WebappAuthenticator addedAuthenticator = new TestWebappAuthenticator();
        repository.addAuthenticator(addedAuthenticator);

        WebappAuthenticator retriedAuthenticator = repository.getAuthenticator(addedAuthenticator.getName());
        Assert.assertEquals(addedAuthenticator.getName(), retriedAuthenticator.getName());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddMalformedAuthenticator() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
        WebappAuthenticator malformedAuthenticator = new MalformedAuthenticator();
        repository.addAuthenticator(malformedAuthenticator);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddAuthenticatorWithNull() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
        repository.addAuthenticator(null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddAuthenticatorWithEmptyString() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
        repository.addAuthenticator(null);
    }

}
