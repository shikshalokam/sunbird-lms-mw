package org.sunbird.learner;

import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.learner.actors.RequestRouterActor;
import org.sunbird.learner.util.SchedulerManager;
import org.sunbird.learner.util.Util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * @author arvind.
 * Remote actor Application start point .
 */
public class Application {
    private static ActorSystem system;
    private static final String ACTOR_CONFIG_NAME = "RemoteMWConfig";
    private static final String ACTOR_SYSTEM_NAME = "RemoteMiddlewareSystem";
    public static void main(String[] args) {
        startRemoteCreationSystem();
    }
   /**
    * This method will do the basic setup for actors.
    */
  private static void startRemoteCreationSystem() {
    Config con = null;
    String host = System.getenv(JsonKey.SUNBIRD_ACTOR_SERVICE_IP);
    String port = System.getenv(JsonKey.SUNBIRD_ACTOR_SERVICE_PORT);
    if (!ProjectUtil.isStringNullOREmpty(host) && !ProjectUtil.isStringNullOREmpty(port)) {
      con = ConfigFactory
          .parseString(
              "akka.remote.netty.tcp.hostname=" + host + ",akka.remote.netty.tcp.port=" + port + "")
          .withFallback(ConfigFactory.load().getConfig(ACTOR_CONFIG_NAME));
    } else {
      con = ConfigFactory.load().getConfig(ACTOR_CONFIG_NAME);
    }
    system = ActorSystem.create(ACTOR_SYSTEM_NAME, con);
    ActorRef learnerActorSelectorRef = system.actorOf(Props.create(RequestRouterActor.class),
        RequestRouterActor.class.getSimpleName());
    ProjectLogger.log("ACTORS STARTED " + learnerActorSelectorRef, LoggerEnum.INFO.name());
    checkCassandraConnection();
  }

    private static void checkCassandraConnection() {
        Util.checkCassandraDbConnections();
        SchedulerManager.schedule();
    }
}