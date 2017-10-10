package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.rpc;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.GatewayFctManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.IGatewayFctRPCManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctGANHandler;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPC;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.gan.GatewayAreaNetworkManager;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.api.ServiceManager;
import com.inductiveautomation.metro.api.services.ServiceState;

import java.io.Serializable;
import java.util.List;


/**
 * Created by regis on 07/08/2017.
 */
public class GatewayFctRPC extends GatewayFctRPCBase implements IGatewayFctRPC,Serializable {

    private ClientReqSession session;
    private GatewayAreaNetworkManager ganManager;
    private IGatewayFctRPCManager rpcManager = null;
    private String serverCurrentName="";
    private String lastRemoteServer="";


    public GatewayFctRPC(GatewayContext _context, ClientReqSession _session) {
        super(_context,_session);
        this.session = _session;
        this.ganManager = _context.getGatewayAreaNetworkManager();
        this.serverCurrentName = ganManager.getServerAddress().getServerName();
        this.rpcManager = GatewayFctManager.getInstance(_context);
    }


    /**
     * Call a local or a gan subscribe function
     *
     * @param remoteServer The name of the remoteServer gateway
     * @param opcServer Name of the OPC server (declare in gateway)
     * @param lstItemPath List of OPC item
     * @param rate Frequency OPC update subscription
     * @return False if the GAN is unavailable / local is unavailable
     */
    public boolean subscribe(String remoteServer, String opcServer, List<String> lstItemPath, int rate) {

        if (remoteServer.equals(serverCurrentName)|| remoteServer.equals("")){
            logger.trace("subscribe()> it is a local request");

            //Check if there is a previous GAN communication
            notifyGANManagerShutdown(lastRemoteServer,500);

            return super.subscribe(opcServer, lstItemPath, rate);
        } else {

            //Check if there is a previous GAN communication
            if (!remoteServer.equals(lastRemoteServer)){
                notifyGANManagerShutdown(lastRemoteServer,500);
            }

            IGatewayFctGANHandler service = callGANService(remoteServer);

            if (service == null) {
                logger.debug("subscribe()> it is a GAN request (Service unavailable) sessionId:[{}]",session.getId());
                lastRemoteServer ="";
                return false;
            }
            else {
                logger.trace("subscribe()> it is a GAN request sessionId:[{}]",session.getId());
                Object[] args = new Object[]{opcServer,lstItemPath,rate};
                String[] keywords = new String[]{String.class.getName(),List.class.getName(),int.class.getName()};
                String result = service.invokeMyGatewayFct(serverCurrentName,session.getId(),"subscribe",args,keywords);
                lastRemoteServer = remoteServer;
                logger.debug("subscribe()> subscribe result:[{}]",result);
                return true;
            }
        }
    }

    /**
     * Call a local or a gan unSubscribe function
     *
     * @param remoteServer The name of the remoteServer gateway
     */
    public void unSubscribe(String remoteServer) {
        if (remoteServer.equals(serverCurrentName) || remoteServer.equals("")){
            logger.trace("unSubscribe()> it is a local request");

            //Check if there is a previous GAN communication
            notifyGANManagerShutdown(lastRemoteServer,500);

            super.unSubscribe();
        } else {
            //Close a previous GAN communication
            if (!remoteServer.equals(lastRemoteServer)){
                notifyGANManagerShutdown(lastRemoteServer,500);
            }

            IGatewayFctGANHandler service = callGANService(remoteServer);

            if (service == null) {
                logger.debug("unSubscribe()> it is a GAN request (Service unavailable) sessionId:[{}]",session.getId());
            }
            else {
                logger.trace("unSubscribe()> it is a GAN request sessionId:[{}]",session.getId());
                lastRemoteServer = remoteServer;
                String result = service.invokeMyGatewayFct(serverCurrentName,session.getId(),"unSubscribe",null,null);
                logger.debug("unSubscribe()> unsubscribe result:[{}]",result);
            }
        }
    }

    /**
     * A keepAlive function to prevent orphan subscription
     *
     */
    @Override
    public void keepAlive(){
        if (lastRemoteServer.equals("")){
            super.keepAlive();
        } else {
            IGatewayFctGANHandler service = callGANService(lastRemoteServer);

            if (service == null) {
                logger.debug("keepAlive()> it is a GAN request (Service unavailable) sessionId:[{}]",session.getId());
            }
            else {
                logger.debug("keepAlive()> it is a GAN request sessionId:[{}]",session.getId());
                String result = service.invokeMyGatewayFct(serverCurrentName,session.getId(),"keepAlive",null,null);

            }
        }
    }


