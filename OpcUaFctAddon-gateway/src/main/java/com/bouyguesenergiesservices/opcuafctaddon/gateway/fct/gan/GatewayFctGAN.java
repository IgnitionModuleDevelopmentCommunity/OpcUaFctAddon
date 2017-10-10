package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.GatewayFct;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.GatewayFctManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctGANHandler;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.gateway.gan.GatewayAreaNetworkManager;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.api.ServiceManager;
import com.inductiveautomation.metro.api.services.ServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Created by regis on 07/08/2017.
 */
public class GatewayFctGAN extends GatewayFct implements IGatewayFctGAN,Serializable{

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private String notifyRemoteServer;
    private String session;
    private GatewayAreaNetworkManager ganManager;
    private GatewayFctManager gFctManager;


    public GatewayFctGAN(GatewayContext _context,String _session, String _notifyRemoteServer) {
        super(_context,_session);
        this.session =_session;
        this.notifyRemoteServer = _notifyRemoteServer;
        this.ganManager = _context.getGatewayAreaNetworkManager();
        this.gFctManager = GatewayFctManager.getInstance(_context);

        logger.trace("GatewayFctGAN> Declare a new GAN session for [{}] Gateway[{}]",_notifyRemoteServer,_session);
    }


    /**
     * Notify my Client that a new value changed
     *
     * @param listNewValue List of all new Value
     */
    public void notifyMyConsumer(List<QualifiedValue> listNewValue) {


        IGatewayFctGANHandler service = null;
        Object[] args = null;
        String[] keywords = null;

        try {

            service = callGANService(notifyRemoteServer);

            if (service == null) {
                logger.debug("notifyMyConsumer()> Service 'IGatewayFctGANHandler' GAN unavailable");
            } else {
                logger.trace("notifyMyConsumer()> Service 'IGatewayFctGANHandler' GAN available");

                args = new Object[]{listNewValue};
                keywords = new String[]{List.class.getName()};

                service.notifyMyGatewayFct(notifyRemoteServer, session, "notifyMyConsumer", args, keywords);

            }
        }catch (Exception ex){
            logger.error("service:[{}] args:[{}] keywords:[{}]",service,args,keywords, ex);
        }


    }

    /**
     * The Gateway Client has notify before closing
     */

    @Override
    public void notifyClosureGANClient() {
        //Async execution for shutdown this current client session
        Runnable runnable = () -> {
            try {
                    //call in async mode the manager to close and delete this session
                    logger.debug("notifyClosureGANClient().runnable> call closeSessionFctGAN notifyRemoteServer:[{}] session:[{}]", notifyRemoteServer, session);
                    gFctManager.closeSessionFctGAN(notifyRemoteServer, session);

            } catch (Exception ex){
                logger.error("notifyClosureGANClient().runnable> Error in closeSessionFctGAN session[{}] manager[{}]",session,gFctManager,ex);
            }
        };

        //Execute with a delay
        execm.executeOnce(runnable,1000);
    }


    /**
     * The notify my GAN consumer that the GatewayFct send a Timeout Alert
     */
    @Override
    public void notifyTimeoutMyConsumer() {

        //Async execution for shutdown this current client session
        Runnable runnable = () -> {
            IGatewayFctGANHandler service;

            try {
                service = callGANService(notifyRemoteServer);
                if (service == null) {
                    logger.debug("notifyTimeoutMyConsumer()> Service 'IGatewayFctGANHandler' GAN unavailable notifyRemoteServer:[{}]",notifyRemoteServer);
                } else {
                    logger.trace("notifyTimeoutMyConsumer()> Service 'IGatewayFctGANHandler' GAN available notifyRemoteServer:[{}]", notifyRemoteServer);

                    service.notifyMyGatewayFct(notifyRemoteServer, session, "notifyTimeoutMyConsumer", null, null);

                    //call in async mode the manager to close and delete this session
                    logger.debug("notifyTimeoutMyConsumer().runnable> call closeSessionFctGAN notifyRemoteServer:[{}] session:[{}]", notifyRemoteServer, session);
                    gFctManager.closeSessionFctGAN(notifyRemoteServer, session);

                }

            } catch (Exception ex){
                logger.error("notifyTimeoutMyConsumer().runnable> Error in closeSessionFctGAN session[{}] manager[{}]",session,gFctManager,ex);
            }
        };

        //Execute with a delay
        execm.executeOnce(runnable,1000);

    }

    /**
     * KeepAlive from the Gateway Client
     */
    @Override
    public void keepAlive() {
        logger.trace("keepAlive> Receive a keepAlive.");
        super.keepAliveFromMyConsumer();
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




    public String toString(){
        return String.format("GatewayFctGAN notifyRemoteServer:[{}] session:[{}]",notifyRemoteServer,session);
    }



}
