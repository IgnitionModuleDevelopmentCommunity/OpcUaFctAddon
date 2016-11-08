package com.bouyguesenergiesservices.gateway;

import com.bouyguesenergiesservices.gateway.service.GetOPCService;
import com.bouyguesenergiesservices.gateway.service.GetOPCServiceImpl;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;

import com.inductiveautomation.metro.api.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class GatewayHook extends AbstractGatewayModuleHook  {

    public static final String TASK_OWNERID = "remoteOPC";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;
    private GetOPCRPCImpl rpc;
    private GetOPCService getOPCService;
    private GetOPCGatewayFunctions scriptModule;


    @Override
    public void setup(GatewayContext gatewayContext) {
        logger.debug("setup()");
        this.context = gatewayContext;
        this.rpc = new GetOPCRPCImpl(context); // Declaration Interface RPC OPC

        //Creation d'un scriptModule
        scriptModule = new GetOPCGatewayFunctions(context.getOPCManager(), context.getExecutionManager(),rpc);

        //Service setup
        ServiceManager sm = context.getGatewayAreaNetworkManager().getServiceManager();
        this.getOPCService = new GetOPCServiceImpl(context,scriptModule);
        sm.registerService(GetOPCService.class, getOPCService);


    }

//TODO : A voir la Creation d'un scriptModule STARTUP DU HOOK
    @Override
    public void startup(LicenseState licenseState) {
        logger.info("startup()");
    }

    @Override
    public void shutdown() {

        if (scriptModule != null) {
            scriptModule.shutdownGatewayScriptModule();
            logger.debug("shutdownGatewayScriptModule()");
        }
        //Remove Services
        ServiceManager sm = context.getGatewayAreaNetworkManager().getServiceManager();
        sm.unregisterService(GetOPCService.class);

        logger.info("shutdown()");
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);


        manager.addScriptModule(
                "system.byes.opc",
                scriptModule,
                new PropertiesFileDocProvider());

        logger.debug("initializeScriptManager()");

    }

    @Override
    public Object getRPCHandler(ClientReqSession session, Long projectId) {
        return scriptModule;
    }


}