    /**
     * Reception of new Value event from GatewayFct (local or GAN)
     *
     * @param listNewValue List of all new Value
     */
    public void notifyMyConsumer(List<QualifiedValue> listNewValue) {
        logger.trace("notifyMyConsumer()> Received a notification from remoteServer");

        if (listNewValue!=null){
            if (!listNewValue.isEmpty()){
                logger.trace("notifyMyConsumer()> Send notifyMyConsumer sessionId:[{}] listNewValue:[{}]",session.getId(),listNewValue);
                super.notifyMyConsumer(listNewValue);
            }
        }
    }

    /**
     * The Ignition Client has notify before closing
     */

    @Override
    public void notifyClosureRPCClient() {

        logger.debug("notifyClosureRPCClient> Notify that the client close");
        //notify last remote Server to close this GAN session
        notifyGANManagerShutdown(lastRemoteServer,500);

        //Notify my RPC manager to close this RPC session
        notifyRPCManagerShutdown(2000);
    }




    /**
     * Notify My GatewayRPCManager that this GatewayFctRPC instance is being closed
     */
    private void notifyRPCManagerShutdown(int delay){

        //Async execution for shutdown this current client session
        Runnable runnable = () -> {
            IGatewayFctRPCManager rpcManager = this.rpcManager;
            try {
                //call in async mode the manager to close and delete this session
                rpcManager.closeSessionFctRPC(session);
            } catch (Exception ex){
                logger.error("notifyRPCManagerShutdown().runnable> Error in closeSessionFctRPC sessionId[{}] manager[{}]",session.getId(),rpcManager,ex);
            }
        };

        execm.executeOnce(runnable,delay);
    }

    /**
     * Notify the remoteServer GatewayGANManager that this GatewayFctRPC instance is being closed
     */
    private void notifyGANManagerShutdown(String remoteServer, int delay) {

        //Async execution for shutdown this current client session
        Runnable runnable = () -> {
            if (!remoteServer.equals("")) {
                logger.trace("notifyGANManagerShutdown()> remoteServer:[{}]", remoteServer);
                //notify remote Server for close this session
                IGatewayFctGANHandler service = callGANService(remoteServer);
                if (service != null) {
                    String result = service.invokeMyGatewayFct(remoteServer, session.getId(), "notifyClosureGANClient", null, null);
                    logger.debug("notifyGANManagerShutdown()> notifyGANManagerShutdown result:[{}]", result);
                }
                lastRemoteServer = "";
            }

        };

        execm.executeOnce(runnable,delay);
    }


    /**
     * Notify from remote Server that my session is in Timeout
     */
    @Override
    public void notifyTimeoutMyConsumer() {
        logger.trace("notifyTimeoutMyConsumer()> Receive a timeout from remoteServer:[{}] client sessionId:[{}]" ,lastRemoteServer, session.getId());
        lastRemoteServer="";

        //Notify GatewayFctRCPManager to close this instance
        //notifyRPCManagerShutdown(500);
    }


    /**
     * Get my GAN service interface
     *
     * @param remoteServer name of the gateway
     * @return Null if the service is unavailable
     */

    private IGatewayFctGANHandler callGANService(String remoteServer){
        IGatewayFctGANHandler remoteService = null;


        ServiceManager sm = ganManager.getServiceManager();
        ServerId serverId = ServerId.fromString(remoteServer);

        // First, verify that the service is available on the remote machine before trying to call.
        ServiceState state = sm.getRemoteServiceState(serverId, IGatewayFctGANHandler.class);
        if (state != ServiceState.Available){
            logger.warn("callGANService()> The Service 'GatewayFctGANHandler' is unavailable [server:{}, currentState:{}]",
                    serverId.toDescriptiveString(),
                    state.toString());
        } else {
            // The service call will time out after 60 seconds if no response is received from the remote Gateway.
            remoteService =  sm.getService(serverId,IGatewayFctGANHandler.class).get();

        }
        return remoteService;
    }




}
