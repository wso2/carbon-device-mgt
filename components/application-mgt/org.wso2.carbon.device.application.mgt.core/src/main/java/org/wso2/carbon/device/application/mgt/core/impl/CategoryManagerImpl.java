/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.impl;

import org.wso2.carbon.device.application.mgt.common.Category;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationCategoryManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.CategoryManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.util.List;

/**
 * This class is the default implementation for the CategoryManager.
 *
 */
public class CategoryManagerImpl implements CategoryManager {

    @Override
    public Category createCategory(Category category) throws ApplicationManagementException {
        if (category == null) {
            throw new ApplicationCategoryManagementException("Category is null. Cannot create a category.");
        }
        if (category.getName() == null) {
            throw new ApplicationCategoryManagementException(
                    "Application category name cannot be null. Application category creation failed.");
        }
        if (getCategory(category.getName()) != null) {
            throw new ApplicationCategoryManagementException(
                    "Application category wth the name " + category.getName() + " "
                            + "exists already. Please select a different name");
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Category createdCategory = DAOFactory.getCategoryDAO().addCategory(category);
            ConnectionManagerUtil.commitDBTransaction();
            return createdCategory;
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<Category> getCategories() throws ApplicationManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return DAOFactory.getCategoryDAO().getCategories();
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Category getCategory(String name) throws ApplicationManagementException {
        if (name == null || name.isEmpty()) {
            throw new ApplicationCategoryManagementException("Name cannot be empty or null. Cannot get category");
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return DAOFactory.getCategoryDAO().getCategory(name);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteCategory(String name) throws ApplicationManagementException {
        Category category = getCategory(name);
        if (category == null) {
            throw new NotFoundException(
                    "Category with the name '" + name + "' not found. Cannot delete the " + "non-existing category");
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            boolean isApplicationExistForCategory = DAOFactory.getApplicationDAO().isApplicationExist(name);
            if (isApplicationExistForCategory) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationCategoryManagementException(
                        "Cannot delete the the category " + name + ". Applications " + "exists for this category");
            }
            DAOFactory.getCategoryDAO().deleteCategory(name);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }
}
