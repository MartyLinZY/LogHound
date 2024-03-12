package core.loghound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {

        logger.info("Start LogHound......");
        logger.info("Check System Env...");
        System.out.println(System.getenv());

        logger.info("Get System File...");
        logger.info("Build AST...");
        logger.info("Get Log File...");
    }
}