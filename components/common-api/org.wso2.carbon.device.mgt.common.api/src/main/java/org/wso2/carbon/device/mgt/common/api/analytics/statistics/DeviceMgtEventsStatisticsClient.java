package org.wso2.carbon.device.mgt.common.api.analytics.statistics;/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.api.analytics.statistics.dto.DeviceEventsDTO;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeviceMgtEventsStatisticsClient {

	private static final Log log = LogFactory.getLog(DeviceMgtEventsStatisticsClient.class);

	private static final String DATA_SOURCE_NAME = "jdbc/WSO2DM_STATS_DB";

	private static volatile DataSource dataSource = null;


	public static void initializeDataSource() throws IoTEventsStatisticsException {
		try {
			Context ctx = new InitialContext();
			dataSource = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
		} catch (NamingException e) {
			throw new IoTEventsStatisticsException("Error while looking up the data " +
														  "source: " + DATA_SOURCE_NAME);
		}
	}

	public List<DeviceEventsDTO> getRecentDeviceStats(String owner, int recordLimit)
			throws IoTEventsStatisticsException {

		if (dataSource == null) {
			throw new IoTEventsStatisticsException("BAM data source hasn't been initialized. Ensure that the data source is properly configured in the APIUsageTracker configuration.");
		}

		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			String query = null;
			String table = "DEVICE_EVENTS";

			if(owner==null){
				throw new IoTEventsStatisticsException("Owner cannot be null!");
			}

			String ownerString = "";
			ownerString = String.format(" AND owner = '%s'", owner);

			String limitString = "";
			if(recordLimit > 0){
				limitString = String.format(" LIMIT %d", recordLimit);
			}

			query = String.format("SELECT * FROM %s WHERE 1=1 %s ORDER BY `time` DESC %s"
										  ,table, ownerString, limitString);

			log.info("query: " + query);

			if (query == null) {
				throw new IoTEventsStatisticsException("SQL query is null!");
			}

			List<DeviceEventsDTO> DeviceEventsDTOs = new ArrayList<DeviceEventsDTO>();
			rs = statement.executeQuery(query);
			while (rs.next()) {
				DeviceEventsDTO DeviceEventsDTO = new DeviceEventsDTO();
				DeviceEventsDTO.setTime(rs.getString("TIME"));
				DeviceEventsDTO.setDeviceActivity(rs.getString("ACTIVITY"));
				//(id + type) uniquely identifies a device
				DeviceEventsDTO.setDeviceId(rs.getString("DEVICEID"));
				DeviceEventsDTO.setDeviceType(rs.getString("DEVICETYPE"));

				DeviceEventsDTOs.add(DeviceEventsDTO);

			}

			return DeviceEventsDTOs;

		} catch (Exception e) {
//			throw new IoTEventsStatisticsException(
//					"Error occurred while querying from JDBC database", e);
			//Exception hiding to avoid GC error
			log.error("Error occurred while querying from JDBC database: " + e.getMessage());
			return new ArrayList<>();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {

				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignore) {

				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignore) {

				}
			}
		}
	}

}
