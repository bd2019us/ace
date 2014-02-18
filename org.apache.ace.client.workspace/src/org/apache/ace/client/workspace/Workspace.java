/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ace.client.workspace;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.ace.client.repository.ObjectRepository;
import org.apache.ace.client.repository.RepositoryObject;
import org.apache.ace.client.repository.object.Artifact2FeatureAssociation;
import org.apache.ace.client.repository.object.ArtifactObject;
import org.apache.ace.client.repository.object.Distribution2TargetAssociation;
import org.apache.ace.client.repository.object.DistributionObject;
import org.apache.ace.client.repository.object.Feature2DistributionAssociation;
import org.apache.ace.client.repository.object.FeatureObject;
import org.apache.ace.client.repository.stateful.StatefulTargetObject;
import org.osgi.service.useradmin.User;

/**
 * Workspace represents the modifiable client-side state of an ACE repository. It facilitates a workflow whereby a
 * repository can be checked out, queried, modified and committed back to the server.
 * <p>
 * Workspace has a generic API based on RepositoryObjects and their associations, as well as a more specific one dealing
 * with resource processors, artifacts, features, distributions and targets. The latter is mostly intended for
 * scripting, hence the shorthand notation of its method names:
 * <p>
 * Command syntax, first character is the "operation", then the "entity type" or "association". Note: not all
 * combinations exist.<br>
 * Operations: [c]reate, [l]ist, [d]elete, [u]pdate<br>
 * Entities: [a]rtifact, [f]eature, [d]istribution, [t]arget<br>
 * Associations: [a2f], [f2d], [d2t]<br>
 * <p>
 * Workspace objects are most commonly obtained from a WorkspaceManager acting on behalf of the client.
 * 
 * @see ObjectRepository
 * @see WorkspaceManager
 */
public interface Workspace {
    static final String ARTIFACT = "artifact";
    static final String ARTIFACT2FEATURE = "artifact2feature";
    static final String FEATURE = "feature";
    static final String FEATURE2DISTRIBUTION = "feature2distribution";
    static final String DISTRIBUTION = "distribution";
    static final String DISTRIBUTION2TARGET = "distribution2target";
    static final String TARGET = "target";

    /**
     * @return the session ID of this workspace, never <code>null</code>.
     */
    public String getSessionID();

    /**
     * Login to the repository as the specified User.
     * 
     * Note that if the login was not successful (as indicated by the return value of this method), modifying the state
     * of the Workspace and then committing it may not work as intended.
     * 
     * @param user
     *            the user
     * @return true if the login was successful, false otherwise
     */
    public boolean login(User user);

    /**
     * Checkout the latest the state of the repository into this Workspace.
     * 
     * @throws IOException
     *             in case of I/O problems.
     */
    public void checkout() throws IOException;

    /**
     * Commit the current state of this Workspace to the repository.
     * 
     * @throws IOException
     *             in case of I/O problems.
     */
    public void commit() throws IOException;

    /**
     * Logout from the repository.
     * 
     * Only has any effect if there was a successful login earlier.
     * 
     * @throws IOException
     *             in case of I/O problems.
     */
    public void logout() throws IOException;

    /**
     * Create a new RepositoryObject with the specified characteristics.
     * 
     * @param entityType
     *            the type of the RepositoryObject
     * @param attributes
     *            the attributes of the RepositoryObject
     * @param tags
     *            the tags of the RepositoryObject
     * @return a new RepositoryObject with the specified characteristics
     * @throws IllegalArgumentException
     *             when the ObjectRepository this Workspace represents would throw this same exception
     */
    public RepositoryObject createRepositoryObject(String entityType, Map<String, String> attributes,
            Map<String, String> tags) throws IllegalArgumentException;

    /**
     * @param entityType
     *            the type of the RepositoryObject
     * @param entityId
     *            the identifier of the RepositoryObject
     * @return the RepositoryObject for the specified type and identifier
     */
    public RepositoryObject getRepositoryObject(String entityType, String entityId);

    /**
     * @param entityType
     *            the type of RepositoryObjects to get
     * @return all RepositoryObjects of the specified type. Might be empty, will not be <code>null</code>
     */
    public List<RepositoryObject> getRepositoryObjects(String entityType);

    /**
     * Update the RepositoryObject for the specified type and identifier with the specified attributes and tags.
     * 
     * The result of this call is that the specified RepositoryObject has only the specified attributes and tags.
     * 
     * @param entityType
     *            the type of the RepositoryObject
     * @param entityId
     *            the identifier of the RepositoryObject
     * @param attributes
     *            the attributes of the RepositoryObject
     * @param tags
     *            the tags of the RepositoryObject
     */
    public void updateRepositoryObject(String entityType, String entityId, Map<String, String> attributes,
            Map<String, String> tags);

