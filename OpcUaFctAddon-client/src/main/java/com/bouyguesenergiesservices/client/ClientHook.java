package com.bouyguesenergiesservices.client;


import com.inductiveautomation.vision.api.client.AbstractClientModuleHook;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHook extends AbstractClientModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);
        manager.addScriptModule(
                "system.byes.opc",
                new GetOPCClientFunctions(),
                new PropertiesFileDocProvider());
        logger.debug("initializeScriptManager()");
    }

}
