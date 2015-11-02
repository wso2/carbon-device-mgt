/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.core.permission.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;

import java.util.StringTokenizer;

/**
 * This class represents a tree data structure which will be used for adding and retrieving permissions.
 */
public class PermissionTree {

    private PermissionNode rootNode;
    private static final String DYNAMIC_PATH_NOTATION = "*";
    private static final String ROOT = "/";
    private static final Log log = LogFactory.getLog(PermissionTree.class);

    public PermissionTree() {
        rootNode = new PermissionNode(ROOT); // initializing the root node.
    }

    /**
     * This method is used to add permissions to the tree. Once it receives the permission
     * it will traverse through the given request path with respect to the permission and place
     * the permission in the appropriate place in the tree.
     *
     * @param permission Permission object.
     */
    public void addPermission(Permission permission) {
        StringTokenizer st = new StringTokenizer(permission.getUrl(), ROOT);
        PermissionNode tempRoot = rootNode;
        PermissionNode tempChild;
        while (st.hasMoreTokens()) {
            tempChild = new PermissionNode(st.nextToken());
            tempRoot = addPermissionNode(tempRoot, tempChild);
        }
        tempRoot.addPermission(permission.getMethod(), permission); //setting permission to the vertex
        if (log.isDebugEnabled()) {
            log.debug("Added permission '" + permission.getName() + "'");
        }
    }

    /**
     * This method is used to add vertex to the graph. The method will check for the given child
     * whether exists within the list of children of the given parent.
     *
     * @param parent Parent PermissionNode.
     * @param child  Child PermissionNode.
     * @return returns the newly created child or the existing child.
     */
    private PermissionNode addPermissionNode(PermissionNode parent, PermissionNode child) {
        PermissionNode existChild = parent.getChild(child.getPathName());
        if (existChild == null) {
            parent.addChild(child);
            return child;
        }
        return existChild;
    }

    /**
     * This method is used to retrieve the permission for a given url and http method.
     * Breath First Search (BFS) is used to traverse the tree.
     *
     * @param url        Request URL.
     * @param httpMethod HTTP method of the request.
     * @return returns the permission with related to the request path or null if there is
     * no any permission that is stored with respected to the given request path.
     */
    public Permission getPermission(String url, String httpMethod) {
        StringTokenizer st = new StringTokenizer(url, ROOT);
        PermissionNode tempRoot;
        PermissionNode currentRoot = rootNode;
        while (st.hasMoreTokens()) {
            String currentToken = st.nextToken();

            // returns the child node which matches with the 'currentToken' path.
            tempRoot = currentRoot.getChild(currentToken);

            // if tempRoot is null, that means 'currentToken' is not matched with the child's path.
            // It means that it is at a point where the request must have dynamic path variables.
            // Therefor it looks for '*' in the request path. ('*' denotes dynamic path variable).
            if (tempRoot == null) {
                tempRoot = currentRoot.getChild(DYNAMIC_PATH_NOTATION);
                // if tempRoot is null, that means there is no any permission which matches with the
                // given path
                if (tempRoot == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Permission for request path '" + url + "' does not exist");
                    }
                    return null;
                }
            }
            currentRoot = tempRoot;
        }
        return currentRoot.getPermission(httpMethod);
    }
}