    /**
     * Create an association of the specified between a left-hand side object and a right-hand side object with the
     * specified cardinalities.
     * 
     * @param entityType
     *            the association type
     * @param leftEntityId
     *            the identifier of the left-hand side object
     * @param rightEntityId
     *            the identifier of the right-hand side object
     * @param leftCardinality
     *            the cardinality of the left-hand side
     * @param rightCardinality
     *            the cardinality of the right-hand side
     */
    public void createAssocation(String entityType, String leftEntityId, String rightEntityId, String leftCardinality,
            String rightCardinality);

    /**
     * Get the RepositoryObject that represents the left-hand side of the specified association.
     * 
     * For example, in an association linking an Artifact to a Feature, a Feature object would be the right-hand side
     * 
     * @param entityType
     *            the association type
     * @param entityId
     *            the object identifier (note: not the association identifier)
     * @return the left-hand side of the specified association, or null if no such object could be found
     */
    public RepositoryObject getLeft(String entityType, String entityId);

    /**
     * Get the RepositoryObject that represents the right-hand side of the specified association.
     * 
     * For example, in an association linking an Artifact to a Feature, a Feature object would be the right-hand side
     * 
     * @param entityType
     *            the association type
     * @param entityId
     *            the object identifier (note: not the association identifier)
     * @return the right-hand side of the specified association, or null if no such object could be found
     */
    public RepositoryObject getRight(String entityType, String entityId);

    /**
     * Remove the RepositoryObject with the specified type and identifier.
     * 
     * @param entityType
     *            the object type
     * @param entityId
     *            the object indentifier
     */
    public void deleteRepositoryObject(String entityType, String entityId);

    /*** resource processors ***/

    public List<ArtifactObject> lrp();

    public List<ArtifactObject> lrp(String filter) throws Exception;

    /*** artifact ***/

    public List<ArtifactObject> la();

    public List<ArtifactObject> la(String filter) throws Exception;

    public void ca(String name, String url, String bsn, String version);

    public void ca(Map<String, String> attrs);

    public void ca(Map<String, String> attrs, Map<String, String> tags);

    public void da(RepositoryObject repositoryObject);

    /*** artifact to feature association ***/

    public List<Artifact2FeatureAssociation> la2f();

    public List<Artifact2FeatureAssociation> la2f(String filter) throws Exception;

    public void ca2f(String left, String right);

    public void ca2f(String left, String right, String leftCardinality, String rightCardinalty);

    public void da2f(Artifact2FeatureAssociation repositoryObject);

    /*** feature ***/

    public List<FeatureObject> lf();

    public List<FeatureObject> lf(String filter) throws Exception;

    public void cf(String name);

    public void cf(Map<String, String> attrs);

    public void cf(Map<String, String> attrs, Map<String, String> tags);

    public void df(FeatureObject repositoryObject);

    /*** feature to distribution association ***/

    public List<Feature2DistributionAssociation> lf2d();

    public List<Feature2DistributionAssociation> lf2d(String filter) throws Exception;

    public void cf2d(String left, String right);

    public void cf2d(String left, String right, String leftCardinality, String rightCardinalty);

    public void df2d(Feature2DistributionAssociation repositoryObject);

    /*** distribution ***/

    public List<DistributionObject> ld();

    public List<DistributionObject> ld(String filter) throws Exception;

    public void cd(String name);

    public void cd(Map<String, String> attrs);

    public void cd(Map<String, String> attrs, Map<String, String> tags);

    public void dd(DistributionObject repositoryObject);

    /*** distribution to target association ***/

    public List<Distribution2TargetAssociation> ld2t();

    public List<Distribution2TargetAssociation> ld2t(String filter) throws Exception;

    public void cd2t(String left, String right);

    public void cd2t(String left, String right, String leftCardinality, String rightCardinalty);

    public void dd2t(Distribution2TargetAssociation repositoryObject);

    /*** target ***/

    public List<StatefulTargetObject> lt();

    public List<StatefulTargetObject> lt(String filter) throws Exception;

    public RepositoryObject ct(String name);

    public RepositoryObject ct(Map<String, String> attrs);

    public RepositoryObject ct(Map<String, String> attrs, Map<String, String> tags);

    public void dt(StatefulTargetObject repositoryObject);

    /**
     * Approves a given stateful target object.
     * 
     * @param targetObject
     *            the target object to approve, cannot be <code>null</code>.
     * @return the approved stateful target object, cannot be <code>null</code>.
     */
    public StatefulTargetObject approveTarget(StatefulTargetObject targetObject);

    /**
     * Registers a given stateful target object.
     * 
     * @param targetObject
     *            the target object to register, cannot be <code>null</code>.
     * @return the registered stateful target object, can be <code>null</code> only if the given target object is
     *         already registered.
     */
    public StatefulTargetObject registerTarget(StatefulTargetObject targetObject);

    /*** other/generic ***/

    public void cas(String entityType, String leftEntityId, String rightEntityId, String leftCardinality,
            String rightCardinality);

    public boolean isModified() throws IOException;

    public boolean isCurrent() throws IOException;

}