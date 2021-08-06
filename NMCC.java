package application;
/*
 * 
 
 * Group: N7
 * Group Members: Nilesh Samnani,
                  Mushir Shaikh
                  Unnit Patel

 * Note: comment 'ADC' means "add your code here"
 *       comment 'NOC' means don't change i.e. you should use as is
 *
 */
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;

public class NMCC {

  // add any private vars below 'pfile'
  // if you want to write/use additional methods, put them
  // *after* time_now()
  private String pbroker, pqueue, puser, ppass; // properties
  private String pfile  = "USS.properties";     // must be in current directory
  public String pboat;

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: java Sonar <shipname>  ## Consumer mode\n");
      System.exit(2);
    }
    System.out.println("ran");
    NMCC c = new NMCC(args[0]);
    c.run();
  } // main

  public void loadprop(String filename) throws IOException { // load properties to pbroker, pqueue etc
    // ADC
    FileReader fl = new FileReader(new File(pfile));
    Properties pr = new Properties();
    pr.load(fl);
    pbroker = pr.getProperty("broker");
    pqueue = pr.getProperty("queue");
    puser = pr.getProperty("username");
    ppass = pr.getProperty("password");
    pboat = filename;
  }

  // this is the only Ctor permitted
  public NMCC(String a) throws IOException {
    loadprop(a);

    // other init stuff: ADC
  } // ctor CX()

  // the actual worker, do not change the signature!
  int run() throws javax.jms.JMSException {
    int n = 0;
    Connection cx = null; Session sx = null;    // NOC
    ConnectionFactory cf = null;              // NOC
    // add additional vars here if needed

    try {
      cf = new ActiveMQConnectionFactory(pbroker);
      cx = cf.createConnection(puser, ppass);
      sx = cx.createSession(false, Session.AUTO_ACKNOWLEDGE); // NOC
      // createSession should not change

      // make Destination, Consumer
      // Destination destination =sx.createQueue(pqueue);// ADC, must use session.createXXXXXX
      Destination destination =sx.createQueue(pqueue);
      MessageProducer msprod = sx.createProducer(destination); // ADC, must use session.createConsumer
      Destination destination2 = sx.createTopic("TSONAR");
      MessageProducer msprod2 = sx.createProducer(destination2);
      // MessageConsumer mscon=sx.createConsumer(destination, pboat)
      cx.start();
      System.out.println("*DRILL *DRILL *DRILL USS "+pboat+" NMCC connected to: "+pbroker);
      System.out.println("*DRILL *DRILL *DRILL USS "+pboat+" at "+time_now()+": READY");
      Auth mykey = new Auth("secret00");
      Auth mykey2 = new Auth("ABCDEFG");
      String[] msg = {"EAM DRILL-TARGET-PACKAGE-SLB4XY/1 vwsvhe","EAM DRILL-TARGET-PACKAGE-SLB4XY/2 THORS-TWINS","EAM DRILL-TARGET-PACKAGE-LNC4XY/3 LAUNCH-MISSILES","EAM DRILL-TARGET-PACKAGE-RTN4ZM/4 RETURN-MISSION-SUSPENDED","EAM DRILL-TARGET-PACKAGE-WAI4EN/5 STAY-ALERT-OF-ENEMIES"};
      // start consumer
      int a=35;
      Random rand = new Random();
	  Long wait= (long) (rand.nextInt(1200 - 800 + 1) + 800);
	  msprod.setTimeToLive(1202);
	  String signature;
	  TextMessage textMessage;
      msprod2.send(sx.createTextMessage("END"));
      for (int i = a; i >= 0 ; i--) {
    	  String tmp=msg[i%4];
    	  textMessage = sx.createTextMessage(tmp);
    	  if(i%4==0)
    		  {signature = mykey.sign(tmp);textMessage.setStringProperty("signature", signature);textMessage.setStringProperty("boat", "YORK");}
    	  else if(i%4==1)
    		  {signature = mykey2.sign(tmp);textMessage.setStringProperty("signature", signature);textMessage.setStringProperty("boat", "COLT");}
    	  else if(i%4==2)
    		  textMessage.setStringProperty("boat", "WEST");
    	  else
    		  textMessage.setStringProperty("boat", "TIER");
    	  System.out.println("before send time:"+time_now()+"  |  wait time:"+wait+" key:"+i);
    	  textMessage.setLongProperty("JMS_jbmq_Delay", wait);
    	  textMessage.setJMSExpiration(1500);
    	  msprod.send(textMessage);
    	  System.out.println(time_now()+" "+pboat+" "+textMessage.getText());
    	  Thread.sleep(wait);
	}
	  textMessage = sx.createTextMessage("END");
      signature = mykey.sign("END");
      textMessage.setStringProperty("signature", signature);
      msprod2.send(textMessage);
      System.out.println(time_now()+" "+pboat+": **TERMINATE DRILL");

    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      if (sx != null) { sx.close(); }
      if (cx != null) { cx.close(); }
    } // try {} ends here

    return n;
  } // run() run - Consumer

  String time_now() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss.SSS");
    return sdf.format(new Date());
  }
} // class

