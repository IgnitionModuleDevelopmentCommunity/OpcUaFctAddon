package com.bouyguesenergiesservices.opcuafctaddon.gateway;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan.GatewayFctGANHandler;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.GatewayFctManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctGANHandler;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPC;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
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
    private GatewayFctGANHandler service;
    private GatewayFctManager manager;

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
       this.manager = GatewayFctManager.getInstance(gatewayContext);



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

        //init my GAN service
        ServiceManager sm = context.getGatewayAreaNetworkManager().getServiceManager();
        service = new GatewayFctGANHandler(context);
        sm.registerService(IGatewayFctGANHandler.class, service);

        logger.trace("startup()");

    }

    /**
     * Called to shutdown this module.
     *
     * Note that this instance will never be started back up - a new one will be created if a restart is desired
     */
    @Override
    public void shutdown() {

        manager.shutdownAll();

        //Remove GAN Services
        ServiceManager sm = context.getGatewayAreaNetworkManager().getServiceManager();
        sm.unregisterService(IGatewayFctGANHandler.class);

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
        IGatewayFctRPC sessionRPC = manager.getSessionFctRPC(session);
        return sessionRPC;
    }



}
