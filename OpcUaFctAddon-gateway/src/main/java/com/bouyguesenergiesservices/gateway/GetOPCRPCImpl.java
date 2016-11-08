package com.bouyguesenergiesservices.gateway;

import com.bouyguesenergiesservices.gateway.service.GetOPCService;
import com.bouyguesenergiesservices.GetOPCRPC;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.api.ServiceManager;
import com.inductiveautomation.metro.api.services.ServiceState;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by regis on 04/11/2016.
 * Implementation of GetOPCRPCImpl, and handles remote calls from clients and designers.
 */
public class GetOPCRPCImpl implements GetOPCRPC{

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;

    public GetOPCRPCImpl(GatewayContext context){
        this.context = context;
    }


    @Override
    public List<QualifiedValue> readValues(String opcServer, List<String> lstItemPath) {
        return null;
    }

    @Override
    public boolean isSubscribe(String subscriptionName) {
        return false;
    }

    @Override
    public String subscribe(String opcServer, List<String> lstItemPath, int rate) {
        return null;
    }

    @Override
    public List<QualifiedValue> readSubscribeValues(String subscriptionName) {
        return null;
    }

    @Override
    public boolean unsubscribe(String subscriptionName) {
        return false;
    }

    @Override
    public void unsubscribeAll() {

    }

    /**
     * Read OPC Values on a connection declare in the Gateway with GAN
     * @param remoteServer Name of the remote Gateway Server
     * @param opcServer OPC Connection to use
     * @param lstItemPath List of Item Path
     * @return last value of all Item 'List<QualifiedValue>'. Return None if the service isn't available
     */
    @Override
    public List<QualifiedValue> getRemoteReadValues(String remoteServer, String opcServer, List<String> lstItemPath) {

        GetOPCService opcService = callService(remoteServer);

        if (opcService == null) {
            return null;
        }
        else {
            return opcService.getServiceReadValues(opcServer, lstItemPath);

        }
    }



    @Override
    public boolean getRemoteIsSubscribe(String remoteServer, String subscriptionName) {
        GetOPCService opcService = callService(remoteServer);

        if (opcService == null) {
            return false;
        } else {
            return opcService.getServiceIsSubscribe(subscriptionName);
        }


    }

    @Override
    public String getRemoteSubscribe(String remoteServer, String opcServer, List<String> lstItemPath, int rate) {
        GetOPCService opcService = callService(remoteServer);

        if (opcService == null) {
            return null;
        } else {
            return opcService.getServiceSubscribe(opcServer, lstItemPath, rate);
        }
    }

    @Override
    public List<QualifiedValue> getRemoteReadSubscribeValues(String remoteServer, String subscriptionName) {
        GetOPCService opcService = callService(remoteServer);

        if (opcService == null) {
            return null;
        } else {
            return opcService.getServiceReadSubscribeValues(subscriptionName);
        }
    }

    @Override
    public boolean getRemoteUnsubscribe(String remoteServer, String subscriptionName) {
        GetOPCService opcService = callService(remoteServer);

        if (opcService == null) {
            return false;
        } else {
            return opcService.getServiceUnsubscribe(subscriptionName);
        }
    }

    @Override
    public void getRemoteUnsubscribeAll(String remoteServer) {
        GetOPCService opcService = callService(remoteServer);

        if (opcService != null) {
            opcService.getServiceUnsubscribeAll();
        }
    }

    /**
     * Function 'GetOPCService' Interface available on any Gateway
     * @param remoteServer Name of the remote Gateway Server
     * @return return an interface GetOPCService available on the remoteServer
     */
    private GetOPCService callService(String remoteServer){
        GetOPCService opcService = null;
        ServiceManager sm = context.getGatewayAreaNetworkManager().getServiceManager();
        ServerId serverId = ServerId.fromString(remoteServer);

        // First, verify that the service is available on the remote machine before trying to call.
        ServiceState state = sm.getRemoteServiceState(serverId, GetOPCService.class);
        if (state != ServiceState.Available){
            logger.error("Service non disponible pour le serveur '{}', son état courant est '{}'",
                    serverId.toDescriptiveString(),
                    state.toString());
        } else {
            // The service call will time out after 60 seconds if no response is received from the remote Gateway.
            opcService =  sm.getService(serverId,GetOPCService.class).get();
        }
        return opcService;
    }
}
