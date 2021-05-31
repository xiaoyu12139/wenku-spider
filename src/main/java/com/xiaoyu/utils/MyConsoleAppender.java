package com.xiaoyu.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;

public class MyConsoleAppender extends AppenderBase<ILoggingEvent> {
	  private Encoder<ILoggingEvent> encoder = new EchoEncoder<ILoggingEvent>();
	  private ByteArrayOutputStream  out     = new ByteArrayOutputStream();

	  public MyConsoleAppender() {
		  System.out.println("1");
	     LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	     setContext(lc);
	     start();
	     lc.getLogger("ROOT").addAppender(this);
	  }

	  @Override
	  public void start() {
	     try {
	        encoder.init(out);
	     } catch (IOException e) {}
	     super.start();
	  }

	  @Override
	  public void append(ILoggingEvent event) {
	     try {
	        encoder.doEncode(event);
	        out.flush();
	        String line = out.toString(); // TODO: append _line_ to your JTextPane
	        System.out.println("Ωÿ»°µΩ£∫" + line);
	        out.reset();
	     } catch (IOException e) {}
	  }
	}