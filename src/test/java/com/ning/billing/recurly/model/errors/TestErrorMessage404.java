/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.recurly.model.errors;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.recurly.model.TestModelBase;

public class TestErrorMessage404  extends TestModelBase {

    @Test(groups = "fast")
    public void testDeserialization() throws Exception {
        //See https://docs.recurly.com/api/basics/status-codes
        final String errorData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<error>\n" + 
                "  <symbol>not_found</symbol>\n" + 
                "  <description>The record could not be located.</description>\n" + 
                "</error>\n";

        ErrorMessage404 errorMessage404 = xmlMapper.readValue(errorData, ErrorMessage404.class);

        Assert.assertNotNull(errorMessage404);
        Assert.assertEquals(errorMessage404.getDescription(), "The record could not be located.");
        Assert.assertEquals(errorMessage404.getSymbol(), "not_found");
    }
}
