package com.bouyguesenergiesservices.opcuafctaddon.gateway_interface;


/**
 * An interface to call GAN Functions
 */
public interface IGatewayFctGANHandler {

    String SUCCESS_MSG = "SUCCESS";
    String FAIL_MSG = "FAIL";

    /**
     * From the Gateway hosting the RPC client to the Gateway realize the OPC subscription
     *
     * @param remoteServer the Gateway name
     * @param sessionId the name session Id
     * @param functionName name of the IGatewayFctGAN function to invoke
     * @param args Arguments[]
     * @param keywords Name[] of Args
     * @return FAIL there is a problem
     */
    String invokeMyGatewayFct(String remoteServer, String sessionId, String functionName,Object[]args, String[]keywords);

    /**
     * From the Gateway realizing the OPC subscription to the Gateway host the RPC client
     *
     * @param remoteServer the Gateway name
     * @param sessionId the name session Id
     * @param functionName name of the IGatewayFctGAN function to invoke
     * @param args Arguments[]
     * @param keywords Name[] of Args
     * @return FAIL there is a problem
     */
    String notifyMyGatewayFct(String remoteServer, String sessionId, String functionName,Object[]args, String[]keywords);

}
