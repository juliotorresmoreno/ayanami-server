package us.onnasoft.ayanami.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomLogger {

  private final Logger logger;

  private CustomLogger(Class<?> clazz) {
    this.logger = LogManager.getLogger(clazz);
  }

  public static CustomLogger getLogger(Class<?> clazz) {
    return new CustomLogger(clazz);
  }

  public void info(String message) {
    logger.info(message);
  }

  public void debug(String message) {
    logger.debug(message);
  }

  public void warn(String message) {
    logger.warn(message);
  }

  public void error(String message) {
    logger.error(message);
  }

  public void error(String message, Throwable throwable) {
    logger.error(message, throwable);
  }
}