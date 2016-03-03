/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cdancy.artifactory.rest.features;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.cdancy.artifactory.rest.BaseArtifactoryApiLiveTest;
import com.cdancy.artifactory.rest.domain.artifact.Artifact;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.jclouds.io.Payloads;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "live", testName = "ArtifactApiLiveTest")
public class ArtifactApiLiveTest extends BaseArtifactoryApiLiveTest {

    private File tempArtifact;
    private String repoKey = "libs-snapshot-local";
    private String itemPath;
    private String itemPathWithProperties;
    private Map<String, String> itemProperties = new HashMap<String, String>();
    private List<File> filesToDelete = new CopyOnWriteArrayList<File>();

    @BeforeClass
    public void testInitialize() {
        tempArtifact = randomFile();
        itemPath = randomPath();
        itemPathWithProperties = randomPath();
        itemProperties.put("key1", "value1");
        itemProperties.put("key2", "value2");
        itemProperties.put("key3", "value3");
    }

    @Test
    public void testDeployArtifact() {
        Artifact artifact = api().deployArtifact(repoKey, itemPath + "/" + tempArtifact.getName(),
            Payloads.newPayload(tempArtifact), null);
        assertNotNull(artifact);
        assertTrue(artifact.repo().equals(repoKey));
    }

    @Test (dependsOnMethods = "testDeployArtifact")
    public void testRetrieveArtifact() {
        File tempFile = null;
        try {
            tempFile = api().retrieveArtifact(repoKey, itemPath + "/" + tempArtifact.getName(), null);
            assertNotNull(tempFile);
            assertTrue(tempFile.exists());
            filesToDelete.add(tempFile);

            String expectedText = FileUtils.readFileToString(tempArtifact);
            String randomFileText = FileUtils.readFileToString(tempFile);
            assertTrue(expectedText.equals(randomFileText));
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Test(dependsOnMethods = "testRetrieveArtifact")
    public void testDeleteArtifact() {
      assertTrue(api().deleteArtifact(repoKey, itemPath));
    }

    @Test
    public void testDeployArtifactWithProperties() {
        Artifact artifact = api().deployArtifact(repoKey, itemPathWithProperties + "/" + tempArtifact.getName(),
                Payloads.newPayload(tempArtifact), itemProperties);
        assertNotNull(artifact);
        assertTrue(artifact.repo().equals(repoKey));
    }

    @Test(dependsOnMethods = "testDeployArtifactWithProperties")
    public void testRetrieveArtifactWithProperties() {
        File tempFile = null;
        try {
            tempFile = api().retrieveArtifact(repoKey, itemPathWithProperties + "/" + tempArtifact.getName(), itemProperties);
            assertNotNull(tempFile);
            assertTrue(tempFile.exists());
            filesToDelete.add(tempFile);

            String expectedText = FileUtils.readFileToString(tempArtifact);
            String randomFileText = FileUtils.readFileToString(tempFile);
            assertTrue(expectedText.equals(randomFileText));
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Test (dependsOnMethods = "testRetrieveArtifactWithProperties")
    public void testRetrieveArtifactWithIllegalPropertyValue() {
        File tempFile = null;
        try {
            Map<String, String> illegalPropertyValues = new HashMap<String, String>(itemProperties);
            illegalPropertyValues.put("key1", "HelloWorld");
            File inputStream = api().retrieveArtifact(repoKey, itemPathWithProperties + "/" + tempArtifact.getName(), illegalPropertyValues);
            assertNull(inputStream);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Test(dependsOnMethods = "testRetrieveArtifactWithIllegalPropertyValue")
    public void testDeleteArtifactWithProperties() {
        assertTrue(api().deleteArtifact(repoKey, itemPathWithProperties));
    }

    @Test
    public void testDeleteNonExistentArtifact() {
      assertFalse(api().deleteArtifact(repoKey, randomPath()));
    }

    @Test
    public void testRetrieveNonExistentArtifact() {
        try {
            File inputStream = api().retrieveArtifact(repoKey, randomPath() + ".txt", null);
            assertNull(inputStream);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @AfterClass
    public void testFinalize() {
        for (File file : filesToDelete) {
            if (file != null && file.exists()) {
                FileUtils.deleteQuietly(file.getParentFile());
            }
        }
        assertTrue(tempArtifact.delete());
        assertTrue(api().deleteArtifact(repoKey, itemPath.split("/")[0]));
        assertTrue(api().deleteArtifact(repoKey, itemPathWithProperties.split("/")[0]));
    }

    private ArtifactApi api() {
      return api.artifactApi();
    }
}