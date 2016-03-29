/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.device.mgt.core.search.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.search.Condition;
import org.wso2.carbon.device.mgt.core.search.mgt.Constants;
import org.wso2.carbon.device.mgt.core.search.mgt.InvalidOperatorException;
import org.wso2.carbon.device.mgt.core.search.mgt.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilderImpl implements QueryBuilder {


    private static final Log log = LogFactory.getLog(QueryBuilderImpl.class);

    @Override
    public Map<String, List<String>> buildQueries(List<Condition> conditions) throws InvalidOperatorException {

        List<Condition> andColumns = new ArrayList<>();
        List<Condition> orColumns = new ArrayList<>();
        List<Condition> otherANDColumns = new ArrayList<>();
        List<Condition> otherORColumns = new ArrayList<>();
        Condition locConditon = new Condition();

        if (conditions.size() == 1) {

            if (conditions.get(0).getKey().equalsIgnoreCase(Constants.LOCATION)) {
                locConditon = conditions.get(0);
            } else if (Utils.getDeviceDetailsColumnNames().containsKey(conditions.get(0)) ||
                    Utils.getDeviceLocationColumnNames().containsKey(conditions.get(0))) {
                andColumns.add(conditions.get(0));
            } else {
                otherANDColumns.add(conditions.get(0));
            }
        } else {
            for (Condition con : conditions) {
                if (con.getKey().equalsIgnoreCase(Constants.LOCATION)) {
                    locConditon = con;
                } else if (Utils.getDeviceDetailsColumnNames().containsKey(con.getKey()) ||
                        Utils.getDeviceLocationColumnNames().containsKey(con.getKey())) {
                    if (con.getState().equals(Condition.State.AND)) {
                        andColumns.add(con);
                    } else if (con.getState().equals(Condition.State.OR)) {
                        orColumns.add(con);
                    } else {
                        throw new InvalidOperatorException(con.getState() + " is not a valid operator.");
                    }
                } else {
                    if (con.getState().equals(Condition.State.AND)) {
                        otherANDColumns.add(con);
                    } else if (con.getState().equals(Condition.State.OR)) {
                        otherORColumns.add(con);
                    } else {
                        throw new InvalidOperatorException(con.getState() + " is not a valid operator.");
                    }
                }
            }
        }

        Map<String, List<String>> queries = new HashMap<>();
        queries.put(Constants.GENERAL, Utils.convertStringToList(this.getGenericQueryPart() + this.processAND(andColumns) +
                this.processOR(orColumns)));
        queries.put(Constants.PROP_AND, this.processANDProperties(otherANDColumns));
        queries.put(Constants.PROP_OR, this.processORProperties(otherORColumns));
        queries.put(Constants.LOCATION, this.processLocation(locConditon));

        if (log.isDebugEnabled()) {
            log.debug("General Query : " + queries.get(Constants.GENERAL));
            log.debug("Property with AND Query : " + queries.get(Constants.PROP_AND));
            log.debug("Property with OR Query : " + queries.get(Constants.PROP_OR));
            log.debug("Location related Query : " + queries.get(Constants.LOCATION));
        }

        return queries;
    }

    @Override
    public String processAND(List<Condition> conditions) throws InvalidOperatorException {

        String querySuffix = "";

        for (Condition con : conditions) {
            if (Utils.getDeviceDetailsColumnNames().containsKey(con.getKey())) {
                querySuffix = querySuffix + " AND DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey()) +
                        con.getOperator() + con.getValue();
            } else if (Utils.getDeviceLocationColumnNames().containsKey(con.getKey())) {
                querySuffix = querySuffix + " AND DL." + Utils.getDeviceLocationColumnNames().get(con.getKey()) +
                        con.getOperator() + con.getValue();
            }
        }

        return querySuffix;
    }

    @Override
    public String processOR(List<Condition> conditions) throws InvalidOperatorException {

        String querySuffix = "";

        for (Condition con : conditions) {
            if (Utils.getDeviceDetailsColumnNames().containsKey(con.getKey())) {
                querySuffix = querySuffix + " OR DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey()) +
                        con.getOperator() + con.getValue();
            } else if (Utils.getDeviceLocationColumnNames().containsKey(con.getKey())) {
                querySuffix = querySuffix + " OR DL." + Utils.getDeviceLocationColumnNames().get(con.getKey()) +
                        con.getOperator() + con.getValue();
            }
        }
        return querySuffix;
    }

    @Override
    public List<String> processLocation(Condition condition) throws InvalidOperatorException {
        List<String> queryList = new ArrayList<>();
        queryList.add(this.buildLocationQuery(condition.getValue()));
        return queryList;
    }

    @Override
    public List<String> processANDProperties(List<Condition> conditions) throws InvalidOperatorException {
        return this.getQueryList(conditions);
    }

    @Override
    public List<String> processORProperties(List<Condition> conditions) throws InvalidOperatorException {
        return this.getQueryList(conditions);
    }

    private List<String> getQueryList(List<Condition> conditions) {
        List<String> queryList = new ArrayList<>();
        for (Condition con : conditions) {

            String querySuffix = this.getPropertyQueryPart() + " AND DI.KEY_FIELD = " + "\'" + con.getKey() + "\'" +
                    " AND DI.VALUE_FIELD " + con.getOperator() + "\'" + con.getValue() + "\'";
            queryList.add(querySuffix);
        }
        return queryList;
    }

    private String buildLocationQuery(String location) {

        String query = this.getGenericQueryPart();
        query = query + " OR STREET1 LIKE \'%" + location + "%\'";
        query = query + " OR STREET2 LIKE \'%" + location + "%\'";
        query = query + " OR CITY LIKE \'%" + location + "%\'";
        query = query + " OR STATE LIKE \'%" + location + "%\'";
        query = query + " OR COUNTRY LIKE \'%" + location + "%\'";
        query = query + " OR ZIP LIKE \'%" + location + "%\'";
        return query;
    }

    private String getGenericQueryPart() {

        return "SELECT D.ID, D.DESCRIPTION, D.NAME,  \n" +
                "  D.DEVICE_TYPE_ID, D.DEVICE_IDENTIFICATION,  DT.ID AS DEVICE_TYPE_ID, \n" +
                "DT.NAME AS DEVICE_TYPE_NAME, DD.DEVICE_ID, DD.IMEI, DD.IMSI, DD.DEVICE_MODEL, DD.VENDOR, \n" +
                "DD.OS_VERSION, DD.BATTERY_LEVEL, DD.INTERNAL_TOTAL_MEMORY, DD.INTERNAL_AVAILABLE_MEMORY,\n" +
                "DD.EXTERNAL_TOTAL_MEMORY, DD.EXTERNAL_AVAILABLE_MEMORY, DD.OPERATOR, DD.CONNECTION_TYPE, \n" +
                "DD.MOBILE_SIGNAL_STRENGTH, DD.SSID, DD.CPU_USAGE, DD.TOTAL_RAM_MEMORY, DD.AVAILABLE_RAM_MEMORY, \n" +
                "DD.PLUGGED_IN, DL.LATITUDE, DL.LONGITUDE, DL.STREET1, DL.STREET2, DL.CITY, DL.ZIP, \n" +
                "DL.STATE, DL.COUNTRY FROM DM_DEVICE_DETAIL AS DD, DM_DEVICE AS D, DM_DEVICE_LOCATION AS DL, " +
                "DM_DEVICE_TYPE AS DT WHERE D.TENANT_ID = " +
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();


    }

    private String getPropertyQueryPart() {

        return "SELECT D.ID, D.DESCRIPTION, D.NAME,  \n" +
                " D.DEVICE_TYPE_ID, D.DEVICE_IDENTIFICATION,  DT.ID AS DEVICE_TYPE_ID, \n" +
                "DT.NAME AS DEVICE_TYPE_NAME, DD.DEVICE_ID, DD.IMEI, DD.IMSI, DD.DEVICE_MODEL, DD.VENDOR, \n" +
                "DD.OS_VERSION, DD.BATTERY_LEVEL, DD.INTERNAL_TOTAL_MEMORY, DD.INTERNAL_AVAILABLE_MEMORY,\n" +
                "DD.EXTERNAL_TOTAL_MEMORY, DD.EXTERNAL_AVAILABLE_MEMORY, DD.OPERATOR, DD.CONNECTION_TYPE, \n" +
                "DD.MOBILE_SIGNAL_STRENGTH, DD.SSID, DD.CPU_USAGE, DD.TOTAL_RAM_MEMORY, DD.AVAILABLE_RAM_MEMORY, \n" +
                "DD.PLUGGED_IN, DL.LATITUDE, DL.LONGITUDE, DL.STREET1, DL.STREET2, DL.CITY, DL.ZIP, \n" +
                "DL.STATE, DL.COUNTRY, DI.KEY_FIELD, DI.VALUE_FIELD FROM DM_DEVICE_DETAIL AS DD, " +
                "DM_DEVICE AS D, DM_DEVICE_LOCATION AS DL, \n" +
                "DM_DEVICE_INFO AS DI, DM_DEVICE_TYPE AS DT WHERE D.TENANT_ID = " +
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

    }
}
