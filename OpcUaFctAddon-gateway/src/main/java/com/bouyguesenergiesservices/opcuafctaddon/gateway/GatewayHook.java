package com.bouyguesenergiesservices.opcuafctaddon.gateway;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.GatewayFctManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.IGatewayFctRPCManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPCBase;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The main Gateway entry point to the remote  OPC module.
 */
public class GatewayHook extends AbstractGatewayModuleHook  {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;

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

       this.context = gatewayContext;

       logger.trace("setup()");
    }

    /**
     * Just to Trace in logger when the Module start
     * And release may be orphan subscriptions
     *
     * @param licenseState - Represents what license state a specific module is in.
     */
    @Override
    public void startup(LicenseState licenseState) {

        //Reinit subscription All (Local / GAN)  GatewayFct
        GatewayFctManager.getInstance(context).allUnsubscribe();

        logger.trace("startup()");

    }

    /**
     * Called to shutdown this module.
     *
     * Note that this instance will never be started back up - a new one will be created if a restart is desired
     */
    @Override
    public void shutdown() {

        //Shutdown All (Local / GAN)  GatewayFct
        GatewayFctManager.getInstance(context).shutdown();

        logger.trace("shutdown()");
    }

    /**
     * Initialize a newly-instantiated script manager.
     * This will be called exactly once.
     *
     * @param manager - An available manager create by Gateway
     */
    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);

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
        //Create a session just for this client in the gateway context
        IGatewayFctRPCManager managerRPC = GatewayFctManager.getInstance(context);
        IGatewayFctRPCBase sessionRPC = managerRPC.getSessionFctRPC(session);
        return sessionRPC;
    }



}
