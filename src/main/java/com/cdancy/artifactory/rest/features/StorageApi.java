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

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.cdancy.artifactory.rest.binders.BindListPropertiesToPath;
import com.cdancy.artifactory.rest.binders.BindMapPropertiesToPath;
import com.cdancy.artifactory.rest.domain.artifact.Artifact;
import com.cdancy.artifactory.rest.domain.storage.FileList;
import com.cdancy.artifactory.rest.domain.storage.StorageInfo;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.Fallbacks;
import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.rest.annotations.*;

import com.cdancy.artifactory.rest.filters.ArtifactoryAuthenticationFilter;

import java.util.List;
import java.util.Map;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@RequestFilters(ArtifactoryAuthenticationFilter.class)
public interface StorageApi {

    @Named("storage:set-item-properties")
    @Path("/storage/{repoKey}/{itemPath}")
    @Fallback(FalseOnNotFoundOr404.class)
    @QueryParams(keys = { "recursive" }, values = { "1" })
    @PUT
    boolean setItemProperties(@PathParam("repoKey") String repoKey,
                              @PathParam("itemPath") String itemPath,
                              @BinderParam(BindMapPropertiesToPath.class) Map<String, List<String>> properties);

    @Named("storage:get-item-properties")
    @Path("/storage/{repoKey}/{itemPath}")
    @QueryParams(keys = { "properties" })
    @Fallback(Fallbacks.EmptyMapOnNotFoundOr404.class)
    @SelectJson({"properties"})
    @GET
    Map<String, List<String>> getItemProperties(@PathParam("repoKey") String repoKey,
                                                @PathParam("itemPath") String itemPath);

    @Named("storage:delete-item-properties")
    @Path("/storage/{repoKey}/{itemPath}")
    @Fallback(FalseOnNotFoundOr404.class)
    @QueryParams(keys = { "recursive" }, values = { "1" })
    @DELETE
    boolean deleteItemProperties(@PathParam("repoKey") String repoKey,
                                 @PathParam("itemPath") String itemPath,
                                 @BinderParam(BindListPropertiesToPath.class) List<String> properties);

    @Named("storage:info")
    @Path("/storageinfo")
    @GET
    StorageInfo storageInfo();

    @Named("storage:file-list")
    @Path("/storage/{repoKey}/{itemPath}")
    @QueryParams(keys = { "list" }, values = { "true" })
    @GET
    FileList fileList(@PathParam("repoKey") String repoKey,
                     @PathParam("itemPath") String itemPath,
                     @Nullable @QueryParam("deep") Integer deep,
                     @Nullable @QueryParam("depth") Integer depth,
                     @Nullable @QueryParam("listFolders") Integer listFolders,
                     @Nullable @QueryParam("includeRootPath") Integer includeRootPath);

    @Named("storage:file-info")
    @Path("/storage/{repoKey}/{itemPath}")
    @Fallback(Fallbacks.NullOnNotFoundOr404.class)
    @GET
    Artifact fileInfo(@PathParam("repoKey") String repoKey,
                      @PathParam("itemPath") String itemPath);
}
