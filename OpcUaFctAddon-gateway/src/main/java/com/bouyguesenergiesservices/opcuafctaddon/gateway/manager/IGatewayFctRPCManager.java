package com.bouyguesenergiesservices.opcuafctaddon.gateway.manager;


import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPC;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPCBase;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;

/**
 * Interface of the Manager of all RPC communication (Client Ignition) with GAN options
 *
 * Created by regis on 08/08/2017.
 */
public interface IGatewayFctRPCManager extends IGatewayFctRPCManagerBase{


    /**
     * Find GatewayFct associate to a specific session RPC
     * Declare a new 'GatewayFctRPC' with GAN options if this session isn't declare in the Manager
     *
     * @param session Context of the client session
     * @return Interface to 'GatewayFctRPC'
     */
    IGatewayFctRPC getSessionFctRPC(ClientReqSession session);


}
