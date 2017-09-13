package com.bouyguesenergiesservices.opcuafctaddon.gateway.manager;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan.IGatewayFctGAN;

/**
 * Interface of the Manager of all GAN communication (Gateway - Remote Gateway)
 *
 * Created by regis on 08/08/2017.
 */
public interface IGatewayFctGANManager {
    /**
     * Find GatewayFctGAN associate to a specific session GAN
     * Declare a new 'GatewayFctGAN' if this session isn't declare in the Manager
     *
     * @param remoteServer Name of the remote Gateway
     * @param session Context of the client session
     * @return Interface toIGatewayFctGAN
     */
    IGatewayFctGAN getSessionFctGAN(String remoteServer, String session);

    /**
     * Close this specific 'GatewayFctGAN' associate to this session GAN
     *
     * @param remoteServer Name of the remote Gateway
     * @param session Context of the client session
     */
    void closeSessionFctGAN(String remoteServer, String session);

}




