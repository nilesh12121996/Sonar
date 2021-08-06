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
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.activemq.ActiveMQConnectionFactory;

public class SonarC {

  // add any private vars below 'pfile'
  // if you want to write/use additional methods, put them
  // *after* time_now()
  private String pbroker, pqueue, puser, ppass; // properties
  private String pfile  = "USS.properties";     // must be in current directory
  public String pboat;
  public Boolean b=true;

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: java Sonar <shipname>  ## Consumer mode\n");
      System.exit(2);
    }
    String boat = args[0];
    SonarC c = new SonarC(args[1]);
    c.pboat=boat;
    c.run();
  } // main

  public void loadprop(String filename) throws IOException { // load properties to pbroker, pqueue etc
    // ADC
    FileReader fl = new FileReader(new File(filename));
    Properties pr = new Properties();
    pr.load(fl);
    pbroker = pr.getProperty("broker");
    pqueue = pr.getProperty("queue");
    puser = pr.getProperty("username");
    ppass = pr.getProperty("password");
  }

  // this is the only Ctor permitted
  public SonarC(String a) throws IOException {
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
      Session sx2 = cx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cx.start();
      // make Destination, Consumer +"?consumer.exclusive=true"
      //Destination destination = sx.createQueue(pqueue); // ADC, must use session.createXXXXXX
      Destination destination= sx.createQueue(pqueue);
      Destination destination2= sx2.createTopic("TSONAR");
      MessageConsumer mscon2= sx2.createConsumer(destination2);
      String msgselector="boat = '"+pboat+"' OR boat IS null";
      MessageConsumer mscon = sx.createConsumer(destination,msgselector);// ADC, must use session.createConsumer
      // start consumer
      // mscon.start();
      System.out.println("*DRILL *DRILL *DRILL USS " + pboat + " Sonar connected to: " + pbroker);
      System.out.println("*DRILL *DRILL *DRILL USS " + pboat + " at " + time_now() + ": READY");
      mscon2.setMessageListener(new MessageListener() {
  		
  		@Override
  		public void onMessage(Message arg0) {
  			// TODO Auto-generated method stub
  		try {
  			TextMessage tm = (TextMessage) arg0;
  			String signature=tm.getStringProperty("signature");
  			if (tm.getText().contentEquals("END") && authorize(signature, tm.getText())) {
  				mscon2.close();
  				b=false;
  			}
  			else
  				System.out.println(time_now() + " " + pboat + ": " + tm.getText()+" (NON AUTHENTIC)");
  		} catch (JMSException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
  		}
  	});
      long waittime=360;
      Date now;
      String signature;
      while (b) {        // NOC: while loop as is
    	  try {
    	  Thread.sleep(waittime);
          TextMessage tm = (TextMessage) mscon.receiveNoWait();
          signature=tm.getStringProperty("signature");
          if(authorize(signature, tm.getText()))
        	  System.out.println(time_now() + " " + pboat + ": " + tm.getText()+" (AUTHENTIC)");
          else
        	  System.out.println(time_now() + " " + pboat + ": " + tm.getText()+" (NON AUTHENTIC)");
          now=new Date();
          waittime=now.getTime()-tm.getJMSTimestamp();
          if(waittime>1200)
        	  waittime=1200;
          else if(waittime<360)
        	  waittime=360;
    	  }
    	  catch (NullPointerException e) {
			// TODO: handle exception
		}
        } // while
    }
    catch(Exception e) {
      e.printStackTrace();
    }
	System.out.println(time_now() + " " + pboat + ": **TERMINATE DRILL");
    sx.close();
    cx.close();
    return n;
  } // run() run - Consumer

  String time_now() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss.SSS");
    return sdf.format(new Date());
  }
  
  String time_now(long tmp) {
	    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
	    return sdf.format(tmp);
	  }
  
  boolean authorize(String signature, String message)
  {
      Auth mykey=new Auth("secret00");
	  String signature1;
	  if(signature==null)
		  return false;
	  else
	  {
		  signature1 = mykey.sign(message);
          if(signature.equals(signature1))
        	  return true;
          else
        	  return false;
	  }
  }
}

