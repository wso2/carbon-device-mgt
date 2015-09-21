package org.wso2.carbon.device.mgt.es.extensions.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.es.extensions.CDMFStoreConstants;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.jaggery.scxml.management.DynamicValueInjector;
import org.wso2.jaggery.scxml.management.StateExecutor;
import org.wso2.jaggery.scxml.threading.JaggeryThreadLocalMediator;
import org.wso2.jaggery.scxml.threading.contexts.JaggeryThreadContext;

import java.util.List;
import java.util.Map;

public class PublishDeviceTypeValidator implements Execution {

	private static final Log log = LogFactory.getLog(PublishDeviceTypeValidator.class);

	private PrivilegedCarbonContext ctx;
	private UserRealm userRealm;
	private int tenantId;
	private StateExecutor stateExecutor;

	@Override
	public void init(Map map) {
		this.userRealm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
		this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		this.stateExecutor = new StateExecutor(map);
	}

	/**
	 * This method performs some custom logic during state transitions of an asset. Ideally the
	 * "execute" method returns true to denote the successful execution of this logic. However,
	 * this overridden method also returns false in order to terminate state transitions based on
	 * a condition
	 *
	 * @param requestContext: Contains context data about the transition
	 * @param fromState:      The current lifecycle state of the Asset
	 * @param toState:        The state to which the transition is about to occur
	 * @return: True if the check condition is true and the state transition is to be allowed.
	 * False otherwise.
	 */
	@Override
	public boolean execute(RequestContext requestContext, String fromState, String toState) {

		String resourceID = requestContext.getResource().getUUID();
		String currentSessionUser = CurrentSession.getUser();
		int currentSessionTenantID = CurrentSession.getTenantId();

		String deviceTypeProvider = null;
		String deviceTypeName = null;
		String deviceTypeVersion = null;

		try {

			// Get the current registry
			Registry registry =
					RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(
							currentSessionUser, currentSessionTenantID);

			// Load Govenance Artifacts
			GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
			GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
																				CDMFStoreConstants
																						.DEVICE_RXT_NAME);

			GenericArtifact deviceTypeArtifact = artifactManager.getGenericArtifact(resourceID);

			deviceTypeProvider = deviceTypeArtifact.getAttribute("overview_provider");
			deviceTypeName = deviceTypeArtifact.getAttribute("overview_name");
			deviceTypeVersion = deviceTypeArtifact.getAttribute("overview_version");

			PrivilegedCarbonContext.startTenantFlow();
			this.ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
			this.ctx.setTenantId(tenantId, true);

			DeviceManagementProviderService dmService =
					(DeviceManagementProviderService) ctx.getOSGiService(
							DeviceManagementProviderService.class, null);

			List<DeviceType> registeredDeviceTypeList = dmService.getDeviceTypes();

			PrivilegedCarbonContext.endTenantFlow();

			for (DeviceType deviceType : registeredDeviceTypeList) {
				String dTypeNameInIteration = deviceType.getName();
				if (dTypeNameInIteration.equals(deviceTypeName)) {
					if (log.isDebugEnabled()) {
						log.debug("Device type: " + deviceTypeName +
										  " is a registered type and can be Promoted to " +
										  "lifecycle" +
										  " " +
										  "state: " +
										  toState);
					}
					return true;
				}
			}

		} catch (RegistryException e) {
			log.error(
					"An error occured whilst loading artifact details from the registry for " +
							"User: " + currentSessionUser + " with TenantID: " +
							currentSessionTenantID);
			return false;
		} catch (DeviceManagementException e) {
			log.error("An error occured whilst retreiving device-type details from the database");
			return false;
		}

		if (log.isDebugEnabled()) {
			log.debug("Device type: " + deviceTypeName +
							  " is a not registered in CDMF and cannot be Promoted to lifecycle " +
							  "state: " +
							  toState +
							  ".\n Ensure that the device-type plugin has been implemented and " +
							  "deployed into CDMF");
		}

		return false;
	}


//	/**
//	 * This method could be used to force life-cycle state transitions by skipping the normal flow
//	 * of the transitions.
//	 *
//	 * @param requestContext: Contains context data about the transition
//	 * @param fromState:      The current lifecycle state of the Asset
//	 * @param toState:        The state to which the transition is to occur
//	 */
//	private void allowLifecycleStateTransition(RequestContext requestContext, String fromState,
//											   String toState) {
//		JaggeryThreadContext jaggeryThreadContext = new JaggeryThreadContext();
//
//		//The path of the asset
//		String pathToAsset = requestContext.getResource().getPath();
//
//		//Used to inject asset specific information to a permission instruction
//		DynamicValueInjector dynamicValueInjector = new DynamicValueInjector();
//
//		boolean isEmailEnabled = Boolean.parseBoolean(
//				CarbonUtils.getServerConfiguration().getFirstProperty("EnableEmailUserName"));
//		String provider = requestContext.getResource().getAuthorUserName();
////        TODO: Check email enabled case and remove or uncomment the following
////            if (provider != null && !isEmailEnabled && provider.contains("-AT-")) {
////                provider = provider.substring(0, provider.indexOf("-AT-"));
////
////            }
//
//		//Set the asset author key
//		dynamicValueInjector.setDynamicValue(DynamicValueInjector.ASSET_AUTHOR_KEY, provider);
//
//		//Execute all permissions for the current state
//		//this.stateExecutor.executePermissions(this.userRealm,dynamicValueInjector,path,s2);
//
//		jaggeryThreadContext.setFromState(fromState);
//		jaggeryThreadContext.setToState(toState);
//		jaggeryThreadContext.setAssetPath(pathToAsset);
//		jaggeryThreadContext.setDynamicValueInjector(dynamicValueInjector);
//		jaggeryThreadContext.setUserRealm(userRealm);
//		jaggeryThreadContext.setStateExecutor(stateExecutor);
//
//		JaggeryThreadLocalMediator.set(jaggeryThreadContext);
//	}

}
