package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan;


import com.bouyguesenergiesservices.opcuafctaddon.gateway.manager.GatewayFctManager;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPC;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctGANHandler;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Created by regis on 11/08/2017.
 */
public class GatewayFctGANHandler implements IGatewayFctGANHandler {

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private final GatewayContext context;
    private final GatewayFctManager fctManager;

    public GatewayFctGANHandler(GatewayContext context) {
        this.context = context;
        this.fctManager = GatewayFctManager.getInstance(context);
    }


    /**
     * Invoke 'IGatewayFctGAN' function
     *
     * @param fromServer Gateway to the request (origin)
     * @param sessionId Client Id
     * @param functionName Name of the function
     * @param args Table of arguments
     * @param keywords Description of the arguments
     * @return FAIL_MSG if there is a exception
     */
    public String invokeMyGatewayFct(String fromServer, String sessionId, String functionName, Object[] args, String[] keywords) {

        IGatewayFctGAN gatewayFct = fctManager.getSessionFctGAN(fromServer, sessionId);

        String result= FAIL_MSG;
        if (gatewayFct == null) {
            logger.debug("invokeMyGatewayFct()> GAN From remoteServer:[{}] session:[{}] is unknown on this localServer",fromServer,sessionId );
        }else {
            try {
                result = invoke(gatewayFct, functionName, args, keywords);
            } catch (Exception ex) {
                logger.error("invokeMyGatewayFct() > Error in reflexion searching", ex);
            }
        }

       return result;
    }

    /**
     * Invoke 'IGatewayFctRPC' function
     *
     * @param remoteServer Gateway to the request (origin)
     * @param sessionId Client Id
     * @param functionName Name of the function
     * @param args Table of arguments
     * @param keywords Description of the arguments
     * @return FAIL_MSG if there is a exception
     */
    @Override
    public String notifyMyGatewayFct(String remoteServer, String sessionId, String functionName, Object[] args, String[] keywords) {
        IGatewayFctRPC gatewayFct = fctManager.getSessionFctRPC(sessionId);


        String result= FAIL_MSG;
        if (gatewayFct == null) {
            logger.debug("notifyMyGatewayFct()> GAN From remoteServer:[{}] session:[{}] is unknown on this localServer",remoteServer,sessionId );
        }else {
            try {
                result = invoke(gatewayFct, functionName, args, keywords);
            } catch (Exception ex) {
                logger.error("notifyMyGatewayFct() > Error in reflexion searching", ex);
            }
        }

       return result;
    }


    /**
     * Subfunction to format args and invoke the function
     *
     * @param instance an IGateway associate to the sessionId
     * @param functionName Name of the function
     * @param args Table of arguments
     * @param keywords Description of the arguments
     * @return FAIL_MSG if there is a exception
     */
    private String invoke(Object instance, String functionName, Object[] args, String[] keywords) {

        String result = FAIL_MSG;
        Object value = null;

        Class<?> myClass = instance.getClass();

        try {
            Class<?>[] argTypes = Utils.createArgumentTypes(keywords);

            //Arrays.stream(myClass.getMethods()).forEach( elt -> logger.debug("{} {}",elt.getName(),elt.getParameters().length));

            Method myMethod = myClass.getMethod(functionName, argTypes);
            myMethod.invoke(instance, args);
            logger.trace("invoke()> invoke method [{}] keywords:[{}] ",functionName,keywords);
            result = SUCCESS_MSG;
        } catch (NoSuchMethodException ex) {
            logger.error("invoke()> Function method:[{}] keywords:[{}] unknown", functionName, keywords, ex);
        } catch (IllegalAccessException ex) {
            logger.error("invoke()> Function method:[{}] keywords:[{}]  IllegalAccessException exception", functionName, keywords, ex);
        } catch (InvocationTargetException ex) {
            logger.error("invoke()> Function method:[{}] keywords:[{}] InvocationTargetException exception", functionName, keywords, ex);
        }

        return result;

    }







}
