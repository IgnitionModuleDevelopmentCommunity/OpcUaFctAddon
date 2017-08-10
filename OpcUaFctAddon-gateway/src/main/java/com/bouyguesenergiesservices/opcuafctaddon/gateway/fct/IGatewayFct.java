package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct;

import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPCBase;

/**
 * Created by regis on 13/07/2017.
 */
public interface IGatewayFct extends IGatewayFctRPCBase {
    void shutdown();
}
