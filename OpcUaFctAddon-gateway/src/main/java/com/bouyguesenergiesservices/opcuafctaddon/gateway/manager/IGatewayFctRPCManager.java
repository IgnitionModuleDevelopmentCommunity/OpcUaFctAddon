package com.bouyguesenergiesservices.opcuafctaddon.gateway.manager;


import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPCBase;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;

/**
 * Interface of the Manager of all RPC communication (Client Ignition)
 *
 * Created by regis on 08/08/2017.
 */
public interface IGatewayFctRPCManager {
    /**
     * Find GatewayFct associate to a specific session RPC
     * Declare a new 'GatewayFctRPC' if this session isn't declare in the Manager
     *
     * @param session Context of the client session
     * @return Interface to 'GatewayFctRPC'
     */
    IGatewayFctRPCBase getSessionFctRPC(ClientReqSession session);

    //IGatewayFctRPCBase getSessionFctRPCGAN(String session);

    /**
     * Close this specific 'GatewayFct' associate to this session RPC
     *
     * @param session Context of the client session
     */
    void closeSessionFctRPC(ClientReqSession session);


}
