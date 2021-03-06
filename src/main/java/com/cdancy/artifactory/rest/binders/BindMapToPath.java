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
package com.cdancy.artifactory.rest.binders;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpRequest.Builder;
import org.jclouds.rest.Binder;

import com.cdancy.artifactory.rest.ArtifactoryUtils;

@Singleton
public class BindMapToPath implements Binder {
   @SuppressWarnings("unchecked")
   @Override
   public <R extends HttpRequest> R bindToRequest(final R request, final Object properties) {

      checkArgument(properties instanceof Map, "binder is only valid for Map");
      Map<String, List<String>> props = (Map<String, List<String>>) properties;
      checkArgument(props.size() > 0, "properties Map cannot be empty");

      Builder<?> builder = request.toBuilder();
      for (Map.Entry<String, List<String>> prop : props.entrySet()) {
         String potentialKey = prop.getKey().trim();
         if (potentialKey.length() > 0) {
            String potentialValue = ArtifactoryUtils.collectionToString(prop.getValue(), ",");
            String encodedValue = "";
            try {
               if (potentialValue != null)
                  encodedValue = potentialValue.replaceAll(" ", "%20");
            } catch (Exception e) {
               encodedValue = potentialValue;
            }
            builder.addQueryParam(potentialKey, encodedValue);
         }
      }

      return (R) builder.build();
   }
}