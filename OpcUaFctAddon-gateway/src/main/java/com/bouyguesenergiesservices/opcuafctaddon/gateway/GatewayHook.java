package com.bouyguesenergiesservices.opcuafctaddon.gateway;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.service.GetOPCService;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.service.GetOPCServiceImpl;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;

import com.inductiveautomation.metro.api.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The main Gateway entry point to the remote  OPC module.
 */
public class GatewayHook extends AbstractGatewayModuleHook  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GatewayContext context;
    private GetOPCService getOPCService;
    private GetOPCGatewayFunctions scriptModule;


    /**
     * A free BOUYGUES ENERGIES AND SERVICES module
     *
     * @return boolean - TRUE
     */
    public boolean isFreeModule(){
        return true;
    }

    /**
     * Called to before startup. It create a new ScriptModule and initiate the service through AreaNetwork

     * @param gatewayContext - An interface with the context
     */
    @Override
    public void setup(GatewayContext gatewayContext) {

       logger.trace("setup()");

        this.context = gatewayContext;

        //Creation d'un scriptModule
        scriptModule = new GetOPCGatewayFunctions(context);

        //Service setup
        ServiceManager sm = context.getGatewayAreaNetworkManager().getServiceManager();
        this.getOPCService = new GetOPCServiceImpl(context,scriptModule);
        sm.registerService(GetOPCService.class, getOPCService);
    }

    /**
     * Just to Trace in logger when the Module start
     *
     * @param licenseState - Represents what license state a specific module is in.
     */
    @Override
    public void startup(LicenseState licenseState) {

        logger.trace("startup()");
    }

    /**
     * Called to shutdown this module.
     * - Notify "shutdownGatewayScriptModule" to the script Module
     * - Release all resources from the AreaNetwork
     *
     * Note that this instance will never be started back up - a new one will be created if a restart is desired
     */
    @Override
    public void shutdown() {

        if (scriptModule != null) {
            scriptModule.shutdownGatewayScriptModule();
            logger.trace("shutdown()> shutdownGatewayScriptModule");
        }
        //Remove Services
        ServiceManager sm = context.getGatewayAreaNetworkManager().getServiceManager();
        sm.unregisterService(GetOPCService.class);

       logger.trace("shutdown()");
    }

    /**
     * Initialize a newly-instantiated script manager.
     * This will be called exactly once.
     * Add a ScriptModule instance "system.byes.opc" to manage by the Gateway Instance.
     *
     * @param manager - An available manager create by Gateway
     */
    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);


        manager.addScriptModule(
                "system.byes.opc",
                scriptModule,
                new PropertiesFileDocProvider());

        logger.trace("initializeScriptManager()");

    }

    /**
     * A class whose functions will become exposed automatically through reflection to the Designer and the Client through RPC
     *
     * @param session The session for the calling thread.
     * @param projectId Unique but irreversible hash of the id
     *
     * @return Object functions expose
     */
    @Override
    public Object getRPCHandler(ClientReqSession session, Long projectId) {
        return scriptModule;
    }


}
