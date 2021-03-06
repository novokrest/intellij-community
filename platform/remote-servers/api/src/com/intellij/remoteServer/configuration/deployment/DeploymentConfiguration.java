package com.intellij.remoteServer.configuration.deployment;

import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.remoteServer.configuration.RemoteServer;

/**
 * @author nik
 */
public abstract class DeploymentConfiguration {
  public abstract PersistentStateComponent<?> getSerializer();

  public abstract void checkConfiguration(RemoteServer<?> server, DeploymentSource deploymentSource)
    throws RuntimeConfigurationException;
}
