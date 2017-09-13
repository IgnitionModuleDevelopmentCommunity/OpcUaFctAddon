package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan;



import java.util.List;

/**
 * Just an interface to describe all functions available trough the GAN
 */
public interface IGatewayFctGAN {


    boolean subscribe(String opcServer, List<String> lstItemPath, int rate);

    void unSubscribe();

    void notifyClosureGANClient();

    void shutdown();

    String toString();

    void keepAlive();


}
