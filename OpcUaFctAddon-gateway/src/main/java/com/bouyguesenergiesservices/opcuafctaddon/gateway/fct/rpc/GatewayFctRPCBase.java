package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.rpc;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.GatewayFct;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.GatewayFctManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.IGatewayFctRPCManager;
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

    public static final int TIMEOUT_RPC = 60000; //ms

    private final ClientReqSession session;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    //Manager timeout client last request
    private TimeoutManager timeoutManager = new TimeoutManager();
    private boolean isTimeoutManagerStarted = false;


    public GatewayFctRPCBase(GatewayContext _context, ClientReqSession _session) {
        super(_context,_session.getId());
        this.session = _session;
        execm.register(session.getId(),TimeoutManager.class.getName(), timeoutManager,RATE_TIMEOUT_MANAGER, TimeUnit.MILLISECONDS);
    }

    /**
     * Shutdown and Stop Timeout manager
     */
    @Override
    public void shutdown(){
        super.shutdown();
        execm.unRegister(session.getId(),TimeoutManager.class.getName());
        isTimeoutManagerStarted = false;
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
     * The Ignition Client notify that shutdown is in progress
     */
    @Override
    public void notifyShutdown() {
        logger.trace("notifyShutdown()> The client notify to shutdown sessionId:[{}]", session.getId());

        notifyRPCManagerShutdown();
    }


    /**
     * Notify My RPCManager that the Client (Igntion) shutdown
     */
    private void notifyRPCManagerShutdown(){

        //Async execution for shutdown this current client session
        Runnable runnable = () -> {
            IGatewayFctRPCManager rpcManager = null;
            try {
                //call in async mode the manager to close and delete this session
                rpcManager = GatewayFctManager.getInstance(context);
                rpcManager.closeSessionFctRPC(session);
            } catch (Exception ex){
                logger.error("notifyRPCManagerShutdown().runnable> Error in closeSessionFctRPC sessionId[{}] manager[{}] context[{}]",session.getId(),rpcManager,ex);
            }
        };

        CompletableFuture.runAsync(runnable, executor)
                .thenRun(()->logger.debug("notifyRPCManagerShutdown().thenRun> Async closeSessionFctRPC"));

        executor.shutdown();
    }


    /**
     * Class to manage the Timeout client (Ignition) session (Orphan)
     */
    private class TimeoutManager implements Runnable {

        @Override
        public void run() {
            logger.trace("TimeoutManager.run()> sessionId [{}]",session.getId());

            if  (isTimeoutManagerStarted) {
                //Notify client
                try {
                    //it could be Ignition Client Notification or Gateway (GAN) Notification
                    Long currentTimeMillis = System.currentTimeMillis();
                    logger.trace("TimeoutManager.run()> currentTimeMillis[{}] getLastAccessedTime[{}]",currentTimeMillis,session.getLastAccessedTime(),RATE_TIMEOUT_MANAGER);
                    if ((currentTimeMillis - session.getLastAccessedTime()) > TIMEOUT_RPC){
                        //notify manager to kill me
                        logger.debug("TimeoutManager.run()> Force shutdown due to Timeout consumer sessionId:[{}]",session.getId());
                        notifyRPCManagerShutdown();
                    }

                } catch (Exception ex) {
                    logger.error("TimeoutManager.run()> Erreur sending Timeout notification to the consumer sessionId:[{}]",session.getId(), ex);
                }
            } else {
                //pass the first cyclic calling
                isTimeoutManagerStarted = true;
            }
        }
    }

}
