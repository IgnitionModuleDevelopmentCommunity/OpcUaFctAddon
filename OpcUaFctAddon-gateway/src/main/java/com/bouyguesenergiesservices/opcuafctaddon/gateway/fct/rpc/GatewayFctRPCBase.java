package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.rpc;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.GatewayFct;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.GatewayFctManagerBase;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.IGatewayFctRPCManagerBase;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPCBase;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Extends 'GatewayFct' OPC AddOn for RPC communications (Ignition Client)
 *
 * Created by regis on 07/08/2017.
 */
public class GatewayFctRPCBase extends GatewayFct implements IGatewayFctRPCBase {


    private final ClientReqSession session;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private GatewayContext context;


    public GatewayFctRPCBase(GatewayContext _context, ClientReqSession _session) {
        super(_context,_session.getId());
        this.session = _session;

    }

    /**
     * Shutdown and Stop Timeout manager
     */
    @Override
    public void shutdown(){
        super.shutdown();
    }

    /**
     * Notify this Ignition Client session with the new value
     *
     * @param listNewValue List of all new Value
     */
    @Override
    public void notifyMyConsumer(List<QualifiedValue> listNewValue) {
        logger.trace("notifyMyConsumer()> Receive change values [{}] from GatewayFct sessionId:[{}]",listNewValue,session.getId());

        session.addNotification("com.bouyguesenergiesservices.OpcUaFctAddon", "TagChanged", (Serializable) listNewValue);

    }

    /**
     * Notify this Extends that the Client communication is in Timeout Proc
     */
    @Override
    public void notifyTimeoutMyConsumer() {
        logger.trace("notifyTimeoutMyConsumer()> Receive a timeout client sessionId:[{}]" ,session.getId());

        //Notify GatewayFctRCPManager to close this instance
        notifyRPCManagerShutdown(1000);
    }

    /**
     * The Ignition Client notify that shutdown is in progress
     */
    public void notifyClosureRPCClient() {
        logger.trace("notifyClosureRPCClient()> The client notify to shutdown sessionId:[{}]", session.getId());

        //Notify GatewayFctRCPManager to close this instance
        notifyRPCManagerShutdown(1000);
    }

    /**
     * The Consumer is still Alive
     */
    @Override
    public void keepAlive() {
        logger.trace("keepAlive()> Receive a keepAlive from my consumer");
        super.keepAliveFromMyConsumer();
    }


    /**
     * Notify My RPCManager that the Client (Igntion) shutdown
     */
    private void notifyRPCManagerShutdown(int delay){

        //Async execution for shutdown this current client session
        Runnable runnable = () -> {
            IGatewayFctRPCManagerBase rpcManager = null;
            try {
                //call in async mode the manager to close and delete this session
                rpcManager = GatewayFctManagerBase.getInstance(context);
                rpcManager.closeSessionFctRPC(session);
            } catch (Exception ex){
                logger.error("notifyRPCManagerShutdown().runnable> Error in closeSessionFctRPC sessionId[{}] manager[{}] context[{}]",session.getId(),rpcManager,ex);
            }
        };

        execm.executeOnce(runnable,delay);


    }

}
